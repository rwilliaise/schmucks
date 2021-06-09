package com.alotofletters.schmucks.specialization;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.mixin.RegistryAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import static com.alotofletters.schmucks.Schmucks.id;

public abstract class Specialization {
	public static final RegistryKey<Registry<Specialization>> SPECIALIZATION_KEY = RegistryKey.ofRegistry(id("specialization"));
	public static final Registry<Specialization> SPECIALIZATION = RegistryAccessor.callCreate(SPECIALIZATION_KEY, () -> Specializations.EMPTY);

	@Nullable
	protected String translationKey;

	@Nullable
	public static Specialization byRawId(int id) {
		return SPECIALIZATION.get(id);
	}

	public void toTag(NbtCompound tag, int level) {
		Identifier regId = SPECIALIZATION.getId(this);
		if (regId == null) {
			return;
		}
		String id = regId.toString();
		tag.putString("Id", id);
		tag.putInt("Level", level);
	}

	public Entry fromTag(NbtCompound tag) {
		return new Entry(new Identifier(tag.getString("Id")), tag.getInt("Level"));
	}

	protected String getOrCreateTranslationKey() {
		if (this.translationKey == null) {
			this.translationKey = Util.createTranslationKey("specialization", SPECIALIZATION.getId(this));
		}

		return this.translationKey;
	}

	public String getTranslationKey() {
		return this.getOrCreateTranslationKey();
	}

	/**
	 * Used for the cost of each level.
	 * @param level Current level
	 * @return How much does the next level cost
	 */
	public int getCost(int level) {
		return 1 + level;
	}

	/**
	 * Used to see how many levels a specialization can be.
	 * @return The maximum level a specialization can be.
	 */
	public int getMaxLevel() {
		return 3;
	}

	/**
	 * Applies a specialization to all of a players schmucks.
	 * @param player Player that owns Schmucks
	 * @param level Level to apply to Schmucks
	 */
	public void apply(PlayerEntity player, int level) {
		World world = player.world;
		if (world instanceof ServerWorld serverWorld) {
			serverWorld.getEntitiesByType(TypeFilter.instanceOf(SchmuckEntity.class), entity -> player.getUuid().equals(entity.getOwnerUuid()))
					.forEach(entity -> {
						this.cleanup(entity);
						this.apply(entity, level);
					});
		}
	}

	public abstract void apply(SchmuckEntity entity, int level);

	public void cleanup(SchmuckEntity entity) { }

	record Entry(Identifier id, int level) {
		public void apply(PlayerEntity player) {
			Specialization spec = SPECIALIZATION.get(this.id);
			if (spec == null) {
				return;
			}
			spec.apply(player, this.level);
		}

		public void apply(SchmuckEntity schmuck) {
			Specialization spec = SPECIALIZATION.get(this.id);
			if (spec == null) {
				return;
			}
			spec.apply(schmuck, this.level);
		}
	}
}
