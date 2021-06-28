package com.alotofletters.schmucks.specialization.modifier;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.specialization.modifier.general.SpeedModifier;
import net.minecraft.util.registry.Registry;

public class Modifiers {
	public static final Modifier SPEED = register("speed", new SpeedModifier());
	public static final Modifier MOLLIFY = register("mollify", new GenericModifier());
	public static final Modifier EMPTY = register("empty", new Modifier() {
		public void apply(SchmuckEntity entity, int level) { }
	});

	private static Modifier register(String id, Modifier entry) {
		return Registry.register(Modifier.REGISTRY, Schmucks.id(id), entry);
	}
}
