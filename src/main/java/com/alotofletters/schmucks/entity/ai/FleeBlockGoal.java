package com.alotofletters.schmucks.entity.ai;

import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public abstract class FleeBlockGoal extends MoveToTargetPosGoal {
	private final int range;
	private final int maxYDifference;

	public FleeBlockGoal(PathAwareEntity mob, double speed, int range) {
		this(mob, speed, range, 1);
	}

	public FleeBlockGoal(PathAwareEntity mob, double speed, int range, int maxYDifference) {
		super(mob, speed, range, maxYDifference);
		this.range = range;
		this.maxYDifference = maxYDifference;
	}

	@Override
	protected int getInterval(PathAwareEntity mob) {
		return 160;
	}

	@Override
	protected BlockPos getTargetPos() {
		return this.targetPos;
	}

	protected boolean findTargetPos() {
		int range = this.range;
		BlockPos blockPos = this.mob.getBlockPos();
		BlockPos.Mutable mutable = new BlockPos.Mutable();

		for (int y = this.lowestY; y <= this.maxYDifference; y = y > 0 ? -y : 1 - y) {
			for (int r = 0; r < this.range; ++r) {
				for (int x = 0; x <= r; x = x > 0 ? -x : 1 - x) {
					for (int z = x < r && x > -r ? r : 0; z <= r; z = z > 0 ? -z : 1 - z) {
						mutable.set(blockPos, x, y - 1, z);
						if (this.mob.isInWalkTargetRange(mutable) && this.isTargetPos(this.mob.world, mutable)) {
							Vec3d position = new Vec3d(mutable.getX(), mutable.getY(), mutable.getZ());
							Vec3d target = NoPenaltyTargeting.find(this.mob, this.range, this.maxYDifference, position);
							if (target != null) {
								this.targetPos = new BlockPos(target.x, target.y, target.z);
								return true;
							}
							return false;
						}
					}
				}
			}
		}

		return false;
	}
}
