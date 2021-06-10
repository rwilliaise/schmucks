package com.alotofletters.schmucks.specialization;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.util.Identifier;

import java.util.Map;
import java.util.Set;

public class SpecializationManager {
	private final Map<Identifier, Specialization> specializations = Maps.newHashMap();
	private final Set<Specialization> roots = Sets.newLinkedHashSet();

}
