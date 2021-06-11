package com.alotofletters.schmucks.specialization;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.StringNbtReader;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import org.jetbrains.annotations.Nullable;

public class SpecializationDisplay {
	private final Text title;
	private final Text description;
	private final Icon icon;
	private final Identifier background;
	private final boolean announceToChat;
	private float x;
	private float y;

	public SpecializationDisplay(Text title, Text description, Icon icon, boolean announceToChat, @Nullable Identifier background) {
		this.title = title;
		this.description = description;
		this.icon = icon;
		this.background = background;
		this.announceToChat = announceToChat;
	}

	public static SpecializationDisplay fromJson(JsonObject object) {
		Text text = Text.Serializer.fromJson(object.get("title"));
		Text text2 = Text.Serializer.fromJson(object.get("description"));
		if (text != null && text2 != null) {
			Icon icon = object.has("icon") ? Icon.fromJson(object.get("icon")) : null;
			Identifier identifier = object.has("background") ? new Identifier(JsonHelper.getString(object, "background")) : null;
			boolean announce = JsonHelper.getBoolean(object, "announce_to_chat", true);
			return new SpecializationDisplay(text, text2, icon, announce, identifier);
		} else {
			throw new JsonSyntaxException("Both title and description must be set");
		}
	}

	public static class Icon {
		private ItemStack stack;
		private Identifier texture;

		public Icon(ItemStack stack) {
			this.stack = stack;
		}

		public Icon(Identifier texture) {
			this.texture = texture;
		}

		public static Icon fromJson(JsonElement element) {
			if (JsonHelper.isString(element)) {
				return new Icon(new Identifier(element.getAsString()));
			}
			if (element.isJsonObject()) {
				ItemStack itemStack = iconFromJson(element.getAsJsonObject());
				return new Icon(itemStack);
			}
			return new Icon(new Identifier("schmucks:icon.png"));
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

		public boolean isStack() {
			return this.stack != null;
		}

		public boolean isTexture() {
			return this.texture != null;
		}

		public ItemStack getStack() {
			return stack;
		}

		public Identifier getTexture() {
			return texture;
		}
	}
}
