package com.alotofletters.schmucks.client.gui.screen.ingame.widget;

import com.alotofletters.schmucks.specialization.client.SpecializationIcon;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.util.math.MatrixStack;

public abstract class ControlWandTab extends DrawableHelper {

	protected final int index;

	public ControlWandTab(int index) {
		this.index = index;
	}

	public abstract SpecializationIcon getIcon();

	public abstract void render(MatrixStack stack);

	public void renderTab(MatrixStack stack, int x, int y, boolean selected, int index) {

	}
}
