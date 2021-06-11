package com.alotofletters.schmucks.specialization;

import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.Set;
import java.util.function.Function;

public class Specialization {
	private final Identifier id;
	private final Identifier modifierId;
	private final int maxLevel;
	private final SpecializationDisplay display;
	private final Specialization parent;
	private final Set<Specialization> children = Sets.newLinkedHashSet();

	public Specialization(Identifier id,
	                      Identifier modifierId,
	                      int maxLevel,
	                      @Nullable SpecializationDisplay display,
	                      @Nullable Specialization parent) {
		this.parent = parent;
		this.id = id;
		this.modifierId = modifierId;
		this.display = display;
		this.maxLevel = maxLevel;
	}

	/**
	 * A raw specialization, before getting parent and getting id.
	 */
	public static class Raw {
		private Identifier parentId;
		private Specialization parent;
		private Identifier modifierId;
		private SpecializationDisplay display;
		private int maxLevel;

		Raw(Identifier parentId, Identifier modifierId, SpecializationDisplay display, int maxLevel) {
			this.parentId = parentId;
			this.modifierId = modifierId;
			this.display = display;
			this.maxLevel = maxLevel;
		}

		public Raw() {
		}

		public static Raw fromJson(JsonObject object) {
			Identifier parent = object.has("parent") ? new Identifier(JsonHelper.getString(object, "parent")) : null;
			Identifier modifier = object.has("modifier") ? new Identifier(JsonHelper.getString(object, "modifier")) : null;
			SpecializationDisplay display = object.has("display") ?
					SpecializationDisplay.fromJson(JsonHelper.getObject(object, "display")) :
					null;
			int maxLevel = JsonHelper.getInt(object, "max_level", 3);
			return new Specialization.Raw(parent, modifier, display, maxLevel);
		}

		public boolean findParent(Function<Identifier, Specialization> get) {
			if (this.parentId == null) {
				return true; // its a root!
			}
			if (this.parent == null) {
				this.parent = get.apply(this.parentId);
			}
			return this.parent != null;
		}

		public Specialization build(Identifier id) {
			if (this.parent == null && this.parentId != null) {
				// throw if we HAVENT found a parent
				throw new IllegalStateException("Tried to build incomplete Specialization!");
			}
			return new Specialization(id, this.modifierId, this.maxLevel, this.display, this.parent);
		}
	}
}
