package com.alotofletters.schmucks.client.gui.screen.ingame;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.client.gui.screen.ingame.widget.ControlWandButtonWidget;
import com.alotofletters.schmucks.client.gui.screen.ingame.widget.ControlWandDropdown;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.item.ControlWandItem;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import static com.alotofletters.schmucks.item.ControlWandItem.ControlAction.*;
import static com.alotofletters.schmucks.item.ControlWandItem.ControlGroup.*;

public class ControlWandScreen extends Screen {
	private static final Identifier TEXTURE = Schmucks.id("textures/gui/schmuck.png");
	protected int backgroundWidth = 176;
	protected int backgroundHeight = 88;

	private final SchmuckEntity schmuck;

	public ControlWandScreen(SchmuckEntity schmuck) {
		super(new TranslatableText("gui.schmucks.control_wand.title"));
		this.schmuck = schmuck;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		if (this.schmuck != null) {
			this.drawBackground(matrices, mouseX, mouseY);
		} else {
			drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 40, 16777215);
		}
		super.render(matrices, mouseX, mouseY, delta);
	}

	public void drawBackground(MatrixStack matrices, int mouseX, int mouseY) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.client.getTextureManager().bindTexture(TEXTURE);
		int i = (this.width - this.backgroundWidth) / 2;
		int j = (this.height - this.backgroundHeight) / 2;
		this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
		InventoryScreen.drawEntity(i + 141, j + 55, 52, (float)(i + 141) - mouseX, (float)(j + 5) - mouseY, this.schmuck);
	}

	public MinecraftClient getClient() {
		return this.client;
	}

	@Override
	protected void init() {
		if (this.schmuck == null) {
			this.createButton(STOP_ALL,
					"stop_all",
					this.width / 2 - 102,
					this.height / 4 + 48 + -16,
					98);
			this.createButton(START_ALL,
					"start_all",
					this.width / 2 + 4,
					this.height / 4 + 48 + -16,
					98);
			this.createButton(STOP_TELEPORT,
					"stop_teleport",
					this.width / 2 - 102,
					this.height / 4 + 72 + -16,
					98);
			this.createButton(START_TELEPORT,
					"start_teleport",
					this.width / 2 + 4,
					this.height / 4 + 72 + -16,
					98);
			this.createButton(STOP_ATTACKING,
					"stop_attacking",
					this.height / 4 + 96 + -16);
			this.addButton(new ControlWandButtonWidget.CancelButtonWidget(this.width / 2 + 80, this.height / 4 + 24 + -17, this));
		} else {
			int i = (this.width - this.backgroundWidth) / 2;
			int j = (this.height - this.backgroundHeight) / 2;
			this.createButton(STOP_ALL,
					"stop_all",
					i + 7,
					j + 14,
					104);
			this.createButton(START_TELEPORT,
					"start_teleport",
					i + 7,
					j + 38,
					104);
			this.addButton(new ControlWandDropdown(this, i + 7, j + 65, THIS, ALL_BUT_THIS, SAME_TOOL, ALL_BUT_SAME_TOOL));
		}
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

	public <T extends AbstractButtonWidget> T addButton(T child) {
		return super.addButton(child);
	}
}
