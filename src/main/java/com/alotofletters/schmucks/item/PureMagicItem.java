package com.alotofletters.schmucks.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Pure freakin' magic!
 * TODO: add actual magic!
 */
public class PureMagicItem extends Item {

	private final String name;

	public PureMagicItem(String name) {
		super(new FabricItemSettings().group(ItemGroup.MISC).rarity(Rarity.RARE));
		this.name = name;
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		tooltip.add(new TranslatableText(String.format("item.schmucks.%s.tooltip", this.name)).formatted(Formatting.DARK_GRAY, Formatting.ITALIC));
	}
}
