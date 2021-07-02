package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.config.SchmucksConfig;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;

public class SchmuckSmeltGoal extends SchmuckJobGoal {
	private final SchmuckEntity schmuck;
	private int timer;

	public SchmuckSmeltGoal(SchmuckEntity schmuck, double speed) {
		super(schmuck, speed, AutoConfig.getConfigHolder(SchmucksConfig.class).getConfig().jobRange, 2);
		this.schmuck = schmuck;
	}

	@Override
	public boolean canStart() {
		ItemStack itemStack = this.schmuck.getMainHandStack();
		if (itemStack.isEmpty()) {
			return false;
		}
		return this.schmuck.isTamed() && this.hasUsable() && super.canStart();
	}

	public static boolean canUse(ItemStack stack) {
		return AbstractFurnaceBlockEntity.canUseAsFuel(stack)
				|| stack.isIn(Schmucks.RAW_MINERAL_TAG)
				|| stack.isIn(Schmucks.RAW_MEAT_TAG);
	}

	public boolean hasUsable() {
		return this.hasItem(SchmuckSmeltGoal::canUse);
	}

	public ItemStack getUsable() {
		return this.getItem(SchmuckSmeltGoal::canUse);
	}

	public boolean isGoodSmelter(BlockState state) {
		ItemStack itemStack = this.getUsable();
		Item item = itemStack.getItem();
		if (state.getBlock() instanceof AbstractFurnaceBlock && AbstractFurnaceBlockEntity.canUseAsFuel(itemStack)) {
			return true;
		}
		if (!state.isIn(Schmucks.ORE_SMELTERS_TAG) && !state.isIn(Schmucks.FOOD_SMELTERS_TAG)) {
			return state.getBlock() instanceof AbstractFurnaceBlock;
		}
		if (Schmucks.RAW_MINERAL_TAG.contains(item) && state.isIn(Schmucks.ORE_SMELTERS_TAG)) {
			return true;
		}
		return Schmucks.RAW_MEAT_TAG.contains(item) && state.isIn(Schmucks.FOOD_SMELTERS_TAG);
	}

	@Override
	public void tick() {
		if (this.hasReached()) {
			if (this.timer >= 10) {
				this.putIntoFurnace();
			} else {
				this.timer++;
			}
		}

		super.tick();
	}

	private void putIntoFurnace() {
		BlockState blockState = this.schmuck.world.getBlockState(this.targetPos);
		if (this.isGoodSmelter(blockState)) {
			AbstractFurnaceBlockEntity blockEntity = (AbstractFurnaceBlockEntity) this.schmuck.world.getBlockEntity(this.targetPos);
			if (blockEntity != null) {
				ItemStack itemStack = this.getUsable();
				HopperBlockEntity.transfer(null, blockEntity, itemStack, AbstractFurnaceBlockEntity.canUseAsFuel(itemStack) ? Direction.WEST : Direction.UP);
			}
		}
	}

	@Override
	public void start() {
		this.timer = 0;
		super.start();
	}

	@Override
	protected BlockPos getTargetPos() {
		return SchmuckUseToolGoal.getStandablePosition(this.schmuck, this.targetPos);
	}

	@Override
	public double getDesiredSquaredDistanceToTarget() {
		return 2.0D;
	}

	public boolean hasFuel() {
		return this.hasItem(AbstractFurnaceBlockEntity::canUseAsFuel);
	}

	@Override
	protected boolean isTargetPos(WorldView world, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos);
		if (this.isGoodSmelter(blockState) && schmuck.getWhitelist().contains(pos)) {
			AbstractFurnaceBlockEntity blockEntity = (AbstractFurnaceBlockEntity) world.getBlockEntity(pos);
			if (blockEntity != null) {
				ItemStack mainHandStack = this.getUsable();
				ItemStack currentStack = blockEntity.getStack(AbstractFurnaceBlockEntity.canUseAsFuel(mainHandStack) ? 1 : 0);
				return (currentStack.isEmpty() || currentStack.isItemEqual(mainHandStack));
			}
		}
		return false;
	}
}
