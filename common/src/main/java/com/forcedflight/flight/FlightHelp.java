package com.forcedflight.flight;

import java.util.Locale;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.forcedflight.config.ModConfig;
import com.forcedflight.permission.FlightSpeed;

public final class FlightHelp {
	private FlightHelp() {
	}

	public static void tell(ServerPlayer player) {
		if (!ModConfig.get().joinMessage) {
			return;
		}
		if (!ModConfig.get().enabled) {
			player.sendSystemMessage(Component.literal(
					"Forced Creative Flight is installed, but disabled in config/forcedflight/config.json."), false);
			return;
		}
		float max = FlightSpeed.multiplier(ModConfig.get().maxSpeed);
		player.sendSystemMessage(Component.literal("Forced Creative Flight").withStyle(ChatFormatting.GOLD), false);
		player.sendSystemMessage(Component.literal(
				"Press I, or type /flight, to turn your own flight on or off. Operators can use /flight <player> for someone else.")
				.withStyle(ChatFormatting.GRAY), false);
		player.sendSystemMessage(Component.literal(
				"Double-tap jump to take off or land. Hold jump to rise, hold sneak to drop, and use WASD to move.")
				.withStyle(ChatFormatting.GRAY), false);
		player.sendSystemMessage(Component.literal(String.format(Locale.ROOT,
				"Change speed from Flight in the pause menu, with /flight speed <number>, or with the speed keys. Hold the speed key and scroll by default. Scroll up is faster and scroll down is slower. The server cap is %.2fx. This stays off in creative and spectator.",
				max)).withStyle(ChatFormatting.GRAY), false);
		player.sendSystemMessage(Component.literal(ModConfig.get().flyPenalty
				? "Breaking blocks while flying is slower. Operators can turn that off with /flight flypenalty."
				: "Breaking blocks while flying uses normal speed. Operators can turn the slowdown back on with /flight flypenalty.")
				.withStyle(ChatFormatting.GRAY), false);
		player.sendSystemMessage(Component.literal(ModConfig.get().fallDamage
				? "Falling still hurts when flight is granted but toggled off. Operators can turn that off with /flight falldamage."
				: "Falling does not hurt when flight is granted but toggled off. Operators can turn that back on with /flight falldamage.")
				.withStyle(ChatFormatting.GRAY), false);
		player.sendSystemMessage(Component.literal(ModConfig.get().keepFlying
				? "Landing keeps you flying until you double-tap jump. Operators can turn that off with /flight keepflying."
				: "Landing cancels flight like normal. Operators can keep flight after landing with /flight keepflying.")
				.withStyle(ChatFormatting.GRAY), false);
	}
}
