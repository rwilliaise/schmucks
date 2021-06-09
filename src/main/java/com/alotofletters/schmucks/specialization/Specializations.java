package com.alotofletters.schmucks.specialization;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.entity.SchmuckEntity;
import net.minecraft.util.registry.Registry;

public class Specializations {
	public static final Specialization MINING = register("mining", new MiningSpecialization());
	public static final Specialization EMPTY = register("empty", new Specialization() {
		public void apply(SchmuckEntity entity, int level) { }
	});

	private static Specialization register(String id, Specialization entry) {
		return Registry.register(Specialization.SPECIALIZATION, Schmucks.id(id), entry);
	}
}
