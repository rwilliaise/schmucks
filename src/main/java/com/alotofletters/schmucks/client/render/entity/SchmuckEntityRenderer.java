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
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.entity.BipedEntityRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.ElytraFeatureRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

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

	protected void setupTransforms(SchmuckEntity schmuckEntity, MatrixStack matrixStack, float f, float g, float h) {
		float i = schmuckEntity.getLeaningPitch(h);
		float n;
		float k;
		if (schmuckEntity.isFallFlying()) {
			super.setupTransforms(schmuckEntity, matrixStack, f, g, h);
			n = (float)schmuckEntity.getRoll() + h;
			k = MathHelper.clamp(n * n / 100.0F, 0.0F, 1.0F);
			if (!schmuckEntity.isUsingRiptide()) {
				matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(k * (-90.0F - schmuckEntity.pitch)));
			}

			Vec3d vec3d = schmuckEntity.getRotationVec(h);
			Vec3d vec3d2 = schmuckEntity.getVelocity();
			double d = Entity.squaredHorizontalLength(vec3d2);
			double e = Entity.squaredHorizontalLength(vec3d);
			if (d > 0.0D && e > 0.0D) {
				double l = (vec3d2.x * vec3d.x + vec3d2.z * vec3d.z) / Math.sqrt(d * e);
				double m = vec3d2.x * vec3d.z - vec3d2.z * vec3d.x;
				matrixStack.multiply(Vector3f.POSITIVE_Y.getRadialQuaternion((float)(Math.signum(m) * Math.acos(l))));
			}
		} else if (i > 0.0F) {
			super.setupTransforms(schmuckEntity, matrixStack, f, g, h);
			n = schmuckEntity.isTouchingWater() ? -90.0F - schmuckEntity.pitch : -90.0F;
			k = MathHelper.lerp(i, 0.0F, n);
			matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(k));
			if (schmuckEntity.isInSwimmingPose()) {
				matrixStack.translate(0.0D, -1.0D, 0.30000001192092896D);
			}
		} else {
			super.setupTransforms(schmuckEntity, matrixStack, f, g, h);
		}

	}
}
