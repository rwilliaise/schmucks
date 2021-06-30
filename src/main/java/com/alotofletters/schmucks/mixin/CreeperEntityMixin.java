package com.alotofletters.schmucks.mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin extends HostileEntity {

	public CreeperEntityMixin(EntityType<? extends HostileEntity> type, World world) {
		super(type, world);
	}

	public void handleStatus(byte status) {
		if (status == 18) {
			double d = this.random.nextGaussian() * 0.02D;
			double e = this.random.nextGaussian() * 0.02D;
			double f = this.random.nextGaussian() * 0.02D;
			this.world.addParticle(
					ParticleTypes.HEART,
					this.getParticleX(1.0D),
					this.getRandomBodyY() + 0.5D,
					this.getParticleZ(1.0D),
					d,
					e,
					f);
		} else {
			super.handleStatus(status);
		}
	}
}
