package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.SchmuckEntity;
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

public class SchmuckSmeltFood extends MoveToTargetPosGoal {
	private final SchmuckEntity schmuck;
	private int timer;

	public SchmuckSmeltFood(SchmuckEntity schmuck, double speed) {
		super(schmuck, speed, 8);
		this.schmuck = schmuck;
	}

	@Override
	public boolean canStart() {
		ItemStack itemStack = this.schmuck.getMainHandStack();
		if (itemStack.isEmpty()) {
			return false;
		}
		Item item = this.schmuck.getMainHandStack().getItem();
		return schmuck.isTamed() && Schmucks.RAW_MEAT_TAG.contains(item) && super.canStart();
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
		if (blockState.isIn(Schmucks.FOOD_SMELTERS_TAG)) {
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
	protected boolean isTargetPos(WorldView world, BlockPos pos) {
		BlockState blockState = world.getBlockState(pos);
		if (blockState.isIn(Schmucks.FOOD_SMELTERS_TAG)) {
			AbstractFurnaceBlockEntity blockEntity = (AbstractFurnaceBlockEntity) world.getBlockEntity(pos);
			if (blockEntity != null) {
				ItemStack currentStack = blockEntity.getStack(0);
				return (currentStack.isEmpty() || currentStack.isItemEqual(this.schmuck.getMainHandStack()));
			}
		}
		return false;
	}
}
