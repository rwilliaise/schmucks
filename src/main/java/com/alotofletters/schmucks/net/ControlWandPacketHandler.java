package com.alotofletters.schmucks.net;


import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.item.ControlWandItem;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Consumer;

import static com.alotofletters.schmucks.item.ControlWandItem.*;

public class ControlWandPacketHandler implements ServerPlayNetworking.PlayChannelHandler {
	@Override
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		if (!player.isHolding(Schmucks.CONTROL_WAND)) {
			return;
		}
		if (player.getItemCooldownManager().getCooldownProgress(Schmucks.CONTROL_WAND, 0) != 0) {
			return;
		}
		ControlAction action = buf.readEnumConstant(ControlWandItem.ControlAction.class);
		switch (action) {
			case STOP_ALL:
				this.setSittingNearby(player, true);
				break;
			case START_ALL:
				this.setSittingNearby(player, false);
				break;
			case STOP_TELEPORT:
				this.setAllowTeleport(player, false);
				break;
			case START_TELEPORT:
				this.setAllowTeleport(player, true);
				break;
			case STOP_ATTACKING:
				this.forEachSchmuckNearby(player, this::stopSchmuck);
				break;
		}
	}

	public void forEachSchmuckNearby(ServerPlayerEntity player, Consumer<SchmuckEntity> consumer) {
		player.world.getEntitiesByClass(SchmuckEntity.class, player.getBoundingBox().expand(10.0d), entity -> entity.getOwner() == player)
				.forEach(consumer);
	}

	public void stopSchmuck(SchmuckEntity entity) {
		entity.setJumping(false);
		entity.getNavigation().stop();
		entity.setTarget(null);
	}

	public void setAllowTeleport(ServerPlayerEntity player, boolean teleport) {
		forEachSchmuckNearby(player, entity -> entity.setCanTeleport(teleport));
	}

	public void setSittingNearby(ServerPlayerEntity player, boolean sitting) {
		forEachSchmuckNearby(player, entity -> {
				entity.setSitting(sitting);
				this.stopSchmuck(entity);
			});
	}
}
