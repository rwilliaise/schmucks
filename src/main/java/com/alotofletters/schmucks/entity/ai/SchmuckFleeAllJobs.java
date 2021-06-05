package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class SchmuckFleeAllJobs extends FleeBlockGoal {
	private final SchmuckEntity schmuck;

	public SchmuckFleeAllJobs(SchmuckEntity mob, double speed) {
		super(mob, speed, 4, 2);
		this.schmuck = mob;
	}

	@Override
	public boolean canStart() {
		return !schmuck.isSitting() && super.canStart();
	}

	@Override
	protected boolean isTargetPos(WorldView world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		return state.isIn(Schmucks.JOBS_TAG) || state.getBlock() instanceof OreBlock;
	}
}
