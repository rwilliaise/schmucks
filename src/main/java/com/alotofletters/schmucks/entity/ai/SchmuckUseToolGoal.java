package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.config.SchmucksConfig;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class SchmuckUseToolGoal extends MoveToTargetPosGoal {
    public final SchmuckEntity schmuck;
    private final int maxProgress;
    private int breakProgress;
    private int lastProgress;

    public SchmuckUseToolGoal(SchmuckEntity schmuck, double speed, int maxProgress) {
        super(schmuck, speed, Schmucks.CONFIG.jobRange);
        this.maxProgress = maxProgress;
        this.schmuck = schmuck;
    }

    @Override
    protected int getInterval(PathAwareEntity mob) {
        SchmucksConfig config = Schmucks.CONFIG;
        int min = config.toolInterval.min;
        if (min < config.toolInterval.max) {
            min += mob.getRandom().nextInt(config.toolInterval.max - min);
        }
        return min;
    }

    public boolean use() {
        this.breakProgress++;
        int newProgress = (int) Math.floor((float) this.breakProgress / this.getMaxProgress() * 10.0F);
        if (this.lastProgress != newProgress) {
            this.schmuck.swingHand(Hand.MAIN_HAND);
            this.lastProgress = newProgress;
        }
        this.schmuck.world.setBlockBreakingInfo(this.schmuck.getEntityId(), this.targetPos, newProgress);
        return this.breakProgress >= this.getMaxProgress();
    }

    @Override
    public void start() {
        super.start();
        this.breakProgress = 0;
        this.lastProgress = 0;
    }

    protected boolean isStandable(BlockPos pos) {
        World world = this.schmuck.world;
        return world.isAir(pos) && world.getBlockState(pos.down()).hasSolidTopSurface(world, pos, this.schmuck);
    }

    protected BlockPos getStandablePosition() {
        BlockPos pos = this.targetPos;
        if (isStandable(pos.up())) {
            return pos.up();
        } else if (isStandable(pos.down())) {
            return pos.down();
        } else if (isStandable(pos.north())) {
            return pos.north();
        } else if (isStandable(pos.south())) {
            return pos.south();
        } else if (isStandable(pos.west())) {
            return pos.west();
        } else {
            return pos.east();
        }
    }

    public int getMaxProgress() {
        return maxProgress;
    }
}
