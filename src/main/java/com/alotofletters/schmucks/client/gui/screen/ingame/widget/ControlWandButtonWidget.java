package com.alotofletters.schmucks.client.gui.screen.ingame.widget;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.client.gui.screen.ingame.ControlWandScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.PressableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.function.Consumer;

/**
 * Used for the labelled buttons on the Schmuck Staff screen.
 */
public abstract class ControlWandButtonWidget extends PressableWidget {
	private static final Identifier TEXTURE = Schmucks.id("textures/gui/schmuck.png");
	private static final Identifier WIDGETS_TEXTURE = Schmucks.id("textures/gui/widgets.png");
	protected final ControlWandScreen screen;

	protected ControlWandButtonWidget(int x, int y, ControlWandScreen screen) {
		super(x, y, 22, 22, LiteralText.EMPTY);
		this.screen = screen;
	}

	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
		int j = 0;
		if (!this.active) {
			j += this.width * 2;
		} else if (this.isHovered()) {
			j += this.width * 3;
		}

		this.drawTexture(matrices, this.x, this.y, j, 219, this.width, this.height);
		this.renderExtra(matrices);
		RenderSystem.setShaderTexture(0, TEXTURE);
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
		this.method_37021(builder);
	}

	protected abstract void renderExtra(MatrixStack matrices);

	public static class IconOverlayButtonWidget extends ControlWandButtonWidget.IconButtonWidget {
		private final StatusEffect icon;
		private final List<Text> message;
		private final Consumer<IconOverlayButtonWidget> onPress;
		private Sprite sprite;

		public IconOverlayButtonWidget(int x, int y, boolean success, ControlWandScreen screen, StatusEffect icon, Consumer<IconOverlayButtonWidget> onPress) {
			this(x, y, success, screen, icon, null, onPress);
		}

		public IconOverlayButtonWidget(int x, int y, boolean success, ControlWandScreen screen, StatusEffect icon, List<Text> message, Consumer<IconOverlayButtonWidget> onPress) {
			super(x, y, success ? 90 : 112, 220, screen);
			this.icon = icon;
			this.message = message;
			this.onPress = onPress;
		}

		@Override
		protected void renderExtra(MatrixStack matrices) {
			StatusEffectSpriteManager statusEffectSpriteManager = screen.getClient().getStatusEffectSpriteManager();
			Sprite sprite = statusEffectSpriteManager.getSprite(this.icon);
			RenderSystem.setShaderTexture(0, sprite.getAtlas().getId());
			drawSprite(matrices, this.x + 1, this.y + 1, this.getZOffset(), 18, 18, sprite);
			RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
			super.renderExtra(matrices);
			RenderSystem.setShaderTexture(0, TEXTURE);
		}

		@Override
		public void onPress() {
			this.onPress.accept(this);
		}

		public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
			if (this.message != null) {
				this.screen.renderTooltip(matrices, this.message, mouseX, mouseY);
			}
		}
	}

	public static class ItemOverlayButtonWidget extends ControlWandButtonWidget.IconButtonWidget {
		private final ItemStack item;
		private final Consumer<ItemOverlayButtonWidget> onPress;
		private final List<Text> tooltip;

		public ItemOverlayButtonWidget(int x, int y, boolean success, ControlWandScreen screen, Item item, Consumer<ItemOverlayButtonWidget> onPress) {
			this(x, y, success, screen, item, null, onPress);
		}

		public ItemOverlayButtonWidget(int x, int y, boolean success, ControlWandScreen screen, Item item, List<Text> message, Consumer<ItemOverlayButtonWidget> onPress) {
			super(x, y, success ? 90 : 112, 220, screen);
			this.item = new ItemStack(item);
			this.onPress = onPress;
			this.tooltip = message;
		}

		@Override
		protected void renderExtra(MatrixStack matrices) {
			MatrixStack matrixStack = RenderSystem.getModelViewStack();
			matrixStack.push();
			matrixStack.translate(0.5f, 0.5f, 0);
			screen.getClient().getItemRenderer().renderInGuiWithOverrides(this.item, this.x + 3, this.y + 2);
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, WIDGETS_TEXTURE);
			RenderSystem.disableDepthTest();
			super.renderExtra(matrices);
			RenderSystem.enableDepthTest();
			RenderSystem.setShaderTexture(0, TEXTURE);
			matrixStack.pop();
		}

		@Override
		public void onPress() {
			this.onPress.accept(this);
		}

		public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
			if (this.tooltip != null) {
				this.screen.renderTooltip(matrices, this.tooltip, mouseX, mouseY);
			}
		}
	}

	@Environment(EnvType.CLIENT)
	abstract static class IconButtonWidget extends ControlWandButtonWidget {
		private final int u;
		private final int v;

		protected IconButtonWidget(int x, int y, int u, int v, ControlWandScreen screen) {
			super(x, y, screen);
			this.u = u;
			this.v = v;
		}

		protected void renderExtra(MatrixStack matrices) {
			this.drawTexture(matrices, this.x + 2, this.y + 1, this.u, this.v, 18, 18);
		}
	}
}