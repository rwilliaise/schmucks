package com.alotofletters.schmucks.specialization;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.specialization.modifier.Modifier;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Specialization {
	private final Identifier id;
	private final Modifier modifier;
	private final int maxLevel;
	private final SpecializationDisplay display;
	private final Set<Specialization> parents;
	private final Set<Specialization> children = Sets.newLinkedHashSet();

	public Specialization(Set<Specialization> parents,
	                      Identifier id,
	                      Identifier modifierId,
	                      int maxLevel,
	                      @Nullable SpecializationDisplay display) {
		this.parents = parents;
		this.id = id;
		this.modifier = Modifier.REGISTRY.get(modifierId);
		this.display = display;
		this.maxLevel = maxLevel;
	}

	public Raw toRaw() {
		return new Raw(this.parents.stream().map(Specialization::getId).collect(Collectors.toSet()),
					   this.getModifierId(),
					   this.getDisplay(),
					   this.getMaxLevel());
	}

	public Identifier getId() {
		return id;
	}

	public Identifier getModifierId() {
		return this.modifier.getId();
	}

	public int getMaxLevel() {
		return maxLevel;
	}

	public SpecializationDisplay getDisplay() {
		return display;
	}

	public Set<Specialization> getParents() {
		return parents;
	}

	public boolean hasParents() {
		return this.parents != null && !this.parents.isEmpty();
	}

	public Iterable<Specialization> getChildren() {
		return children;
	}

	public void addChild(Specialization child) {
		this.children.add(child);
	}

	public Modifier getModifier() {
		return modifier;
	}

	/**
	 * A raw specialization, before getting parent and getting id.
	 */
	public static class Raw {
		private Set<Identifier> parentIds;
		private Set<Specialization> parents;
		private Identifier modifierId;
		private SpecializationDisplay display;
		private int maxLevel;

		Raw(Set<Identifier> parentIds, Identifier modifierId, SpecializationDisplay display, int maxLevel) {
			this.parentIds = parentIds;
			this.modifierId = modifierId;
			this.display = display;
			this.maxLevel = maxLevel;
		}

		public Raw() {
		}

		public static Raw fromJson(JsonObject object) {
			Set<Identifier> parents = Sets.newLinkedHashSet();
			if (JsonHelper.hasArray(object, "parents")) {
				JsonHelper.getArray(object, "parents")
						.forEach(element -> parents.add(new Identifier(JsonHelper.asString(element, "parent"))));
			}
			Identifier modifier = object.has("modifier") ? new Identifier(JsonHelper.getString(object, "modifier")) : null;
			SpecializationDisplay display = object.has("display") ?
					SpecializationDisplay.fromJson(JsonHelper.getObject(object, "display")) :
					null;
			int maxLevel = JsonHelper.getInt(object, "max_level", 3);
			return new Specialization.Raw(parents, modifier, display, maxLevel);
		}

		public boolean findParents(Function<Identifier, Specialization> get) {
			if (!this.hasParentIds()) {
				return true; // its a root!
			}
			if (!this.hasParents()) {
				this.parents = Sets.newLinkedHashSet();
				for (Identifier parentId : this.parentIds) {
					if (parentId != null) {
						this.parents.add(get.apply(parentId));
					}
				}
			}
			return this.hasParents();
		}

		public boolean hasParentIds() {
			return this.parentIds != null && !this.parentIds.isEmpty();
		}

		public boolean hasParents() {
			return this.parents != null && !this.parents.isEmpty();
		}

		public void toPacket(PacketByteBuf buf) {
			buf.writeCollection(this.parentIds, PacketByteBuf::writeIdentifier);
			if (this.modifierId == null) {
				buf.writeBoolean(false);
			} else {
				buf.writeBoolean(true);
				buf.writeIdentifier(this.modifierId);
			}
			if (this.display == null) {
				buf.writeBoolean(false);
			} else {
				buf.writeBoolean(true);
				this.display.toPacket(buf);
			}
			buf.writeVarInt(this.maxLevel);
		}

		public static Raw fromPacket(PacketByteBuf buf) {
			Set<Identifier> parentIds = buf.readCollection(HashSet::new, PacketByteBuf::readIdentifier);
			Identifier modifierId = null;
			if (buf.readBoolean()) {
				modifierId = buf.readIdentifier();
			}
			SpecializationDisplay display = null;
			if (buf.readBoolean()) {
				display = SpecializationDisplay.fromPacket(buf);
			}
			int maxLevel = buf.readVarInt();
			return new Raw(parentIds, modifierId, display, maxLevel);
		}

		public Specialization build(Identifier id) {
			if (!this.hasParents() && this.hasParentIds()) {
				// throw if we HAVENT found a parent
				throw new IllegalStateException("Tried to build incomplete Specialization!");
			}
			return new Specialization(this.parents, id, this.modifierId, this.maxLevel, this.display);
		}
	}
}
