package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.entity.ai.goal.FollowOwnerGoal;

public class SchmuckFollowOwner extends FollowOwnerGoal {
	private final SchmuckEntity schmuck;

	public SchmuckFollowOwner(SchmuckEntity tameable, double speed, float minDistance, float maxDistance, boolean leavesAllowed) {
		super(tameable, speed, minDistance, maxDistance, leavesAllowed);
		this.schmuck = tameable;
	}

	@Override
	public boolean canStart() {
		if (this.schmuck.getCanTeleport()) {
			return super.canStart();
		}
		return false;
	}
}
