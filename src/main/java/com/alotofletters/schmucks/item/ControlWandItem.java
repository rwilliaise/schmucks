package com.alotofletters.schmucks.item;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.client.gui.screen.ingame.ControlWandScreen;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.entity.WhitelistComponent;
import com.alotofletters.schmucks.screen.SchmuckScreenHandler;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Set;

public class ControlWandItem extends Item {
	public ControlWandItem(Settings settings) {
		super(settings.maxCount(1));
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		if (user.isSneaking()) {
			return super.use(world, user, hand);
		}
		HitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.NONE);
		if (hitResult.getType() == HitResult.Type.BLOCK) {
			return TypedActionResult.pass(user.getStackInHand(hand));
		}
		user.getItemCooldownManager().set(this, 20);
		if (!world.isClient) {
			user.openHandledScreen(new ExtendedScreenHandlerFactory() {
				@Override
				public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
					buf.writeBoolean(false);
				}

				@Override
				public Text getDisplayName() {
					return new TranslatableText("gui.schmucks.control_wand.title");
				}

				@Override
				public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
					return new SchmuckScreenHandler(syncId, inv, new SimpleInventory(5), new SimpleInventory(6), null);
				}
			});
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
		if (player == null) {
			return ActionResult.PASS;
		}
		BlockPos blockPos = context.getBlockPos();
		BlockState blockState = world.getBlockState(blockPos);
		BlockEntity blockEntity = world.getBlockEntity(blockPos);
		if (player.isSneaking() && blockEntity != null) {
			return updateFromContext(context, "%s");
		} else if (player.isSneaking()) {
			if (blockState.isOf(Blocks.FARMLAND) || blockState.isIn(Schmucks.TILLABLE_TAG)) {
				return updateFromContext(context, "%s_farmland");
			} else if (blockState.isIn(BlockTags.LOGS) || blockState.getBlock() instanceof SaplingBlock) {
				if (this.cascade(world, (ServerPlayerEntity) player, blockPos)) {
					player.sendMessage(new TranslatableText("item.schmucks.control_wand.added_lumber", blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
				} else {
					player.sendMessage(new TranslatableText("item.schmucks.control_wand.removed_lumber", blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
				}
				return ActionResult.SUCCESS;
			} else if (blockState.getBlock() instanceof PlantBlock plant && plant.canPlantOnTop(Blocks.FARMLAND.getDefaultState(), world, blockPos)) {
				WhitelistComponent component = Schmucks.getWhitelistComponent(player);
				if (component.containsWhiteList(blockPos.down())) {
					player.sendMessage(new TranslatableText("item.schmucks.control_wand.removed_farmland", blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
					component.removeWhitelist(blockPos.down());
				} else {
					player.sendMessage(new TranslatableText("item.schmucks.control_wand.added_farmland", blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
					component.addWhitelist(blockPos.down());
				}
				component.sync();
				return ActionResult.SUCCESS;
			} else if (blockState.getBlock() instanceof GourdBlock && checkNeighbors(world, (ServerPlayerEntity) player, blockPos)) {
				WhitelistComponent component = Schmucks.getWhitelistComponent(player);
				BlockPos pos = getNeighbor(world, (ServerPlayerEntity) player, blockPos);
				if (component.containsWhiteList(pos)) {
					player.sendMessage(new TranslatableText("item.schmucks.control_wand.removed_farmland", pos.getX(), pos.getY(), pos.getZ()), true);
					component.removeWhitelist(pos);
				} else {
					player.sendMessage(new TranslatableText("item.schmucks.control_wand.added_farmland", pos.getX(), pos.getY(), pos.getZ()), true);
					component.addWhitelist(pos);
				}
				component.sync();
				return ActionResult.SUCCESS;
			} else {
				WhitelistComponent component = Schmucks.getWhitelistComponent(player);
				if (component.containsWhiteList(blockPos)) {
					component.removeWhitelist(blockPos);
					component.sync();
					player.sendMessage(new TranslatableText("item.schmucks.control_wand.removed", blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
					return ActionResult.SUCCESS;
				}
			}
		}
		return super.useOnBlock(context);
	}

	private boolean cascade(World world, ServerPlayerEntity player, BlockPos start) {
		Set<BlockPos> cascading = Sets.newConcurrentHashSet();
		Set<BlockPos> visited = Sets.newConcurrentHashSet();
		cascading.add(start);
		visited.add(start);
		WhitelistComponent component = Schmucks.getWhitelistComponent(player);
		boolean newState = !component.containsWhiteList(start);
		while (cascading.size() > 0) {
			cascading.forEach(pos -> {
				if (newState && !component.containsWhiteList(pos)) {
					component.addWhitelist(pos);
				} else if (!newState && component.containsWhiteList(pos)) {
					component.removeWhitelist(pos);
				}
				cascading.remove(pos);
				Direction[] sides = new Direction[] {Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.UP, Direction.DOWN};
				for (Direction side : sides) {
					BlockPos check = pos.offset(side, 1);
					BlockState state = world.getBlockState(check);
					if (state.isIn(BlockTags.LOGS) && !visited.contains(check)) {
						cascading.add(check);
						visited.add(check);
					}
				}
			});
		}
		component.sync();
		return newState;
	}

	private boolean checkNeighbors(World world, ServerPlayerEntity player, BlockPos pos) {
		return getNeighbor(world, player, pos) != null;
	}

	private BlockPos getNeighbor(World world, ServerPlayerEntity player, BlockPos pos) {
		Direction[] sides = new Direction[] {Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH};
		for (Direction side : sides) {
			BlockPos check = pos.offset(side, 1);
			BlockState state = world.getBlockState(check);
			if (state.getBlock() instanceof AttachedStemBlock && state.get(AttachedStemBlock.FACING) == side.getOpposite()) {
				return check.down();
			}
		}
		return null;
	}


	private ActionResult updateFromContext(ItemUsageContext context, String fmt) {
		PlayerEntity player = context.getPlayer();
		if (player == null) {
			return ActionResult.PASS;
		}
		BlockPos blockPos = context.getBlockPos();
		WhitelistComponent component = Schmucks.getWhitelistComponent(player);
		if (component.containsWhiteList(blockPos)) {
			player.sendMessage(new TranslatableText(String.format("item.schmucks.control_wand." + fmt, "removed"), blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
			component.removeWhitelist(blockPos);
		} else {
			player.sendMessage(new TranslatableText(String.format("item.schmucks.control_wand." + fmt, "added"), blockPos.getX(), blockPos.getY(), blockPos.getZ()), true);
			component.addWhitelist(blockPos);
		}
		component.sync();
		return ActionResult.SUCCESS;
	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		if (entity instanceof SchmuckEntity schmuck) {
			if (schmuck.getOwner() == user) {
				this.openScreen(schmuck, user);
				return ActionResult.SUCCESS;
			}
		}
		return super.useOnEntity(stack, user, entity, hand);
	}

	private void openScreen(SchmuckEntity entity, PlayerEntity player) {
		if (!entity.world.isClient) {
			player.openHandledScreen(entity);
		}
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
