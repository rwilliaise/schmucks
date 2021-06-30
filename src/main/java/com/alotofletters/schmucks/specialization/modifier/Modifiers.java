package com.alotofletters.schmucks.specialization.modifier;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.entity.ai.DriftlessMiningGoal;
import com.alotofletters.schmucks.specialization.modifier.general.HealthModifier;
import com.alotofletters.schmucks.specialization.modifier.general.SpeedModifier;
import net.minecraft.util.registry.Registry;

public class Modifiers {
	public static Modifier SPEED;
	public static Modifier HEALTH;
	public static Modifier MOLLIFY;
	public static Modifier DRIFTLESS_MINING;

	public static final Modifier EMPTY = register("empty", new Modifier() {
		public void apply(SchmuckEntity entity, int level) { }
	});

	public static void register() {
		SPEED = register("speed", new SpeedModifier());
		HEALTH = register("health", new HealthModifier());
		MOLLIFY = register("mollify", new GenericModifier());
		DRIFTLESS_MINING = register("driftless_mining", new GenericModifier());
	}

	private static Modifier register(String id, Modifier entry) {
		return Registry.register(Modifier.REGISTRY, Schmucks.id(id), entry);
	}
}
