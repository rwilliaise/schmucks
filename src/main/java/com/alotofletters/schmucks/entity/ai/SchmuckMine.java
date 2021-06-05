package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.config.SchmucksConfig;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class SchmuckMine extends MoveToTargetPosGoal {
	private final SchmuckEntity schmuck;
	private final int maxProgress;
	private int breakProgress;
	private int lastProgress;

	public SchmuckMine(SchmuckEntity schmuck, double speed, int maxProgress) {
		super(schmuck, speed, Schmucks.CONFIG.jobRange);
		this.schmuck = schmuck;
		this.maxProgress = maxProgress;
	}

	@Override
	public boolean canStart() {
		if (!super.canStart()) {
			return false;
		} else {
			return this.schmuck.getMainHandStack().getItem() instanceof PickaxeItem && !this.schmuck.isSitting();
		}
	}

	@Override
	protected int getInterval(PathAwareEntity mob) {
		SchmucksConfig config = Schmucks.CONFIG;
		int min = config.toolInterval.min;
		if (min < config.toolInterval.max) {
			min += mob.getRandom().nextInt(config.toolInterval.max - min);
		}
		return min;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.hasReached()) {
			this.breakProgress++;
			int newProgress = (int) Math.floor((float) this.breakProgress / this.getMaxProgress() * 10.0F);
			if (this.lastProgress != newProgress) {
				this.schmuck.swingHand(Hand.MAIN_HAND);
				this.lastProgress = newProgress;
			}
			this.schmuck.world.setBlockBreakingInfo(this.schmuck.getEntityId(), this.targetPos, newProgress);
			if (this.breakProgress >= this.getMaxProgress()) {
				this.schmuck.world.breakBlock(this.targetPos, true);
			}
		}

		this.mob.getLookControl().lookAt(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ());
	}

	public boolean shouldContinue() {
		return super.shouldContinue() && !this.schmuck.isSitting() && this.isOrePresent();
	}

	@Override
	protected BlockPos getTargetPos() {
		BlockPos pos = this.targetPos;
		if (isStandable(pos.up())) {
			return pos.up();
		} else if (isStandable(pos.down())) {
			return pos.down();
		} else if (isStandable(pos.north())) {
			return pos.north();
		} else if (isStandable(pos.south())) {
			return pos.south();
		} else if (isStandable(pos.west())) {
			return pos.west();
		} else {
			return pos.east();
		}
	}

	private boolean isStandable(BlockPos pos) {
		World world = this.schmuck.world;
		return world.isAir(pos) && world.getBlockState(pos.down()).hasSolidTopSurface(world, pos, this.schmuck);
	}

	public boolean isOrePresent() {
		return !this.schmuck.world.getBlockState(this.targetPos).isAir();
	}

	@Override
	public double getDesiredSquaredDistanceToTarget() {
		return 2.0D;
	}

	@Override
	public void start() {
		super.start();
		breakProgress = 0;
	}

	public boolean isExposed(WorldView world, BlockPos pos) {
		return world.isAir(pos.up()) ||
				world.isAir(pos.down()) ||
				world.isAir(pos.north()) ||
				world.isAir(pos.south()) ||
				world.isAir(pos.west()) ||
				world.isAir(pos.east());
	}

	@Override
	protected boolean isTargetPos(WorldView world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		ItemStack pickaxe = this.schmuck.getMainHandStack();
		return this.isExposed(world, pos) && state.getBlock() instanceof OreBlock && pickaxe.isEffectiveOn(state);
	}

	public int getMaxProgress() {
		return maxProgress;
	}
}
