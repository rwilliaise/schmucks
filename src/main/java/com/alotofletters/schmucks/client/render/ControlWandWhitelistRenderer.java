package com.alotofletters.schmucks.client.render;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.mixin.WorldRendererAccessor;
import com.mojang.blaze3d.platform.GlStateManager;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.GlfwUtil;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.ArrayList;
import java.util.List;

/** Used to render outlines on whitelisted blocks, while holding out the Schmuck Staff. */
public class ControlWandWhitelistRenderer {

    public static boolean onBlockOutline(WorldRenderContext worldRenderContext, HitResult result) {
        ClientWorld world = worldRenderContext.world();
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null || !player.isHolding(Schmucks.CONTROL_WAND)) {
            return true;
        }
        WorldRendererAccessor accessor = (WorldRendererAccessor) worldRenderContext.worldRenderer();
        BufferBuilderStorage storage = accessor.getBufferBuilders();
        VertexConsumerProvider.Immediate immediate = storage.getEntityVertexConsumers();
        VertexConsumer consumer = immediate.getBuffer(RenderLayer.getLines());
        Vec3d cameraPos = worldRenderContext.camera().getPos();
        float shade = (float) ((Math.sin(Math.toRadians((world.getTime() + worldRenderContext.tickDelta()) * 15)) + 1) * 0.5);
        Schmucks.getWhitelistOrEmpty(player).forEach(pos -> {
            BlockState state = world.getBlockState(pos);
            VoxelShape shape = state.getOutlineShape(world, pos, ShapeContext.of(player));
            if (shape.isEmpty()) {
                shape = VoxelShapes.fullCube();
            }
            double x = pos.getX() - cameraPos.getX();
            double y = pos.getY() - cameraPos.getY();
            double z = pos.getZ() - cameraPos.getZ();
            worldRenderContext.matrixStack().push();
            worldRenderContext.matrixStack().translate(x, y, z);
            shape.forEachBox((minX, minY, minZ, maxX, maxY, maxZ) ->
                    WorldRenderer.drawBox(worldRenderContext.matrixStack(),
                            consumer,
                            minX,
                            minY,
                            minZ,
                            maxX,
                            maxY,
                            maxZ,
                            1,
                            1,
                            1,
                            shade));
            worldRenderContext.matrixStack().pop();
        });
        return true;
    }
}
