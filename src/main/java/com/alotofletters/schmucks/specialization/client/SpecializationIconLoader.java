package com.alotofletters.schmucks.specialization.client;

import com.alotofletters.schmucks.Schmucks;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.texture.SpriteAtlasHolder;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class SpecializationIconLoader extends SpriteAtlasHolder {
	public SpecializationIconLoader(TextureManager textureManager) {
		super(textureManager, Schmucks.id("textures/atlas/specialization.png"), "specialization");
	}

	@Override
	protected Stream<Identifier> getSprites() {
		return SpecializationIcon.REGISTRY.getEntries().stream().map(icon -> icon.getValue().location());
	}
}
