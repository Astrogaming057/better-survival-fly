package com.forcedflight.client;

import java.util.Locale;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import com.forcedflight.config.ModConfig;
import com.forcedflight.network.FlightRequests;
import com.mojang.blaze3d.platform.InputConstants;

public final class FlightClientHooks {
	public static KeyMapping toggle;
	private static int ticksSinceJoin;
	private static boolean warnedMissingServer;

	private FlightClientHooks() {
	}

	public static KeyMapping createToggle() {
		int code = GLFW.GLFW_KEY_I;
		String configured = ModConfig.get().defaultToggleKey;
		if (configured != null && !configured.isBlank()) {
			InputConstants.Key key = InputConstants.getKey("key.keyboard." + configured.trim().toLowerCase(Locale.ROOT));
			if (key.getType() == InputConstants.Type.KEYSYM && key.getValue() > 0) {
				code = key.getValue();
			}
		}
		return new KeyMapping("key.forcedflight.toggle", InputConstants.Type.KEYSYM, code, "category.forcedflight");
	}

	public static void onJoin() {
		ticksSinceJoin = 0;
		warnedMissingServer = false;
	}

	public static void onDisconnect() {
		warnedMissingServer = false;
		ClientFlightState.get().reset();
		FlightClientController.updateGate(Minecraft.getInstance());
	}

	public static void tick(Minecraft client) {
		ClientFlightState state = ClientFlightState.get();
		state.clientTicks++;
		if (client.player == null || toggle == null) {
			FlightClientController.updateGate(client);
			return;
		}
		if (client.level != null) {
			ticksSinceJoin++;
			if (!warnedMissingServer && ticksSinceJoin == 60 && !state.hasServerState) {
				warnedMissingServer = true;
				client.player.displayClientMessage(Component.literal(
						"Forced Creative Flight is on this client, but this server has no /flight command. Put the same jar in the server mods folder and restart the server. Singleplayer uses the mods folder of this instance."
				), false);
			}
		}
		while (toggle.consumeClick()) {
			if (client.screen == null && state.canToggle) {
				FlightRequests.sendToggle();
			}
		}
		FlightClientController.updateGate(client);
	}
}
