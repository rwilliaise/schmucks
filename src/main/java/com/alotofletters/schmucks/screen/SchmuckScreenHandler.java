package com.alotofletters.schmucks.screen;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.mojang.datafixers.util.Pair;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.function.Supplier;

public class SchmuckScreenHandler extends ScreenHandler {

	public static final Identifier BLOCK_ATLAS_TEXTURE = new Identifier("textures/atlas/blocks.png");
	public static final Identifier EMPTY_HELMET_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_helmet");
	public static final Identifier EMPTY_CHESTPLATE_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_chestplate");
	public static final Identifier EMPTY_LEGGINGS_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_leggings");
	public static final Identifier EMPTY_BOOTS_SLOT_TEXTURE = new Identifier("item/empty_armor_slot_boots");
	public static final Identifier EMPTY_OFFHAND_ARMOR_SLOT = new Identifier("item/empty_armor_slot_shield");
	static final Identifier[] EMPTY_ARMOR_SLOT_TEXTURES = new Identifier[]{EMPTY_BOOTS_SLOT_TEXTURE, EMPTY_LEGGINGS_SLOT_TEXTURE, EMPTY_CHESTPLATE_SLOT_TEXTURE, EMPTY_HELMET_SLOT_TEXTURE};;
	private static final EquipmentSlot[] EQUIPMENT_SLOT_ORDER = new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

	private final Inventory inventory;
	private final Inventory equipmentInventory;
	private final SchmuckEntity entity;

	public SchmuckScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
		this(syncId, inventory, new SimpleInventory(5), new SimpleInventory(6), ((Supplier<SchmuckEntity>)() -> {
			if (buf.readBoolean()) {
				return (SchmuckEntity) inventory.player.world.getEntityById(buf.readVarInt());
			}
			return null;
		}).get());
	}

	public SchmuckScreenHandler(int syncId, PlayerInventory playerInventory, Inventory inventory, Inventory equipmentInventory, SchmuckEntity entity) {
		super(Schmucks.SCHMUCK_SCREEN_HANDLER, syncId);
		this.inventory = inventory;
		this.equipmentInventory = equipmentInventory;
		this.entity = entity;

		if (this.entity != null) {
			int m;
			int l;

			for (m = 0; m < 5; ++m) {
				this.addSlot(new Slot(inventory, m, 54 + m * 18, 87) {
					public boolean canTakeItems(PlayerEntity playerEntity) {
						ItemStack itemStack = this.getStack();
						return !itemStack.isIn(ItemTags.ARROWS);
					}
				});
			}

			for(m = 0; m < 4; ++m) {
				final EquipmentSlot equipmentSlot = EQUIPMENT_SLOT_ORDER[m];
				this.addSlot(new Slot(equipmentInventory, m, 174, 8 + m * 18) {
					public int getMaxItemCount() {
						return 1;
					}

					public boolean canInsert(ItemStack stack) {
						return equipmentSlot == MobEntity.getPreferredEquipmentSlot(stack);
					}

					public boolean canTakeItems(PlayerEntity playerEntity) {
						ItemStack itemStack = this.getStack();
						return (itemStack.isEmpty() || playerEntity.isCreative() || !EnchantmentHelper.hasBindingCurse(itemStack)) && super.canTakeItems(playerEntity);
					}

					public Pair<Identifier, Identifier> getBackgroundSprite() {
						return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, EMPTY_ARMOR_SLOT_TEXTURES[equipmentSlot.getEntitySlotId()]);
					}
				});
			}

			this.addSlot(new Slot(equipmentInventory, 4, 150, 87));

			this.addSlot(new Slot(equipmentInventory, 5, 174, 87) {
				public Pair<Identifier, Identifier> getBackgroundSprite() {
					return Pair.of(PlayerScreenHandler.BLOCK_ATLAS_TEXTURE, PlayerScreenHandler.EMPTY_OFFHAND_ARMOR_SLOT);
				}
			});

			//the player inventory
			for (m = 0; m < 3; ++m) {
				for (l = 0; l < 9; ++l) {
					this.addSlot(new Slot(playerInventory, l + m * 9 + 9, 18 + l * 18, 116 + m * 18));
				}
			}
			//The player Hotbar
			for (m = 0; m < 9; ++m) {
				this.addSlot(new Slot(playerInventory, m, 18 + m * 18, 174));
			}
		}
	}

	@Override
	public boolean canUse(PlayerEntity player) {
		return this.entity == null || this.entity.getOwner() == player;
	}

	public SchmuckEntity getEntity() {
		return entity;
	}
}
