package com.alotofletters.schmucks;

import com.alotofletters.schmucks.config.SchmucksConfig;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.entity.WhitelistComponent;
import com.alotofletters.schmucks.entity.specialization.SpecializationsComponent;
import com.alotofletters.schmucks.item.ControlWandItem;
import com.alotofletters.schmucks.item.SchmuckItem;
import com.alotofletters.schmucks.item.TooltipItem;
import com.alotofletters.schmucks.net.SchmucksPackets;
import com.alotofletters.schmucks.server.command.SpecializationCommand;
import com.alotofletters.schmucks.specialization.ServerSpecializationLoader;
import com.alotofletters.schmucks.specialization.client.SpecializationIconLoader;
import com.alotofletters.schmucks.specialization.modifier.Modifiers;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.resource.ResourceType;
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
	public final static Item GLADIATOR_HELMET = new ArmorItem(ArmorMaterials.IRON, EquipmentSlot.HEAD, new FabricItemSettings().group(ItemGroup.TOOLS));
	public final static Item LUMBERJACK_HAT = new ArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.HEAD, new FabricItemSettings().group(ItemGroup.TOOLS));
	public final static Item FARMERS_HAT = new ArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.HEAD, new FabricItemSettings().group(ItemGroup.TOOLS));
	public final static Item MINERS_CAP = new ArmorItem(ArmorMaterials.GOLD, EquipmentSlot.HEAD, new FabricItemSettings().group(ItemGroup.TOOLS));
	public final static Item RANGER_HAT = new ArmorItem(ArmorMaterials.LEATHER, EquipmentSlot.HEAD, new FabricItemSettings().group(ItemGroup.TOOLS));
	public final static Item SCHMUCK_ITEM = new SchmuckItem(new FabricItemSettings().group(ItemGroup.MISC));
	public final static Item CONTROL_WAND = new ControlWandItem(new FabricItemSettings().group(ItemGroup.TOOLS));

	public final static Identifier CONTROL_WAND_PACKET_ID = id("control_wand");

	public final static Tag<Item> RAW_MEAT_TAG = TagRegistry.item(commonId("raw_food"));
	public final static Tag<Item> RAW_MINERAL_TAG = TagRegistry.item(commonId("raw_mineral"));
	public final static Tag<Item> PLANTABLE_TAG = TagRegistry.item(commonId("plantable"));
	public final static Tag<Item> JOB_HATS_TAG = TagRegistry.item(id("job_hats")); // schmucks specific

	public final static Tag<Block> JOBS_TAG = TagRegistry.block(id("jobs")); // schmucks specific
	public final static Tag<Block> FOOD_SMELTERS_TAG = TagRegistry.block(commonId("food_smelters"));
	public final static Tag<Block> ORE_SMELTERS_TAG = TagRegistry.block(commonId("ore_smelters"));
	public final static Tag<Block> TILLABLE_TAG = TagRegistry.block(commonId("tillable"));

	public static final ComponentKey<WhitelistComponent> WHITELIST =
			ComponentRegistry.getOrCreate(id("whitelist"), WhitelistComponent.class);
	public static final ComponentKey<SpecializationsComponent> SPECIALIZATIONS =
			ComponentRegistry.getOrCreate(id("specializations"), SpecializationsComponent.class);

	public final static EntityType<SchmuckEntity> SCHMUCK = Registry.register(Registry.ENTITY_TYPE,
			id("schmuck"),
			FabricEntityTypeBuilder.create(SpawnGroup.MISC, SchmuckEntity::new)
					.dimensions(EntityDimensions.fixed(0.5f, 0.75f))
					.build());

	public static ServerSpecializationLoader LOADER = new ServerSpecializationLoader();

	public static List<BlockPos> getWhitelist(PlayerEntity provider) {
		return Schmucks.getWhitelistComponent(provider).getWhitelist();
	}

	public static List<BlockPos> getWhitelistOrEmpty(PlayerEntity provider) {
		if (provider == null) {
			return new ArrayList<>();
		}
		return Schmucks.WHITELIST.maybeGet(provider).map(WhitelistComponent::getWhitelist).orElse(new ArrayList<>());
	}

	public static WhitelistComponent getWhitelistComponent(PlayerEntity provider) {
		return Schmucks.WHITELIST.get(provider);
	}

	public static Identifier id(String name) {
		return new Identifier(Schmucks.MOD_ID, name);
	}

	public static Identifier commonId(String name) {
		return new Identifier("c", name);
	}

	@Override
	public void onInitialize() {
		ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(Schmucks.LOADER);

		Modifiers.register();

		SchmucksPackets.init();
		Registry.register(Registry.ITEM, id("magic"), Schmucks.PURE_MAGIC);
		Registry.register(Registry.ITEM, id("fiery_magic"), Schmucks.FIERY_MAGIC);
		Registry.register(Registry.ITEM, id("schmuck"), Schmucks.SCHMUCK_ITEM);
		Registry.register(Registry.ITEM, id("dead_schmuck"), Schmucks.DEAD_SCHMUCK);
		Registry.register(Registry.ITEM, id("control_wand"), Schmucks.CONTROL_WAND);
		Registry.register(Registry.ITEM, id("gladiator_helmet"), Schmucks.GLADIATOR_HELMET);
		Registry.register(Registry.ITEM, id("lumberjack"), Schmucks.LUMBERJACK_HAT);
		Registry.register(Registry.ITEM, id("farmer"), Schmucks.FARMERS_HAT);
		Registry.register(Registry.ITEM, id("miners_cap"), Schmucks.MINERS_CAP);
		Registry.register(Registry.ITEM, id("ranger"), Schmucks.RANGER_HAT);

		CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> SpecializationCommand.register(dispatcher));

		FabricDefaultAttributeRegistry.register(SCHMUCK, SchmuckEntity.createSchmuckAttributes());
	}
}
