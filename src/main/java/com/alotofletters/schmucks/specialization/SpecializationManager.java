package com.alotofletters.schmucks.specialization;

import com.alotofletters.schmucks.specialization.Specialization.Raw;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.advancement.Advancement;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;

public class SpecializationManager {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Map<Identifier, Specialization> specializations = Maps.newHashMap();
	private final Set<Specialization> tabs = Sets.newLinkedHashSet();
	private final Set<Specialization> children = Sets.newLinkedHashSet();
	private Listener listener;

	public void load(Map<Identifier, Raw> rawMap) {
		HashMap<Identifier, Raw> copy = Maps.newHashMap(rawMap);

		copyLabel:
		while (!copy.isEmpty()) {
			boolean builtAny = false;
			Iterator<Entry<Identifier, Raw>> copyIterator = copy.entrySet().iterator();

			Entry<Identifier, Raw> copyEntry;
			while (copyIterator.hasNext()) {
				copyEntry = copyIterator.next();

				Identifier specId = copyEntry.getKey();
				Raw raw = copyEntry.getValue();

				if (raw.findParents(this::get)) {
					Specialization built = raw.build(specId);
					this.specializations.put(specId, built);
					builtAny = true;

					copyIterator.remove();

					if (built.hasParents()) {
						this.children.add(built);
						if (this.listener != null) {
							this.listener.onChildAdded(built);
						}
					} else {
						this.tabs.add(built);
						if (this.listener != null) {
							this.listener.onTabAdded(built);
						}
					}
				}
			}

			if (!builtAny) {
				copyIterator = copy.entrySet().iterator();
				while (true) {
					if (!copyIterator.hasNext()) {
						break copyLabel;
					}

					Entry<Identifier, Raw> rawEntry = copyIterator.next();
					LOGGER.error("Failed to load specialization {}!", rawEntry.getKey());
				}
			}

			LOGGER.info("Loaded {} specializations", this.specializations.size());
		}
	}

	public void removeAll(Set<Identifier> specializations) {
		for (Identifier id : specializations) {
			Specialization specialization = this.specializations.get(id);
			if (specialization == null) {
				LOGGER.warn("Tried to remove specialization {}, not found!", id);
			} else {
				this.remove(specialization);
			}
		}
	}

	public void remove(Specialization specialization) {
		for (Specialization child : specialization.getChildren()) {
			this.remove(child);
		}

		LOGGER.info("Forgot about specialization {}", specialization.getId());

		this.specializations.remove(specialization.getId());

		if (!specialization.hasParents()) {
			this.tabs.remove(specialization);
			if (this.listener != null) {
				this.listener.onTabRemoved(specialization);
			}
		} else {
			this.children.remove(specialization);
			if (this.listener != null) {
				this.listener.onChildRemoved(specialization);
			}
		}
	}

	public Specialization get(Identifier id) { return this.specializations.get(id); }

	public void clear() {
		this.specializations.clear();
		this.tabs.clear();
		this.children.clear();
		if (this.listener != null) {
			this.listener.onClear();
		}
	}

	public void setListener(@Nullable Listener listener) {
		this.listener = listener;
		if (listener != null) {
			this.tabs.forEach(listener::onTabAdded);
			this.children.forEach(listener::onChildAdded);
		}
	}

	public interface Listener {
		void onTabRemoved(Specialization specialization);

		void onChildRemoved(Specialization specialization);

		void onTabAdded(Specialization specialization);

		void onChildAdded(Specialization specialization);

		void onClear();
	}
}
