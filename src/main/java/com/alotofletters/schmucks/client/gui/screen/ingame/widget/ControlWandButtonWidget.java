package com.alotofletters.schmucks.client.gui.screen.ingame.widget;

import com.alotofletters.schmucks.client.gui.screen.ingame.ControlWandScreen;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;

public abstract class ControlWandButtonWidget extends AbstractPressableButtonWidget {
	protected final ControlWandScreen screen;

	protected ControlWandButtonWidget(int x, int y, ControlWandScreen screen) {
		super(x, y, 22, 22, LiteralText.EMPTY);
		this.screen = screen;
	}

	public void renderButton(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		MinecraftClient.getInstance().getTextureManager().bindTexture(new Identifier("textures/gui/container/beacon.png"));
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		int j = 0;
		if (!this.active) {
			j += this.width * 2;
		} else if (this.isHovered()) {
			j += this.width * 3;
		}

		this.drawTexture(matrices, this.x, this.y, j, 219, this.width, this.height);
		this.renderExtra(matrices);
		if (this.isHovered()) {
			this.renderToolTip(matrices, mouseX, mouseY);
		}
	}

	protected abstract void renderExtra(MatrixStack matrices);

	@Environment(EnvType.CLIENT)
	public static class CancelButtonWidget extends ControlWandButtonWidget.IconButtonWidget {
		public CancelButtonWidget(int x, int y, ControlWandScreen screen) {
			super(x, y, 112, 220, screen);
		}

		public void onPress() {
			this.screen.getClient().openScreen(null);
		}

		public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
			this.screen.renderTooltip(matrices, ScreenTexts.CANCEL, mouseX, mouseY);
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
			this.drawTexture(matrices, this.x + 2, this.y + 2, this.u, this.v, 18, 18);
		}
	}
}