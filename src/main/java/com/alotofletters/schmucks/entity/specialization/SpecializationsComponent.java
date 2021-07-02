package com.alotofletters.schmucks.entity.specialization;

import com.alotofletters.schmucks.entity.SchmuckEntity;
import com.alotofletters.schmucks.specialization.ServerSpecializationLoader;
import com.alotofletters.schmucks.specialization.Specialization;
import com.alotofletters.schmucks.specialization.SpecializationManager;
import com.alotofletters.schmucks.specialization.modifier.Modifier;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public interface SpecializationsComponent extends AutoSyncedComponent, ServerTickingComponent {

	/**
	 * Returns all specs that can be seen.
	 *
	 * @return All specs that are visible.
	 */
	Set<Specialization> getVisible();

	/**
	 * Returns which Specializations have changed visibility.
	 *
	 * @return Specs with changed visibility.
	 */
	Set<Specialization> getLevelUpdates();

	/**
	 * Returns a map of all specs to their current upgrade status.
	 *
	 * @return Map of all specs and their level
	 */
	Map<Specialization, Integer> getLevels();

	/**
	 * Returns if a spec can be seen in the tree.
	 *
	 * @param spec Spec to test.
	 * @return If the spec can be seen in the tree.
	 */
	boolean canSee(Specialization spec);

	/**
	 * Invoked whenever the specialization loader reloads.
	 *
	 * @param loader Loader that reloaded
	 */
	void reload(ServerSpecializationLoader loader);

	@Environment(EnvType.CLIENT)
	void startListening(SpecializationManager.Listener listener);

	void setLevel(Specialization spec, int level);

	void upgradeLevel(Specialization spec);

	void apply();

	void apply(SchmuckEntity schmuck);

	default boolean hasModifier(Modifier modifier) {
		return this.getLevels().entrySet().stream().anyMatch((entry) -> entry.getKey().getModifier() == modifier && entry.getValue() > 0);
	}

	default int getModifierLevel(Modifier modifier) {
		AtomicInteger out = new AtomicInteger();
		this.getLevels().forEach((specialization, integer) -> {
			if (specialization.getModifierId() == modifier.getId()) {
				out.addAndGet(integer);
			}
		});
		return out.get();
	}

	/**
	 * Returns if a Specialization has been upgraded or bought at all.
	 *
	 * @param spec Specialization to test.
	 * @return If the specialization has been bought once.
	 */
	default boolean hasUsed(Specialization spec) {
		return this.getLevel(spec) > 0;
	}

	/**
	 * Used to get the level of a Specialization.
	 * Defaults to one if the Specialization is a unique, and has been bought.
	 *
	 * @param spec Specialization to get level of
	 * @return The level the Specialization is
	 */
	default int getLevel(Specialization spec) {
		return this.getLevels().get(spec);
	}
}
