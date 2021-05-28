package com.alotofletters.schmucks.client.render.entity;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.client.render.entity.model.SchmuckEntityModel;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class SchmuckEntityRenderer extends BipedEntityRenderer<SchmuckEntity, SchmuckEntityModel> {
	private final Map<UUID, Identifier> CACHED_TEXTURES = new HashMap<>();
	private final Map<UUID, Boolean> loadedTexture = new HashMap<>();

	private final Identifier defaultTexture = Schmucks.id("textures/entity/schmuck.png");

	public SchmuckEntityRenderer(EntityRenderDispatcher dispatcher, boolean slim) {
		super(dispatcher, new SchmuckEntityModel(0f, false), 0.5f);
		this.addFeature(
			new ArmorFeatureRenderer<>(this,
				new SchmuckEntityModel(0.5f, true),
				new SchmuckEntityModel(1f, true)));
		this.addFeature(new ElytraFeatureRenderer<>(this));
		if (slim) {
			SchmuckEntityModel model = this.getModel();
			model.leftArm = new ModelPart(model, 32, 48);
			model.leftArm.addCuboid(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F);
			model.leftArm.setPivot(5.0F, 2.5F, 0.0F);
			model.rightArm = new ModelPart(model, 40, 16);
			model.rightArm.addCuboid(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F);
			model.rightArm.setPivot(-5.0F, 2.5F, 0.0F);
		}
	}

	@Override
	protected void scale(SchmuckEntity entity, MatrixStack matrices, float amount) {
		matrices.scale(0.4f, 0.4f, 0.4f);
		super.scale(entity, matrices, amount);
	}

	@Override
	public Identifier getTexture(SchmuckEntity mobEntity) {
		if (mobEntity.getOwnerUuid() != null) {
			return this.getTexturePrecached(mobEntity.getOwnerUuid());
		}
		return super.getTexture(mobEntity);
	}

	public Identifier getTexturePrecached(UUID uuid) {
		if (!this.loadedTexture.containsKey(uuid)) {
			this.loadedTexture.put(uuid, true);
			if (CACHED_TEXTURES.containsKey(uuid)) {
				return CACHED_TEXTURES.get(uuid);
			}
			synchronized (this) {
				MinecraftClient.getInstance().getSkinProvider().loadSkin(new GameProfile(uuid, null), (type, identifier, minecraftProfileTexture) -> {
					if (type == MinecraftProfileTexture.Type.SKIN) {
						CACHED_TEXTURES.put(uuid, identifier);
					}
				}, true);
			}
		}
		return CACHED_TEXTURES.getOrDefault(uuid, defaultTexture);
	}
}
