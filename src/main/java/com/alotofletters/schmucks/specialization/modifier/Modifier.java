package com.alotofletters.schmucks.specialization.modifier;

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

public abstract class Modifier {
	public static final RegistryKey<Registry<Modifier>> REGISTRY_KEY = RegistryKey.ofRegistry(id("modifier"));
	public static final Registry<Modifier> REGISTRY = RegistryAccessor.callCreate(REGISTRY_KEY, () -> Modifiers.EMPTY);

	@Nullable
	protected String translationKey;

	@Nullable
	public static Modifier byRawId(int id) {
		return REGISTRY.get(id);
	}

	public Identifier getId() {
		return REGISTRY.getId(this);
	}

	/**
	 * Used for the cost of each level.
	 *
	 * @param level Current level
	 * @return How much does the next level cost
	 */
	public int getCost(int level) {
		return 1 + level;
	}

	/**
	 * Applies a modifier to all of a players schmucks.
	 *
	 * @param player Player that owns Schmucks
	 * @param level  Level to apply to Schmucks
	 */
	public void applyAll(PlayerEntity player, int level) {
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

	public void apply(PlayerEntity player, int level) { }

	public void cleanup(SchmuckEntity entity) {
	}
}
