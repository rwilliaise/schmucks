package com.alotofletters.schmucks.client.gui.screen.ingame.widget;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.client.gui.screen.ingame.ControlWandScreen;
import com.alotofletters.schmucks.config.SchmucksConfig;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.StringIdentifiable;
import net.minecraft.util.math.MathHelper;

/**
 * Used for choosing a control group for the Schmuck Staff.
 */
public class ControlWandDropdown extends PressableWidget {
	private static final Identifier TEXTURE = Schmucks.id("textures/gui/schmuck.png");
	private static final Identifier WIDGETS_TEXTURE = Schmucks.id("textures/gui/widgets.png");
	private final boolean storeFlag;
	private final StringIdentifiable[] options;
	private final DropdownListEntry[] buttons;
	private int selected;

	private boolean isVisible = false;

	public ControlWandDropdown(ControlWandScreen parent, int x, int y, boolean storeFlag, StringIdentifiable... options) {
		super(x, y, 162, 20, null);
		this.storeFlag = storeFlag;
		this.options = options;
		this.buttons = new DropdownListEntry[options.length];
		if (this.storeFlag) {
			this.selected = Schmucks.CONFIG.wandIndexRange;
		} else {
			this.selected = Schmucks.CONFIG.wandIndexEntity;
		}
		for (int i = 0; i < options.length; i++) {
			buttons[i] = new DropdownListEntry(x, y + (i + 1) * this.height, options[i], i);
			buttons[i].visible = false;
			parent.addButton(buttons[i]);
		}
		this.recalculatePositions();
	}

	public void recalculatePositions() {
		int j = 0;
		for (int i = 0; i < buttons.length; i++) {
			if (i != selected) {
				buttons[i].y = this.y + (j + 1) * this.height;
				j++;
			}
			buttons[i].visible = false;
		}
	}

	public StringIdentifiable getSelected() {
		return this.options[this.selected];
	}

	@Override
	public void onPress() {
		isVisible = !isVisible;
		for (int i = 0; i < buttons.length; i++) {
			if (i != selected) {
				buttons[i].visible = isVisible;
			}
		}
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (this.visible) {
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;

			MinecraftClient minecraftClient = MinecraftClient.getInstance();
			RenderSystem.enableDepthTest();
			TextRenderer textRenderer = minecraftClient.textRenderer;
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1, 1, 1, this.alpha);
			RenderSystem.setShaderTexture(0, ControlWandDropdown.WIDGETS_TEXTURE);
			RenderSystem.enableBlend();
			RenderSystem.defaultBlendFunc();
			RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
			drawTexture(matrices, this.x, this.y, 0, this.isHovered() ? 130 : 110, this.width, this.height);
			int j = this.active ? 16777215 : 10526880;
			if (this.options.length > this.selected) {
				drawCenteredText(matrices,
						textRenderer,
						new TranslatableText(String.format("gui.schmucks.control_wand.%s", this.options[this.selected].asString())),
						this.x + this.width / 2,
						this.y + (this.height - 8) / 2,
						j | MathHelper.ceil(this.alpha * 255.0F) << 24);
			}
			RenderSystem.setShaderTexture(0, TEXTURE);
		}
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
		this.method_37021(builder);
	}

	class DropdownListEntry extends PressableWidget {
		private final int index;

		public DropdownListEntry(int x, int y, StringIdentifiable option, int index) {
			super(x, y, 162, 20, new TranslatableText(String.format("gui.schmucks.control_wand.%s", option.asString())));
			this.index = index;
		}

		@Override
		public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
			if (this.visible) {
				this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
				boolean isBottom = selected != this.index && this.index == buttons.length - 1 || selected == buttons.length - 1 && this.index == buttons.length - 2;

				MinecraftClient minecraftClient = MinecraftClient.getInstance();
				RenderSystem.enableDepthTest();
				TextRenderer textRenderer = minecraftClient.textRenderer;
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderColor(1, 1, 1, this.alpha);
				RenderSystem.setShaderTexture(0, ControlWandDropdown.WIDGETS_TEXTURE);
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				RenderSystem.blendFunc(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
				drawTexture(matrices, x, y, 0, hovered ? 190 : (isBottom ? 170 : 150), this.width, this.height);
				drawCenteredText(matrices,
						textRenderer,
						this.getMessage(),
						x + this.width / 2,
						y + (this.height - 8) / 2,
						16777215 | MathHelper.ceil(255.0F) << 24);
				RenderSystem.setShaderTexture(0, TEXTURE);
			}
		}

		@Override
		public void onPress() {
			isVisible = false;
			for (int i = 0; i < buttons.length; i++) {
				if (i != selected) {
					buttons[i].visible = !buttons[i].visible;
				}
				selected = this.index;
				recalculatePositions();

				if (storeFlag) {
					Schmucks.CONFIG.wandIndexRange = this.index;
				} else {
					Schmucks.CONFIG.wandIndexEntity = this.index;
				}
				SchmucksConfig.save();
			}
		}

		@Override
		public void appendNarrations(NarrationMessageBuilder builder) {
			this.method_37021(builder);
		}
	}
}
