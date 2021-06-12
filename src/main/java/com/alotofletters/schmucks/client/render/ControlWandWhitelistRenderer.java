package com.alotofletters.schmucks.client.render;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.mixin.WorldRendererAccessor;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.lwjgl.glfw.GLFW;

/**
 * Used to render outlines on whitelisted blocks, while holding out the Schmuck Staff.
 */
public class ControlWandWhitelistRenderer {

	public static boolean onBlockOutline(WorldRenderContext worldRenderContext, HitResult result) {
		return renderWhitelist(worldRenderContext, RenderLayer.getLines());
	}

	private static boolean renderWhitelist(WorldRenderContext worldRenderContext, RenderLayer layer) {
		ClientWorld world = worldRenderContext.world();
		ClientPlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null || !player.isHolding(Schmucks.CONTROL_WAND)) {
			return true;
		}
		WorldRendererAccessor accessor = (WorldRendererAccessor) worldRenderContext.worldRenderer();
		BufferBuilderStorage storage = accessor.getBufferBuilders();
		VertexConsumerProvider.Immediate immediate = storage.getEntityVertexConsumers();
		VertexConsumer consumer = immediate.getBuffer(layer);
		Vec3d cameraPos = worldRenderContext.camera().getPos();
		int minDistance = Schmucks.CONFIG.wandRenderDistance / 4;
		int distance = Schmucks.CONFIG.wandRenderDistance - minDistance;
		Schmucks.getWhitelistOrEmpty(player).forEach(pos -> {
			double dist = player.squaredDistanceTo(pos.getX(), pos.getY(), pos.getZ());
			if (dist > Schmucks.CONFIG.wandRenderDistance) {
				return;
			}
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
			float transparency = (float) (dist - minDistance + Schmucks.CONFIG.wandRenderDistance);
			float ratio = (transparency / distance) - 1;
			float shade = (float) ((Math.sin(Math.toRadians((GLFW.glfwGetTime()) * 300)) + 1) * 0.5);
			float finalTrans = Math.min(1, shade + ratio);
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
							1 - finalTrans));
			worldRenderContext.matrixStack().pop();
		});
		return true;
	}
}
