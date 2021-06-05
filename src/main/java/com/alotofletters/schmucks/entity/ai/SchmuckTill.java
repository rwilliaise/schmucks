package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.config.SchmucksConfig;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.goal.MoveToTargetPosGoal;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.item.HoeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class SchmuckTill extends MoveToTargetPosGoal {
    private final SchmuckEntity schmuck;

    public SchmuckTill(SchmuckEntity mob, double speed, int range) {
        super(mob, speed, range);
        this.schmuck = mob;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.hasReached() && this.isTargetPos(this.schmuck.world, this.targetPos)) {
            this.schmuck.world.setBlockState(this.targetPos, Blocks.FARMLAND.getDefaultState(), 11);
            this.schmuck.playSound(SoundEvents.ITEM_HOE_TILL, 1.0F, 1.0F);
        }
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

    @Override
    public boolean canStart() {
        return this.schmuck.getMainHandStack().getItem() instanceof HoeItem && super.canStart();
    }

    @Override
    protected boolean isTargetPos(WorldView world, BlockPos pos) {
        return world.isAir(pos.up()) &&
                world.getBlockState(pos).isIn(Schmucks.TILLABLE_TAG) &&
                this.schmuck.whiteListed.contains(pos);
    }
}
