package com.alotofletters.schmucks.client;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.client.gui.screen.ingame.ControlWandScreen;
import com.alotofletters.schmucks.client.render.ControlWandWhitelistRenderer;
import com.alotofletters.schmucks.client.render.entity.SchmuckEntityRenderer;
import com.alotofletters.schmucks.entity.specialization.SpecializationsComponent;
import com.alotofletters.schmucks.screen.SchmuckScreenHandler;
import com.alotofletters.schmucks.specialization.client.SpecializationIcon;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.minecraft.client.MinecraftClient;

@Environment(EnvType.CLIENT)
public class SchmucksClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		SpecializationIcon.register();

		EntityRendererRegistry.INSTANCE.register(Schmucks.SCHMUCK, (context) -> new SchmuckEntityRenderer(context, false));
		WorldRenderEvents.BEFORE_BLOCK_OUTLINE.register(ControlWandWhitelistRenderer::onBlockOutline);

		ScreenRegistry.<SchmuckScreenHandler, ControlWandScreen>register(Schmucks.SCHMUCK_SCREEN_HANDLER, ControlWandScreen::new);
	}

	public static SpecializationsComponent getPlayerComponent() {
		return Schmucks.SPECIALIZATIONS.get(MinecraftClient.getInstance().player);
	}
}
