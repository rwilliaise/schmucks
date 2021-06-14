package com.alotofletters.schmucks.entity.specialization;

import com.alotofletters.schmucks.specialization.Specialization;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.advancement.Advancement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Set;

public class SpecializationsImpl implements SpecializationsComponent {
	private final Map<Specialization, Integer> levels = Maps.newHashMap();
	private final Set<Specialization> visible = Sets.newLinkedHashSet();
	private final Set<Specialization> dirty = Sets.newLinkedHashSet();
	private final PlayerEntity provider;

	public SpecializationsImpl(PlayerEntity provider) {
		this.provider = provider;
	}

	@Override
	public void readFromNbt(NbtCompound tag) {

	}

	@Override
	public void writeToNbt(NbtCompound tag) {

	}

	@Override
	public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
		ImmutableMap.Builder<Identifier, Specialization.Raw> builder = ImmutableMap.builder();
		this.dirty.forEach(spec -> builder.put(spec.getId(), spec.toRaw()));
		buf.writeMap(builder.build(), PacketByteBuf::writeIdentifier, (byteBuf, raw) -> {});
		SpecializationsComponent.super.writeSyncPacket(buf, recipient);
	}

	@Override
	public void applySyncPacket(PacketByteBuf buf) {
		SpecializationsComponent.super.applySyncPacket(buf);
	}

	@Override
	public Set<Specialization> getVisible() {
		return this.visible;
	}

	@Override
	public Set<Specialization> getDirty() {
		return this.dirty;
	}

	@Override
	public Map<Specialization, Integer> getLevels() {
		return this.levels;
	}

	@Override
	public boolean canSee(Specialization spec) {
		return this.getVisible().contains(spec);
	}

	@Override
	public void serverTick() {

	}
}
