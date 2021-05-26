package com.alotofletters.schmucks.client.gui.screen.ingame;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.item.ControlWandItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.AbstractPressableButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

public class ControlWandScreen extends Screen {
	public ControlWandScreen() {
		super(new TranslatableText("gui.schmucks.control_wand.title"));
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 40, 16777215);
		super.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	protected void init() {
		this.createButton(ControlWandItem.ControlAction.STOP_ALL,
			"stop_all",
			this.width / 2 - 102,
			this.height / 4 + 48 + -16,
			98);
		this.createButton(ControlWandItem.ControlAction.START_ALL,
			"start_all",
			this.width / 2 + 4,
			this.height / 4 + 48 + -16,
			98);
		this.createButton(ControlWandItem.ControlAction.STOP_TELEPORT,
			"stop_teleport",
			this.width / 2 - 102,
			this.height / 4 + 72 + -16,
			98);
		this.createButton(ControlWandItem.ControlAction.START_TELEPORT,
			"start_teleport",
			this.width / 2 + 4,
			this.height / 4 + 72 + -16,
			98);
		this.createButton(ControlWandItem.ControlAction.STOP_ATTACKING,
			"stop_attacking",
			this.height / 4 + 96 + -16);
		this.addButton(new CancelButtonWidget(this.width / 2 + 80, this.height / 4 + 24 + -17));
	}

	private void createButton(ControlWandItem.ControlAction action, String key, int y) {
		this.createButton(action, key, this.width / 2 - 102, y, 204);
	}

	private void createButton(ControlWandItem.ControlAction action, String key, int x, int y, int width) {
		this.addButton(new ButtonWidget(x,
				y,
				width,
				20,
				new TranslatableText(String.format("gui.schmucks.control_wand.%s", key)),
				(button) -> this.sendControlPacket(action)));
	}

	private void sendControlPacket(ControlWandItem.ControlAction action) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeEnumConstant(action);
		ClientPlayNetworking.send(Schmucks.CONTROL_WAND_PACKET_ID, buf);
		this.client.openScreen(null);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	@Environment(EnvType.CLIENT)
	class CancelButtonWidget extends ControlWandScreen.IconButtonWidget {
		public CancelButtonWidget(int x, int y) {
			super(x, y, 112, 220);
		}

		public void onPress() {
			ControlWandScreen.this.client.openScreen(null);
		}

		public void renderToolTip(MatrixStack matrices, int mouseX, int mouseY) {
			ControlWandScreen.this.renderTooltip(matrices, ScreenTexts.CANCEL, mouseX, mouseY);
		}
	}

	@Environment(EnvType.CLIENT)
	abstract static class IconButtonWidget extends ControlWandScreen.BaseButtonWidget {
		private final int u;
		private final int v;

		protected IconButtonWidget(int x, int y, int u, int v) {
			super(x, y);
			this.u = u;
			this.v = v;
		}

		protected void renderExtra(MatrixStack matrices) {
			this.drawTexture(matrices, this.x + 2, this.y + 2, this.u, this.v, 18, 18);
		}
	}

	@Environment(EnvType.CLIENT)
	abstract static class BaseButtonWidget extends AbstractPressableButtonWidget {
		protected BaseButtonWidget(int x, int y) {
			super(x, y, 22, 22, LiteralText.EMPTY);
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
		}

		protected abstract void renderExtra(MatrixStack matrices);
	}
}
