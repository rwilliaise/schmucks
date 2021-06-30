package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.FleeEntityGoal;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Used for fleeing the player (albeit a small radius) so space is given for the player to roam around.
 */
public class SchmuckFleeGoal<T extends LivingEntity> extends FleeEntityGoal<T> {

	public SchmuckFleeGoal(SchmuckEntity schmuck, Class<T> fleeFromType) {
		super(schmuck, fleeFromType, 2, 1.0D, 1.0D, (entity) -> !(entity instanceof PlayerEntity));
	}
}
