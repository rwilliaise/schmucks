package com.alotofletters.schmucks.mixin;

import com.alotofletters.schmucks.client.render.entity.SchmuckEntityRenderer;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.resource.ResourceManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

	private static EntityRenderer<SchmuckEntity> RENDERER_DEFAULT;
	private static EntityRenderer<SchmuckEntity> RENDERER_SLIM;
	@Shadow
	@Final
	private ItemRenderer itemRenderer;
	@Shadow
	@Final
	private EntityModelLoader modelLoader;
	@Shadow
	@Final
	private TextRenderer textRenderer;

	@Inject(at = @At("TAIL"), method = "reload")
	public void reload(ResourceManager manager, CallbackInfo ci) {
		EntityRendererFactory.Context context = new EntityRendererFactory.Context((EntityRenderDispatcher) (Object) this, this.itemRenderer, manager, this.modelLoader, this.textRenderer);
		RENDERER_DEFAULT = new SchmuckEntityRenderer(context, false);
		RENDERER_SLIM = new SchmuckEntityRenderer(context, true);
	}

	@Inject(at = @At("HEAD"), method = "getRenderer", cancellable = true)
	public void getRenderer(Entity entity, CallbackInfoReturnable<EntityRenderer<SchmuckEntity>> callbackInfo) {
		if (entity instanceof SchmuckEntity) {
			String model = ((SchmuckEntity) entity).getModel();
			switch (model) {
				case "default" -> {
					callbackInfo.setReturnValue(RENDERER_DEFAULT);
					return;
				}
				case "slim" -> {
					callbackInfo.setReturnValue(RENDERER_SLIM);
					return;
				}
			}
			callbackInfo.setReturnValue(RENDERER_DEFAULT);
		}
	}
}
