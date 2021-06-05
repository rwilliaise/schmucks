package com.alotofletters.schmucks.entity.ai;

import net.minecraft.entity.ai.TargetFinder;
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

		for(int k = this.lowestY; k <= this.maxYDifference; k = k > 0 ? -k : 1 - k) {
			for(int l = 0; l < this.range; ++l) {
				for(int m = 0; m <= l; m = m > 0 ? -m : 1 - m) {
					for(int n = m < l && m > -l ? l : 0; n <= l; n = n > 0 ? -n : 1 - n) {
						mutable.set(blockPos, m, k - 1, n);
						if (this.mob.isInWalkTargetRange(mutable) && this.isTargetPos(this.mob.world, mutable)) {
							Vec3d position = new Vec3d(mutable.getX(), mutable.getY(), mutable.getZ());
							Vec3d target = TargetFinder.findGroundTargetAwayFrom(this.mob, this.range, this.maxYDifference, position);
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
