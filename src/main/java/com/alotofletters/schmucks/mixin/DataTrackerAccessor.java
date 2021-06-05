package com.alotofletters.schmucks.mixin;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(DataTracker.class)
public interface DataTrackerAccessor {
    @Invoker
    <T> DataTracker.Entry<T> invokeGetEntry(TrackedData<T> trackedData);

    @Accessor
    void setDirty(boolean dirty);
}
