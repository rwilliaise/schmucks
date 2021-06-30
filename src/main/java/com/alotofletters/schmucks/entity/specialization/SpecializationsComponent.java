package com.alotofletters.schmucks.entity.specialization;

import com.alotofletters.schmucks.specialization.ServerSpecializationLoader;
import com.alotofletters.schmucks.specialization.Specialization;
import com.alotofletters.schmucks.specialization.modifier.Modifier;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.Map;
import java.util.Set;

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

	void setLevel(Specialization spec, int level);

	void upgradeLevel(Specialization spec);

	void apply();

	default boolean hasModifier(Modifier modifier) {
		return this.getLevels().keySet().stream().anyMatch(spec -> modifier.getId() == spec.getModifierId() && this.getLevels().get(spec) > 0);
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
