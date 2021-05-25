package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class SchmuckMine extends MoveToTargetPosGoal {
	private final SchmuckEntity schmuck;
	private final int maxProgress;
	private int breakProgress;

	public SchmuckMine(SchmuckEntity schmuck, double speed, int maxProgress) {
		super(schmuck, speed, 8);
		this.schmuck = schmuck;
		this.maxProgress = maxProgress;
	}

	@Override
	public boolean canStart() {
		if (!super.canStart()) {
			return false;
		} else {
			return this.schmuck.getMainHandStack().getItem() instanceof PickaxeItem;
		}
	}

	@Override
	public void tick() {
		if (this.hasReached()) {
			if (this.schmuck.getRandom().nextInt(20) == 0) {
				this.schmuck.swingHand(Hand.MAIN_HAND);
			}
			breakProgress++;
			int newProgress = (int) Math.floor((float) this.breakProgress / this.getMaxProgress() * 10.0F);
			this.schmuck.world.setBlockBreakingInfo(this.schmuck.getEntityId(), this.targetPos, newProgress);
			if (this.breakProgress >= this.getMaxProgress()) {
				this.schmuck.world.breakBlock(this.targetPos, true);
			}
		}

		super.tick();
	}

	public boolean shouldContinue() {
		return this.isOrePresent();
	}

	public boolean isOrePresent() {
		return !this.schmuck.world.getBlockState(this.targetPos).isAir();
	}

	@Override
	public void start() {
		super.start();
		breakProgress = 0;
	}

	@Override
	protected boolean isTargetPos(WorldView world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		ItemStack pickaxe = this.schmuck.getMainHandStack();
		return state.getBlock() instanceof OreBlock && pickaxe.isEffectiveOn(state);
	}

	public int getMaxProgress() {
		return maxProgress;
	}
}
