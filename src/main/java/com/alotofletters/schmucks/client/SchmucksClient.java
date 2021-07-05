package com.alotofletters.schmucks.client;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.client.render.ControlWandWhitelistRenderer;
import com.alotofletters.schmucks.client.render.entity.SchmuckEntityRenderer;
import com.alotofletters.schmucks.entity.specialization.SpecializationsComponent;
import com.alotofletters.schmucks.specialization.client.SpecializationIcon;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class SchmucksClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		SpecializationIcon.register();

		EntityRendererRegistry.INSTANCE.register(Schmucks.SCHMUCK, (context) -> new SchmuckEntityRenderer(context, false));
		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(ControlWandWhitelistRenderer::onBlockOutline);
	}

	public static SpecializationsComponent getPlayerComponent() {
		return Schmucks.SPECIALIZATIONS.get(MinecraftClient.getInstance().player);
	}
}
