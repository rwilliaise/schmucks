package com.alotofletters.schmucks.client.gui.screen.ingame;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.client.gui.screen.ingame.widget.ControlWandButtonWidget;
import com.alotofletters.schmucks.client.gui.screen.ingame.widget.ControlWandDropdown;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.item.ControlWandItem;
import com.alotofletters.schmucks.screen.SchmuckScreenHandler;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

import java.util.Arrays;
import java.util.Iterator;

import static com.alotofletters.schmucks.item.ControlWandItem.ControlAction.*;
import static com.alotofletters.schmucks.item.ControlWandItem.ControlGroup.*;

/**
 * The Schmuck Staff GUI
 */
public class ControlWandScreen extends HandledScreen<SchmuckScreenHandler> {
	private static final Identifier TEXTURE = Schmucks.id("textures/gui/schmuck.png");
	public final SchmuckEntity schmuck;
	private ControlWandDropdown dropdown;

	public ControlWandScreen(SchmuckScreenHandler handler, PlayerInventory inventory, Text title) {
		super(handler, inventory, title);
		this.schmuck = handler.getEntity();
		this.backgroundWidth = 196;
		this.backgroundHeight = 198;
		this.titleX = -1000;
		this.playerInventoryTitleX = -1000;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		if (this.schmuck != null) {
			super.render(matrices, mouseX, mouseY, delta);
			this.drawMouseoverTooltip(matrices, mouseX, mouseY);
		} else {
			super.render(matrices, mouseX, mouseY, delta);
		}
		this.children().stream()
				.filter(element -> element instanceof ControlWandButtonWidget)
				.filter(element -> ((ControlWandButtonWidget) element).isHovered())
				.forEach(element -> ((ControlWandButtonWidget) element).renderToolTip(matrices, mouseX, mouseY));
	}

	@Override
	protected void drawBackground(MatrixStack matrices, float delta, int mouseX, int mouseY) {
		if (this.schmuck != null) {
			RenderSystem.setShader(GameRenderer::getPositionTexShader);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			RenderSystem.setShaderTexture(0, TEXTURE);
			int i = (this.width - this.backgroundWidth) / 2;
			int j = (this.height - this.backgroundHeight) / 2;
			this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
			InventoryScreen.drawEntity(i + 141, j + 55, 52, (float) (i + 141) - mouseX, (float) (j + 5) - mouseY, this.schmuck);
		} else {
			drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 40, 16777215);
		}
	}

	public MinecraftClient getClient() {
		return this.client;
	}

	@Override
	protected void init() {
		super.init();
		int x, y;
		boolean storeFlag = false;
		ControlWandItem.ControlGroup[] options;
		if (this.schmuck == null) {
			this.createGraphicalButton(this.width / 2 - 25,
					this.height / 4 + 24 + -16,
					START_TELEPORT,
					true,
					"start_teleport",
					Items.ENDER_PEARL);
			this.createGraphicalButton(this.width / 2 + 1,
					this.height / 4 + 24 + -16,
					STOP_TELEPORT,
					false,
					"stop_teleport",
					Items.ENDER_PEARL);
			this.createGraphicalButton(this.width / 2 - 25,
					this.height / 4 + 50 + -16,
					START_ALL,
					true,
					"start_all",
					StatusEffects.SLOWNESS);
			this.createGraphicalButton(this.width / 2 + 1,
					this.height / 4 + 50 + -16,
					STOP_ALL,
					false,
					"stop_all",
					StatusEffects.SLOWNESS);
			this.createGraphicalButton(this.width / 2 - 25,
					this.height / 4 + 76 + -16,
					START_FOLLOWING,
					true,
					"start_follow",
					StatusEffects.SPEED);
			this.createGraphicalButton(this.width / 2 + 1,
					this.height / 4 + 76 + -16,
					STOP_FOLLOWING,
					false,
					"stop_follow",
					StatusEffects.SPEED);
			this.createGraphicalButton(this.width / 2 - 12,
					this.height / 4 + 102 + -16,
					STOP_ATTACKING,
					false,
					"stop_attacking",
					Items.IRON_SWORD);
			x = this.width / 2 - 81;
			y = this.height / 4 + 128 + -16;
			storeFlag = true;
			options = new ControlWandItem.ControlGroup[]{ALL, NOT_STOPPED, ALL_NO_TOOL};
		} else {
			int i = (this.width - this.backgroundWidth) / 2;
			int j = (this.height - this.backgroundHeight) / 2;
			x = i + 6;
			y = j + 62;
			options = new ControlWandItem.ControlGroup[]{THIS, ALL_BUT_THIS, SAME_TOOL, ALL_BUT_SAME_TOOL};
			this.createGraphicalButton(i + 11,
					j + 7,
					START_TELEPORT,
					true,
					"start_teleport",
					Items.ENDER_PEARL);
			this.createGraphicalButton(i + 37,
					j + 7,
					STOP_TELEPORT,
					false,
					"stop_teleport",
					Items.ENDER_PEARL);
			this.createGraphicalButton(i + 63,
					j + 7,
					START_ALL,
					true,
					"start_all",
					StatusEffects.SLOWNESS);
			this.createGraphicalButton(i + 89,
					j + 7,
					STOP_ALL,
					false,
					"stop_all",
					StatusEffects.SLOWNESS);
			this.createGraphicalButton(i + 37,
					j + 33,
					STOP_ATTACKING,
					false,
					"stop_attacking",
					Items.IRON_SWORD);
			this.createGraphicalButton(i + 63,
					j + 33,
					START_FOLLOWING,
					true,
					"start_follow",
					StatusEffects.SPEED);
			this.createGraphicalButton(i + 89,
					j + 33,
					STOP_FOLLOWING,
					false,
					"stop_follow",
					StatusEffects.SPEED);
		}
		this.dropdown = new ControlWandDropdown(this, x, y, storeFlag, options);
		this.addButton(dropdown);
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
				(button) -> this.sendControlPacket(action, (ControlWandItem.ControlGroup) this.dropdown.getSelected())));
	}

	private void createGraphicalButton(int x, int y, ControlWandItem.ControlAction action, boolean success, String key, Item item) {
		this.addButton(new ControlWandButtonWidget.ItemOverlayButtonWidget(x,
				y,
				success,
				this,
				item,
				Arrays.asList(new TranslatableText(String.format("gui.schmucks.control_wand.%s", key)),
						new TranslatableText(String.format("gui.schmucks.control_wand.%s.tooltip", key))
								.formatted(Formatting.GRAY)),
				(button) -> this.sendControlPacket(action, (ControlWandItem.ControlGroup) this.dropdown.getSelected())));
	}

	private void createGraphicalButton(int x, int y, ControlWandItem.ControlAction action, boolean success, String key, StatusEffect icon) {
		this.addButton(new ControlWandButtonWidget.IconOverlayButtonWidget(x,
				y,
				success,
				this,
				icon,
				Arrays.asList(new TranslatableText(String.format("gui.schmucks.control_wand.%s", key)),
						new TranslatableText(String.format("gui.schmucks.control_wand.%s.tooltip", key))
								.formatted(Formatting.GRAY)),
				(button) -> this.sendControlPacket(action, (ControlWandItem.ControlGroup) this.dropdown.getSelected())));
	}


	private void sendControlPacket(ControlWandItem.ControlAction action, ControlWandItem.ControlGroup group) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeEnumConstant(action);
		buf.writeEnumConstant(group);
		buf.writeBoolean(this.schmuck != null);
		if (this.schmuck != null) {
			buf.writeInt(this.schmuck.getId());
		}
		ClientPlayNetworking.send(Schmucks.CONTROL_WAND_PACKET_ID, buf);
//		this.client.player.closeScreen();
	}

	@Override
	public void onClose() {
		super.onClose();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	public void addButton(ClickableWidget child) {
		super.addDrawableChild(child);
	}
}
