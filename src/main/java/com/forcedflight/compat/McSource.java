package com.forcedflight.compat;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public final class McSource {
	private McSource() {
	}

	public static ServerPlayer player(CommandSourceStack source) {
		//? if >=1.19.2 {
		return source.getPlayer();
		//?} else {
		/*net.minecraft.world.entity.Entity entity = source.getEntity();
		return entity instanceof ServerPlayer player ? player : null;
		*///?}
	}
}
