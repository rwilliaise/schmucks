package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.config.SchmucksConfig;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.SaplingBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

/**
 * Makes Schmucks put unneeded items inside of whitelisted storage containers, or if possible, on farmland.
 */
public class SchmuckPutUnneeded extends SchmuckJobGoal {
	private final SchmuckEntity schmuck;
	private boolean placed;

	public SchmuckPutUnneeded(SchmuckEntity mob, double speed) {
		super(mob, speed, AutoConfig.getConfigHolder(SchmucksConfig.class).getConfig().jobRange, 2);
		this.schmuck = mob;
	}

	@Override
	public boolean canStart() {
		return !this.schmuck.isSitting() && this.schmuck.isTamed() && this.isStorable(this.schmuck.getMainHandStack()) && super.canStart();
	}

	@Override
	protected int getInterval(PathAwareEntity mob) {
		return 60;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.hasReached()) {
			BlockState state = this.schmuck.world.getBlockState(this.targetPos);
			if (this.isPlantable(this.schmuck.getMainHandStack()) && state.isOf(Blocks.FARMLAND)) {
				this.putFarmland();
			} else if (this.isSapling(this.schmuck.getMainHandStack()) && state.isAir()) {
				this.putSapling();
			} else if (this.isStorable(this.schmuck.getMainHandStack())) {
				this.putChest();
			}

			this.schmuck.swingHand(Hand.MAIN_HAND);
		}
	}

	@Override
	public boolean shouldContinue() {
		if (placed) {
			return false;
		} else if (this.isPlantable(this.schmuck.getMainHandStack()) && this.schmuck.world.isAir(this.targetPos.up())) {
			return true;
		} else if (this.isSapling(this.schmuck.getMainHandStack()) && this.schmuck.world.isAir(this.targetPos)) {
			return true;
		} else return this.isStorable(this.schmuck.getMainHandStack()) && this.isContainer(this.targetPos);
	}

	/**
	 * See if an item is worth storing.
	 *
	 * @param itemStack ItemStack to test
	 * @return If the item should be stored
	 */
	public boolean isStorable(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (itemStack.isEmpty()) { // we dont need to go anywhere
			return false;
		}
		return !(item instanceof ToolItem || item instanceof ArmorItem || item instanceof RangedWeaponItem);
	}

	/**
	 * See if an item is worth storing.
	 *
	 * @param itemStack ItemStack to test
	 * @return If the item should be stored
	 */
	public boolean isSapling(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (itemStack.isEmpty()) {
			return false;
		}
		return item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof SaplingBlock;
	}

	/**
	 * See if an item is worth planting.
	 *
	 * @param itemStack ItemStack to test
	 * @return If the item should be planted
	 */
	public boolean isPlantable(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (itemStack.isEmpty()) {
			return false;
		}
		return Schmucks.PLANTABLE_TAG.contains(item);
	}

	@Override
	public double getDesiredSquaredDistanceToTarget() {
		return 1.5D;
	}

	@Override
	protected boolean isTargetPos(WorldView world, BlockPos pos) {
		if (world.isAir(pos.up())) { // TODO: barrels don't need this, but its still checked
			BlockState blockState = world.getBlockState(pos);
			if (this.isSapling(this.schmuck.getMainHandStack()) && canPlantSapling(pos)) {
				return true;
			} else if (blockState.isOf(Blocks.FARMLAND) && this.isPlantable(this.schmuck.getMainHandStack())) {
				return true;
			} else if (this.isContainer(pos) && this.isNotFurnace(blockState)) {
				return isValidContainer(world, pos);
			}
		}
		return false;
	}

	@Override
	protected BlockPos getTargetPos() {
		return SchmuckUseToolGoal.getStandablePosition(this.schmuck, this.targetPos);
	}

	@Override
	public void stop() {
		super.stop();
		this.placed = false;
	}

	/**
	 * Sees if a sapling is placeable at a given position.
	 *
	 * @param pos Position to test
	 * @return If a sapling can be placed at <code>pos</code>
	 */
	private boolean canPlantSapling(BlockPos pos) {
		World world = this.schmuck.world;
		return this.schmuck.getWhitelist().contains(pos) &&
				world.isAir(pos) &&
				Blocks.OAK_SAPLING.getDefaultState().canPlaceAt(world, pos);
	}

	/**
	 * Puts the current held item onto farmland.
	 */
	private void putFarmland() {
		BlockState blockState = this.schmuck.world.getBlockState(this.targetPos);
		BlockState blockStateUp = this.schmuck.world.getBlockState(this.targetPos.up());
		if (blockState.isOf(Blocks.FARMLAND) && blockStateUp.isAir() && !placed) {
			ItemStack mainHandStack = this.schmuck.getMainHandStack();
			Item item = mainHandStack.getItem();
			if (item instanceof BlockItem) {
				ItemPlacementContext context = new AutomaticItemPlacementContext(this.schmuck.world,
						this.targetPos.up(),
						Direction.UP,
						mainHandStack,
						Direction.UP);
				((BlockItem) item).place(context);
			} else if (Blocks.WHEAT.getDefaultState().canPlaceAt(this.schmuck.world, this.targetPos.up())) {
				this.schmuck.world.setBlockState(this.targetPos.up(),
						Blocks.WHEAT.getDefaultState());
				mainHandStack.decrement(1);
				this.schmuck.equipNoUpdate(EquipmentSlot.MAINHAND, mainHandStack);
			}
			placed = true;
		}
	}

	private void putSapling() {
		if (!placed && canPlantSapling(this.targetPos)) {
			ItemStack mainHandStack = this.schmuck.getMainHandStack();
			Item item = mainHandStack.getItem();
			if (item instanceof BlockItem) {
				ItemPlacementContext context = new AutomaticItemPlacementContext(this.schmuck.world,
						this.targetPos,
						Direction.UP,
						mainHandStack,
						Direction.UP);
				((BlockItem) item).place(context);
			}
			placed = true;
		}
	}

	/**
	 * Puts the currently held item inside the targeted chest.
	 */
	private void putChest() {
		BlockState blockState = this.schmuck.world.getBlockState(this.targetPos);
		if (this.schmuck.world.getBlockEntity(this.targetPos) instanceof Inventory && this.isNotFurnace(blockState)) {
			Inventory blockEntity = HopperBlockEntity.getInventoryAt(this.schmuck.world, this.targetPos);
			if (blockEntity != null) {
				ItemStack mainHandStack = this.schmuck.getMainHandStack();
				HopperBlockEntity.transfer(null, blockEntity, mainHandStack, Direction.UP);
				this.schmuck.equipNoUpdate(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
				this.schmuck.playSound(SoundEvents.BLOCK_CHEST_CLOSE, 0.5f, 1);
			}
		}
	}

	/**
	 * Used to blacklist furnaces from being storage mediums.
	 *
	 * @param state BlockState to check
	 * @return If the given BlockState is not a furnace.
	 */
	private boolean isNotFurnace(BlockState state) {
		return !state.isIn(Schmucks.ORE_SMELTERS_TAG) && !state.isIn(Schmucks.FOOD_SMELTERS_TAG);
	}

	/**
	 * Used to see if a position is a container
	 *
	 * @param pos Position to test
	 * @return If pos contains a container
	 */
	private boolean isContainer(BlockPos pos) {
		BlockEntity blockEntity = this.schmuck.world.getBlockEntity(pos);
		return blockEntity instanceof Inventory;
	}

	/**
	 * Used to see if a container is valid for use
	 *
	 * @return If container is whitelisted and has an empty slot
	 */
	private boolean isValidContainer(WorldView world, BlockPos pos) {
		Inventory blockEntity = (Inventory) world.getBlockEntity(pos);
		if (blockEntity != null && this.schmuck.getWhitelist().contains(pos)) {
			int empty = -1;
			ItemStack mainHandStack = this.schmuck.getMainHandStack();
			for (int i = 0; i < 27; i++) {
				ItemStack stack = blockEntity.getStack(i);
				if (stack.isEmpty() || mainHandStack.isItemEqual(stack)) {
					empty = i;
					break;
				}
			}
			return empty != -1;
		}
		return false;
	}
}
