package com.alotofletters.schmucks.entity;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.ai.*;
import com.alotofletters.schmucks.entity.ai.control.SchmuckLookControl;
import com.alotofletters.schmucks.screen.SchmuckScreenHandler;
import com.alotofletters.schmucks.specialization.modifier.Modifier;
import com.alotofletters.schmucks.specialization.modifier.Modifiers;
import com.google.common.collect.Maps;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.BlockState;
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
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Angerable;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileUtil;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.*;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.ItemTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.intprovider.IntProvider;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class SchmuckEntity extends TameableEntity implements
		Angerable, InventoryOwner, RangedAttackMob, ExtendedScreenHandlerFactory {
	private static final TrackedData<Integer> ANGER_TIME = DataTracker.registerData(SchmuckEntity.class, TrackedDataHandlerRegistry.INTEGER);

	private static final IntProvider ANGER_TIME_RANGE = Durations.betweenSeconds(20, 39);
	private static final Map<PredicatedGoalFactory, Integer> GOALS = Maps.newHashMap();
	private static final Map<PredicatedGoalFactory, Integer> TARGET_SELECTORS = Maps.newHashMap();

	static {
		addGoal(1, SwimGoal::new);
		addGoal(2, SchmuckFleeCreeperGoal::new);
		addGoal(3, SitGoal::new);
		addGoal(6, schmuck -> new SchmuckFollowOwner(schmuck, 1.0D, 15.0F, 4.0F, false));
//		addGoal(7, schmuck -> new AnimalMateGoal(schmuck, 1));
		addGoal(8, schmuck -> new SchmuckSmeltGoal(schmuck, 1));
		addGoal(9, schmuck -> new SchmuckPutUnneeded(schmuck, 1));
		addGoal(10, SchmuckPickUpItemGoal::new);
		addGoal(11, schmuck -> new SchmuckFleeAllJobs(schmuck, 1.0D));
		addGoal(12, schmuck -> new SchmuckFleeGoal<>(schmuck, PlayerEntity.class));
		addGoal(13, schmuck -> new WanderAroundFarGoal(schmuck, 1.0D));
		addGoal(14, schmuck -> new LookAtEntityGoal(schmuck, PlayerEntity.class, 8.0F));
		addGoal(14, schmuck -> new LookAtEntityGoal(schmuck, SchmuckEntity.class, 8.0F));
		addGoal(14, LookAroundGoal::new);
		addSelector(1, TrackOwnerAttackerGoal::new);
		addSelector(2, AttackWithOwnerGoal::new);
		addSelectorPredicate(
				2,
				schmuck -> new FollowTargetGoal<>(schmuck, HostileEntity.class, true),
				schmuck -> schmuck.isGladiator() || schmuck.isRanger());
		addSelectorPredicate(3, SchmuckRevengeGoal::new, schmuck -> schmuck.shortTempered);
		addSelectorPredicate(3, schmuck -> new SchmuckRevengeGoal(schmuck, SchmuckEntity.class), schmuck -> !schmuck.shortTempered);
		addSelector(4, schmuck -> new UniversalAngerGoal<>(schmuck, true));

		Function<SchmuckEntity, Goal> goal = schmuck -> new SchmuckBowAttackGoal(schmuck, 1.0D, 20, 15.0F);
		addGoalPredicate(5, goal, SchmuckEntity::isRanger);
		addGoalPredicate(5, schmuck -> new SchmuckMine(schmuck, 1.0D, 60), SchmuckEntity::isMiner);
		addGoalPredicate(5, schmuck -> new SchmuckTill(schmuck, 1.0D, 10), SchmuckEntity::isFarmer);
		addGoalPredicate(5, schmuck -> new SchmuckFellTree(schmuck, 1.0D, 40), SchmuckEntity::isLumberjack);
//		addGoalPredicate(4, schmuck -> new PounceAtTargetGoal(schmuck, 0.3F), schmuck -> !schmuck.hasJob() || schmuck.isGladiator());
		addGoalPredicate(5, schmuck -> new MeleeAttackGoal(schmuck, 1.2D, false), schmuck -> !schmuck.hasJob() || schmuck.isGladiator());
	}

	/**
	 * Used for mid-elytra flight.
	 */
	private final FlightMoveControl flightMoveControl = new FlightMoveControl(this, 20, false);
	/**
	 * Used for mid-elytra flight, more specifically how the Schmuck will path.
	 */
	private final BirdNavigation flightNavigation = this.createFlightNavigation();
	private final SimpleInventory inventory = new SimpleInventory(5);
	private final SchmuckEquipmentInventory equipmentInventory;
	private UUID targetUuid;
	private PlayerEntity targetPlayer;
	private boolean shortTempered = false;
	private boolean canTeleport = true;
	private boolean canFollow = true;
	private boolean hasGivenArrows = false;
	private int eggUsageTime;
	private int flyCheckCooldown;
	/**
	 * Used to replace the flight control back with the old one (before starting flight control).
	 */
	private MoveControl oldMoveControl;
	/**
	 * Used to replace the flight nav with the old one (before starting flight navigation)
	 */
	private EntityNavigation oldNavigation;

	public SchmuckEntity(EntityType<? extends SchmuckEntity> entityType, World world) {
		super(entityType, world);
		this.lookControl = new SchmuckLookControl(this);
		this.equipmentInventory = new SchmuckEquipmentInventory(this);
		this.setCanPickUpLoot(true);
		((MobNavigation) this.getNavigation()).setCanPathThroughDoors(true);
		this.getNavigation().setCanSwim(true);
	}

	public static DefaultAttributeContainer.Builder createSchmuckAttributes() {
		return MobEntity.createMobAttributes()
				.add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
				.add(EntityAttributes.GENERIC_FLYING_SPEED, 0.6)
				.add(EntityAttributes.GENERIC_MAX_HEALTH, 16.0)
				.add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 2.0);
	}

	@Override
	public void remove(RemovalReason reason) {
		super.remove(reason);

		GOALS.forEach((factory, priority) -> factory.remove(this));
		TARGET_SELECTORS.forEach((factory, priority) -> factory.remove(this));
	}

	@Override
	protected void dropEquipment(DamageSource source, int lootingMultiplier, boolean allowDrops) {
		super.dropEquipment(source, lootingMultiplier, allowDrops);
		this.inventory.clearToList().forEach(this::dropStack);
	}

	public PlayerInventory getOwnerInventory() {
		if (this.getOwner() != null) {
			return ((PlayerEntity) this.getOwner()).getInventory();
		}
		return null;
	}

	public static void addSelectorPredicate(int priority, Function<SchmuckEntity, Goal> goal, Predicate<SchmuckEntity> predicate) {
		SchmuckEntity.TARGET_SELECTORS.put(new PredicatedGoalFactory(predicate, goal), priority);
	}

	public static void addSelector(int priority, Function<SchmuckEntity, Goal> goal) {
		SchmuckEntity.addSelectorPredicate(priority, goal, e -> true);
	}

	public static void addGoalPredicate(int priority, Function<SchmuckEntity, Goal> goal, Predicate<SchmuckEntity> predicate) {
		SchmuckEntity.GOALS.put(new PredicatedGoalFactory(predicate, goal), priority);
	}

	public static void addGoal(int priority, Function<SchmuckEntity, Goal> goal) {
		SchmuckEntity.addGoalPredicate(priority, goal, e -> true);
	}

	public static void addGoalItem(int priority, Function<SchmuckEntity, Goal> goal, Item item) {
		SchmuckEntity.addGoalPredicate(priority, goal, e -> item.equals(e.getMainHandStack().getItem()));
	}

	public static void addGoalTag(int priority, Function<SchmuckEntity, Goal> goal, Tag<Item> tag) {
		SchmuckEntity.addGoalPredicate(priority, goal, e -> e.getMainHandStack().isIn(tag));
	}

	public static void addGoalModifier(int priority, Function<SchmuckEntity, Goal> goal, Modifier modifier) {
		SchmuckEntity.addGoalPredicate(priority, goal, e -> e.hasModifier(modifier));
	}

	public static void addGoalModifier_n(int priority, Function<SchmuckEntity, Goal> goal, Modifier modifier) {
		SchmuckEntity.addGoalPredicate(priority, goal, e -> !e.hasModifier(modifier));
	}

	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable NbtCompound entityTag) {
		if (random.nextFloat() < Schmucks.CONFIG.leatherHelmetChance.floatValue() / 100) {
			this.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.LEATHER_HELMET));
		}
		shortTempered = Schmucks.CONFIG.chaosMode || random.nextFloat() < Schmucks.CONFIG.shortTemperChance.floatValue() / 100; // will attack teammates if damaged
		this.applyExistingModifiers();
		this.refreshGoals();
		return super.initialize(world, difficulty, spawnReason, entityData, entityTag);
	}

	public void refreshGoals() {
		super.initGoals();
		GOALS.forEach((factory, priority) -> factory.apply(priority, this, this.goalSelector));
		TARGET_SELECTORS.forEach((factory, priority) -> factory.apply(priority, this, this.targetSelector));
		if (!this.hasGivenArrows && this.isRanger()) {
			ItemStack stack = new ItemStack(Items.ARROW, 10 + this.getModifierLevel(Modifiers.EXPANDABLE_QUIVER) * 30);
			if (this.inventory.canInsert(stack)) {
				this.inventory.addStack(stack);
				this.hasGivenArrows = true;
			}
		}
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.getAttacker() instanceof SchmuckEntity schmuck && schmuck.getOwner() == this.getOwner()) {
			return false;
		}
		return super.damage(source, amount);
	}

	@Nullable
	@Override
	public ItemEntity dropStack(ItemStack stack, float yOffset) {
		if (stack.isIn(ItemTags.ARROWS)) {
			return null;
		}
		return super.dropStack(stack, yOffset);
	}

	@Override
	public ActionResult interactMob(PlayerEntity player, Hand hand) {
		if (!player.isSneaking()) {
			return super.interactMob(player, hand);
		}

		if (player != this.getOwner()) {
			return ActionResult.PASS;
		}

		if (player.getStackInHand(hand).isEmpty()) {
			if (!player.giveItemStack(this.getMainHandStack())) {
				this.dropStack(this.getMainHandStack());
			}
			this.equipStack(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
			return ActionResult.SUCCESS;
		}

		if (player.getStackInHand(hand).getItem() == Schmucks.CONTROL_WAND) {
			this.toggleSit();
			return ActionResult.SUCCESS;
		}

		return super.interactMob(player, hand);
	}

	public boolean hasModifier(Modifier modifier) {
		if (this.getOwner() == null || !(this.getOwner() instanceof PlayerEntity player)) {
			return false;
		}
		return false;//Schmucks.SPECIALIZATIONS.get(player).hasModifier(modifier);
	}

	public int getModifierLevel(Modifier modifier) {
		if (this.getOwner() == null || !(this.getOwner() instanceof PlayerEntity player)) {
			return 0;
		}
		return 0; //Schmucks.SPECIALIZATIONS.get(player).getModifierLevel(modifier);
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
			this.tickAngerLogic((ServerWorld) this.world, true);

			if (this.targetPlayer != null && this.targetPlayer.isDead()) {
				this.targetPlayer = null;
			} else if (this.targetPlayer != null && this.getTarget() == null) {
				this.setTarget(this.targetPlayer);
			}

			if (this.getTarget() != null && this.getTarget().getType() == EntityType.PLAYER) {
				List<SchmuckEntity> entities = this.world.getEntitiesByClass(SchmuckEntity.class,
						this.getBoundingBox().expand(10),
						schmuck -> schmuck.getOwner() == this.getTarget());

				if (entities.size() > 1) {
					this.targetPlayer = (PlayerEntity) this.getTarget();
					this.setTarget(entities.get(this.random.nextInt(entities.size() - 1)));
				}
			}
		}
	}

	@Override
	public void tick() {
		super.tick();
		this.updateAnimation();
	}

	@Override
	public void writeCustomDataToNbt(NbtCompound tag) {
		super.writeCustomDataToNbt(tag);
		tag.putBoolean("ShortTemper", this.shortTempered);
		tag.putBoolean("CanTeleport", this.canTeleport);
		tag.putBoolean("HasGivenArrows", this.hasGivenArrows);
		tag.putBoolean("CanFollow", this.canFollow);
		tag.put("Inventory", this.inventory.toNbtList());
		this.writeAngerToNbt(tag);
	}

	@Override
	public void readCustomDataFromNbt(NbtCompound tag) {
		super.readCustomDataFromNbt(tag);
		this.readAngerFromNbt(this.world, tag);
		this.shortTempered = tag.getBoolean("ShortTemper");
		this.canTeleport = tag.getBoolean("CanTeleport");
		this.hasGivenArrows = tag.getBoolean("HasGivenArrows");
		this.canFollow = tag.getBoolean("CanFollow");
		this.inventory.readNbtList(tag.getList("Inventory", 10));
		if (tag.contains("Whitelisted") && this.getOwner() != null) {
			NbtList list = tag.getList("Whitelisted", 10);
			WhitelistComponent component = Schmucks.getWhitelistComponent((PlayerEntity) this.getOwner());
			list.forEach(blockPosTag -> {
				BlockPos pos = NbtHelper.toBlockPos((NbtCompound) blockPosTag);
				if (!component.containsWhiteList(pos)) {
					component.addWhitelist(pos);
				}
			});
			component.sync();
		}
		this.refreshGoals();
	}

	public void applyExistingModifiers() {
		if (this.getOwner() == null || !(this.getOwner() instanceof PlayerEntity player)) {
			return;
		}
		//Schmucks.SPECIALIZATIONS.get(player).apply(this);
	}

	/**
	 * Used to create the flight navigation for mid-elytra flight. Without it, they would take unsatisfactory twists and
	 * turns.
	 *
	 * @return BirdNavigation that the SchmuckEntity uses during flight.
	 */
	public BirdNavigation createFlightNavigation() {
		BirdNavigation birdNavigation = new BirdNavigation(this, this.world);
		birdNavigation.setCanPathThroughDoors(false);
		birdNavigation.setCanSwim(true);
		birdNavigation.setCanEnterOpenDoors(true);
		return birdNavigation;
	}

	/**
	 * Replaces the current MoveControl with a FlightMoveControl. This allows the Schmuck to fly in a sensible way
	 */
	public void startFlightControl() {
		this.oldMoveControl = this.moveControl;
		this.oldNavigation = this.navigation;
		this.navigation = this.flightNavigation;
		this.moveControl = this.flightMoveControl;
	}

	/**
	 * Replaces the FlightMoveControl with the old default MoveControl.
	 */
	public void stopFlightControl() {
		if (this.oldMoveControl == null || this.oldNavigation == null) {
			return;
		}
		this.moveControl = this.oldMoveControl;
		this.navigation = this.oldNavigation;
	}

	/**
	 * Checks if the Schmuck is currently flying with an Elytra.
	 */
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

	public boolean displaysHat() {
		return (this.hasJob() && this.getEquippedStack(EquipmentSlot.HEAD).isEmpty())
				|| this.getEquippedStack(EquipmentSlot.HEAD).isIn(Schmucks.JOB_HATS_TAG);
	}

	public boolean displaysMinersCap() {
		return (this.isMiner() && this.getEquippedStack(EquipmentSlot.HEAD).isEmpty())
				|| this.getEquippedStack(EquipmentSlot.HEAD).getItem() == Schmucks.MINERS_CAP;
	}

	public boolean hasJob() {
		return this.isGladiator()
				|| this.isMiner()
				|| this.isFarmer()
				|| this.isLumberjack()
				|| this.isRanger();
	}

	public boolean isGladiator() {
		return this.getMainHandStack().isIn(FabricToolTags.SWORDS);
	}

	public boolean isMiner() {
		return this.getMainHandStack().isIn(FabricToolTags.PICKAXES);
	}

	public boolean isFarmer() {
		return this.getMainHandStack().isIn(FabricToolTags.HOES);
	}

	public boolean isLumberjack() {
		return this.getMainHandStack().isIn(FabricToolTags.AXES);
	}

	public boolean isRanger() {
		Item item = this.getMainHandStack().getItem();
		return item == Items.BOW || item == Items.EGG;
	}

	public boolean canGather(ItemStack stack) {
		if (stack.getItem() instanceof Wearable) {
			return this.getEquippedStack(getPreferredEquipmentSlot(stack)).isEmpty();
		}
		if (!this.hasJob()) {
			Item item = stack.getItem();
			return item instanceof ToolItem || item instanceof RangedWeaponItem;
		} else if (this.isGladiator()) {
			return stack.isIn(Schmucks.GLADIATOR_LIKES);
		} else if (this.isMiner()) {
			return stack.isIn(Schmucks.MINER_LIKES);
		} else if (this.isFarmer()) {
			return stack.isIn(Schmucks.FARMER_LIKES);
		} else if (this.isLumberjack()) {
			return stack.isIn(Schmucks.LUMBERJACK_LIKES);
		} else if (this.isRanger()) {
			return stack.isIn(Schmucks.RANGER_LIKES);
		}
		return false;
	}

	@Override
	protected void loot(ItemEntity item) {
		var itemStack = item.getStack();
		if (this.canGather(itemStack)) {
			var slot = getPreferredEquipmentSlot(itemStack);
			if (this.getEquippedStack(slot).isEmpty()) {
				super.loot(item);
				return;
			} else if (!this.hasJob() && !this.canGather(this.getEquippedStack(slot))) { // doesn't want non-job item
				this.dropStack(this.getEquippedStack(slot));
				this.equipNoUpdate(slot, ItemStack.EMPTY);
				super.loot(item);
				return;
			}
			var simpleInventory = this.getInventory();
			var bl = simpleInventory.canInsert(itemStack);
			if (!bl) {
				return;
			}

			this.triggerItemPickedUpByEntityCriteria(item);
			this.sendPickup(item, itemStack.getCount());
			ItemStack itemStack2 = simpleInventory.addStack(itemStack);
			if (itemStack2.isEmpty()) {
				item.discard();
			} else {
				itemStack.setCount(itemStack2.getCount());
			}
		}
	}

	public SimpleInventory getInventory() {
		return this.inventory;
	}

	/**
	 * Sets the internal flags for the Schmuck to start doing the fly animation.
	 */
	public void startFallFlying() {
		this.setFlag(7, true);
		this.startFlightControl();
	}

	/**
	 * Sets the internal flags for the Schmuck to stop doing the fly animation.
	 */
	public void stopFallFlying() {
		this.stopFlightControl();
		this.setFlag(7, false);
	}

	@Nullable
	@Override
	public SchmuckEntity createChild(ServerWorld serverWorld, PassiveEntity passiveEntity) {
		var child = Schmucks.SCHMUCK.create(serverWorld);
		if (child != null) {
			child.setOwnerUuid(this.getOwnerUuid());
		}
		return child;
	}

	protected void initDataTracker() {
		super.initDataTracker();
		this.dataTracker.startTracking(ANGER_TIME, 0);
	}

	/**
	 * This sets the current pose of the Schmuck, based on several variables.
	 */
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
	public void applyDamageEffects(LivingEntity attacker, Entity target) {
		super.applyDamageEffects(attacker, target);
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
		this.setAngerTime(ANGER_TIME_RANGE.get(this.random));
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
			this.refreshGoals();
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

	public Set<BlockPos> getWhitelist() {
		return Schmucks.getWhitelistOrEmpty((PlayerEntity) this.getOwner());
	}

	@Override
	public void onDeath(DamageSource source) {
		super.onDeath(source);
	}

	public void equipNoUpdate(EquipmentSlot slot, ItemStack stack) {
		super.equipStack(slot, stack);
	}

	public boolean canUseRangedWeapon(RangedWeaponItem weapon) {
		return weapon == Items.BOW;
	}

	public void attack(LivingEntity target, float pullProgress) {
		if (this.isHolding(Items.EGG)) {
			var itemStack = this.getMainHandStack();
			var eggEntity = new EggEntity(world, this);
			eggEntity.setItem(itemStack);
			shootEntity(target, eggEntity);
			this.world.spawnEntity(eggEntity);
			this.playSound(SoundEvents.ENTITY_EGG_THROW, 0.5F, 0.4F / (this.random.nextFloat() * 0.4F + 0.8F));
			return;
		}
		var itemStack = this.getArrowType(this.getStackInHand(ProjectileUtil.getHandPossiblyHolding(this, Items.BOW)));
		PersistentProjectileEntity persistentProjectileEntity = this.createArrowProjectile(itemStack, pullProgress);
		shootEntity(target, persistentProjectileEntity);
		this.world.spawnEntity(persistentProjectileEntity);
		this.playSound(SoundEvents.ENTITY_SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
	}

	private void shootEntity(LivingEntity target, ProjectileEntity projectileEntity) {
		var d = target.getX() - this.getX();
		var e = target.getBodyY(0.3333333333333333D) - projectileEntity.getY();
		var f = target.getZ() - this.getZ();
		var g = MathHelper.sqrt((float) (d * d + f * f));
		projectileEntity.setVelocity(d, e + g * 0.20000000298023224D, f, 1.6F, 2f);
	}

	/**
	 * Creates an arrow projectile from an ItemStack. Borrowed from the Skeleton code.
	 *
	 * @param arrow          ItemStack of arrow to recreate
	 * @param damageModifier Multiplier for how much damage the arrow should do
	 * @return Arrow entity created from given stack and modifier.
	 */
	protected PersistentProjectileEntity createArrowProjectile(ItemStack arrow, float damageModifier) {
		return ProjectileUtil.createArrowProjectile(this, arrow, damageModifier);
	}

	/**
	 * Get the model ("default" or "slim") of the owners model. If an owner is not available, "default" is returned.
	 *
	 * @return "default" or "slim"
	 */
	public String getModel() {
		return this.getOwnerUuid() != null ? DefaultSkinHelper.getModel(this.getOwnerUuid()) : "default";
	}

	@Override
	public int getItemUseTime() {
		if (++this.eggUsageTime == 6) {
			this.eggUsageTime = 0;
		}
		return this.getMainHandStack().getItem() == Items.EGG ? 15 + this.eggUsageTime : super.getItemUseTime();
	}

	/**
	 * Overridden to ensure that eggs work for the SchmuckBowAttackGoal.
	 */
	@Override
	public boolean isUsingItem() {
		return this.getMainHandStack().getItem() == Items.EGG || super.isUsingItem();
	}

	@Override
	public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
		buf.writeBoolean(true);
		buf.writeVarInt(this.getId());
	}

	@Nullable
	@Override
	public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
		return new SchmuckScreenHandler(syncId, inv, this.getInventory(), this.getEquipmentInventory(), this);
	}

	public SchmuckEquipmentInventory getEquipmentInventory() {
		return equipmentInventory;
	}

	static class PredicatedGoalFactory {
		private final Predicate<SchmuckEntity> predicate;
		private final Function<SchmuckEntity, Goal> goal;
		private final Map<UUID, Goal> goals = Maps.newHashMap();
		private final Map<UUID, Boolean> applied = Maps.newHashMap();

		PredicatedGoalFactory(Predicate<SchmuckEntity> predicate, Function<SchmuckEntity, Goal> goal) {
			this.predicate = predicate;
			this.goal = goal;
		}

		public void apply(int priority, SchmuckEntity schmuck, GoalSelector selector) {
			var result = this.predicate.test(schmuck);
			if (result && !getApplied(schmuck)) {
				selector.add(priority, this.getGoal(schmuck));
				applied.put(schmuck.uuid, true);
			} else if (!result && getApplied(schmuck)) {
				selector.remove(this.getGoal(schmuck));
				applied.put(schmuck.uuid, false);
			}
		}

		public void remove(SchmuckEntity schmuck) {
			goals.remove(schmuck.uuid);
			applied.remove(schmuck.uuid);
		}

		private boolean getApplied(SchmuckEntity schmuck) {
			if (applied.containsKey(schmuck.uuid)) {
				return applied.get(schmuck.uuid);
			}
			applied.put(schmuck.uuid, false);
			return false;
		}

		private Goal getGoal(SchmuckEntity schmuck) {
			if (goals.containsKey(schmuck.uuid)) {
				return goals.get(schmuck.uuid);
			}
			Goal out = this.goal.apply(schmuck);
			goals.put(schmuck.uuid, out);
			return out;
		}
	}

}
