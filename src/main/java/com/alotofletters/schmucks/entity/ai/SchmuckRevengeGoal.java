package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.specialization.modifier.Modifiers;
import net.minecraft.entity.ai.goal.RevengeGoal;

public class SchmuckRevengeGoal extends RevengeGoal {

	private final SchmuckEntity schmuck;

	public SchmuckRevengeGoal(SchmuckEntity mob, Class<?>... noRevengeTypes) {
		super(mob, noRevengeTypes);
		this.schmuck = mob;
	}

	@Override
	public boolean canStart() {
		if (this.schmuck.hasModifier(Modifiers.PROTECTOR)) {
			this.setGroupRevenge();
		}
		return super.canStart();
	}
}
