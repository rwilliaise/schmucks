package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class DriftlessMiningGoal extends SchmuckUseToolGoal {

	public DriftlessMiningGoal(SchmuckEntity schmuck, double speed, int maxProgress) {
		super(schmuck, speed, maxProgress);
	}

	@Override
	protected boolean isTargetPos(WorldView world, BlockPos pos) {
		return false;
	}
}
