package com.alotofletters.schmucks.entity;

import com.alotofletters.schmucks.entity.specialization.SpecializationsImpl;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer;
import dev.onyxstudios.cca.api.v3.entity.RespawnCopyStrategy;

import static com.alotofletters.schmucks.Schmucks.SPECIALIZATIONS;
import static com.alotofletters.schmucks.Schmucks.WHITELIST;

public class PlayerComponents implements EntityComponentInitializer {
	@Override
	public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
		registry.registerForPlayers(WHITELIST, WhitelistComponent.Impl::new, RespawnCopyStrategy.ALWAYS_COPY);
		registry.registerForPlayers(SPECIALIZATIONS, SpecializationsImpl::new, RespawnCopyStrategy.ALWAYS_COPY);
	}
}
