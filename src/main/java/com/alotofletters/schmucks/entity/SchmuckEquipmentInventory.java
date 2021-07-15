package com.alotofletters.schmucks.entity;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;

import java.util.Arrays;

public class SchmuckEquipmentInventory implements Inventory {

	private final static EquipmentSlot[] EQUIPMENT_SLOT_ORDER = new EquipmentSlot[] {
			EquipmentSlot.HEAD,
			EquipmentSlot.CHEST,
			EquipmentSlot.LEGS,
			EquipmentSlot.FEET,
			EquipmentSlot.MAINHAND,
			EquipmentSlot.OFFHAND
	};

	private final SchmuckEntity schmuck;

	public SchmuckEquipmentInventory(SchmuckEntity schmuckEntity) {
		this.schmuck = schmuckEntity;
	}

	@Override
	public int size() {
		return EQUIPMENT_SLOT_ORDER.length;
	}

	@Override
	public boolean isEmpty() {
		return Arrays.stream(EQUIPMENT_SLOT_ORDER).allMatch(slot -> this.schmuck.getEquippedStack(slot).isEmpty());
	}

	@Override
	public ItemStack getStack(int slot) {
		if (slot > this.size()) {
			return ItemStack.EMPTY;
		}
		return this.schmuck.getEquippedStack(EQUIPMENT_SLOT_ORDER[slot]);
	}

	@Override
	public ItemStack removeStack(int slot, int amount) {
		return this.getStack(slot).split(amount);
	}

	@Override
	public ItemStack removeStack(int slot) {
		ItemStack stack = this.getStack(slot);
		this.setStack(slot, ItemStack.EMPTY);
		return stack;
	}

	@Override
	public void setStack(int slot, ItemStack stack) {
		this.schmuck.equipStack(EQUIPMENT_SLOT_ORDER[slot], stack);
	}

	@Override
	public void markDirty() { }

	@Override
	public boolean canPlayerUse(PlayerEntity player) {
		return player == this.schmuck.getOwner();
	}

	@Override
	public void clear() {
		for (int i = 0; i < this.size(); i++) {
			this.setStack(i, ItemStack.EMPTY);
		}
	}
}
