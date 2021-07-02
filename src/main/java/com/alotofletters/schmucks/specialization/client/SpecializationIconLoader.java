package com.alotofletters.schmucks.specialization.client;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.specialization.Specialization;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasHolder;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.util.stream.Stream;

@Environment(EnvType.CLIENT)
public class SpecializationIconLoader extends SpriteAtlasHolder {

	public SpecializationIconLoader() {
		this(MinecraftClient.getInstance().getTextureManager());
	}

	public SpecializationIconLoader(TextureManager textureManager) {
		super(textureManager, Schmucks.id("textures/atlas/specialization.png"), "specialization");
	}

	@Override
	protected Stream<Identifier> getSprites() {
		return SpecializationIcon.REGISTRY.getEntries().stream().map(icon -> icon.getValue().location());
	}

	public Sprite getSprite(SpecializationIcon icon) {
		return this.getSprite(SpecializationIcon.REGISTRY.getId(icon));
	}
}
