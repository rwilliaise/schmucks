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
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Environment(EnvType.CLIENT)
public class SchmuckEntityRenderer extends BipedEntityRenderer<SchmuckEntity, SchmuckEntityModel> {
	public static final Map<UUID, Identifier> CACHED_TEXTURES = new HashMap<>();

	private Identifier textureCached = Schmucks.id("textures/entity/schmuck.png");
	private boolean slim = false;
	private boolean loadedTexture = false;

	public SchmuckEntityRenderer(EntityRenderDispatcher dispatcher) {
		super(dispatcher, new SchmuckEntityModel(0f, false), 0.5f);
		this.addFeature(
			new ArmorFeatureRenderer<>(this,
				new SchmuckEntityModel(0.5f, true),
				new SchmuckEntityModel(1f, true)));
	}

	@Override
	protected void scale(SchmuckEntity entity, MatrixStack matrices, float amount) {
		matrices.scale(0.4f, 0.4f, 0.4f);
		super.scale(entity, matrices, amount);
	}

	private void updateSlim() {
		if (this.slim) {
			SchmuckEntityModel model = this.getModel();
			model.leftArm = new ModelPart(model, 32, 48);
			model.leftArm.addCuboid(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F);
			model.leftArm.setPivot(5.0F, 2.5F, 0.0F);
			model.rightArm = new ModelPart(model, 40, 16);
			model.rightArm.addCuboid(-2.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, 0.0F);
			model.rightArm.setPivot(-5.0F, 2.5F, 0.0F);
		} else {
			SchmuckEntityModel model = this.getModel();
			model.leftArm = new ModelPart(model, 32, 48);
			model.leftArm.addCuboid(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F);
			model.leftArm.setPivot(5.0F, 2.0F, 0.0F);
			model.rightArm = new ModelPart(model, 40, 16);
			model.rightArm.addCuboid(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, 0.0F);
			model.rightArm.setPivot(-5.0F, 2.0F, 0.0F);
		}
	}

	@Override
	public Identifier getTexture(SchmuckEntity mobEntity) {
		if (mobEntity.getOwnerProfile() != null) {
			return this.getTexturePrecached(mobEntity.getOwnerProfile());
		}
		return super.getTexture(mobEntity);
	}

	public Identifier getTexturePrecached(GameProfile profile) {
		if (!this.loadedTexture) {
			this.loadedTexture = true;
			if (CACHED_TEXTURES.containsKey(profile.getId())) {
				this.textureCached = CACHED_TEXTURES.get(profile.getId());
				return this.textureCached;
			}
			synchronized (this) { // super duper dangerous but who cares! haha.... ha....
				MinecraftClient.getInstance().getSkinProvider().loadSkin(profile, (type, identifier, minecraftProfileTexture) -> {
					if (type == MinecraftProfileTexture.Type.SKIN) {
						String model = minecraftProfileTexture.getMetadata("model");
						this.slim = "slim".equals(model); // just assume its slim because i dont know metadata values
						this.updateSlim();
						CACHED_TEXTURES.put(profile.getId(), identifier);
						this.textureCached = identifier;
					}
				}, true);
			}
		}
		return this.textureCached;
	}
}
