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
	public static KeyMapping speedModifier;
	public static KeyMapping speedUp;
	public static KeyMapping speedDown;
	private static int ticksSinceJoin;
	private static boolean warnedMissingServer;
	private static boolean speedUpWasDown;
	private static boolean speedDownWasDown;
	private static int speedKeyRepeat;

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

	public static void createSpeedKeys() {
		speedModifier = new KeyMapping("key.forcedflight.speed_modifier", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_LEFT_ALT, "category.forcedflight");
		speedUp = new KeyMapping("key.forcedflight.speed_up", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UP, "category.forcedflight");
		speedDown = new KeyMapping("key.forcedflight.speed_down", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_DOWN, "category.forcedflight");
	}

	public static boolean speedModifierDown() {
		if (speedModifier != null && speedModifier.isDown()) {
			return true;
		}
		int bound = GLFW.GLFW_KEY_LEFT_ALT;
		if (speedModifier != null) {
			InputConstants.Key key = InputConstants.getKey(speedModifier.saveString());
			if (key.getType() != InputConstants.Type.KEYSYM) {
				return false;
			}
			bound = key.getValue();
		}
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.getWindow() == null) {
			return false;
		}
		long window = client.getWindow().getWindow();
		if (bound == GLFW.GLFW_KEY_LEFT_ALT || bound == GLFW.GLFW_KEY_RIGHT_ALT) {
			return keyDown(window, GLFW.GLFW_KEY_LEFT_ALT) || keyDown(window, GLFW.GLFW_KEY_RIGHT_ALT);
		}
		if (bound == GLFW.GLFW_KEY_LEFT_SHIFT || bound == GLFW.GLFW_KEY_RIGHT_SHIFT) {
			return keyDown(window, GLFW.GLFW_KEY_LEFT_SHIFT) || keyDown(window, GLFW.GLFW_KEY_RIGHT_SHIFT);
		}
		if (bound == GLFW.GLFW_KEY_LEFT_CONTROL || bound == GLFW.GLFW_KEY_RIGHT_CONTROL) {
			return keyDown(window, GLFW.GLFW_KEY_LEFT_CONTROL) || keyDown(window, GLFW.GLFW_KEY_RIGHT_CONTROL);
		}
		return false;
	}

	private static boolean keyDown(long window, int key) {
		return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
	}

	public static void onJoin() {
		ticksSinceJoin = 0;
		warnedMissingServer = false;
	}

	public static void onDisconnect() {
		warnedMissingServer = false;
		ClientFlightState.get().reset();
		ClientRoster.clear();
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
			if (client.screen != null || !state.canToggle) {
				continue;
			}
			if (FlightClientController.usesVanillaFlight(client.player)) {
				client.player.displayClientMessage(Component.literal(
						"Forced Creative Flight is for survival only. Creative and spectator keep vanilla flight."), true);
				continue;
			}
			FlightRequests.sendToggle();
		}
		tickSpeedKeys(client);
		FlightClientController.updateGate(client);
	}

	private static void tickSpeedKeys(Minecraft client) {
		if (ClientPreferences.mode() != ClientPreferences.SpeedMode.KEYS || client.screen != null || speedUp == null || speedDown == null) {
			speedUpWasDown = false;
			speedDownWasDown = false;
			speedKeyRepeat = 0;
			return;
		}
		boolean up = speedUp.isDown();
		boolean down = speedDown.isDown();
		boolean edge = (up && !speedUpWasDown) || (down && !speedDownWasDown);
		speedUpWasDown = up;
		speedDownWasDown = down;
		if (up == down) {
			speedKeyRepeat = 0;
			return;
		}
		speedKeyRepeat++;
		if (edge || speedKeyRepeat >= 3) {
			speedKeyRepeat = 0;
			FlightClientController.adjustFromScroll(up ? 1.0 : -1.0);
		}
	}
}
