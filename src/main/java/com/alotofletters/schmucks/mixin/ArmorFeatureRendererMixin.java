package com.alotofletters.schmucks.mixin;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.ModelWithHead;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin {
	private final static ItemStack GLADIATOR_HELMET_STACK = new ItemStack(Schmucks.GLADIATOR_HELMET);
	private final static ItemStack MINERS_CAP_STACK = new ItemStack(Schmucks.MINERS_CAP);
	private final static ItemStack FARMERS_HAT_STACK = new ItemStack(Schmucks.FARMERS_HAT);
	private final static ItemStack LUMBERJACK_HAT_STACK = new ItemStack(Schmucks.LUMBERJACK_HAT);
	private final static ItemStack RANGER_HAT_STACK = new ItemStack(Schmucks.RANGER_HAT);

	@Inject(method = "renderArmor", at = @At("HEAD"), cancellable = true)
	private <T extends LivingEntity, A extends BipedEntityModel<T>> void renderArmor(MatrixStack matrices,
																					 VertexConsumerProvider vertexConsumers,
																					 T entity,
																					 EquipmentSlot armorSlot,
																					 int light,
																					 A model,
																					 CallbackInfo ci) {
		if ((entity instanceof SchmuckEntity schmuck
				&& schmuck.displaysHat()
				|| entity.getEquippedStack(armorSlot).isIn(Schmucks.JOB_HATS_TAG))
				&& armorSlot == EquipmentSlot.HEAD) {
			matrices.push();
			((ModelWithHead) ((ArmorFeatureRenderer) (Object) this).getContextModel()).getHead().rotate(matrices);
			this.translate(matrices);
			ItemStack stack = entity.getEquippedStack(armorSlot);
			if (entity instanceof SchmuckEntity schmuck
					&& schmuck.hasJob()
					&& schmuck.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
				if (schmuck.isGladiator()) {
					stack = GLADIATOR_HELMET_STACK;
				}
				if (schmuck.isMiner()) {
					stack = MINERS_CAP_STACK;
				}
				if (schmuck.isFarmer()) {
					stack = FARMERS_HAT_STACK;
				}
				if (schmuck.isLumberjack()) {
					stack = LUMBERJACK_HAT_STACK;
				}
				if (schmuck.isRanger()) {
					stack = RANGER_HAT_STACK;
				}
			}
			MinecraftClient.getInstance().getHeldItemRenderer().renderItem(entity, stack, ModelTransformation.Mode.HEAD, false, matrices, vertexConsumers, light);
			matrices.pop();
			ci.cancel();
		}
	}

	private void translate(MatrixStack matrices) {
		matrices.translate(0.0D, -0.25D, 0.0D);
		matrices.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
		matrices.scale(0.625F, -0.625F, -0.625F);
	}
}
