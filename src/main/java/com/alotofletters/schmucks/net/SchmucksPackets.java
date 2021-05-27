package com.alotofletters.schmucks.net;

import com.alotofletters.schmucks.Schmucks;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

public class SchmucksPackets {

	public static void init() {
		ServerPlayNetworking.registerGlobalReceiver(Schmucks.CONTROL_WAND_PACKET_ID, new ControlWandServerChannelHandler());
	}
}
