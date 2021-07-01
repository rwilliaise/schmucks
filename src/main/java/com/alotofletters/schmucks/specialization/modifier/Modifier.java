package com.alotofletters.schmucks.specialization.modifier;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.mixin.RegistryAccessor;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.TypeFilter;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

import static com.alotofletters.schmucks.Schmucks.id;

public abstract class Modifier {
	public static final RegistryKey<Registry<Modifier>> REGISTRY_KEY = RegistryKey.ofRegistry(id("modifier"));
	public static final Registry<Modifier> REGISTRY = RegistryAccessor.callCreate(REGISTRY_KEY, () -> Modifiers.EMPTY);

	private static Integer id = 0;

	private final Map<EntityAttribute, EntityAttributeModifier> attributes = new HashMap<>();

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
			serverWorld.getEntitiesByType(
					TypeFilter.instanceOf(SchmuckEntity.class),
					entity -> player.equals(entity.getOwner()))
					.forEach(entity -> {
						this.cleanup(entity);
						this.apply(entity, level);
					});
		}
	}

	public void addModifier(EntityAttribute attribute, double amount, EntityAttributeModifier.Operation op) {
		this.attributes.put(attribute, new EntityAttributeModifier((id++).toString(), amount, op));
	}

	public void applyModifiers(SchmuckEntity entity, int level) {
		this.attributes.forEach((attribute, modifier) -> {
			EntityAttributeInstance entityAttributeInstance = entity.getAttributes().getCustomInstance(attribute);
			if (entityAttributeInstance != null) {
				entityAttributeInstance.addTemporaryModifier(new EntityAttributeModifier(modifier.getId(), modifier.getName(), modifier.getValue() * level, modifier.getOperation()));
			}
		});
	}

	public void removeModifiers(SchmuckEntity entity) {
		this.attributes.forEach((attribute, modifier) -> {
			EntityAttributeInstance entityAttributeInstance = entity.getAttributes().getCustomInstance(attribute);
			if (entityAttributeInstance != null) {
				entityAttributeInstance.removeModifier(modifier.getId());
			}
		});
	}

	public abstract void apply(SchmuckEntity entity, int level);

	public void apply(PlayerEntity player, int level) {
	}

	public void cleanup(SchmuckEntity entity) {
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Modifier modifier = (Modifier) o;

		return modifier.getId() == this.getId();
	}
}
