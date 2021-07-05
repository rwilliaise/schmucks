package com.alotofletters.schmucks.client.gui.screen.ingame.widget;

import com.alotofletters.schmucks.client.gui.screen.ingame.ControlWandTabType;
import com.alotofletters.schmucks.specialization.Specialization;
import com.alotofletters.schmucks.specialization.client.SpecializationIcon;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

public class SpecializationSubtab extends ControlWandTab {
	final Specialization specialization;

	public SpecializationSubtab(int index, Specialization specialization) {
		super(index);
		this.specialization = specialization;
	}

	@Override
	public SpecializationIcon getIcon() {
		return specialization.getDisplay().getIcon();
	}

	@Override
	public ControlWandTabType getTabType() {
		return ControlWandTabType.ABOVE;
	}

	@Override
	public Identifier getBackground() {
		return null;
	}

	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY) {

	}
}
