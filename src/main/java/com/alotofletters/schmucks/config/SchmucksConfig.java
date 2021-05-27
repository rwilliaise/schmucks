package com.alotofletters.schmucks.config;

import com.alotofletters.schmucks.Schmucks;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.SerializedName;

@Config(name = Schmucks.MOD_ID)
public class SchmucksConfig implements ConfigData {
	@ConfigEntry.Gui.Tooltip
	@ConfigEntry.Category("schmucks")
	@SerializedName("chaos_mode")
	public boolean chaosMode = false;

	@ConfigEntry.Gui.Tooltip
	@ConfigEntry.BoundedDiscrete(min = 0, max = 100)
	@ConfigEntry.Category("schmucks")
	@SerializedName("short_temper_chance")
	public int shortTemperChance = 5;

	@ConfigEntry.Gui.Tooltip
	@ConfigEntry.BoundedDiscrete(min = 0, max = 100)
	@ConfigEntry.Category("schmucks")
	@SerializedName("leather_helmet_chance")
	public int leatherHelmetChance = 10;

	@ConfigEntry.Gui.Tooltip(count = 2)
	@ConfigEntry.BoundedDiscrete(min = 1, max = 16)
	@ConfigEntry.Category("lag")
	@SerializedName("job_range")
	public int jobRange = 8;

	@ConfigEntry.Gui.Tooltip
	@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
	@ConfigEntry.Category("lag")
	@SerializedName("job_interval")
	public IntervalRange jobInterval = new IntervalRange();

	@ConfigEntry.Gui.Tooltip
	@ConfigEntry.Gui.CollapsibleObject(startExpanded = true)
	@ConfigEntry.Category("lag")
	@SerializedName("mining_interval")
	public IntervalRange miningInterval = new IntervalRange(40, 40);

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
