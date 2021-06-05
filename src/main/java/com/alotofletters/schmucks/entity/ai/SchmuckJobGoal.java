package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.config.SchmucksConfig;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.mob.PathAwareEntity;

/**
 * Used as a base class for all jobs.
 */
public abstract class SchmuckJobGoal extends MoveToTargetPosGoal {
	protected final SchmuckEntity schmuck;

	public SchmuckJobGoal(SchmuckEntity schmuck, double speed) {
		this(schmuck, speed, Schmucks.CONFIG.jobRange);
	}

	public SchmuckJobGoal(SchmuckEntity schmuck, double speed, int range) {
		this(schmuck, speed, range, 1);
	}

	public SchmuckJobGoal(SchmuckEntity schmuck, double speed, int range, int heightDifference) {
		super(schmuck, speed, range, heightDifference);
		this.schmuck = schmuck;
	}

	@Override
	protected int getInterval(PathAwareEntity mob) {
		SchmucksConfig config = AutoConfig.getConfigHolder(SchmucksConfig.class).getConfig();
		int min = config.jobInterval.min;
		if (min < config.jobInterval.max) {
			min += mob.getRandom().nextInt(config.jobInterval.max - min);
		}
		return min;
	}

	@Override
	public boolean canStart() {
		return !this.schmuck.isSitting() && super.canStart();
	}
}
