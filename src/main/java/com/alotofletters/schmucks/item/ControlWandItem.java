package com.alotofletters.schmucks.item;

import com.alotofletters.schmucks.client.gui.screen.ingame.ControlWandScreen;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.*;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ControlWandItem extends Item {
	public ControlWandItem(Settings settings) {
		super(settings.maxCount(1));
	}

//	@Override
//	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
//		user.getItemCooldownManager().set(this, 20);
//		if (world.isClient) {
//			this.openScreen(null);
//			return TypedActionResult.success(user.getStackInHand(hand));
//		}
//		return super.use(world, user, hand);
//	}

	@Override
	public ActionResult useOnEntity(ItemStack stack, PlayerEntity user, LivingEntity entity, Hand hand) {
		if (entity instanceof SchmuckEntity ) {
			if (entity.world.isClient) {
				this.openScreen((SchmuckEntity) entity);
				return ActionResult.SUCCESS;
			}
		}
		return super.useOnEntity(stack, user, entity, hand);
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(new TranslatableText("item.schmucks.control_wand.tooltip").formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
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
		STOP_ATTACKING,
	}

	public enum ControlGroup implements StringIdentifiable {
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
