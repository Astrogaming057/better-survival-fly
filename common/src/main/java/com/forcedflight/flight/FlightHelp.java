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
				"Hold Alt and scroll to change speed, or type /flight speed <number>. Scroll up is faster, scroll down is slower. The server cap is %.2fx. This stays off in creative and spectator. Operators change the cap with /flight maxspeed. The speed display fades a few seconds after you stop scrolling.",
				max)).withStyle(ChatFormatting.GRAY), false);
	}
}
