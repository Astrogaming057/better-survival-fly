package com.forcedflight.compat;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
//? if >=1.21.9 {
/*import net.minecraft.server.players.NameAndId;
*///?}

public final class McServer {
	private McServer() {
	}

	public static MinecraftServer server(ServerPlayer player) {
		//? if >=1.21.9 {
		/*return player.level().getServer();
		*///?} else {
		return player.getServer();
		//?}
	}

	/**
	 * True only for the owner of an integrated singleplayer world, including when cheats
	 * are off. Dedicated servers always fail here, so they keep vanilla operator checks.
	 * Opening to LAN does not grant this to guests; only the world owner matches.
	 */
	public static boolean isSingleplayerHost(ServerPlayer player) {
		MinecraftServer current = server(player);
		if (current == null || current.isDedicatedServer() || !current.isSingleplayer()) {
			return false;
		}
		//? if >=1.21.9 {
		/*return current.isSingleplayerOwner(new NameAndId(player.getGameProfile()));
		*///?} else {
		return current.isSingleplayerOwner(player.getGameProfile());
		//?}
	}
}
