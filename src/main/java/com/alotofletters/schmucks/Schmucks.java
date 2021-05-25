package com.alotofletters.schmucks;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.item.PureMagicItem;
import com.alotofletters.schmucks.item.SchmuckItem;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public class Schmucks implements ModInitializer {

	public final static String MOD_ID = "schmucks";

	public final static Item PURE_MAGIC = new PureMagicItem("magic");
	public final static Item FIERY_MAGIC = new PureMagicItem("fiery_magic");
	public final static Item SCHMUCK_ITEM = new SchmuckItem(new FabricItemSettings().group(ItemGroup.MISC));
	public final static Item DEAD_SCHMUCK = new Item(new FabricItemSettings().group(ItemGroup.MISC));

	public final static Tag<Item> RAW_MEAT_TAG = TagRegistry.item(id("raw_food"));
	public final static Tag<Item> RAW_MINERAL_TAG = TagRegistry.item(id("raw_mineral"));

	public final static Tag<Block> JOBS_TAG = TagRegistry.block(id("jobs"));
	public final static Tag<Block> FOOD_SMELTERS_TAG = TagRegistry.block(id("food_smelters"));
	public final static Tag<Block> ORE_SMELTERS_TAG = TagRegistry.block(id("ore_smelters"));

	public final static EntityType<SchmuckEntity> SCHMUCK = Registry.register(Registry.ENTITY_TYPE,
			id("schmuck"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, SchmuckEntity::new)
					.dimensions(EntityDimensions.fixed(0.5f, 0.75f))
					.build());

	@Override
	public void onInitialize() {
		Registry.register(Registry.ITEM, id("magic"), Schmucks.PURE_MAGIC);
		Registry.register(Registry.ITEM, id("fiery_magic"), Schmucks.FIERY_MAGIC);
		Registry.register(Registry.ITEM, id("schmuck"), Schmucks.SCHMUCK_ITEM);
		Registry.register(Registry.ITEM, id("dead_schmuck"), Schmucks.DEAD_SCHMUCK);
		FabricDefaultAttributeRegistry.register(SCHMUCK, SchmuckEntity.createSchmuckAttributes());
	}

	public static Identifier id(String name) {
		return new Identifier(Schmucks.MOD_ID, name);
	}
}
