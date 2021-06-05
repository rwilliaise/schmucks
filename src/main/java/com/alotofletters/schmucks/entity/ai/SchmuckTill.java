package com.alotofletters.schmucks.entity.ai;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.fabricmc.fabric.api.tool.attribute.v1.FabricToolTags;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;

public class SchmuckTill extends SchmuckUseToolGoal {
    public SchmuckTill(SchmuckEntity mob, double speed, int maxProgress) {
        super(mob, speed, maxProgress);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.hasReached() && this.use()) {
            this.schmuck.world.setBlockState(this.targetPos, Blocks.FARMLAND.getDefaultState(), 11);
            this.schmuck.playSound(SoundEvents.ITEM_HOE_TILL, 1.0F, 1.0F);
        }
        this.mob.getLookControl().lookAt(this.targetPos.getX(), this.targetPos.getY(), this.targetPos.getZ());
    }

    @Override
    public double getDesiredSquaredDistanceToTarget() {
        return 2.0d;
    }

    @Override
    public void stop() {
        super.stop();
        this.schmuck.world.setBlockBreakingInfo(this.schmuck.getEntityId(), this.targetPos, -1);
    }

    @Override
    public boolean canStart() {
        ItemStack itemStack = this.schmuck.getMainHandStack();
        return FabricToolTags.HOES.contains(itemStack.getItem()) && !this.schmuck.isSitting() && super.canStart();
    }

    @Override
    protected boolean isTargetPos(WorldView world, BlockPos pos) {
        return world.isAir(pos.up()) &&
                world.getBlockState(pos).isIn(Schmucks.TILLABLE_TAG) &&
                this.schmuck.getWhitelist().contains(pos);
    }
}