package com.alotofletters.schmucks.specialization.client;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.mixin.RegistryAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import static com.alotofletters.schmucks.Schmucks.id;

public record SpecializationIcon(Identifier location) {
	public static final RegistryKey<Registry<SpecializationIcon>> ICON_KEY = RegistryKey.ofRegistry(id("icon"));
	public static final Registry<SpecializationIcon> REGISTRY = RegistryAccessor.callCreate(ICON_KEY, () -> SpecializationIcon.MISSING);

	public static SpecializationIcon MISSING;

	// subtabs
	public static SpecializationIcon GENERAL;
	public static SpecializationIcon GATHERER;
	public static SpecializationIcon HUNTER;

	// tabs
	public static SpecializationIcon DASHBOARD;
	public static SpecializationIcon SPECIALIZATION;

	// specializations
	public static SpecializationIcon ADVENTURER;
	public static SpecializationIcon CLEAVE;
	public static SpecializationIcon CONSTITUTION;
	public static SpecializationIcon CULTIVATION;
	public static SpecializationIcon DRIFTLESS_MINING;
	public static SpecializationIcon FIRST_AID;
	public static SpecializationIcon FULL_HARVEST;
	public static SpecializationIcon MOLLIFY;
	public static SpecializationIcon NURTURE;
	public static SpecializationIcon OVERLOADED;
	public static SpecializationIcon SIXTH_SENSE;
	public static SpecializationIcon SNUG_BOOTS;
	public static SpecializationIcon SPECTRAL_MENDING;
	public static SpecializationIcon PROTECTOR;
	public static SpecializationIcon RECLAMATION;
	public static SpecializationIcon THRIFTY;
	public static SpecializationIcon WHETTED_ARMS;
	public static SpecializationIcon ZWEIHANDER;
	public static SpecializationIcon ADVANCED_AXEMANSHIP;

	public static void register() {
		MISSING = register("missing");
		GENERAL = register("general");
		GATHERER = register("gatherer");
		HUNTER = register("hunter");
		DASHBOARD = register("dashboard");
		SPECIALIZATION = register("specialization");
		ADVENTURER = register("adventurer");
		CLEAVE = register("cleave");
		CONSTITUTION = register("constitution");
		CULTIVATION = register("cultivation");
		DRIFTLESS_MINING = register("driftless_mining");
		FIRST_AID = register("first_aid");
		FULL_HARVEST = register("full_harvest");
		MOLLIFY = register("mollify");
		NURTURE = register("nurture");
		OVERLOADED = register("overloaded");
		SIXTH_SENSE = register("sixth_sense");
		SNUG_BOOTS = register("snug_boots");
		SPECTRAL_MENDING = register("spectral_mending");
		PROTECTOR = register("protector");
		RECLAMATION = register("reclamation");
		THRIFTY = register("thrifty");
		WHETTED_ARMS = register("whetted_arms");
		ZWEIHANDER = register("zweihander");
		ADVANCED_AXEMANSHIP = register("advanced_axemanship");
	}

	private static SpecializationIcon register(String name) {
		return Registry.register(REGISTRY, Schmucks.id(name), new SpecializationIcon(Schmucks.id(name)));
	}
}
