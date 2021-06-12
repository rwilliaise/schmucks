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
	public static final Registry<SpecializationIcon> REGISTRY = RegistryAccessor.callCreate(ICON_KEY, () -> SpecializationIcon.MISSING);

	public static final SpecializationIcon MISSING = register("missing");
	public static final SpecializationIcon GENERAL = register("general");
	public static final SpecializationIcon GATHERER = register("gatherer");
	public static final SpecializationIcon HUNTER = register("hunter");
	public static final SpecializationIcon DASHBOARD = register("dashboard");
	public static final SpecializationIcon SPECIALIZATION = register("specialization");

	private static SpecializationIcon register(String name) {
		return Registry.register(REGISTRY, Schmucks.id(name), new SpecializationIcon(Schmucks.id(name)));
	}
}
