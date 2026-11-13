package com.forcedflight.client;
import com.forcedflight.compat.McText;

import java.util.Locale;

//? if <26.1 {
import org.lwjgl.glfw.GLFW;
//?}

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import com.forcedflight.compat.McClient;
import com.forcedflight.config.ModConfig;
import com.forcedflight.network.FlightRequests;
import com.mojang.blaze3d.platform.InputConstants;

public final class FlightClientHooks {
	public static KeyMapping toggle;
	public static KeyMapping speedModifier;
	public static KeyMapping speedUp;
	public static KeyMapping speedDown;
	//? if >=1.21.9 {
	/*public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(com.forcedflight.compat.McIds.id("main"));
	*///?}
	private static int ticksSinceJoin;
	private static boolean warnedMissingServer;
	private static boolean speedUpWasDown;
	private static boolean speedDownWasDown;
	private static int speedKeyRepeat;

	private FlightClientHooks() {
	}

	public static KeyMapping createToggle() {
		int code = InputConstants.KEY_I;
		String configured = ModConfig.get().defaultToggleKey;
		if (configured != null && !configured.isBlank()) {
			InputConstants.Key key = InputConstants.getKey("key.keyboard." + configured.trim().toLowerCase(Locale.ROOT));
			if (key.getType() == McClient.keyboard() && key.getValue() > 0) {
				code = key.getValue();
			}
		}
		return key("key.forcedflight.toggle", code);
	}

	public static void createSpeedKeys() {
		speedModifier = key("key.forcedflight.speed_modifier", InputConstants.KEY_LALT);
		speedUp = key("key.forcedflight.speed_up", InputConstants.KEY_UP);
		speedDown = key("key.forcedflight.speed_down", InputConstants.KEY_DOWN);
	}

	private static KeyMapping key(String name, int code) {
		//? if >=1.21.9 {
		/*return new KeyMapping(name, McClient.keyboard(), code, CATEGORY);
		*///?} else {
		return new KeyMapping(name, McClient.keyboard(), code, "category.forcedflight");
		//?}
	}

	public static boolean speedModifierDown() {
		if (speedModifier != null && speedModifier.isDown()) {
			return true;
		}
		int bound = InputConstants.KEY_LALT;
		if (speedModifier != null) {
			InputConstants.Key key = InputConstants.getKey(speedModifier.saveString());
			if (key.getType() != McClient.keyboard()) {
				return false;
			}
			bound = key.getValue();
		}
		Minecraft client = Minecraft.getInstance();
		if (client == null || client.getWindow() == null) {
			return false;
		}
		//? if >=1.21.9 {
		/*long window = client.getWindow().handle();
		*///?} else {
		long window = client.getWindow().getWindow();
		//?}
		if (bound == InputConstants.KEY_LALT || bound == InputConstants.KEY_RALT) {
			return keyDown(window, InputConstants.KEY_LALT) || keyDown(window, InputConstants.KEY_RALT);
		}
		if (bound == InputConstants.KEY_LSHIFT || bound == InputConstants.KEY_RSHIFT) {
			return keyDown(window, InputConstants.KEY_LSHIFT) || keyDown(window, InputConstants.KEY_RSHIFT);
		}
		if (bound == InputConstants.KEY_LCONTROL || bound == InputConstants.KEY_RCONTROL) {
			return keyDown(window, InputConstants.KEY_LCONTROL) || keyDown(window, InputConstants.KEY_RCONTROL);
		}
		return false;
	}

	private static boolean keyDown(long window, int key) {
		//? if >=26.3 {
		/*return InputConstants.isKeyDown(key);
		*///?} elif >=26.1 {
		/*return InputConstants.isKeyDown(Minecraft.getInstance().getWindow(), key);
		*///?} else {
		return GLFW.glfwGetKey(window, key) == GLFW.GLFW_PRESS;
		//?}
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
				FlightClientController.overlay(client.player, McText.literal(
						"Forced Creative Flight is on this client, but this server has no /flight command. Put the same jar in the server mods folder and restart the server. Singleplayer uses the mods folder of this instance."
				), false);
			}
		}
		while (toggle.consumeClick()) {
			if (McClient.screen(client) != null || !state.canToggle) {
				continue;
			}
			if (FlightClientController.usesVanillaFlight(client.player)) {
				FlightClientController.overlay(client.player, McText.literal(
						"Forced Creative Flight is for survival only. Creative and spectator keep vanilla flight."), true);
				continue;
			}
			FlightRequests.sendToggle();
		}
		tickSpeedKeys(client);
		FlightClientController.updateGate(client);
	}

	private static void tickSpeedKeys(Minecraft client) {
		if (ClientPreferences.mode() != ClientPreferences.SpeedMode.KEYS || McClient.screen(client) != null || speedUp == null || speedDown == null) {
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
