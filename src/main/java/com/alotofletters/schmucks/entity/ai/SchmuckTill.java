package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.specialization.modifier.Modifiers;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropBlock;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.WorldView;

public class SchmuckTill extends SchmuckUseToolGoal {
	public SchmuckTill(SchmuckEntity mob, double speed, int maxProgress) {
		super(mob, speed, maxProgress);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.hasReached() && this.use()) {
			if (this.isFullCrop(this.targetPos)) {
				if (this.schmuck.hasModifier(Modifiers.FULL_HARVEST)
						&& this.schmuck.getRandom().nextFloat() < 0.1) {
					BlockState state = this.schmuck.world.getBlockState(this.targetPos);
					LootContext.Builder builder = (new LootContext.Builder((ServerWorld) this.schmuck.world))
							.parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(this.targetPos))
							.parameter(LootContextParameters.BLOCK_STATE, state)
							.optionalParameter(LootContextParameters.THIS_ENTITY, this.schmuck)
							.parameter(LootContextParameters.TOOL, this.schmuck.getMainHandStack());
					state.getDroppedStacks(builder).forEach(stack -> this.schmuck.world.spawnEntity(new ItemEntity(
							this.schmuck.world,
							this.targetPos.getX(),
							this.targetPos.getY(),
							this.targetPos.getZ(),
							stack)));
				}
				this.schmuck.world.breakBlock(this.targetPos, true, this.schmuck);
			} else if (this.schmuck.world.getBlockState(this.targetPos).isIn(Schmucks.TILLABLE_TAG)) {
				this.schmuck.world.setBlockState(this.targetPos, Blocks.FARMLAND.getDefaultState(), Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
				this.schmuck.playSound(SoundEvents.ITEM_HOE_TILL, 1.0F, 1.0F);
			}
		}
		this.mob.getLookControl().lookAt(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ());
	}

	@Override
	public double getDesiredSquaredDistanceToTarget() {
		return 2.0d;
	}

	@Override
	public void start() {
		super.start();
	}

	@Override
	public void stop() {
		super.stop();
		this.schmuck.world.setBlockBreakingInfo(this.schmuck.getId(), this.targetPos, -1);
	}

	@Override
	public boolean canStart() {
		ItemStack itemStack = this.schmuck.getMainHandStack();
		return this.schmuck.isFarmer() && !this.schmuck.isSitting() && super.canStart();
	}

	public boolean isFullCrop(BlockPos pos) {
		BlockState state = this.schmuck.world.getBlockState(pos);
		return state.getBlock() instanceof CropBlock &&
				state.get(((CropBlock) state.getBlock()).getAgeProperty()) == ((CropBlock) state.getBlock()).getMaxAge() &&
				this.schmuck.getWhitelist().contains(pos.down());
	}

	@Override
	protected boolean isTargetPos(WorldView world, BlockPos pos) {
		return this.isFullCrop(pos) || (world.isAir(pos.up()) &&
				world.getBlockState(pos).isIn(Schmucks.TILLABLE_TAG) &&
				this.schmuck.getWhitelist().contains(pos));
	}
}
