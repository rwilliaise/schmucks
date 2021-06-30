package com.alotofletters.schmucks.specialization.modifier;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.entity.player.PlayerEntity;

/**
 * Used primarily for AI affecting modifiers - AI goals that require a modifier to exist and don't apply to schmucks
 * specifically.
 */
public class GenericModifier extends Modifier {
	@Override
	public void apply(SchmuckEntity entity, int level) {
		// NOOP
	}

	@Override
	public void applyAll(PlayerEntity player, int level) {
		// NOOP
	}
}
