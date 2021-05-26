package com.alotofletters.schmucks.entity;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.config.SchmucksConfig;
import com.alotofletters.schmucks.entity.ai.*;
import com.mojang.authlib.GameProfile;
import me.shedaniel.autoconfig.AutoConfig;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.Durations;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.IntRange;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class SchmuckEntity extends TameableEntity implements Angerable, RangedAttackMob {
	/**
	 * Tracked data handler for a replicated game profile. Primarily used for the skins.
	 */
	public static final TrackedDataHandler<Optional<GameProfile>> OPTIONAL_PROFILE = new TrackedDataHandler<Optional<GameProfile>>() {
		public void write(PacketByteBuf data, Optional<GameProfile> object) {
			data.writeBoolean(object.isPresent() && object.get().getId() != null);

			object.ifPresent(gameProfile -> data.writeUuid(gameProfile.getId()));
		}

		public Optional<GameProfile> read(PacketByteBuf packetByteBuf) {
			return !packetByteBuf.readBoolean() ? Optional.empty() : Optional.of(new GameProfile(packetByteBuf.readUuid(), null));
		}

		public Optional<GameProfile> copy(Optional<GameProfile> object) {
			return object;
		}
	};

	private static final Predicate<ItemEntity> PICKABLE_DROP_FILTER = (itemEntity) -> !itemEntity.cannotPickup() && itemEntity.isAlive();

	private static final TrackedData<Integer> ANGER_TIME = DataTracker.registerData(SchmuckEntity.class, TrackedDataHandlerRegistry.INTEGER);
	private static final TrackedData<Optional<GameProfile>> OWNER_PROFILE = DataTracker.registerData(SchmuckEntity.class, OPTIONAL_PROFILE);

	private static final IntRange ANGER_TIME_RANGE = Durations.betweenSeconds(20, 39);

	private final SchmuckBowAttackGoal bowAttackGoal = new SchmuckBowAttackGoal(1.0D, 20, 15.0F);
	private final MeleeAttackGoal meleeAttackGoal = new MeleeAttackGoal(this, 1.2D, false);
	private final SchmuckMine mineGoal = new SchmuckMine(this, 1.0D, 60);
	private final PounceAtTargetGoal pounceGoal = new PounceAtTargetGoal(this, 0.3F);

	private final RevengeGoal shortTemperRevengeGoal = (new RevengeGoal(this)).setGroupRevenge();
	private final RevengeGoal revengeGoal = (new RevengeGoal(this, SchmuckEntity.class)).setGroupRevenge();

	private UUID targetUuid;

	private boolean shortTempered;
	private boolean canTeleport = true;

	private int eggUsageTime;

	public SchmuckEntity(EntityType<? extends SchmuckEntity> entityType, World world) {
		super(entityType, world);
		this.setCanPickUpLoot(true);
	}

	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable CompoundTag entityTag) {
		if (random.nextFloat() < AutoConfig.getConfigHolder(SchmucksConfig.class).getConfig().leatherHelmetChance) {
			this.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
		}
		shortTempered = AutoConfig.getConfigHolder(SchmucksConfig.class).getConfig().chaosMode || random.nextFloat() < AutoConfig.getConfigHolder(SchmucksConfig.class).getConfig().shortTemperChance; // will attack teammates if damaged
		this.updateAttackType();
		return super.initialize(world, difficulty, spawnReason, entityData, entityTag);
	}

	@Override
	protected void initGoals() {
		super.initGoals();
		this.goalSelector.add(1, new SwimGoal(this));
		this.goalSelector.add(2, new SitGoal(this));
		this.goalSelector.add(5, new SchmuckFollowOwner(this, 1.0D, 15.0F, 4.0F, false));
		this.goalSelector.add(6, new AnimalMateGoal(this, 1.0D));
		this.goalSelector.add(7, new SchmuckSmeltGoal(this, 1.0D));
		this.goalSelector.add(8 , new SchmuckPutUnneeded(this, 1.0D));
		this.goalSelector.add(9, new SchmuckPickUpItemGoal());
		this.goalSelector.add(10, new SchmuckFleeAllJobs(this, 1.0D));
		this.goalSelector.add(11, new SchmuckFleeGoal<>(PlayerEntity.class));
		this.goalSelector.add(12, new WanderAroundFarGoal(this, 1.0D));
		this.goalSelector.add(13, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.add(13, new LookAtEntityGoal(this, SchmuckEntity.class, 8.0F));
		this.goalSelector.add(13, new LookAroundGoal(this));
		this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
		this.targetSelector.add(2, new AttackWithOwnerGoal(this));
		this.targetSelector.add(4, new SchmuckTargetMinions(this));
		this.targetSelector.add(5, new UniversalAngerGoal<>(this, true));
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		if (player.getStackInHand(hand).isEmpty()) {
			player.giveItemStack(this.getMainHandStack());
			this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
			return ActionResult.SUCCESS;
		}

		if (player.getStackInHand(hand).getItem() == Schmucks.CONTROL_WAND) {
			this.toggleSit();
			return ActionResult.SUCCESS;
		}

		return super.interactMob(player, hand);
	}

	public void toggleSit() {
		this.setSitting(!this.isSitting());
		this.jumping = false;
		this.navigation.stop();
		this.setTarget(null);
	}

	@Override
	public void tickMovement() {
		super.tickMovement();

		if (!this.world.isClient) {
			this.tickAngerLogic((ServerWorld)this.world, true);
		}
	}

	@Override
	public void writeCustomDataToTag(CompoundTag tag) {
		super.writeCustomDataToTag(tag);
		tag.putBoolean("ShortTemper", this.shortTempered);
		tag.putBoolean("CanTeleport", this.canTeleport);
		this.angerToTag(tag);
	}

	@Override
	public void readCustomDataFromTag(CompoundTag tag) {
		super.readCustomDataFromTag(tag);
		this.angerFromTag((ServerWorld) this.world, tag);

		if (this.getOwnerUuid() != null) {
			this.setOwnerProfile(new GameProfile(this.getOwnerUuid(), null));
		}
		this.shortTempered = tag.getBoolean("ShortTemper");
		this.canTeleport = tag.getBoolean("CanTeleport");
		this.updateAttackType();
	}

	@Nullable
	@Override
	public SchmuckEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
		SchmuckEntity child = Schmucks.SCHMUCK.create(serverWorld);
		if (child != null) {
			child.setOwnerUuid(this.getOwnerUuid());
		}
		return child;
	}

	protected void initDataTracker() {
		super.initDataTracker();
		this.dataTracker.startTracking(ANGER_TIME, 0);
		this.dataTracker.startTracking(OWNER_PROFILE, Optional.empty());
	}

	public static DefaultAttributeContainer.Builder createSchmuckAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 16.0)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
	}

	@Override
	public int getAngerTime() {
		return this.dataTracker.get(ANGER_TIME);
	}

	@Override
	public void setAngerTime(int ticks) {
		this.dataTracker.set(ANGER_TIME, ticks);
	}

	@Override
	public void chooseRandomAngerTime() {
		this.setAngerTime(ANGER_TIME_RANGE.choose(this.random));
	}

	@Override
	@Nullable
	public UUID getAngryAt() {
		return this.targetUuid;
	}

	@Override
	public void setAngryAt(@Nullable UUID uuid) {
		this.targetUuid = uuid;
	}

	@Override
	public void setOwner(PlayerEntity player) {
		super.setOwner(player);
		this.setOwnerProfile(player.getGameProfile());
	}

	@Override
	public void setAttacking(@Nullable PlayerEntity attacking) {
		super.setAttacking(attacking);
	}

	public void setOwnerProfile(GameProfile profile) {
		this.dataTracker.set(OWNER_PROFILE, Optional.of(profile));
	}

	@Override
	public void equipStack(EquipmentSlot slot, ItemStack stack) {
		super.equipStack(slot, stack);
		if (!this.world.isClient) {
			this.updateAttackType();
		}
	}

	public boolean getCanTeleport() {
		return canTeleport;
	}

	public void setCanTeleport(boolean canTeleport) {
		this.canTeleport = canTeleport;
	}

	@Override
	public void onDeath(DamageSource source) {
		super.onDeath(source);
	}

	public void equipNoUpdate(EquipmentSlot slot, ItemStack stack) {
		super.equipStack(slot, stack);
	}

	@Nullable
	public GameProfile getOwnerProfile() {
		return this.dataTracker.get(OWNER_PROFILE).orElse(null);
	}

	public void updateAttackType() {
		if (this.world != null && !this.world.isClient) {
			this.goalSelector.remove(this.meleeAttackGoal);
			this.goalSelector.remove(this.bowAttackGoal);
			this.goalSelector.remove(this.mineGoal);
			this.goalSelector.remove(this.pounceGoal);
			this.targetSelector.remove(this.shortTemperRevengeGoal);
			this.targetSelector.remove(this.revengeGoal);
			ItemStack itemStack = this.getMainHandStack();
			if (itemStack.getItem() == Items.BOW || itemStack.getItem() == Items.EGG) {
				this.bowAttackGoal.setAttackInterval(itemStack.getItem() == Items.EGG ? 5 : 30);
				this.goalSelector.add(3, this.bowAttackGoal);
			} else if (FabricToolTags.PICKAXES.contains(itemStack.getItem())) {
				this.goalSelector.add(3, this.mineGoal);
			} else {
				this.goalSelector.add(2, this.pounceGoal);
				this.goalSelector.add(3, this.meleeAttackGoal);
			}
			if (this.shortTempered) {
				this.targetSelector.add(3, this.shortTemperRevengeGoal);
			} else {
				this.targetSelector.add(3, this.revengeGoal);
			}
		}
	}

	public boolean canUseRangedWeapon(RangedWeaponItem weapon) {
		return weapon == Items.BOW;
	}

	public void attack(LivingEntity target, float pullProgress) {
		if (this.isHolding(Items.EGG)) {
			ItemStack itemStack = this.getMainHandStack();
			EggEntity eggEntity = new EggEntity(world, this);
			eggEntity.setItem(itemStack);
			double d = target.getX() - this.getX();
			double e = target.getBodyY(0.3333333333333333D) - eggEntity.getY();
			double f = target.getZ() - this.getZ();
			double g = MathHelper.sqrt(d * d + f * f);
			eggEntity.setVelocity(d, e + g * 0.20000000298023224D, f, 1.6F, 2f);
			world.spawnEntity(eggEntity);
			this.playSound(SoundEvents.ENTITY_EGG_THROW, 0.5F, 0.4F / (this.random.nextFloat() * 0.4F + 0.8F));
			return;
		}
		ItemStack itemStack = this.getArrowType(this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW)));
		PersistentProjectileEntity persistentProjectileEntity = this.createArrowProjectile(itemStack, pullProgress);
		double d = target.getX() - this.getX();
		double e = target.getBodyY(0.3333333333333333D) - persistentProjectileEntity.getY();
		double f = target.getZ() - this.getZ();
		double g = MathHelper.sqrt(d * d + f * f);
		persistentProjectileEntity.setVelocity(d, e + g * 0.20000000298023224D, f, 1.6F, 2f);
		this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
		this.world.spawnEntity(persistentProjectileEntity);
	}

	protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier) {
		return ProjectileUtil.createArrowProjectile(this, arrow, damageModifier);
	}

	static {
		TrackedDataHandlerRegistry.register(OPTIONAL_PROFILE);
	}

	class SchmuckPickUpItemGoal extends Goal {

		public SchmuckPickUpItemGoal() {
			this.setControls(EnumSet.of(Control.MOVE));
		}

		@Override
		public boolean canStart() {
			if (!SchmuckEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty()) {
				return false;
			} else if (SchmuckEntity.this.getAttacker() == null) {
				if (SchmuckEntity.this.getRandom().nextInt(10) != 0) {
					return false;
				} else {
					List<ItemEntity> list = SchmuckEntity.this.world.getEntitiesByClass(ItemEntity.class,
							SchmuckEntity.this.getBoundingBox().expand(8.0D, 8.0D, 8.0D),
							SchmuckEntity.PICKABLE_DROP_FILTER);
					return !list.isEmpty() && SchmuckEntity.this.getEquippedStack(EquipmentSlot.MAINHAND).isEmpty();
				}
			} else {
				return false;
			}
		}

		public void tick() {
			List<ItemEntity> list = SchmuckEntity.this.world.getEntitiesByClass(ItemEntity.class,
					SchmuckEntity.this.getBoundingBox().expand(8.0D, 8.0D, 8.0D),
					SchmuckEntity.PICKABLE_DROP_FILTER);
			ItemStack itemStack = SchmuckEntity.this.getEquippedStack(EquipmentSlot.MAINHAND);
			if (itemStack.isEmpty() && !list.isEmpty()) {
				SchmuckEntity.this.getNavigation().startMovingTo(list.get(0), 1.2D);
			}
		}

		public void start() {
			List<ItemEntity> list = SchmuckEntity.this.world.getEntitiesByClass(ItemEntity.class,
					SchmuckEntity.this.getBoundingBox().expand(8.0D, 8.0D, 8.0D),
					SchmuckEntity.PICKABLE_DROP_FILTER);
			if (!list.isEmpty()) {
				SchmuckEntity.this.getNavigation().startMovingTo(list.get(0), 1.2D);
			}
		}
	}

	@Override
	public int getItemUseTime() {
		if (++this.eggUsageTime == 6) {
			this.eggUsageTime = 0;
		}
		return this.getMainHandStack().getItem() == Items.EGG ? 15 + this.eggUsageTime : super.getItemUseTime();
	}

	@Override
	public boolean isUsingItem() {
		return this.getMainHandStack().getItem() == Items.EGG || super.isUsingItem();
	}

	class SchmuckBowAttackGoal extends Goal {
		private final SchmuckEntity actor;
		private final double speed;
		private int attackInterval;
		private final float squaredRange;
		private int cooldown = -1;
		private int targetSeeingTicker;
		private boolean movingToLeft;
		private boolean backward;
		private int combatTicks = -1;

		public SchmuckBowAttackGoal(double speed, int attackInterval, float range) {
			this.actor = SchmuckEntity.this;
			this.speed = speed;
			this.attackInterval = attackInterval;
			this.squaredRange = range * range;
			this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
		}

		public void setAttackInterval(int attackInterval) {
			this.attackInterval = attackInterval;
		}

		public boolean canStart() {
			return !this.actor.isSitting() && this.actor.getTarget() != null && this.isHoldingRangedWeapon();
		}

		protected boolean isHoldingRangedWeapon() {
			return this.actor.isHolding(Items.BOW) || this.actor.isHolding(Items.EGG);
		}

		public boolean shouldContinue() {
			return (this.canStart() || !this.actor.getNavigation().isIdle()) && this.isHoldingRangedWeapon();
		}

		public void start() {
			super.start();
			this.actor.setAttacking(true);
		}

		public void stop() {
			super.stop();
			this.actor.setAttacking(false);
			this.targetSeeingTicker = 0;
			this.cooldown = -1;
			this.actor.clearActiveItem();
		}

		public void tick() {
			LivingEntity livingEntity = this.actor.getTarget();
			if (livingEntity != null) {
				double d = this.actor.squaredDistanceTo(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ());
				boolean bl = this.actor.getVisibilityCache().canSee(livingEntity);
				boolean bl2 = this.targetSeeingTicker > 0;
				if (bl != bl2) {
					this.targetSeeingTicker = 0;
				}

				if (bl) {
					++this.targetSeeingTicker;
				} else {
					--this.targetSeeingTicker;
				}

				if (!(d > (double)this.squaredRange) && this.targetSeeingTicker >= 20) {
					this.actor.getNavigation().stop();
					++this.combatTicks;
				} else {
					this.actor.getNavigation().startMovingTo(livingEntity, this.speed);
					this.combatTicks = -1;
				}

				if (this.combatTicks >= 20) {
					if ((double)this.actor.getRandom().nextFloat() < 0.3D) {
						this.movingToLeft = !this.movingToLeft;
					}

					if ((double)this.actor.getRandom().nextFloat() < 0.3D) {
						this.backward = !this.backward;
					}

					this.combatTicks = 0;
				}

				if (this.combatTicks > -1) {
					if (d > (double)(this.squaredRange * 0.75F)) {
						this.backward = false;
					} else if (d < (double)(this.squaredRange * 0.25F)) {
						this.backward = true;
					}

					this.actor.getMoveControl().strafeTo(this.backward ? -0.5F : 0.5F, this.movingToLeft ? 0.5F : -0.5F);
					this.actor.lookAtEntity(livingEntity, 30.0F, 30.0F);
				} else {
					this.actor.getLookControl().lookAt(livingEntity, 30.0F, 30.0F);
				}

				if (this.actor.isUsingItem()) {
					if (!bl && this.targetSeeingTicker < -60) {
						this.actor.clearActiveItem();
					} else if (bl) {
						int i = this.actor.getItemUseTime();
						if (i >= 20) {
							this.actor.clearActiveItem();
							((RangedAttackMob)this.actor).attack(livingEntity, BowItem.getPullProgress(i));
							this.cooldown = this.attackInterval;
						}
					}
				} else if (--this.cooldown <= 0 && this.targetSeeingTicker >= -60) {
					Hand hand = ProjectileUtil.getHandPossiblyHolding(this.actor, Items.BOW);
					if (hand == null) {
						hand = ProjectileUtil.getHandPossiblyHolding(this.actor, Items.EGG);
					}
					this.actor.setCurrentHand(hand);
				}

			}
		}
	}

	class SchmuckFleeGoal<T extends LivingEntity> extends FleeEntityGoal<T> {

		public SchmuckFleeGoal(Class<T> fleeFromType) {
			super(SchmuckEntity.this, fleeFromType, 2, 1.0D, 1.0D, (entity) -> !(entity instanceof PlayerEntity));
		}
	}
}
