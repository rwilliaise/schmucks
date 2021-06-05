package com.alotofletters.schmucks.item;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.client.gui.screen.ingame.ControlWandScreen;
import com.alotofletters.schmucks.client.render.ControlWandWhitelistRenderer;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;

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
			ControlWandWhitelistRenderer.HAS_RECALCULATED = false;
			return ActionResult.PASS;
		}
		PlayerEntity player = context.getPlayer();
		if (player == null) {
			return ActionResult.PASS;
		}
		BlockPos blockPos = context.getBlockPos();
		BlockState blockState = world.getBlockState(blockPos);
		BlockEntity blockEntity = world.getBlockEntity(blockPos);
		// TODO: less convoluted way for checking if applicable to whitelist, and what its text should be
		if (player.isSneaking() && blockEntity != null) {
			return updateFromContext(context, "%s");
		} else if (player.isSneaking()) {
			if (blockState.isOf(Blocks.FARMLAND) || blockState.isIn(Schmucks.TILLABLE_TAG)) {
				return updateFromContext(context, "%s_farmland");
			} else if (blockState.isIn(BlockTags.LOGS)) {
				return updateFromContext(context, "%s_lumber");
			} else {
				AtomicBoolean removed = new AtomicBoolean(false);
				forEachSchmuckNearby((ServerPlayerEntity) player, schmuckEntity -> {
					if (schmuckEntity.getWhitelist().contains(blockPos)) {
						schmuckEntity.getWhitelist().remove(context.getBlockPos());
						schmuckEntity.updateWhitelist();
						removed.set(true);
					}
				});
				if (removed.get()) {
					player.sendMessage(new TranslatableText("item.schmucks.control_wand.removed", blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
					return ActionResult.SUCCESS;
				}
			}
		}
		return super.useOnBlock(context);
	}

	private ActionResult updateFromContext(ItemUsageContext context, String fmt) {
		PlayerEntity player = context.getPlayer();
		if (player == null) {
			return ActionResult.PASS;
		}
		BlockPos blockPos = context.getBlockPos();
		AtomicBoolean removed = new AtomicBoolean(false);
		AtomicBoolean added = new AtomicBoolean(false);
		updateSchmucks((ServerPlayerEntity) player, blockPos, removed, added);
		if (removed.get()) {
			player.sendMessage(new TranslatableText(String.format("item.schmucks.control_wand." + fmt, "removed"), blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
			return ActionResult.SUCCESS;
		} else if (added.get()) {
			player.sendMessage(new TranslatableText(String.format("item.schmucks.control_wand." + fmt, "added"), blockPos.getY(), blockPos.getZ()), true);
			return ActionResult.SUCCESS;
		}
		return ActionResult.PASS;
	}

	/**
	 * Updates the whitelist for Schmucks nearby from a BlockPos.
	 * @param player Player that used the item
	 * @param blockPos Block position to update nearby Schmucks with
	 * @param removed Atomic flag for the little actionbar message
	 * @param added Atomic flag also for the little actionbar message
	 */
	private void updateSchmucks(ServerPlayerEntity player, BlockPos blockPos, AtomicBoolean removed, AtomicBoolean added) {
		forEachSchmuckNearby(player, schmuckEntity -> {
			if (schmuckEntity.getWhitelist().contains(blockPos)) {
				schmuckEntity.getWhitelist().remove(blockPos);
				schmuckEntity.updateWhitelist();
				removed.set(true);
			} else if (!removed.get()) {
				schmuckEntity.getWhitelist().add(blockPos);
				schmuckEntity.updateWhitelist();
				added.set(true);
			}
		});
	}

	/**
	 * Does an action to all Schmucks owned by <code>player</code> nearby.
	 * @param player Player to test with
	 * @param consumer Ran on every Schmuck nearby.
	 */
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
