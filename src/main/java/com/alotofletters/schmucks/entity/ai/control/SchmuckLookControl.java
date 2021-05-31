package com.alotofletters.schmucks.entity.ai.control;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.entity.ai.control.LookControl;
import net.minecraft.entity.mob.MobEntity;

public class SchmuckLookControl extends LookControl {
    public SchmuckLookControl(SchmuckEntity entity) {
        super(entity);
    }

    @Override
    protected boolean shouldStayHorizontal() {
        return !this.entity.isFallFlying();
    }
}
