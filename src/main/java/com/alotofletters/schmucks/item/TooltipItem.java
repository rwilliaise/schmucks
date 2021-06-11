package com.alotofletters.schmucks.item;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TooltipItem extends Item {

	public TooltipItem(Settings settings) {
		super(settings);
	}

	@Override
	public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
		if (this.getTooltipCount() == 1) {
			tooltip.add(new TranslatableText(this.getTranslationKey(stack) + ".tooltip")
					.formatted(Formatting.GRAY));
		} else if (this.getTooltipCount() > 1) {
			for (int i = 0; i < this.getTooltipCount(); i++) {
				tooltip.add(new TranslatableText(this.getTranslationKey(stack) + ".tooltip[" + i + "]")
						.formatted(Formatting.GRAY));
			}
		}
	}

	public int getTooltipCount() {
		return 1;
	}
}
