package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.specialization.modifier.Modifiers;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.fabricmc.loader.util.sat4j.core.Vec;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.block.RedstoneOreBlock;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ai.NoPenaltyTargeting;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.WorldView;

import java.util.EnumSet;

public class SchmuckMine extends SchmuckUseToolGoal {
	private BlockPos cachedPos;

	public SchmuckMine(SchmuckEntity schmuck, double speed, int maxProgress) {
		super(schmuck, speed, maxProgress);
		this.setControls(EnumSet.of(Control.MOVE, Control.JUMP, Control.LOOK));
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
			this.damage(this.schmuck.getMainHandStack());
		}

		this.mob.getLookControl().lookAt(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ());
	}

	public void damage(ItemStack stack) {
		float chance = 0;
		if (this.schmuck.hasModifier(Modifiers.THRIFTY)) {
			chance = this.schmuck.getModifierLevel(Modifiers.THRIFTY);
		}
		if (this.schmuck.getRandom().nextFloat() > chance) {
			stack.damage(1, this.schmuck, schmuckEntity -> {
				schmuckEntity.sendEquipmentBreakStatus(EquipmentSlot.MAINHAND);
//				schmuckEntity.refreshGoals();
			});
		}
	}

	public boolean shouldContinue() {
		return super.shouldContinue() && !this.schmuck.isSitting() && this.isOrePresent();
	}

	@Override
	protected BlockPos getTargetPos() {
		if (this.cachedPos != null) {
			return this.cachedPos;
		}
		Vec3d newPos = NoPenaltyTargeting.find(this.schmuck, 1, 2, new Vec3d(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ()));
		if (newPos == null) {
			return super.getTargetPos();
		}
		this.cachedPos = new BlockPos(newPos);
		return this.cachedPos;
	}

	public boolean isOrePresent() {
		return !this.schmuck.world.getBlockState(this.targetPos).isAir();
	}

	@Override
	public double getDesiredSquaredDistanceToTarget() {
		return 4.0D;
	}

	public boolean isExposed(WorldView world, BlockPos pos) {
		return world.isAir(pos.up()) ||
				world.isAir(pos.down()) ||
				world.isAir(pos.north()) ||
				world.isAir(pos.south()) ||
				world.isAir(pos.west()) ||
				world.isAir(pos.east());
	}

	@Override
	public void start() {
		super.start();
		this.cachedPos = null;
	}

	@Override
	public void stop() {
		super.stop();
		this.targetPos = BlockPos.ORIGIN;
	}

	public boolean isOre(BlockState state) {
		return state.getBlock() instanceof OreBlock || state.getBlock() instanceof RedstoneOreBlock;
	}

	public boolean raycast(BlockPos pos) {
		var context = new RaycastContext(this.schmuck.getEyePos(), new Vec3d(pos.getX(),
				pos.getY(),
				pos.getZ()), RaycastContext.ShapeType.OUTLINE, RaycastContext.FluidHandling.NONE, this.schmuck);
		var result = this.schmuck.world.raycast(context);
		return pos.equals(result.getBlockPos());
	}

	@Override
	protected boolean isTargetPos(WorldView world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		ItemStack pickaxe = this.schmuck.getMainHandStack();
		return this.isExposed(world, pos) && this.isOre(state) && pickaxe.isSuitableFor(state) && (this.targetPos != BlockPos.ORIGIN || this.raycast(pos));
	}
}
