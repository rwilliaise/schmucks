package com.alotofletters.schmucks.specialization;

import com.alotofletters.schmucks.Schmucks;
import com.alotofletters.schmucks.specialization.client.SpecializationIcon;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SpecializationDisplay {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Text title;
	private final Text description;
	private final Icon icon;
	private final boolean announceToChat;
	private float x;
	private float y;

	public SpecializationDisplay(Text title, Text description, Icon icon, boolean announceToChat) {
		this.title = title;
		this.description = description;
		this.icon = icon;
		this.announceToChat = announceToChat;
	}

	public static SpecializationDisplay fromJson(JsonObject object) {
		Text text = Text.Serializer.fromJson(object.get("title"));
		Text text2 = Text.Serializer.fromJson(object.get("description"));
		if (text != null && text2 != null) {
			Icon icon = object.has("icon") ? Icon.fromJson(object.get("icon")) : null;
			boolean announce = JsonHelper.getBoolean(object, "announce_to_chat", true);
			return new SpecializationDisplay(text, text2, icon, announce);
		} else {
			throw new JsonSyntaxException("Both title and description must be set");
		}
	}

	public static SpecializationDisplay fromPacket(PacketByteBuf buf) {
		Text title = buf.readText();
		Text description = buf.readText();
		Icon icon = new Icon(null, null);
		if (buf.readBoolean()) {
			icon = Icon.fromPacket(buf);
		}
		float x = buf.readFloat();
		float y = buf.readFloat();
		SpecializationDisplay out = new SpecializationDisplay(title, description, icon, false);
		out.setPos(x, y);
		return out;
	}

	public void toPacket(PacketByteBuf buf) {
		buf.writeText(this.title);
		buf.writeText(this.description);
		if (this.icon == null) {
			buf.writeBoolean(false);
		} else {
			buf.writeBoolean(true);
			this.icon.toPacket(buf);
		}
		buf.writeFloat(this.x);
		buf.writeFloat(this.y);
	}

	public Text getTitle() {
		return title;
	}

	public Text getDescription() {
		return description;
	}

	public Icon getIcon() {
		return icon;
	}

	public boolean isAnnounceToChat() {
		return announceToChat;
	}

	public void setPos(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public static class Icon {
		private ItemStack stack;
		private SpecializationIcon icon;

		public Icon(ItemStack stack) {
			this.stack = stack;
		}

		public Icon(Identifier texture) {
			this.icon = SpecializationIcon.REGISTRY.getOrEmpty(texture)
					.orElse(SpecializationIcon.MISSING);
		}

		public Icon(ItemStack stack, Identifier texture) {
			this.stack = stack;
			this.icon = SpecializationIcon.REGISTRY.getOrEmpty(texture)
					.orElse(SpecializationIcon.MISSING);
		}

		public static Icon fromJson(JsonElement element) {
			if (JsonHelper.isString(element)) {
				return new Icon(new Identifier(element.getAsString()));
			}
			if (element.isJsonObject()) {
				ItemStack itemStack = iconFromJson(element.getAsJsonObject());
				return new Icon(itemStack);
			}
			return new Icon(Schmucks.id("missing"));
		}

		private static ItemStack iconFromJson(JsonObject json) {
			if (!json.has("item")) {
				throw new JsonSyntaxException("Unsupported icon type, currently only items are supported (add 'item' key)");
			} else {
				Item item = JsonHelper.getItem(json, "item");
				if (json.has("data")) {
					throw new JsonParseException("Disallowed data tag found");
				} else {
					ItemStack itemStack = new ItemStack(item);
					if (json.has("nbt")) {
						try {
							NbtCompound nbtCompound = StringNbtReader.parse(JsonHelper.asString(json.get("nbt"), "nbt"));
							itemStack.setTag(nbtCompound);
						} catch (CommandSyntaxException var4) {
							throw new JsonSyntaxException("Invalid nbt tag: " + var4.getMessage());
						}
					}

					return itemStack;
				}
			}
		}

		public static Icon fromPacket(PacketByteBuf buf) {
			ItemStack stack = null;
			Identifier icon = null;
			if (buf.readBoolean()) {
				stack = buf.readItemStack();
			}
			if (buf.readBoolean()) {
				icon = buf.readIdentifier();
			}
			return new Icon(stack, icon);
		}

		public void toPacket(PacketByteBuf buf) {
			if (this.isStack()) {
				buf.writeBoolean(true);
				buf.writeItemStack(this.stack);
			}
			Identifier id = SpecializationIcon.REGISTRY.getId(this.getSpecIcon());
			if (this.isSpecIcon() && id != null) {
				buf.writeBoolean(true);
				buf.writeIdentifier(id);
			}
			if (id == null) {
				LOGGER.warn("Id is null!");
			}
		}

		public boolean isStack() {
			return this.stack != null;
		}

		public boolean isSpecIcon() {
			return this.icon != null;
		}

		public ItemStack getStack() {
			return this.stack;
		}

		public SpecializationIcon getSpecIcon() {
			return this.icon;
		}
	}
}
