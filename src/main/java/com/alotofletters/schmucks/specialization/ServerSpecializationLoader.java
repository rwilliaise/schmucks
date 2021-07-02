package com.alotofletters.schmucks.specialization;

import com.alotofletters.schmucks.Schmucks;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ServerSpecializationLoader extends JsonDataLoader implements IdentifiableResourceReloadListener {
	private static final Logger LOGGER = LogManager.getLogger();
	private static final Gson GSON = (new GsonBuilder()).create();
	private SpecializationManager manager = new SpecializationManager();
	private final Map<Identifier, Specialization.Raw> toReplicate = Maps.newHashMap();

	public ServerSpecializationLoader() {
		super(GSON, "specializations");
	}

	@Override
	protected void apply(Map<Identifier, JsonElement> prepared, ResourceManager manager, Profiler profiler) {
		Map<Identifier, Specialization.Raw> rawMap = Maps.newHashMap();

		prepared.forEach((identifier, jsonElement) -> {
			if (identifier.getPath().startsWith("ages/")) {
				// not loading for 1.4
				LOGGER.trace("Loading age {} from mod id {}", identifier.getPath(), identifier.getNamespace());
				return;
			}
			JsonObject object = JsonHelper.asObject(jsonElement, "specialization");
			Specialization.Raw raw = Specialization.Raw.fromJson(object);
			toReplicate.put(identifier, raw);
			rawMap.put(identifier, raw);
		});

		SpecializationManager specializationManager = new SpecializationManager();
		specializationManager.load(rawMap);

		this.manager = specializationManager;
	}

	@Override
	public Identifier getFabricId() {
		return Schmucks.id("specialization");
	}

	public Specialization get(Identifier id) {
		return this.manager.get(id);
	}

	public Collection<Specialization> getSpecializations() {
		return this.manager.getSpecializations();
	}

	public Map<Identifier, Specialization.Raw> popReplicationQueue() {
		Map<Identifier, Specialization.Raw> out = new HashMap<>(this.toReplicate);
		this.toReplicate.clear();
		return out;
	}
}
