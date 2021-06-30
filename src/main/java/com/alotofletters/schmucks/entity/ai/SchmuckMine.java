package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class SchmuckMine extends SchmuckUseToolGoal {
	public SchmuckMine(SchmuckEntity schmuck, double speed, int maxProgress) {
		super(schmuck, speed, maxProgress);
	}

	@Override
	public boolean canStart() {
		if (!super.canStart()) {
			return false;
		} else {
			ItemStack itemStack = this.schmuck.getMainHandStack();
			return FabricToolTags.PICKAXES.contains(itemStack.getItem()) && !this.schmuck.isSitting();
		}
	}

	@Override
	public void tick() {
		super.tick();
		if (this.hasReached() && this.use()) {
			this.schmuck.world.breakBlock(this.targetPos, true);
			this.schmuck.getMainHandStack().damage(1, this.schmuck, schmuckEntity -> {
				schmuckEntity.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
			});
		}

		this.mob.getLookControl().lookAt(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ());
	}

	public boolean shouldContinue() {
		return super.shouldContinue() && !this.schmuck.isSitting() && this.isOrePresent();
	}

	@Override
	protected BlockPos getTargetPos() {
		return SchmuckUseToolGoal.getStandablePosition(this.schmuck, this.targetPos);
	}

	public boolean isOrePresent() {
		return !this.schmuck.world.getBlockState(this.targetPos).isAir();
	}

	@Override
	public double getDesiredSquaredDistanceToTarget() {
		return 2.0D;
	}

	public boolean isExposed(WorldView world, BlockPos pos) {
		return world.isAir(pos.up()) ||
				world.isAir(pos.down()) ||
				world.isAir(pos.north()) ||
				world.isAir(pos.south()) ||
				world.isAir(pos.west()) ||
				world.isAir(pos.east());
	}

	public boolean isOre(BlockState state) {
		return state.getBlock() instanceof OreBlock || state.getBlock() instanceof RedstoneOreBlock;
	}

	@Override
	protected boolean isTargetPos(WorldView world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		ItemStack pickaxe = this.schmuck.getMainHandStack();
		return this.isExposed(world, pos) && this.isOre(state) && pickaxe.isSuitableFor(state);
	}
}
