package com.alotofletters.schmucks.mixin;

import com.alotofletters.schmucks.access.SpecializationIconLoaderHolder;
import com.alotofletters.schmucks.specialization.client.SpecializationIconLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin implements SpecializationIconLoaderHolder {

	@Shadow
	@Final
	private ReloadableResourceManager resourceManager;
	@Shadow
	@Final
	private TextureManager textureManager;
	private SpecializationIconLoader specializationIconLoader;

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/particle/ParticleManager;<init>(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/client/texture/TextureManager;)V"))
	public void init(RunArgs args, CallbackInfo ci) {
//		this.specializationIconLoader = new SpecializationIconLoader(this.textureManager);
//		this.resourceManager.registerReloader(this.specializationIconLoader);
	}

	@Override
	public SpecializationIconLoader getSpecializationIconHolder() {
		return this.specializationIconLoader;
	}
}
