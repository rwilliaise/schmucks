package com.alotofletters.schmucks.entity;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.ai.*;
import com.alotofletters.schmucks.entity.ai.control.SchmuckLookControl;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.Durations;
import net.minecraft.entity.ai.RangedAttackMob;
import net.minecraft.entity.ai.control.FlightMoveControl;
import net.minecraft.entity.ai.control.MoveControl;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.BirdNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.MobNavigation;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.IntRange;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

public class SchmuckEntity extends TameableEntity implements Angerable, RangedAttackMob {
	private static final Predicate<ItemEntity> PICKABLE_DROP_FILTER = (itemEntity) -> !itemEntity.cannotPickup() && itemEntity.isAlive();
	private static final TrackedData<Integer> ANGER_TIME = DataTracker.registerData(SchmuckEntity.class, TrackedDataHandlerRegistry.INTEGER);

	private static final IntRange ANGER_TIME_RANGE = Durations.betweenSeconds(20, 39);

	/** Used for when the Schmuck obtains a bow or egg. */
	private final SchmuckBowAttackGoal bowAttackGoal = new SchmuckBowAttackGoal(1.0D, 20, 15.0F);
	/** Used for when the Schmuck obtains any item other than a pickaxe, bow, or egg. */
	private final MeleeAttackGoal meleeAttackGoal = new MeleeAttackGoal(this, 1.2D, false);
	/** Used for melee. */
	private final PounceAtTargetGoal pounceGoal = new PounceAtTargetGoal(this, 0.3F);
	/** Used for when the Schmuck obtains a pickaxe. */
	private final SchmuckMine mineGoal = new SchmuckMine(this, 1.0D, 60);
	/** Used for when the Schmuck obtains a hoe. */
	private final SchmuckTill tillGoal = new SchmuckTill(this, 1.0D, 10);
	/** Used for when the Schmuck obtains an axe. */
	private final SchmuckFellTree fellGoal = new SchmuckFellTree(this, 1.0D, 40);

	private final RevengeGoal shortTemperRevengeGoal = (new RevengeGoal(this)).setGroupRevenge();
	private final RevengeGoal revengeGoal = (new RevengeGoal(this, SchmuckEntity.class)).setGroupRevenge();

	private UUID targetUuid;

	private boolean shortTempered = false;
	private boolean canTeleport = true;
	private boolean canFollow = true;

	private int eggUsageTime;
	private int flyCheckCooldown;

	/** Used to replace the flight control back with the old one (before starting flight control). */
	private MoveControl oldMoveControl;
	/** Used to replace the flight nav with the old one (before starting flight navigation) */
	private EntityNavigation oldNavigation;
	/** Used for mid-elytra flight. */
	private final FlightMoveControl flightMoveControl = new FlightMoveControl(this, 20, false);
	/** Used for mid-elytra flight, more specifically how the Schmuck will path. */
	private final BirdNavigation flightNavigation = this.createFlightNavigation();

	public SchmuckEntity(EntityType<? extends SchmuckEntity> entityType, World world) {
		super(entityType, world);
		this.lookControl = new SchmuckLookControl(this);
		this.setCanPickUpLoot(true);
		((MobNavigation) this.getNavigation()).setCanPathThroughDoors(true);
	}

	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable CompoundTag entityTag) {
		if (random.nextFloat() < Schmucks.CONFIG.leatherHelmetChance.floatValue() / 100) {
			this.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
		}
		shortTempered = Schmucks.CONFIG.chaosMode || random.nextFloat() < Schmucks.CONFIG.shortTemperChance.floatValue() / 100; // will attack teammates if damaged
		this.updateAttackType();
		return super.initialize(world, difficulty, spawnReason, entityData, entityTag);
	}

	@Override
	protected void initGoals() {
		super.initGoals();
		this.goalSelector.add(1, new SwimGoal(this));
		this.goalSelector.add(2, new FleeEntityGoal<>(this, CreeperEntity.class, 16, 1.0D, 1.2D));
		this.goalSelector.add(3, new SitGoal(this));
		this.goalSelector.add(6, new SchmuckFollowOwner(this, 1.0D, 15.0F, 4.0F, false));
		this.goalSelector.add(7, new AnimalMateGoal(this, 1.0D));
		this.goalSelector.add(8, new SchmuckSmeltGoal(this, 1.0D));
		this.goalSelector.add(9 , new SchmuckPutUnneeded(this, 1.0D));
		this.goalSelector.add(10, new SchmuckPickUpItemGoal());
		this.goalSelector.add(11, new SchmuckFleeAllJobs(this, 1.0D));
		this.goalSelector.add(12, new SchmuckFleeGoal<>(PlayerEntity.class));
		this.goalSelector.add(13, new WanderAroundFarGoal(this, 1.0D));
		this.goalSelector.add(14, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
		this.goalSelector.add(14, new LookAtEntityGoal(this, SchmuckEntity.class, 8.0F));
		this.goalSelector.add(14, new LookAroundGoal(this));
		this.targetSelector.add(1, new TrackOwnerAttackerGoal(this));
		this.targetSelector.add(2, new AttackWithOwnerGoal(this));
		this.targetSelector.add(4, new UniversalAngerGoal<>(this, true));
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		if (!player.isSneaking()) {
			return super.interactMob(player, hand);
		}

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

	/**
	 * Used to make thw Schmuck easily toggle between sitting and standing. As there is no animation for sitting, it's
	 * more appropriately called stopped.
	 */
	public void toggleSit() {
		this.setSitting(!this.isSitting());
		this.jumping = false;
		this.navigation.stop();
		this.setTarget(null);
	}

	@Override
	public void tickMovement() {
		super.tickHandSwing();
		super.tickMovement();

		this.checkFallFlying();

		if (!this.world.isClient) {
			this.tickAngerLogic((ServerWorld)this.world, true);

			if (this.getTarget() != null && this.getTarget().getType() == EntityType.PLAYER) {
				List<SchmuckEntity> entities = this.world.getEntitiesIncludingUngeneratedChunks(SchmuckEntity.class,
						this.getBoundingBox().expand(10),
						schmuck -> schmuck.getOwnerUuid() != this.getOwnerUuid());
				this.setTarget(entities.get(this.random.nextInt(entities.size() - 1)));
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.updateAnimation();
	}

	@Override
	public void writeCustomDataToTag(CompoundTag tag) {
		super.writeCustomDataToTag(tag);
		tag.putBoolean("ShortTemper", this.shortTempered);
		tag.putBoolean("CanTeleport", this.canTeleport);
		tag.putBoolean("CanFollow", this.canFollow);
		this.angerToTag(tag);
	}

	@Override
	public void readCustomDataFromTag(CompoundTag tag) {
		super.readCustomDataFromTag(tag);
		this.angerFromTag((ServerWorld) this.world, tag);
		this.shortTempered = tag.getBoolean("ShortTemper");
		this.canTeleport = tag.getBoolean("CanTeleport");
		this.canFollow = tag.getBoolean("CanFollow");
		if (tag.contains("Whitelisted") && this.getOwner() != null) {
			ListTag list = tag.getList("Whitelisted", 10);
			WhitelistComponent component = Schmucks.getWhitelistComponent((PlayerEntity) this.getOwner());
			list.forEach(blockPosTag -> {
				BlockPos pos = NbtHelper.toBlockPos((CompoundTag) blockPosTag);
				if (!component.containsWhiteList(pos)) {
					component.addWhitelist(pos);
				}
			});
			component.sync();
		}
		this.updateAttackType();
	}

	/**
	 * Used to create the flight navigation for mid-elytra flight. Without it, they would take unsatisfactory twists and
	 * turns.
	 * @return BirdNavigation that the SchmuckEntity uses during flight.
	 * */
	public BirdNavigation createFlightNavigation() {
		BirdNavigation birdNavigation = new BirdNavigation(this, this.world);
		birdNavigation.setCanPathThroughDoors(false);
		birdNavigation.setCanSwim(true);
		birdNavigation.setCanEnterOpenDoors(true);
		return birdNavigation;
	}

	/** Replaces the current MoveControl with a FlightMoveControl. This allows the Schmuck to fly in a sensible way */
	public void startFlightControl() {
		this.oldMoveControl = this.moveControl;
		this.oldNavigation = this.navigation;
		this.navigation = this.flightNavigation;
		this.moveControl = this.flightMoveControl;
	}

	/** Replaces the FlightMoveControl with the old default MoveControl. */
	public void stopFlightControl() {
		if (this.oldMoveControl == null || this.oldNavigation == null) {
			return;
		}
		this.moveControl = this.oldMoveControl;
		this.navigation = this.oldNavigation;
	}

	/** Checks if the Schmuck is currently flying with an elytra. */
	public void checkFallFlying() {
		if (this.flyCheckCooldown-- > 0) {
			return;
		}
		if (!this.onGround &&
			!this.isFallFlying() &&
			!this.isTouchingWater() &&
			!this.hasStatusEffect(StatusEffects.LEVITATION) &&
			this.fallDistance > 2) {
			ItemStack itemStack = this.getEquippedStack(EquipmentSlot.CHEST);
			if (itemStack.getItem() == Items.ELYTRA && ElytraItem.isUsable(itemStack)) {
				this.startFallFlying();
			}
		} else if (this.onGround || this.isTouchingWater() || this.hasStatusEffect(StatusEffects.LEVITATION)) {
			this.stopFallFlying();
		}
		this.flyCheckCooldown = 3;
	}

	/** Sets the internal flags for the Schmuck to start doing the fly animation. */
	public void startFallFlying() {
		System.out.println("Test");
		this.setFlag(7, true);
		this.startFlightControl();
	}

	/** Sets the internal flags for the Schmuck to stop doing the fly animation. */
	public void stopFallFlying() {
//		this.setFlag(7, true);
		this.stopFlightControl();
		this.setFlag(7, false);
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
	}

	public static DefaultAttributeContainer.Builder createSchmuckAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
				.add(EntityAttributes.GENERIC_FLYING_SPEED, 0.6)
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 16.0)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
	}

	/** This sets the current pose of the Schmuck, based on several variables. */
	protected void updateAnimation() {
		if (this.wouldPoseNotCollide(EntityPose.SWIMMING)) {
			EntityPose entityPose6;
			if (this.isFallFlying()) {
				entityPose6 = EntityPose.FALL_FLYING;
			} else if (this.isSleeping()) {
				entityPose6 = EntityPose.SLEEPING;
			} else if (this.isSwimming()) {
				entityPose6 = EntityPose.SWIMMING;
			} else if (this.isUsingRiptide()) {
				entityPose6 = EntityPose.SPIN_ATTACK;
			} else if (this.isSneaking()) {
				entityPose6 = EntityPose.CROUCHING;
			} else {
				entityPose6 = EntityPose.STANDING;
			}

			EntityPose entityPose9;
			if (!this.isSpectator() && !this.hasVehicle() && !this.wouldPoseNotCollide(entityPose6)) {
				if (this.wouldPoseNotCollide(EntityPose.CROUCHING)) {
					entityPose9 = EntityPose.CROUCHING;
				} else {
					entityPose9 = EntityPose.SWIMMING;
				}
			} else {
				entityPose9 = entityPose6;
			}

			this.setPose(entityPose9);
		}
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
	}

	@Override
	public void setAttacking(@Nullable PlayerEntity attacking) {
		super.setAttacking(attacking);
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

	public boolean getCanFollow() {
		return canFollow;
	}

	public void setCanFollow(boolean canFollow) {
		this.canFollow = canFollow;
	}

	public List<BlockPos> getWhitelist() {
		return Schmucks.getWhitelistOrEmpty((PlayerEntity) this.getOwner());
	}

	@Override
	public void onDeath(DamageSource source) {
		super.onDeath(source);
	}

	public void equipNoUpdate(EquipmentSlot slot, ItemStack stack) {
		super.equipStack(slot, stack);
	}

	public void updateAttackType() {
		if (this.world != null && !this.world.isClient) {
			this.goalSelector.remove(this.meleeAttackGoal);
			this.goalSelector.remove(this.bowAttackGoal);
			this.goalSelector.remove(this.mineGoal);
			this.goalSelector.remove(this.pounceGoal);
			this.goalSelector.remove(this.tillGoal);
			this.goalSelector.remove(this.fellGoal);
			this.targetSelector.remove(this.shortTemperRevengeGoal);
			this.targetSelector.remove(this.revengeGoal);
			ItemStack itemStack = this.getMainHandStack();
			if (itemStack.getItem() == Items.BOW || itemStack.getItem() == Items.EGG) {
				this.bowAttackGoal.setAttackInterval(itemStack.getItem() == Items.EGG ? 5 : 30);
				this.goalSelector.add(5, this.bowAttackGoal);
			} else if (FabricToolTags.PICKAXES.contains(itemStack.getItem())) {
				this.goalSelector.add(5, this.mineGoal);
			} else if (FabricToolTags.HOES.contains(itemStack.getItem())) {
				this.goalSelector.add(4, this.meleeAttackGoal);
				this.goalSelector.add(5, this.tillGoal);
			} else if (FabricToolTags.AXES.contains(itemStack.getItem())) {
				this.goalSelector.add(4, this.meleeAttackGoal);
				this.goalSelector.add(5, this.fellGoal);
			} else {
				this.goalSelector.add(4, this.pounceGoal);
				this.goalSelector.add(5, this.meleeAttackGoal);
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

	/**
	 * Creates an arrow projectile from an ItemStack. Borrowed from the Skeleton code.
	 * @param arrow ItemStack of arrow to recreate
	 * @param damageModifier Multiplier for how much damage the arrow should do
	 * @return Arrow entity created from given stack and modifier.
	 */
	protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier) {
		return ProjectileUtil.createArrowProjectile(this, arrow, damageModifier);
	}

	/**
	 * Get the model ("default" or "slim") of the owners model. If an owner is not available, "default" is returned.
	 * @return "default" or "slim"
	 */
	public String getModel() {
		return this.getOwnerUuid() != null ? DefaultSkinHelper.getModel(this.getOwnerUuid()) : "default";
	}

	/** Picks up items in a radius. Incredibly similar to the foxes pickup item goal. */
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

	/** Overridden to ensure that eggs work for the SchmuckBowAttackGoal. */
	@Override
	public boolean isUsingItem() {
		return this.getMainHandStack().getItem() == Items.EGG || super.isUsingItem();
	}

	/** Moves and shoots a target tactically. Borrowed from the Skeleton code. */
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

	/** Used for fleeing the player (albeit a small radius) so space is given for the player to roam around. */
	class SchmuckFleeGoal<T extends LivingEntity> extends FleeEntityGoal<T> {

		public SchmuckFleeGoal(Class<T> fleeFromType) {
			super(SchmuckEntity.this, fleeFromType, 2, 1.0D, 1.0D, (entity) -> !(entity instanceof PlayerEntity));
		}
	}
}
