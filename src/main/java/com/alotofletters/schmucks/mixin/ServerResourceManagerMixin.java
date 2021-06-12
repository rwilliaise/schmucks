package com.alotofletters.schmucks.mixin;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.access.SpecializationLoaderHolder;
import com.alotofletters.schmucks.specialization.SpecializationLoader;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.util.registry.DynamicRegistryManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerResourceManager.class)
public class ServerResourceManagerMixin implements SpecializationLoaderHolder {

	@Shadow
	@Final
	private ReloadableResourceManager resourceManager;

	private SpecializationLoader loader;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void init(DynamicRegistryManager registryManager, CommandManager.RegistrationEnvironment commandEnvironment, int functionPermissionLevel, CallbackInfo ci) {
		this.loader = new SpecializationLoader();
		this.resourceManager.registerReloader(this.loader);
	}

	@Override
	public SpecializationLoader getSpecializationHolder() {
		return this.loader;
	}
}
