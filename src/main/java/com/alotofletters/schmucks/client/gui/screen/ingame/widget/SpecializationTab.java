package com.alotofletters.schmucks.client.gui.screen.ingame.widget;

import com.alotofletters.schmucks.client.SchmucksClient;
import com.alotofletters.schmucks.client.gui.screen.ingame.ControlWandTabType;
import com.alotofletters.schmucks.entity.specialization.SpecializationsComponent;
import com.alotofletters.schmucks.specialization.Specialization;
import com.alotofletters.schmucks.specialization.SpecializationManager;
import com.alotofletters.schmucks.specialization.client.SpecializationIcon;
import com.google.common.collect.Maps;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SpecializationTab extends ControlWandTab implements SpecializationManager.Listener {
	private final SpecializationsComponent component;

	private final Map<Specialization, SpecializationSubtab> subtabs = Maps.newHashMap();
//	private final Set<SpecializationSubtab> subtabs = Sets.newHashSet();

	private SpecializationSubtab selected;

	public SpecializationTab(int index) {
		super(index);
		this.component = SchmucksClient.getPlayerComponent();
		this.component.startListening(this);
		this.component.getManager().getTabs().forEach(spec -> this.subtabs.put(spec, new SpecializationSubtab(0, spec)));
	}

	@Override
	public SpecializationIcon getIcon() {
		return SpecializationIcon.SPECIALIZATION;
	}

	@Override
	public ControlWandTabType getTabType() {
		return ControlWandTabType.LEFT;
	}

	@Override
	public Identifier getBackground() {
		return UNFILLED_TEXTURE;
	}

	@Override
	public void init(int x, int y) {
		this.subtabs.forEach((spec, tab) -> {
			tab.initAll(x, y);
			this.addDrawableChild(tab);
		});
		this.rearrange();
	}

	private void rearrange() {
		AtomicInteger id = new AtomicInteger();
		this.subtabs.forEach((spec, tab) -> tab.index = id.getAndIncrement());
	}

	@Override
	public void render(MatrixStack stack, int mouseX, int mouseY) {
		this.subtabs.forEach((spec, tab) -> tab.renderTab(stack, this.x, this.y, true));
	}

	@Override
	public void onTabRemoved(Specialization specialization) {
		this.subtabs.remove(specialization);
		if (this.selected != null && this.selected.specialization == specialization) {
			this.subtabs.forEach((specialization1, specializationSubtab) -> this.selected = specializationSubtab);
		}
		this.rearrange();
	}

	@Override
	public void onChildRemoved(Specialization specialization) {

	}

	@Override
	public void onTabAdded(Specialization specialization) {
		SpecializationSubtab tab = new SpecializationSubtab(0, specialization);
		this.subtabs.put(specialization, tab);
		tab.initAll(this.x, this.y);
		this.addDrawableChild(tab);
		if (this.selected == null) {
			this.selected = this.subtabs.get(specialization);
		}
		this.rearrange();
	}

	@Override
	public void onChildAdded(Specialization specialization) {

	}

	@Override
	public void onClear() {

	}
}
