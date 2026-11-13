package com.forcedflight.compat;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public final class McText {
	private McText() {
	}

	public static MutableComponent literal(String text) {
		//? if >=1.19.2 {
		return Component.literal(text);
		//?} else {
		/*return new net.minecraft.network.chat.TextComponent(text);
		*///?}
	}

	public static Component empty() {
		//? if >=1.19.2 {
		return Component.empty();
		//?} else {
		/*return net.minecraft.network.chat.TextComponent.EMPTY;
		*///?}
	}

	public static void tell(net.minecraft.server.level.ServerPlayer player, Component text, boolean overlay) {
		//? if >=1.19.2 {
		player.sendSystemMessage(text, overlay);
		//?} else {
		/*if (overlay) {
			player.displayClientMessage(text, true);
		} else {
			player.sendMessage(text, net.minecraft.Util.NIL_UUID);
		}
		*///?}
	}
}
