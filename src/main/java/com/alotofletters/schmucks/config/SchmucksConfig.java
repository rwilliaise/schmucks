package com.alotofletters.schmucks.config;

import com.alotofletters.schmucks.Schmucks;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.annotation.SerializedName;

@Config(name = Schmucks.MOD_ID)
public class SchmucksConfig implements ConfigData {
	@ConfigEntry.Gui.Tooltip
	@SerializedName("chaos_mode")
	public boolean chaosMode = false;

	@ConfigEntry.Gui.Tooltip
	@SerializedName("short_temper_chance")
	public float shortTemperChance = 0.05f;

	@ConfigEntry.Gui.Tooltip
	@SerializedName("leather_helmet_chance")
	public float leatherHelmetChance = 0.10f;

	@ConfigEntry.Gui.Tooltip(count = 2)
	@SerializedName("job_range")
	public int jobRange = 8;
}
