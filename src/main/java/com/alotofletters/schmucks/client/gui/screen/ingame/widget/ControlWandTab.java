package com.alotofletters.schmucks.client.gui.screen.ingame.widget;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.client.gui.screen.ingame.ControlWandTabType;
import com.alotofletters.schmucks.specialization.client.SpecializationIcon;
import com.google.common.collect.Lists;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.List;

public abstract class ControlWandTab extends AbstractParentElement implements Drawable, Selectable {
	public static final Identifier UNFILLED_TEXTURE = new Identifier("textures/gui/advancements/window.png");
	public static final Identifier FILLED_TEXTURE = Schmucks.id("textures/gui/window_filled.png");

	private final List<Element> children = Lists.newArrayList();
	private final List<Selectable> selectables = Lists.newArrayList();
	private final List<Drawable> drawables = Lists.newArrayList();
	public int index;
	protected int x;
	protected int y;

	public ControlWandTab(int index) {
		this.index = index;
	}

	public abstract SpecializationIcon getIcon();

	public abstract ControlWandTabType getTabType();

	public abstract Identifier getBackground();

	public abstract void render(MatrixStack stack, int mouseX, int mouseY);

	public final void render(MatrixStack stack, int mouseX, int mouseY, float delta) {
	}

	public final void draw(MatrixStack stack, int mouseX, int mouseY, float delta) {
		this.render(stack, mouseX, mouseY);
		this.drawables.forEach(drawable -> drawable.render(stack, mouseX, mouseY, delta));
	}

	protected <T extends Element & Drawable & Selectable> T addDrawableChild(T drawableElement) {
		this.drawables.add(drawableElement);
		return this.addSelectableChild(drawableElement);
	}

	protected <T extends Drawable> T addDrawable(T drawable) {
		this.drawables.add(drawable);
		return drawable;
	}

	protected <T extends Element & Selectable> T addSelectableChild(T child) {
		this.children.add(child);
		this.selectables.add(child);
		return child;
	}

	public int getIndex() {
		return index;
	}


	protected void remove(Element child) {
		if (child instanceof Drawable drawable) {
			this.drawables.remove(drawable);
		}

		if (child instanceof Selectable selectable) {
			this.selectables.remove(selectable);
		}

		this.children.remove(child);
	}

	@Override
	public List<? extends Element> children() {
		return this.children;
	}

	public final void initAll(int x, int y) {
		this.x = x;
		this.y = y;
		this.clearChildren();
		this.setFocused(null);
		this.init(x, y);
	}

	public void init(int x, int y) {

	}

	@Override
	public SelectionType getType() {
		return SelectionType.NONE;
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {

	}

	protected void clearChildren() {
		this.drawables.clear();
		this.children.clear();
		this.selectables.clear();
	}

	public void renderTab(MatrixStack stack, int x, int y, boolean selected) {
		ControlWandTabType type = this.getTabType();
		type.drawBackground(stack, this, x, y, selected, this.index);
		type.drawIcon(stack, x, y, this.index, this.getIcon());
	}
}
