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

import java.util.Objects;

public class SpecializationDisplay {
	private static final Logger LOGGER = LogManager.getLogger();
	private final Text title;
	private final Text description;
	private final SpecializationIcon icon;
	private final boolean announceToChat;
	private float x;
	private float y;

	public SpecializationDisplay(Text title, Text description, SpecializationIcon icon, boolean announceToChat) {
		this.title = title;
		this.description = description;
		this.icon = icon;
		Objects.requireNonNull(icon, "No icon is present on specialization with title " + title.getString());
		this.announceToChat = announceToChat;
	}

	public static SpecializationDisplay fromJson(JsonObject object) {
		Text text = Text.Serializer.fromJson(object.get("title"));
		Text text2 = Text.Serializer.fromJson(object.get("description"));
		if (text != null && text2 != null) {
			Identifier icon = object.has("icon") ? new Identifier(JsonHelper.getString(object, "icon")) : null;
			boolean announce = JsonHelper.getBoolean(object, "announce_to_chat", true);
			return new SpecializationDisplay(text, text2, SpecializationIcon.get(icon), announce);
		} else {
			throw new JsonSyntaxException("Both title and description must be set");
		}
	}

	public static SpecializationDisplay fromPacket(PacketByteBuf buf) {
		Text title = buf.readText();
		Text description = buf.readText();
		SpecializationIcon icon = SpecializationIcon.get(buf.readIdentifier());
		float x = buf.readFloat();
		float y = buf.readFloat();
		SpecializationDisplay out = new SpecializationDisplay(title, description, icon, false);
		out.setPos(x, y);
		return out;
	}

	public void toPacket(PacketByteBuf buf) {
		buf.writeText(this.title);
		buf.writeText(this.description);
		buf.writeIdentifier(this.icon.location());
		buf.writeFloat(this.x);
		buf.writeFloat(this.y);
	}

	public Text getTitle() {
		return title;
	}

	public Text getDescription() {
		return description;
	}

	public SpecializationIcon getIcon() {
		return icon;
	}

	public boolean isAnnounceToChat() {
		return announceToChat;
	}

	public void setPos(float x, float y) {
		this.x = x;
		this.y = y;
	}
}
