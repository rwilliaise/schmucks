package com.alotofletters.schmucks.entity.ai;

import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.MobEntity;

public class SixthSenseGoal extends TrackTargetGoal {
	public SixthSenseGoal(MobEntity mob, boolean checkVisibility) {
		super(mob, checkVisibility);
	}

	@Override
	public boolean canStart() {
		return false;
	}
}
