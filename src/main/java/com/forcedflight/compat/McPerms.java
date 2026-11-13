package com.forcedflight.compat;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public final class McPerms {
	private McPerms() {
	}

	public static boolean allowed(CommandSourceStack source, int level) {
		//? if >=1.21.11 {
		/*return source.permissions().hasPermission(new net.minecraft.server.permissions.Permission.HasCommandLevel(net.minecraft.server.permissions.PermissionLevel.byId(level)));
		*///?} else {
		return source.hasPermission(level);
		//?}
	}

	public static boolean allowed(ServerPlayer player, int level) {
		//? if >=1.21.11 {
		/*return player.permissions().hasPermission(new net.minecraft.server.permissions.Permission.HasCommandLevel(net.minecraft.server.permissions.PermissionLevel.byId(level)));
		*///?} else {
		return player.hasPermissions(level);
		//?}
	}
}
