package com.alotofletters.schmucks.entity.specialization;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.specialization.ServerSpecializationLoader;
import com.alotofletters.schmucks.specialization.Specialization;
import com.alotofletters.schmucks.specialization.SpecializationManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.advancement.Advancement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SpecializationsImpl implements SpecializationsComponent {
	private final Map<Specialization, Integer> levels = Maps.newHashMap();
	private final Set<Specialization> visible = Sets.newLinkedHashSet();
	private final Set<Specialization> dirty = Sets.newLinkedHashSet();
	private final PlayerEntity provider;

	@Environment(EnvType.CLIENT)
	private final SpecializationManager manager = new SpecializationManager();

	public SpecializationsImpl(PlayerEntity provider) {
		this.provider = provider;
	}

	@Override
	public void reload(ServerSpecializationLoader loader) {
		this.visible.clear();
		this.dirty.clear();
	}

	@Override
	public void readFromNbt(NbtCompound tag) {
		tag.getList("Levels", 10).forEach(element -> {
			if (element instanceof NbtCompound compound) {
				Identifier id = new Identifier(compound.getString("Id"));
				int level = compound.getInt("Level");
				Specialization specialization = Schmucks.LOADER.get(id);
				this.levels.put(specialization, level);
			}
		});
	}

	@Override
	public void writeToNbt(NbtCompound tag) {
		NbtList list = new NbtList();
		this.levels.forEach((specialization, integer) -> {
			NbtCompound compound = new NbtCompound();
			compound.putString("Id", specialization.getId().toString());
			compound.putInt("Level", integer);
			list.add(compound);
		});
		tag.put("Levels", list);
	}

	@Override
	public void writeSyncPacket(PacketByteBuf buf, ServerPlayerEntity recipient) {
		if (recipient != this.provider) {
			return;
		}
		ImmutableMap.Builder<Identifier, Specialization.Raw> builder = ImmutableMap.builder();
		ImmutableMap.Builder<Identifier, Integer> levelsBuilder = ImmutableMap.builder();
		Set<Identifier> toRemove = this.dirty.stream().filter(spec -> !this.visible.contains(spec)).map(Specialization::getId).collect(Collectors.toUnmodifiableSet());

		this.dirty.forEach(spec -> builder.put(spec.getId(), spec.toRaw()));
		this.levels.forEach((specialization, level) -> levelsBuilder.put(specialization.getId(), level));

		buf.writeMap(builder.build(), PacketByteBuf::writeIdentifier, (byteBuf, raw) -> raw.toPacket(byteBuf));
		buf.writeMap(levelsBuilder.build(), PacketByteBuf::writeIdentifier, PacketByteBuf::writeVarInt);
		buf.writeCollection(toRemove, PacketByteBuf::writeIdentifier);
	}

	@Override
	public void applySyncPacket(PacketByteBuf buf) {
		Map<Identifier, Specialization.Raw> toEarn = buf.readMap(PacketByteBuf::readIdentifier, Specialization.Raw::fromPacket);
		Map<Identifier, Integer> levels = buf.readMap(PacketByteBuf::readIdentifier, PacketByteBuf::readVarInt);
		Set<Identifier> toRemove = buf.readCollection(Sets::newLinkedHashSetWithExpectedSize, PacketByteBuf::readIdentifier);
		manager.removeAll(toRemove);
		manager.load(toEarn);
		levels.forEach((id, level) -> this.levels.put(manager.get(id), level));
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
