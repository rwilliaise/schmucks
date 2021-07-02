package com.alotofletters.schmucks.entity.specialization;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.specialization.ServerSpecializationLoader;
import com.alotofletters.schmucks.specialization.Specialization;
import com.alotofletters.schmucks.specialization.SpecializationManager;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SpecializationsImpl implements SpecializationsComponent {
	private final Map<Specialization, Integer> levels = Maps.newHashMap();
	private final Set<Specialization> visible = Sets.newLinkedHashSet();
	private final Set<Specialization> levelUpdates = Sets.newLinkedHashSet();
	private final PlayerEntity provider;

	@Environment(EnvType.CLIENT)
	private final SpecializationManager manager = new SpecializationManager();

	public SpecializationsImpl(PlayerEntity provider) {
		this.provider = provider;
	}

	@Override
	public void reload(ServerSpecializationLoader loader) {
		this.visible.clear();
		this.levelUpdates.clear();
	}

	@Override
	public void startListening(SpecializationManager.Listener listener) {
		this.manager.setListener(listener);
	}

	@Override
	public void readFromNbt(NbtCompound tag) {
		tag.getList("Levels", 10).forEach(element -> {
			if (element instanceof NbtCompound compound) {
				Identifier id = new Identifier(compound.getString("Id"));
				int level = compound.getInt("Level");
				Specialization specialization = Schmucks.LOADER.get(id);
				this.levels.put(specialization, level);
				this.levelUpdates.add(specialization);
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

		Schmucks.LOADER.popReplicationQueue().forEach(builder::put);
		this.levels.forEach((specialization, level) -> levelsBuilder.put(specialization.getId(), level));

		buf.writeMap(builder.build(), PacketByteBuf::writeIdentifier, (byteBuf, raw) -> raw.toPacket(byteBuf));
		buf.writeMap(levelsBuilder.build(), PacketByteBuf::writeIdentifier, PacketByteBuf::writeVarInt);
	}

	@Override
	public void applySyncPacket(PacketByteBuf buf) {
		Map<Identifier, Specialization.Raw> toLoad = buf.readMap(PacketByteBuf::readIdentifier, Specialization.Raw::fromPacket);
		Map<Identifier, Integer> levels = buf.readMap(PacketByteBuf::readIdentifier, PacketByteBuf::readVarInt);
		if (toLoad.size() > 0) {
			manager.removeAll(toLoad.keySet());
			manager.load(toLoad);
		}
		levels.forEach((id, level) -> this.levels.put(manager.get(id), level));
	}

	public void upgradeLevel(Specialization spec) {
		Integer level = this.levels.get(spec);
		if (level == null) {
			level = 0;
		}
		this.setLevel(spec, level + 1);
	}

	@Override
	public void apply() {
		this.levelUpdates.forEach(spec -> {
			int lvl = this.levels.get(spec);
			spec.getModifier().applyAll(this.provider, lvl);
		});
		((ServerWorld)this.provider.world).getEntitiesByType(
				TypeFilter.instanceOf(SchmuckEntity.class),
				entity -> this.provider.equals(entity.getOwner()))
				.forEach(SchmuckEntity::refreshGoals);
		Schmucks.SPECIALIZATIONS.sync(this.provider);
		this.levelUpdates.clear();
	}

	@Override
	public void apply(SchmuckEntity schmuck) {
		this.levels.forEach((spec, lvl) -> spec.getModifier().apply(schmuck, lvl));
	}

	public void setLevel(Specialization spec, int level) {
		if (spec.getMaxLevel() >= level) {
			this.levels.put(spec, level);
			this.levelUpdates.add(spec);
		}
	}

	@Override
	public Set<Specialization> getVisible() {
		return this.visible;
	}

	@Override
	public Set<Specialization> getLevelUpdates() {
		return this.levelUpdates;
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
		if (this.levelUpdates.size() > 0) {
			this.apply();
		}
	}
}
