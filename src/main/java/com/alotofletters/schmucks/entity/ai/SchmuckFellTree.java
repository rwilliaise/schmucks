package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.google.common.collect.Lists;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

import java.util.ArrayList;
import java.util.List;

public class SchmuckFellTree extends SchmuckUseToolGoal {
	private List<BlockPos> cascadingPos;
	private boolean cascading;

	public SchmuckFellTree(SchmuckEntity schmuck, double speed, int maxProgress) {
		super(schmuck, speed, maxProgress);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.cascading) {
			this.cascade();
			return;
		}
		if (this.hasReached() && this.use()) {
			this.schmuck.world.breakBlock(this.targetPos, true);
			this.cascading = true;
			this.cascadingPos = Lists.newArrayList(this.targetPos);
		}
		this.mob.getLookControl().lookAt(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ());
	}

	@Override
	public boolean shouldContinue() {
		return !this.canStop() || super.shouldContinue();
	}

	@Override
	public boolean canStop() {
		return !this.cascading;
	}

	@Override
	public void start() {
		super.start();
		this.cascading = false;
		this.cascadingPos = null;
	}

	@Override
	public double getDesiredSquaredDistanceToTarget() {
		return 2.0D;
	}

	@Override
	public boolean canStart() {
		ItemStack stack = this.schmuck.getMainHandStack();
		return FabricToolTags.AXES.contains(stack.getItem()) && super.canStart();
	}

	@Override
	protected boolean isTargetPos(WorldView world, BlockPos pos) {
		return isValidLog(world.getBlockState(pos)) && this.schmuck.getWhitelist().contains(pos);
	}

	@Override
	protected BlockPos getTargetPos() {
		return getStandablePosition(this.schmuck, this.targetPos);
	}

	/**
	 * Used to see if a state is a valid log.
	 *
	 * @param state State to test
	 * @return If the state is a valid log for cutting.
	 */
	private boolean isValidLog(BlockState state) {
		return state.isIn(BlockTags.LOGS);
	}

	private synchronized void addNeighbors(List<BlockPos> cascadingOut, World world, BlockPos pos) {
		for (Direction direction : Direction.values()) {
			BlockPos newPosition = pos.offset(direction);
			if (isValidLog(world.getBlockState(newPosition))) {
				cascadingOut.add(newPosition);
			}
		}
	}

	private void cascade() {
		List<BlockPos> cascadingOut = new ArrayList<>(this.cascadingPos);
		for (BlockPos pos : this.cascadingPos) {
			cascadingOut.remove(pos);
			this.schmuck.world.breakBlock(pos, true, this.schmuck);
			this.addNeighbors(cascadingOut, this.schmuck.world, pos);
		}
		this.cascadingPos = cascadingOut;
		if (this.cascadingPos.size() == 0) {
			this.cascading = false;
		}
	}
}
