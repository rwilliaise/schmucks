package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.specialization.modifier.Modifiers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;

public class SchmuckFleeCreeperGoal extends FleeEntityGoal<CreeperEntity> {

	private final SchmuckEntity schmuck;

	public SchmuckFleeCreeperGoal(SchmuckEntity schmuck) {
		super(schmuck, CreeperEntity.class, 4, 1.0D, 1.2D);
		this.schmuck = schmuck;
	}

	@Override
	public boolean canStart() {
		if (this.schmuck.hasModifier(Modifiers.MOLLIFY)) {
			return false;
		}
		return super.canStart();
	}
}
