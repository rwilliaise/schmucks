package com.alotofletters.schmucks.specialization.client;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.mixin.RegistryAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import static com.alotofletters.schmucks.Schmucks.id;

@Environment(EnvType.CLIENT)
public record SpecializationIcon(Identifier location) {
	public static final RegistryKey<Registry<SpecializationIcon>> ICON_KEY = RegistryKey.ofRegistry(id("icon"));
	public static final SpecializationIcon MISSING = register("missing");
	public static final Registry<SpecializationIcon> REGISTRY = RegistryAccessor.callCreate(ICON_KEY, () -> SpecializationIcon.MISSING);
	// subtabs
	public static final SpecializationIcon GENERAL = register("general");
	public static final SpecializationIcon GATHERER = register("gatherer");
	public static final SpecializationIcon HUNTER = register("hunter");

	// tabs
	public static final SpecializationIcon DASHBOARD = register("dashboard");
	public static final SpecializationIcon SPECIALIZATION = register("specialization");

	// specializations
	public static final SpecializationIcon ADVENTURER = register("adventurer");
	public static final SpecializationIcon CLEAVE = register("cleave");
	public static final SpecializationIcon CONSTITUTION = register("constitution");
	public static final SpecializationIcon CULTIVATION = register("cultivation");
	public static final SpecializationIcon DRIFTLESS_MINING = register("driftless_mining");
	public static final SpecializationIcon FIRST_AID = register("first_aid");
	public static final SpecializationIcon FULL_HARVEST = register("full_harvest");
	public static final SpecializationIcon MOLLIFY = register("mollify");
	public static final SpecializationIcon NURTURE = register("nurture");
	public static final SpecializationIcon OVERLOADED = register("overloaded");
	public static final SpecializationIcon SIXTH_SENSE = register("sixth_sense");
	public static final SpecializationIcon SNUG_BOOTS = register("snug_boots");
	public static final SpecializationIcon SPECTRAL_MENDING = register("spectral_mending");
	public static final SpecializationIcon PROTECTOR = register("protector");
	public static final SpecializationIcon RECLAMATION = register("reclamation");
	public static final SpecializationIcon THRIFTY = register("thrifty");
	public static final SpecializationIcon WHETTED_ARMS = register("whetted_arms");
	public static final SpecializationIcon ZWEIHANDER = register("zweihander");
	public static final SpecializationIcon ADVANCED_AXEMANSHIP = register("advanced_axemanship");

	private static SpecializationIcon register(String name) {
		return Registry.register(REGISTRY, Schmucks.id(name), new SpecializationIcon(Schmucks.id(name)));
	}
}
