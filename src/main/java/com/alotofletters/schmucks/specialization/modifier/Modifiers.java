package com.alotofletters.schmucks.specialization.modifier;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.specialization.modifier.general.HealthModifier;
import com.alotofletters.schmucks.specialization.modifier.general.SpeedModifier;
import com.alotofletters.schmucks.specialization.modifier.hunter.DamageModifier;
import net.minecraft.util.registry.Registry;

public class Modifiers {
	public static final Modifier EMPTY = register("empty", new Modifier() {
		public void apply(SchmuckEntity entity, int level) {
		}
	});
	public static Modifier SPEED;
	public static Modifier HEALTH;
	public static Modifier DAMAGE;
	public static Modifier MOLLIFY;
	public static Modifier FULL_HARVEST;
	public static Modifier NURTURE;
	public static Modifier EXPANDABLE_QUIVER;
	public static Modifier DRIFTLESS_MINING;
	public static Modifier THRIFTY;
	public static Modifier TREE_FELL;
	public static Modifier PROTECTOR;

	public static void register() {
		SPEED = register("speed", new SpeedModifier());
		HEALTH = register("health", new HealthModifier());
		DAMAGE = register("damage", new DamageModifier());
		MOLLIFY = register("mollify", new GenericModifier());
		NURTURE = register("nurture", new GenericModifier());
		EXPANDABLE_QUIVER = register("expandable_quiver", new GenericModifier());
		FULL_HARVEST = register("full_harvest", new GenericModifier());
		DRIFTLESS_MINING = register("driftless_mining", new GenericModifier());
		THRIFTY = register("thrifty", new GenericModifier());
		TREE_FELL = register("tree_fell", new GenericModifier());
		PROTECTOR = register("protector", new GenericModifier());
	}

	private static Modifier register(String id, Modifier entry) {
		return Registry.register(Modifier.REGISTRY, Schmucks.id(id), entry);
	}
}
