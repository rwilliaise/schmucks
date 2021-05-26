package com.alotofletters.schmucks.item;

import com.alotofletters.schmucks.client.gui.screen.ingame.ControlWandScreen;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ControlWandItem extends Item {
	public ControlWandItem(Settings settings) {
		super(settings);
	}

	@Override
	public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
		user.getItemCooldownManager().set(this, 20);
		if (world.isClient) {
			MinecraftClient.getInstance().openScreen(new ControlWandScreen());
		}
		return super.use(world, user, hand);
	}

	public enum ControlAction {
		STOP_ALL,
		START_ALL,
		STOP_TELEPORT,
		START_TELEPORT,
		STOP_ATTACKING,
	}
}
