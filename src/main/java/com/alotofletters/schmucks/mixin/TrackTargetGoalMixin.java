package com.alotofletters.schmucks.mixin;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.specialization.modifier.Modifiers;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.ai.goal.TrackTargetGoal;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.particle.ParticleTypes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(TrackTargetGoal.class)
public class TrackTargetGoalMixin {

	@Shadow @Final protected MobEntity mob;

	@Inject(method = "canTrack", at = @At("TAIL"), cancellable = true)
	protected void canTrack(LivingEntity target, TargetPredicate targetPredicate, CallbackInfoReturnable<Boolean> cir) {
		if (this.mob instanceof CreeperEntity && target instanceof SchmuckEntity schmuck && schmuck.hasModifier(Modifiers.MOLLIFY)) {
			this.mob.world.sendEntityStatus(this.mob, (byte) 18);
			cir.setReturnValue(false);
		}
	}
}
