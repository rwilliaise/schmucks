package com.alotofletters.schmucks.client.gui.screen.ingame;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.client.gui.screen.ingame.widget.*;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.entity.specialization.SpecializationsComponent;
import com.alotofletters.schmucks.item.ControlWandItem;
import com.alotofletters.schmucks.specialization.Specialization;
import com.alotofletters.schmucks.specialization.SpecializationManager;
import com.google.common.collect.Sets;
import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import java.util.Arrays;
import java.util.Set;

/**
 * The Schmuck Staff GUI
 */
public class ControlWandScreen extends Screen {
	public final SchmuckEntity schmuck;
	protected final int backgroundWidth = 252;
	protected final int backgroundHeight = 140;
	private ControlWandDropdown dropdown;

	private final Set<ControlWandTab> tabs = Sets.newHashSet();
	private ControlWandTab selectedTab;

	private SpecializationTab specializationTab;
	private DashboardTab dashboardTab;

	public ControlWandScreen(SchmuckEntity schmuck) {
		super(new TranslatableText("gui.schmucks.control_wand.title"));
		this.schmuck = schmuck;
		assert MinecraftClient.getInstance().player != null;
		if (schmuck != null) {
			this.dashboardTab = new DashboardTab(0, schmuck);
			this.selectedTab = this.dashboardTab;
			tabs.add(dashboardTab);
		} else {
			this.dashboardTab = new DashboardTab(0, null);
			this.specializationTab = new SpecializationTab(1);
			this.selectedTab = this.dashboardTab;
			tabs.add(dashboardTab);
			tabs.add(specializationTab);
		}
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		super.render(matrices, mouseX, mouseY, delta);
		RenderSystem.enableBlend();
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, this.selectedTab.getBackground());
		int i = (this.width - this.backgroundWidth) / 2;
		int j = (this.height - this.backgroundHeight) / 2;
		this.drawTexture(matrices, i, j, 0, 0, this.backgroundWidth, this.backgroundHeight);
		this.drawTabs(matrices, i, j);
		this.selectedTab.draw(matrices, mouseX, mouseY, delta);
		this.children().forEach(element -> {
			if (element instanceof ControlWandButtonWidget) {
				if (((ControlWandButtonWidget) element).isHovered()) {
					((ControlWandButtonWidget) element).renderToolTip(matrices, mouseX, mouseY);
				}
			}
		});
	}

	public void drawTabs(MatrixStack matrices, int i, int j) {
		this.tabs.forEach(tab -> tab.renderTab(matrices, i, j, tab == this.selectedTab));
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int i = (this.width - 252) / 2;
		int j = (this.height - 140) / 2;
		this.tabs.forEach(tab -> {
			if (tab.getTabType().isClickOnTab(i, j, tab.getIndex(), mouseX, mouseY)) {
				this.selectedTab = tab;
			}
		});
		return super.mouseClicked(mouseX, mouseY, button);
	}

	public MinecraftClient getClient() {
		return this.client;
	}

	@Override
	protected void init() {
		int x = (this.width - this.backgroundWidth) / 2;
		int y = (this.height - this.backgroundHeight) / 2;
		this.tabs.forEach(tab -> tab.initAll(x, y));
		this.tabs.forEach(this::addDrawableChild);
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
		this.client.player.closeScreen();
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}

	public void addButton(ClickableWidget child) {
		super.addDrawableChild(child);
	}
}
