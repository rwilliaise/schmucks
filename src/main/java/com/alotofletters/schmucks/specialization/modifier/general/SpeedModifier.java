package com.alotofletters.schmucks.specialization.modifier.general;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.specialization.modifier.Modifier;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;

public class SpeedModifier extends Modifier {
	public SpeedModifier() {
		this.addModifier(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.1, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
	}

	@Override
	public void apply(SchmuckEntity entity, int level) {
		System.out.println("SpeedModifier.apply");
		this.applyModifiers(entity, level);
	}

	@Override
	public void cleanup(SchmuckEntity entity) {
		this.removeModifiers(entity);
	}
}
