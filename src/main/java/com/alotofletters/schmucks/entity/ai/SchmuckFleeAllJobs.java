package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.Schmucks;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class SchmuckFleeAllJobs extends FleeBlockGoal {
	public SchmuckFleeAllJobs(PathAwareEntity mob, double speed) {
		super(mob, speed, 4, 2);
	}

	@Override
	protected boolean isTargetPos(WorldView world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		return state.isIn(Schmucks.JOBS_TAG) || state.getBlock() instanceof OreBlock;
	}
}
