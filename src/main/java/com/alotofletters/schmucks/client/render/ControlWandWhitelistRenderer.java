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

import java.util.ArrayList;
import java.util.List;

/** Used to render outlines on whitelisted blocks, while holding out the Schmuck Staff. */
public class ControlWandWhitelistRenderer {
    public static boolean HAS_RECALCULATED = false;

    private static final List<BlockPos> NEAR_WHITELIST = new ArrayList<>();

    public static boolean onBlockOutline(WorldRenderContext worldRenderContext, HitResult result) {
        WorldRendererAccessor accessor = (WorldRendererAccessor) worldRenderContext.worldRenderer();
        BufferBuilderStorage storage = accessor.getBufferBuilders();
        VertexConsumerProvider.Immediate immediate = storage.getEntityVertexConsumers();
        VertexConsumer consumer = immediate.getBuffer(RenderLayer.getLines());
        ClientWorld world = worldRenderContext.world();
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (world.getTime() % 5 == 0) {
            HAS_RECALCULATED = false;
        }
        if (player == null) {
            return true;
        }
        if (!HAS_RECALCULATED && player.isHolding(Schmucks.CONTROL_WAND)) {
            NEAR_WHITELIST.clear();
            world.getEntitiesIncludingUngeneratedChunks(SchmuckEntity.class,
                    player.getBoundingBox().expand(10),
                    schmuck -> player.getUuid().equals(schmuck.getOwnerUuid()))
                            .stream()
                            .map(SchmuckEntity::getWhitelist)
                            .forEach(list -> list.forEach(pos -> {
                                if (!NEAR_WHITELIST.contains(pos)) {
                                    NEAR_WHITELIST.add(pos);
                                }
                            }));
            HAS_RECALCULATED = true;
        } else if (HAS_RECALCULATED && !player.isHolding(Schmucks.CONTROL_WAND)) {
            HAS_RECALCULATED = false;
        }
        if (!player.isHolding(Schmucks.CONTROL_WAND)) {
            return true;
        }
        Vec3d cameraPos = worldRenderContext.camera().getPos();
        float shade = (float) ((Math.sin(Math.toRadians((world.getTime() + worldRenderContext.tickDelta()) * 15)) + 1) * 0.5);
        NEAR_WHITELIST.forEach(pos -> {
            BlockState state = world.getBlockState(pos);
            VoxelShape shape = state.getOutlineShape(world, pos, ShapeContext.of(player));
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
