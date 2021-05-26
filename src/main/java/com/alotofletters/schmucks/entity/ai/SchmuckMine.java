package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.config.SchmucksConfig;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.OreBlock;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PickaxeItem;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;

public class SchmuckMine extends MoveToTargetPosGoal {
	private final SchmuckEntity schmuck;
	private final int maxProgress;
	private int breakProgress;

	public SchmuckMine(SchmuckEntity schmuck, double speed, int maxProgress) {
		super(schmuck, speed, AutoConfig.getConfigHolder(SchmucksConfig.class).getConfig().jobRange);
		this.schmuck = schmuck;
		this.maxProgress = maxProgress;
	}

	@Override
	public boolean canStart() {
		if (!super.canStart()) {
			return false;
		} else {
			return this.schmuck.getMainHandStack().getItem() instanceof PickaxeItem && !this.schmuck.isSitting();
		}
	}

	@Override
	public void tick() {
		if (this.hasReached()) {
			if (this.schmuck.getRandom().nextInt(20) == 0) {
				this.schmuck.swingHand(Hand.MAIN_HAND);
			}
			breakProgress++;
			int newProgress = (int) Math.floor((float) this.breakProgress / this.getMaxProgress() * 10.0F);
			this.schmuck.world.setBlockBreakingInfo(this.schmuck.getEntityId(), this.targetPos, newProgress);
			if (this.breakProgress >= this.getMaxProgress()) {
				this.schmuck.world.breakBlock(this.targetPos, true);
			}
		}

		super.tick();
	}

	public boolean shouldContinue() {
		return super.shouldContinue() && !this.schmuck.isSitting() && this.isOrePresent();
	}

	@Override
	protected BlockPos getTargetPos() {
		World world = this.schmuck.world;
		BlockPos pos = this.targetPos;
		if (world.isAir(pos.up())) {
			return pos.up();
		} else if (world.isAir(pos.down())) {
			return pos.down();
		} else if (world.isAir(pos.north())) {
			return pos.north();
		} else if (world.isAir(pos.south())) {
			return pos.south();
		} else if (world.isAir(pos.west())) {
			return pos.west();
		} else { // east
			return pos.east();
		}
	}

	public boolean isOrePresent() {
		return !this.schmuck.world.getBlockState(this.targetPos).isAir();
	}

	@Override
	public double getDesiredSquaredDistanceToTarget() {
		return 2.0D;
	}

	@Override
	public void start() {
		super.start();
		breakProgress = 0;
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
	protected boolean isTargetPos(WorldView world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		ItemStack pickaxe = this.schmuck.getMainHandStack();
		return this.isExposed(world, pos) && state.getBlock() instanceof OreBlock && pickaxe.isEffectiveOn(state);
	}

	public int getMaxProgress() {
		return maxProgress;
	}
}
