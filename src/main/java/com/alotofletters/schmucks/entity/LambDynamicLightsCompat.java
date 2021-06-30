package com.alotofletters.schmucks.entity;

import com.alotofletters.schmucks.Schmucks;
import dev.lambdaurora.lambdynlights.api.DynamicLightHandlers;
import dev.lambdaurora.lambdynlights.api.DynamicLightsInitializer;

public class LambDynamicLightsCompat implements DynamicLightsInitializer {
	@Override
	public void onInitializeDynamicLights() {
		DynamicLightHandlers.registerDynamicLightHandler(Schmucks.SCHMUCK, schmuck -> {
			if (schmuck.displaysMinersCap()) {
				return 8;
			}
			return 0;
		});
	}
}
