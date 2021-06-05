package com.alotofletters.schmucks.config;

import com.alotofletters.schmucks.Schmucks;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.SerializedName;

@SuppressWarnings("CanBeFinal") // this is invalid, at least for config
@Config(name = Schmucks.MOD_ID)
public class SchmucksConfig implements ConfigData {
	@ConfigEntry.Gui.Tooltip
	@SerializedName("chaos_mode")
	public boolean chaosMode = false;

	@ConfigEntry.Gui.Tooltip
	@ConfigEntry.BoundedDiscrete(min = 0, max = 100)
	@SerializedName("short_temper_chance")
	public Integer shortTemperChance = 5;

	@ConfigEntry.Gui.Tooltip
	@ConfigEntry.BoundedDiscrete(min = 0, max = 100)
	@SerializedName("leather_helmet_chance")
	public Integer leatherHelmetChance = 10;

	@ConfigEntry.Gui.Tooltip(count = 2)
	@ConfigEntry.BoundedDiscrete(min = 1, max = 16)
	@SerializedName("job_range")
	public int jobRange = 8;

	/** The index of the option selected in the entity control wand screen */
	@ConfigEntry.Gui.Excluded
	@SerializedName("wand_index_entity")
	public int wandIndexEntity = 0;

	/** The index of the option selected in the ranged control wand screen */
	@ConfigEntry.Gui.Excluded
	@SerializedName("wand_index_range")
	public int wandIndexRange = 0;

	@ConfigEntry.Gui.Tooltip
	@ConfigEntry.BoundedDiscrete(min = 2, max = 64)
	@SerializedName("wand_range")
	public int wandRange = 32;

	@ConfigEntry.Gui.Tooltip
	@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
	@SerializedName("job_interval")
	public IntervalRange jobInterval = new IntervalRange();

	@ConfigEntry.Gui.Tooltip
	@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
	@SerializedName("mining_interval")
	public IntervalRange miningInterval = new IntervalRange(40, 40);

	public static SchmucksConfig init() {
		return AutoConfig.register(SchmucksConfig.class, GsonConfigSerializer::new)
					.getConfig();
	}

	public static void save() {
		AutoConfig.getConfigHolder(SchmucksConfig.class).save();
	}

	public static class IntervalRange {
		@ConfigEntry.Gui.Tooltip
		@ConfigEntry.BoundedDiscrete(min = 1, max = 800)
		@SerializedName("min")
		public int min;
		@ConfigEntry.Gui.Tooltip
		@ConfigEntry.BoundedDiscrete(min = 1, max = 800)
		@SerializedName("max")
		public int max;

		IntervalRange() {
			this(200, 400);
		}

		IntervalRange(int min, int max) {
			this.min = min;
			this.max = max;
		}
	}
}
