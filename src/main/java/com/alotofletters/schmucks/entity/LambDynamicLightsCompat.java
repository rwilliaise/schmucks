package com.alotofletters.schmucks.entity;

import com.alotofletters.schmucks.Schmucks;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;
import net.minecraft.entity.EquipmentSlot;

public class LambDynamicLightsCompat implements DynamicLightsInitializer {
	@Override
	public void onInitializeDynamicLights() {
		DynamicLightHandlers.registerDynamicLightHandler(Schmucks.SCHMUCK, schmuck -> {
			if (schmuck.getEquippedStack(EquipmentSlot.HEAD).getItem() == Schmucks.MINERS_CAP) {
				return 12;
			}
			return 0;
		});
	}
}
