package com.alotofletters.schmucks.client.gui.screen.ingame;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.specialization.client.SpecializationIcon;
import com.alotofletters.schmucks.specialization.client.SpecializationIconLoader;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public enum ControlWandTabType {
	ABOVE(0, 0, 28, 32, 8),
	BELOW(84, 0, 28, 32, 8),
	LEFT(0, 64, 32, 28, 5),
	RIGHT(96, 64, 32, 28, 5);

	private static final Identifier TABS_TEXTURE = new Identifier("textures/gui/advancements/tabs.png");

	private final int u;
	private final int v;
	private final int width;
	private final int height;
	private final int tabCount;

	ControlWandTabType(int u, int v, int width, int height, int tabCount) {
		this.u = u;
		this.v = v;
		this.width = width;
		this.height = height;
		this.tabCount = tabCount;
	}

	public int getTabCount() {
		return this.tabCount;
	}

	public void drawBackground(MatrixStack matrices, DrawableHelper drawableHelper, int x, int y, boolean bl, int k) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, TABS_TEXTURE);
		int l = this.u;
		if (k > 0) {
			l += this.width;
		}

		if (k == this.tabCount - 1) {
			l += this.width;
		}

		int m = bl ? this.v + this.height : this.v;
		drawableHelper.drawTexture(matrices, x + this.getTabX(k), y + this.getTabY(k), l, m, this.width, this.height);
	}

	public void drawIcon(MatrixStack matrices, int x, int y, int index, SpecializationIcon icon) {
		int i = x + this.getTabX(index);
		int j = y + this.getTabY(index);
		switch (this) {
			case ABOVE -> {
				i += 6;
				j += 9;
			}
			case BELOW -> {
				i += 6;
				j += 6;
			}
			case LEFT -> {
				i += 10;
				j += 5;
			}
			case RIGHT -> {
				i += 6;
				j += 5;
			}
		}

		SpecializationIconLoader loader = Schmucks.getIconLoader();
		Sprite sprite = loader.getSprite(icon);
		RenderSystem.setShaderTexture(0, sprite.getAtlas().getId());
		DrawableHelper.drawSprite(matrices, i, j, 1000, 16, 16, sprite);
	}

	public int getTabX(int index) {
		return switch (this) {
			case ABOVE, BELOW -> (this.width + 4) * index;
			case LEFT -> -this.width + 4;
			case RIGHT -> 248;
		};
	}

	public int getTabY(int index) {
		return switch (this) {
			case ABOVE -> -this.height + 4;
			case BELOW -> 136;
			case LEFT, RIGHT -> this.height * index;
		};
	}

	public boolean isClickOnTab(int screenX, int screenY, int index, double mouseX, double mouseY) {
		int i = screenX + this.getTabX(index);
		int j = screenY + this.getTabY(index);
		return mouseX > (double)i && mouseX < (double)(i + this.width) && mouseY > (double)j && mouseY < (double)(j + this.height);
	}
}
