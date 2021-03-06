package com.alotofletters.schmucks.net;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.item.ControlWandItem.ControlAction;
import com.alotofletters.schmucks.item.ControlWandItem.ControlGroup;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class ControlWandServerChannelHandler implements ServerPlayNetworking.PlayChannelHandler {
	@Override
	public void receive(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		if (!player.isHolding(Schmucks.CONTROL_WAND)) {
			return;
		}
		ControlAction action = buf.readEnumConstant(ControlAction.class);
		ControlGroup group = buf.readEnumConstant(ControlGroup.class);
		SchmuckEntity schmuck = null;
		if (buf.readBoolean()) {
			schmuck = (SchmuckEntity) player.world.getEntityById(buf.readInt());
		}
		switch (action) {
			case STOP_ALL -> this.setSittingNearby(player, group, schmuck, true);
			case START_ALL -> this.setSittingNearby(player, group, schmuck, false);
			case STOP_TELEPORT -> this.setAllowTeleport(player, group, schmuck, false);
			case START_TELEPORT -> this.setAllowTeleport(player, group, schmuck, true);
			case STOP_FOLLOWING -> this.setAllowFollow(player, group, schmuck, false);
			case START_FOLLOWING -> this.setAllowFollow(player, group, schmuck, true);
			case STOP_ATTACKING -> this.forEachSchmuckNearby(player, group, schmuck, this::stopSchmuck);
		}
	}

	public void forEachSchmuckNearby(ServerPlayerEntity player, ControlGroup group, SchmuckEntity schmuck, Consumer<SchmuckEntity> consumer) {
		player.world.getEntitiesByClass(SchmuckEntity.class, player.getBoundingBox().expand(Schmucks.CONFIG.wandRange), entity -> entity.getOwner() == player)
				.stream()
				.filter(fromGroup(group, schmuck))
				.forEach(consumer);
	}

	public Predicate<SchmuckEntity> fromGroup(ControlGroup group, SchmuckEntity entity) {
		return switch (group) {
			case THIS -> schmuckEntity -> schmuckEntity.equals(entity);
			case SAME_TOOL -> schmuckEntity -> {
				if (entity == null) {
					return false;
				}
				return schmuckEntity.getMainHandStack().isItemEqual(entity.getMainHandStack());
			};
			case ALL_NO_TOOL -> schmuckEntity -> schmuckEntity.getMainHandStack().isEmpty();
			case ALL_BUT_THIS -> schmuckEntity -> !schmuckEntity.equals(entity);
			case ALL_BUT_SAME_TOOL -> schmuckEntity -> {
				if (entity == null) {
					return false;
				}
				return !schmuckEntity.getMainHandStack().isItemEqual(entity.getMainHandStack());
			};
			case NOT_STOPPED -> schmuckEntity -> !schmuckEntity.isSitting();
			default -> schmuckEntity -> true;
		};
	}

	public void stopSchmuck(SchmuckEntity entity) {
		entity.setJumping(false);
		entity.getNavigation().stop();
		entity.setTarget(null);
	}

	public void setAllowFollow(ServerPlayerEntity player, ControlGroup group, SchmuckEntity schmuck, boolean follow) {
		forEachSchmuckNearby(player, group, schmuck, entity -> entity.setCanFollow(follow));
	}

	public void setAllowTeleport(ServerPlayerEntity player, ControlGroup group, SchmuckEntity schmuck, boolean teleport) {
		forEachSchmuckNearby(player, group, schmuck, entity -> entity.setCanTeleport(teleport));
	}

	public void setSittingNearby(ServerPlayerEntity player, ControlGroup group, SchmuckEntity schmuck, boolean sitting) {
		forEachSchmuckNearby(player, group, schmuck, entity -> {
			entity.setSitting(sitting);
			this.stopSchmuck(entity);
		});
	}
}
