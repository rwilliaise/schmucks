package com.alotofletters.schmucks.specialization.modifier;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.util.registry.Registry;

public class Modifiers {
	public static final Modifier MINING = register("mining", new MiningModifier());
	public static final Modifier EMPTY = register("empty", new Modifier() {
		public void apply(SchmuckEntity entity, int level) {
		}
	});

	private static Modifier register(String id, Modifier entry) {
		return Registry.register(Modifier.MODIFIER, Schmucks.id(id), entry);
	}
}
