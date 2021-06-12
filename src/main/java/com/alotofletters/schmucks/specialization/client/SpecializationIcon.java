package com.alotofletters.schmucks.specialization.client;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.mixin.RegistryAccessor;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import static com.alotofletters.schmucks.Schmucks.id;

public record SpecializationIcon(Identifier location) {
	public static final RegistryKey<Registry<SpecializationIcon>> ICON_KEY = RegistryKey.ofRegistry(id("icon"));
	public static final Registry<SpecializationIcon> REGISTRY = RegistryAccessor.callCreate(ICON_KEY, () -> SpecializationIcon.EMPTY);
	public static final SpecializationIcon EMPTY = register("empty", TextureManager.MISSING_IDENTIFIER);
	public static final SpecializationIcon CONTROLLER = register("controller", Schmucks.id("controller"));

	private static SpecializationIcon register(String name, Identifier location) {
		return Registry.register(REGISTRY, Schmucks.id(name), new SpecializationIcon(location));
	}

}
