package com.alotofletters.schmucks.specialization.modifier.hunter;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.specialization.modifier.Modifier;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;

public class DamageModifier extends Modifier {
	public DamageModifier() {
		this.addModifier(EntityAttributes.GENERIC_ATTACK_DAMAGE, 0.1, EntityAttributeModifier.Operation.MULTIPLY_TOTAL);
	}

	@Override
	public void apply(SchmuckEntity entity, int level) {
		this.applyModifiers(entity, level);
	}

	@Override
	public void cleanup(SchmuckEntity entity) {
		this.removeModifiers(entity);
	}

}
