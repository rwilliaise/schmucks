package com.alotofletters.schmucks.mixin;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.client.render.entity.SchmuckEntityRenderer;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.fabricmc.fabric.api.client.rendereregistry.v1.EntityRendererRegistry;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ReloadableResourceManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

	private static EntityRenderer<SchmuckEntity> RENDERER_DEFAULT;
	private static EntityRenderer<SchmuckEntity> RENDERER_SLIM;

	@Inject(at = @At("TAIL"), method = "<init>")
	public void init(TextureManager textureManager,
	                 ItemRenderer itemRenderer,
	                 ReloadableResourceManager reloadableResourceManager,
	                 TextRenderer textRenderer,
	                 GameOptions gameOptions,
	                 CallbackInfo ci) {
		RENDERER_DEFAULT = new SchmuckEntityRenderer((EntityRenderDispatcher) (Object) this, false);
		RENDERER_SLIM = new SchmuckEntityRenderer((EntityRenderDispatcher) (Object) this, true);
	}

	@Inject(at = @At("HEAD"), method = "getRenderer", cancellable = true)
	public void getRenderer(Entity entity, CallbackInfoReturnable<EntityRenderer<SchmuckEntity>> callbackInfo) {
		if (entity instanceof SchmuckEntity) {
			String model = ((SchmuckEntity) entity).getModel();
			switch (model) {
				case "default":
					callbackInfo.setReturnValue(RENDERER_DEFAULT);
					return;
				case "slim":
					callbackInfo.setReturnValue(RENDERER_SLIM);
					return;
			}
			callbackInfo.setReturnValue(RENDERER_DEFAULT);
		}
	}
}
