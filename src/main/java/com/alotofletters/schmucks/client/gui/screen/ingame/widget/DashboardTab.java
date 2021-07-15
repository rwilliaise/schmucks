package com.alotofletters.schmucks.client.gui.screen.ingame.widget;

import com.alotofletters.schmucks.client.gui.screen.ingame.ControlWandTabType;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.specialization.client.SpecializationIcon;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.CheckboxWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;


public class DashboardTab extends ControlWandTab {
	private final SchmuckEntity schmuck;

	public DashboardTab(int index, SchmuckEntity schmuck) {
		super(index);
		this.schmuck = schmuck;
	}

	@Override
	public SpecializationIcon getIcon() {
		return SpecializationIcon.DASHBOARD;
	}

	@Override
	public ControlWandTabType getTabType() {
		return ControlWandTabType.LEFT;
	}

	@Override
	public Identifier getBackground() {
		return FILLED_TEXTURE;
	}

	@Override
	public void init(int x, int y) {
		this.addDrawableChild(new CheckboxWidget(x + 7, y + 65, 150, 20, new LiteralText("STOP!!!"), false));
	}

	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY) {
		if (schmuck != null)
			InventoryScreen.drawEntity(this.x + 141, this.y + 55, 52, (float) (this.x + 141) - mouseX, (float) (this.y + 5) - mouseY, this.schmuck);
	}
}
