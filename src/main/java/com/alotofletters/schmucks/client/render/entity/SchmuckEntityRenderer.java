package com.alotofletters.schmucks.client.render.entity;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeadFeatureRenderer;
import net.minecraft.client.render.entity.feature.HeldItemFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.PlayerEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class SchmuckEntityRenderer extends BipedEntityRenderer<SchmuckEntity, PlayerEntityModel<SchmuckEntity>> {
	private final Map<UUID, Identifier> CACHED_TEXTURES = new HashMap<>();
	private final Map<UUID, Boolean> loadedTexture = new HashMap<>();

	private final Identifier defaultTexture = Schmucks.id("textures/entity/schmuck.png");

	public SchmuckEntityRenderer(EntityRendererFactory.Context ctx, boolean slim) {
		super(ctx, new PlayerEntityModel<>(ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM : EntityModelLayers.PLAYER), false), 0.25f);
		this.addFeature(new ArmorFeatureRenderer<>(this, new BipedEntityModel<>(ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM_INNER_ARMOR : EntityModelLayers.PLAYER_INNER_ARMOR)), new BipedEntityModel<>(ctx.getPart(slim ? EntityModelLayers.PLAYER_SLIM_OUTER_ARMOR : EntityModelLayers.PLAYER_OUTER_ARMOR))));
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

	protected void setupTransforms(SchmuckEntity schmuckEntity, MatrixStack matrixStack, float f, float g, float h) {
		float i = schmuckEntity.getLeaningPitch(h);
		float n;
		float k;
		if (schmuckEntity.isFallFlying()) {
			super.setupTransforms(schmuckEntity, matrixStack, f, g, h);
			n = (float)schmuckEntity.getRoll() + h;
			k = MathHelper.clamp(n * n / 100.0F, 0.0F, 1.0F);
			if (!schmuckEntity.isUsingRiptide()) {
				matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(k * (-90.0F - schmuckEntity.getPitch())));
			}

			Vec3d vec3d = schmuckEntity.getRotationVec(h);
			Vec3d vec3d2 = schmuckEntity.getVelocity();
			double d = vec3d2.horizontalLengthSquared();
			double e = vec3d.horizontalLengthSquared();
			if (d > 0.0D && e > 0.0D) {
				double l = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(d * e);
				double m = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
				matrixStack.multiply(Vec3f.POSITIVE_Y.getRadialQuaternion((float)(Math.signum(m) * Math.acos(l))));
			}
		} else if (i > 0.0F) {
			super.setupTransforms(schmuckEntity, matrixStack, f, g, h);
			n = schmuckEntity.isTouchingWater() ? -90.0F - schmuckEntity.getPitch() : -90.0F;
			k = MathHelper.lerp(i, 0.0F, n);
			matrixStack.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(k));
			if (schmuckEntity.isInSwimmingPose()) {
				matrixStack.translate(0.0D, -1.0D, 0.30000001192092896D);
			}
		} else {
			super.setupTransforms(schmuckEntity, matrixStack, f, g, h);
		}

	}
}
