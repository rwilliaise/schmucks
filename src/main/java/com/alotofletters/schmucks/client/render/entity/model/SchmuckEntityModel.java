package com.alotofletters.schmucks.client.render.entity.model;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.CrossbowPosing;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;

public class SchmuckEntityModel extends BipedEntityModel<SchmuckEntity> {
	public SchmuckEntityModel(float scale, boolean armor) {
		super(scale, 0f, 64, armor ? 32 : 64);
	}

	@Override
	public void animateModel(SchmuckEntity schmuckEntity, float f, float g, float h) {
		super.animateModel(schmuckEntity, f, g, h);
		this.rightArmPose = ArmPose.EMPTY;
		this.leftArmPose = ArmPose.EMPTY;
		ItemStack itemStack = schmuckEntity.getStackInHand(Hand.MAIN_HAND);
		if (itemStack.getItem() == Items.BOW && schmuckEntity.isAttacking()) {
			if (schmuckEntity.getMainArm() == Arm.RIGHT) {
				this.rightArmPose = ArmPose.BOW_AND_ARROW;
			} else {
				this.leftArmPose = ArmPose.BOW_AND_ARROW;
			}
		}
	}

	@Override
	public void setAngles(SchmuckEntity mobEntity, float f, float g, float h, float i, float j) {
		super.setAngles(mobEntity, f, g, h, i, j);
		ItemStack itemStack = mobEntity.getMainHandStack();
		if (mobEntity.isAttacking() && (itemStack.isEmpty() || itemStack.getItem() != Items.BOW)) {
			float k = MathHelper.sin(this.handSwingProgress * 3.1415927F);
			float l = MathHelper.sin((1.0F - (1.0F - this.handSwingProgress) * (1.0F - this.handSwingProgress)) * 3.1415927F);
			this.rightArm.roll = 0.0F;
			this.leftArm.roll = 0.0F;
			this.rightArm.yaw = -(0.1F - k * 0.6F);
			this.leftArm.yaw = 0.1F - k * 0.6F;
			this.rightArm.pitch = -1.5707964F;
			this.leftArm.pitch = -1.5707964F;
			ModelPart var10000 = this.rightArm;
			var10000.pitch -= k * 1.2F - l * 0.4F;
			var10000 = this.leftArm;
			var10000.pitch -= k * 1.2F - l * 0.4F;
			CrossbowPosing.method_29351(this.rightArm, this.leftArm, mobEntity, this.handSwingProgress, h);
		}
	}
}
