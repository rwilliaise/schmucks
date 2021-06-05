package com.alotofletters.schmucks.client;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.client.render.entity.SchmuckEntityRenderer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;

@Environment(EnvType.CLIENT)
public class SchmucksClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		EntityRendererRegistry.INSTANCE.register(Schmucks.SCHMUCK, (dispatcher, context) -> new SchmuckEntityRenderer(dispatcher, false));
	}
}
