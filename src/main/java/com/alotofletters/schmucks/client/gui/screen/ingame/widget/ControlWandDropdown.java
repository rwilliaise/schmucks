package com.alotofletters.schmucks.client.gui.screen.ingame.widget;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.client.gui.screen.ingame.ControlWandScreen;
import com.alotofletters.schmucks.config.SchmucksConfig;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.shedaniel.autoconfig.AutoConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.MathHelper;

public class ControlWandDropdown extends AbstractPressableButtonWidget {
	private static final Identifier TEXTURE = Schmucks.id("textures/gui/schmuck.png");
	private final StringIdentifiable[] options;
	private final DropdownListEntry[] buttons;
	private int selected = 0;

	public ControlWandDropdown(ControlWandScreen parent, int x, int y, StringIdentifiable ...options) {
		super(x, y, 162, 20, null);
		this.options = options;
		this.buttons = new DropdownListEntry[options.length];
		this.selected = Schmucks.CONFIG.controlWandSelectedIndex;
		for (int i = 0; i < options.length; i++) {
			buttons[i] = new DropdownListEntry(x, y + (i + 1) * this.height, options[i], i);
			buttons[i].visible = false;
			parent.addButton(buttons[i]);
		}
	}

	@Override
	public void onPress() {
		for (DropdownListEntry button : buttons) {
			button.visible = !button.visible;
		}
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (this.visible) {
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

			MinecraftClient minecraftClient = MinecraftClient.getInstance();
			minecraftClient.getTextureManager().bindTexture(TEXTURE);
			RenderSystem.enableDepthTest();
			TextRenderer textRenderer = minecraftClient.textRenderer;
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, this.alpha);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
			drawTexture(matrices, this.x, this.y, 0, this.isHovered() ? 112 : 92, this.width, this.height);
			int j = this.active ? 16777215 : 10526880;
			if (this.options.length > this.selected) {
				drawCenteredText(matrices,
						textRenderer,
						new TranslatableText(String.format("gui.schmucks.control_wand.%s", this.options[this.selected].asString())),
						this.x + this.width / 2,
						this.y + (this.height - 8) / 2,
						j | MathHelper.ceil(this.alpha * 255.0F) << 24);
			}
		}
	}

	class DropdownListEntry extends AbstractPressableButtonWidget {
		private final StringIdentifiable option;
		private final int index;

		public DropdownListEntry(int x, int y, StringIdentifiable option, int index) {
			super(x, y, 162, 20, new TranslatableText(String.format("gui.schmucks.control_wand.%s", option.asString())));
			this.option = option;
			this.index = index;
		}

		@Override
		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			if (this.visible) {
				this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

				MinecraftClient minecraftClient = MinecraftClient.getInstance();
				minecraftClient.getTextureManager().bindTexture(TEXTURE);
				RenderSystem.enableDepthTest();
				TextRenderer textRenderer = minecraftClient.textRenderer;
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
				drawTexture(matrices, x, y, 0, hovered ? 152 : 132, this.width, this.height);
				drawCenteredText(matrices,
						textRenderer,
						this.getMessage(),
						x + this.width / 2,
						y + (this.height - 8) / 2,
						16777215 | MathHelper.ceil(255.0F) << 24);
			}
		}

		@Override
		public void onPress() {
			for (DropdownListEntry button : buttons) {
				button.visible = !button.visible;
				selected = this.index;
				Schmucks.CONFIG.controlWandSelectedIndex = this.index;
				SchmucksConfig.save();
			}
		}
	}
}
