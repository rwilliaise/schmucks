package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.config.SchmucksConfig;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.DoubleInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.*;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.WorldView;

public class SchmuckPutUnneeded extends MoveToTargetPosGoal {
	private SchmuckEntity schmuck;
	private int empty;

	public SchmuckPutUnneeded(SchmuckEntity mob, double speed) {
		super(mob, speed, AutoConfig.getConfigHolder(SchmucksConfig.class).getConfig().jobRange, 2);
		this.schmuck = mob;
	}

	@Override
	public boolean canStart() {
		return !this.schmuck.isSitting() && this.schmuck.isTamed() && this.isStorable(this.schmuck.getMainHandStack()) && super.canStart();
	}

	@Override
	public void tick() {
		if (this.hasReached() && this.isStorable(this.schmuck.getMainHandStack())) {
			BlockState blockState = this.schmuck.world.getBlockState(this.targetPos);
			if (blockState.isOf(Blocks.CHEST)) {
				Inventory blockEntity = HopperBlockEntity.getInventoryAt(this.schmuck.world, this.targetPos);
				if (blockEntity != null) {
					ItemStack mainHandStack = this.schmuck.getMainHandStack();
					HopperBlockEntity.transfer(null, blockEntity, mainHandStack, Direction.UP);
					this.schmuck.equipNoUpdate(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
					this.schmuck.playSound(SoundEvents.BLOCK_CHEST_CLOSE, 0.5f, 1);
				}
			}
		}
		super.tick();
	}

	@Override
	public boolean shouldContinue() {
		return this.isStorable(this.schmuck.getMainHandStack()) ;
	}

	/**
	 * See if an item is worth storing.
	 * @param itemStack ItemStack to test
	 * @return If the item should be stored
	 */
	public boolean isStorable(ItemStack itemStack) {
		Item item = itemStack.getItem();
		if (itemStack.isEmpty()) { // we dont need to go anywhere
			return false;
		}
		return !(item instanceof ToolItem || item instanceof ArmorItem || item instanceof BowItem);
	}

	@Override
	public double getDesiredSquaredDistanceToTarget() {
		return 2.0D;
	}

	@Override
	protected boolean isTargetPos(WorldView world, BlockPos pos) {
		if (world.isAir(pos.up())) {
			BlockState blockState = world.getBlockState(pos);
			if (blockState.isOf(Blocks.CHEST)) {
				ChestBlockEntity blockEntity = (ChestBlockEntity) world.getBlockEntity(pos);
				if (blockEntity != null) {
					this.empty = -1;
					ItemStack mainHandStack = this.schmuck.getMainHandStack();
					for (int i = 0; i < 27; i++) {
						ItemStack stack = blockEntity.getStack(i);
						if (stack.isEmpty() || mainHandStack.isItemEqual(stack)) {
							this.empty = i;
							break;
						}
					}
					return empty != -1;
				}
			}
		}
		return false;
	}
}
