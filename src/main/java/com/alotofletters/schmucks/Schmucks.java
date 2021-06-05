package com.alotofletters.schmucks;

import com.alotofletters.schmucks.config.SchmucksConfig;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.item.ControlWandItem;
import com.alotofletters.schmucks.item.SchmuckItem;
import com.alotofletters.schmucks.item.TooltipItem;
import com.alotofletters.schmucks.net.SchmucksPackets;
import com.google.common.collect.Lists;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public class Schmucks implements ModInitializer {

	public final static String MOD_ID = "schmucks";

	public final static SchmucksConfig CONFIG = SchmucksConfig.init();

	public final static Item PURE_MAGIC = new TooltipItem(new FabricItemSettings().group(ItemGroup.MISC).rarity(Rarity.RARE));
	public final static Item FIERY_MAGIC = new TooltipItem(new FabricItemSettings().group(ItemGroup.MISC).rarity(Rarity.RARE));
	public final static Item DEAD_SCHMUCK = new TooltipItem(new FabricItemSettings().group(ItemGroup.MISC));
	public final static Item SCHMUCK_ITEM = new SchmuckItem(new FabricItemSettings().group(ItemGroup.MISC));
	public final static Item CONTROL_WAND = new ControlWandItem(new FabricItemSettings().group(ItemGroup.TOOLS));

	public final static Identifier CONTROL_WAND_PACKET_ID = id("control_wand");

	public final static Tag<Item> RAW_MEAT_TAG = TagRegistry.item(commonId("raw_food"));
	public final static Tag<Item> RAW_MINERAL_TAG = TagRegistry.item(commonId("raw_mineral"));
	public final static Tag<Item> PLANTABLE_TAG = TagRegistry.item(commonId("plantable"));

	public final static Tag<Block> JOBS_TAG = TagRegistry.block(id("jobs")); // schmucks specific
	public final static Tag<Block> FOOD_SMELTERS_TAG = TagRegistry.block(commonId("food_smelters"));
	public final static Tag<Block> ORE_SMELTERS_TAG = TagRegistry.block(commonId("ore_smelters"));
	public final static Tag<Block> TILLABLE_TAG = TagRegistry.block(commonId("tillable"));

	public static final TrackedDataHandler<List<BlockPos>> BLOCK_POS_LIST = new TrackedDataHandler<List<BlockPos>>() {
		public void write(PacketByteBuf data, List<BlockPos> object) {
			data.writeInt(object.size());
			for (BlockPos blockPos : object) {
				data.writeBlockPos(blockPos);
			}
		}

		public List<BlockPos> read(PacketByteBuf packetByteBuf) {
			List<BlockPos> out = new ArrayList<>();
			for (int i = 0; i < packetByteBuf.readInt(); i++) {
				out.add(packetByteBuf.readBlockPos());
			}
			return out;
		}

		public List<BlockPos> copy(List<BlockPos> object) {
			return Lists.newArrayList(object);
		}
	};

	public final static EntityType<SchmuckEntity> SCHMUCK = Registry.register(Registry.ENTITY_TYPE,
			id("schmuck"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, SchmuckEntity::new)
					.dimensions(EntityDimensions.fixed(0.5f, 0.75f))
					.build());

	@Override
	public void onInitialize() {
		SchmucksPackets.init();
		Registry.register(Registry.ITEM, id("magic"), Schmucks.PURE_MAGIC);
		Registry.register(Registry.ITEM, id("fiery_magic"), Schmucks.FIERY_MAGIC);
		Registry.register(Registry.ITEM, id("schmuck"), Schmucks.SCHMUCK_ITEM);
		Registry.register(Registry.ITEM, id("dead_schmuck"), Schmucks.DEAD_SCHMUCK);
		Registry.register(Registry.ITEM, id("control_wand"), Schmucks.CONTROL_WAND);
		FabricDefaultAttributeRegistry.register(SCHMUCK, SchmuckEntity.createSchmuckAttributes());
	}

	public static Identifier id(String name) {
		return new Identifier(Schmucks.MOD_ID, name);
	}
	public static Identifier commonId(String name) {
		return new Identifier("c", name);
	}

	static {
		TrackedDataHandlerRegistry.register(BLOCK_POS_LIST);
	}
}
