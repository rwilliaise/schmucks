package com.alotofletters.schmucks.mixin;

import com.alotofletters.schmucks.Schmucks;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractFireBlock.class)
public class FireBlockMixin {

	@Inject(method = "onEntityCollision", at = @At("HEAD"), cancellable = true)
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity, CallbackInfo callbackInfo) {
		if (entity instanceof ItemEntity itemEntity) {
			ItemStack stack = itemEntity.getStack();
			if (stack.getItem() == Schmucks.PURE_MAGIC) {
				itemEntity.setStack(new ItemStack(Schmucks.FIERY_MAGIC, stack.getCount()));
				world.breakBlock(pos, false);
				if (!world.isClient) {
					((ServerWorld) world).spawnParticles(ParticleTypes.LARGE_SMOKE,
							pos.getX(),
							pos.getY(),
							pos.getZ(),
							8,
							0.5D,
							0.25D,
							0.5D,
							0.0D);
					entity.playSound(SoundEvents.BLOCK_FIRE_EXTINGUISH,
							1,
							1);
				}
				callbackInfo.cancel();
			}
		}
	}

}
