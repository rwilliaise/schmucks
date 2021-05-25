package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.config.SchmucksConfig;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;

public class SchmuckSmeltGoal extends MoveToTargetPosGoal {
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
		Item item = this.schmuck.getMainHandStack().getItem();
		return schmuck.isTamed() && this.isSmeltable(item) && super.canStart();
	}

	public boolean isSmeltable(Item item) {
		return Schmucks.RAW_MINERAL_TAG.contains(item) || Schmucks.RAW_MEAT_TAG.contains(item);
	}

	public boolean isGoodSmelter(BlockState state) {
		Item item = this.schmuck.getMainHandStack().getItem();
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
				ItemStack itemStack = this.schmuck.getMainHandStack();
				HopperBlockEntity.transfer(null, blockEntity, itemStack, Direction.UP);
				this.schmuck.equipNoUpdate(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
			}
		}
	}

	@Override
	public void start() {
		this.timer = 0;
		super.start();
	}

	@Override
	public double getDesiredSquaredDistanceToTarget() {
		return 2.0D;
	}

	@Override
	protected boolean isTargetPos(WorldView world, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos);
		if (this.isGoodSmelter(blockState)) {
			AbstractFurnaceBlockEntity blockEntity = (AbstractFurnaceBlockEntity) world.getBlockEntity(pos);
			if (blockEntity != null) {
				ItemStack currentStack = blockEntity.getStack(0);
				return (currentStack.isEmpty() || currentStack.isItemEqual(this.schmuck.getMainHandStack()));
			}
		}
		return false;
	}
}
