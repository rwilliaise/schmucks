package com.alotofletters.schmucks.mixin;

import com.alotofletters.schmucks.client.render.entity.SchmuckEntityRenderer;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ClientWorld.class)
public class ClientWorldMixin {

	/**
	 * Forcefully clears the cache when reloading a world; fixing a few bugs.
	 * This may not be a great way to do it, but whatever haha
	 */
	@Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/client/network/ClientPlayNetworkHandler;Lnet/minecraft/client/world/ClientWorld$Properties;Lnet/minecraft/util/registry/RegistryKey;Lnet/minecraft/world/dimension/DimensionType;ILjava/util/function/Supplier;Lnet/minecraft/client/render/WorldRenderer;ZJ)V")
	public void init(ClientPlayNetworkHandler networkHandler,
	                 ClientWorld.Properties properties,
	                 RegistryKey<World> registryRef,
	                 DimensionType dimensionType,
	                 int loadDistance,
	                 Supplier<Profiler> profiler,
	                 WorldRenderer worldRenderer,
	                 boolean debugWorld,
	                 long seed,
	                 CallbackInfo ci) {
		SchmuckEntityRenderer.CACHED_TEXTURES.clear();
	}
}
