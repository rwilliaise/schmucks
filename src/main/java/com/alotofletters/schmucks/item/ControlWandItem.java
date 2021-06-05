package com.alotofletters.schmucks.item;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.client.gui.screen.ingame.ControlWandScreen;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ControlWandItem extends TooltipItem {
	public ControlWandItem(Settings settings) {
		super(settings.maxCount(1));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		if (!user.isSneaking()) {
			return super.use(world, user, hand);
		}
		HitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.NONE);
		if (hitResult.getType() == HitResult.Type.BLOCK) {
			return TypedActionResult.pass(user.getStackInHand(hand));
		}
		user.getItemCooldownManager().set(this, 20);
		if (world.isClient) {
			this.openScreen(null);
			return TypedActionResult.success(user.getStackInHand(hand));
		}
		return super.use(world, user, hand);
	}

	@Override
	public ActionResult useOnBlock(ItemUsageContext context) {
		World world = context.getWorld();
		if (world.isClient) {
			return ActionResult.PASS;
		}
		PlayerEntity player = context.getPlayer();
		BlockPos blockPos = context.getBlockPos();
		BlockEntity blockEntity = world.getBlockEntity(blockPos);
		if (player != null && player.isSneaking() && blockEntity != null) {
			AtomicBoolean removed = new AtomicBoolean(false);
			forEachSchmuckNearby((ServerPlayerEntity) player, schmuckEntity -> {
				if (schmuckEntity.whiteListed.contains(context.getBlockPos())) {
					schmuckEntity.whiteListed.remove(context.getBlockPos());
					removed.set(true);
				} else if (!removed.get()) {
					schmuckEntity.whiteListed.add(context.getBlockPos());
				}
			});
			if (removed.get()) {
				player.sendMessage(new TranslatableText("item.schmucks.control_wand.removed", blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
			} else {
				player.sendMessage(new TranslatableText("item.schmucks.control_wand.added", blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
			}
			return ActionResult.SUCCESS;
		} else if (player != null && player.isSneaking()) {
			forEachSchmuckNearby((ServerPlayerEntity) player, schmuckEntity -> {
				if (schmuckEntity.whiteListed.contains(blockPos)) {
					schmuckEntity.whiteListed.remove(context.getBlockPos());
				}
			});
		}
		return super.useOnBlock(context);
	}

	private void forEachSchmuckNearby(ServerPlayerEntity player, Consumer<SchmuckEntity> consumer) {
		player.world.getEntitiesByClass(SchmuckEntity.class, player.getBoundingBox().expand(Schmucks.CONFIG.wandRange), entity -> entity.getOwner() == player)
				.forEach(consumer);
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		if (entity instanceof SchmuckEntity) {
			if (entity.world.isClient && ((SchmuckEntity) entity).getOwner() == user) {
				this.openScreen((SchmuckEntity) entity);
				return ActionResult.SUCCESS;
			}
		}
		return super.useOnEntity(stack, user, entity, hand);
	}

	@Environment(EnvType.CLIENT)
	private void openScreen(SchmuckEntity entity) {
		MinecraftClient.getInstance().openScreen(new ControlWandScreen(entity));
	}

	public enum ControlAction {
		STOP_ALL,
		START_ALL,
		STOP_TELEPORT,
		START_TELEPORT,
		STOP_FOLLOWING,
		START_FOLLOWING,
		STOP_ATTACKING,
	}

	public enum ControlGroup implements StringIdentifiable {
		ALL("all"),
		NOT_STOPPED("not_stopped"),
		ALL_NO_TOOL("all_no_tool"),
		THIS("apply_this"),
		ALL_BUT_THIS("all_but_this"),
		SAME_TOOL("apply_same_tool"),
		ALL_BUT_SAME_TOOL("all_but_same_tool");

		private final String name;

		ControlGroup(String name) {
			this.name = name;
		}

		@Override
		public String asString() {
			return this.name;
		}
	}
}
