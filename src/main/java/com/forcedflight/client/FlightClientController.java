package com.forcedflight.client;
import com.forcedflight.compat.McText;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.level.GameType;

import com.forcedflight.ForcedFlightServices;
import com.forcedflight.compat.McClient;
import com.forcedflight.network.FlightRequests;
import com.forcedflight.network.FlightStatePayload;
import com.forcedflight.permission.FlightPolicy;
import com.forcedflight.permission.FlightSpeed;

public final class FlightClientController {
	private static volatile boolean consumeScroll;

	private FlightClientController() {
	}

	public static boolean consumeScroll() {
		return consumeScroll;
	}

	public static boolean usesVanillaFlight(LocalPlayer player) {
		return vanillaOwns(player);
	}

	public static void installMiningHook() {
		ForcedFlightServices.cancelClientFlyPenalty = player -> {
			if (!(player instanceof LocalPlayer local)) {
				return false;
			}
			ClientFlightState state = ClientFlightState.get();
			return state.hasServerState && FlightPolicy.cancelFlyMiningPenalty(
					state.flyPenalty,
					local.getAbilities().flying,
					com.forcedflight.compat.McPlayer.onGround(local),
					vanillaOwns(local)
			);
		};
	}

	public static void overlay(LocalPlayer player, Component text, boolean actionBar) {
		//? if >=26.1 {
		/*if (actionBar) {
			player.sendOverlayMessage(text);
		} else {
			player.sendSystemMessage(text);
		}
		*///?} else {
		player.displayClientMessage(text, actionBar);
		//?}
	}

	public static void applyServerState(FlightStatePayload payload, LocalPlayer player) {
		ClientFlightState state = ClientFlightState.get();
		boolean announce = payload.announce();
		state.apply(payload);
		if (player != null) {
			applyToPlayer(player);
			if (announce) {
				overlay(player, McText.literal(payload.authorized() ? "Flight enabled" : "Flight disabled"), true);
			}
		}
		updateGate(Minecraft.getInstance());
	}

	public static void enforceLocal(LocalPlayer player) {
		ClientFlightState state = ClientFlightState.get();
		if (!state.hasServerState || !state.authorized || vanillaOwns(player)) {
			return;
		}
		applyToPlayer(player);
	}

	public static void onLocalFlyingSample(LocalPlayer player) {
		ClientFlightState state = ClientFlightState.get();
		if (!state.hasServerState || !state.authorized || vanillaOwns(player)) {
			return;
		}
		state.flying = player.getAbilities().flying;
	}

	public static void adjustFromScroll(double vertical) {
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || vanillaOwns(client.player)) {
			return;
		}
		ClientFlightState state = ClientFlightState.get();
		if (!state.hasServerState || !state.authorized) {
			return;
		}
		int steps = (int) Math.round(vertical);
		if (steps == 0) {
			steps = vertical > 0 ? 1 : -1;
		}
		steps = Math.max(-5, Math.min(5, steps));
		float next = FlightSpeed.step(state.flySpeed, steps, state.minSpeed, state.maxSpeed, state.speedStep);
		if (Math.abs(next - state.flySpeed) < 0.0001f) {
			return;
		}
		state.flySpeed = next;
		state.bumpHud();
		client.player.getAbilities().setFlyingSpeed(next);
		FlightRequests.sendSpeed(next);
	}

	public static void updateGate(Minecraft client) {
		boolean gate = false;
		if (client != null && client.player != null && McClient.screen(client) == null && client.gameMode != null) {
			ClientFlightState state = ClientFlightState.get();
			gate = state.hasServerState
					&& state.authorized
					&& !vanillaOwns(client.player)
					&& ClientPreferences.mode() == ClientPreferences.SpeedMode.SCROLL;
		}
		consumeScroll = gate;
	}

	private static void applyToPlayer(LocalPlayer player) {
		ClientFlightState state = ClientFlightState.get();
		if (!state.authorized || vanillaOwns(player)) {
			return;
		}
		Abilities abilities = player.getAbilities();
		abilities.mayfly = true;
		abilities.flying = state.flying;
		if (Math.abs(abilities.getFlyingSpeed() - state.flySpeed) >= 0.0001f) {
			abilities.setFlyingSpeed(state.flySpeed);
		}
	}

	private static boolean vanillaOwns(LocalPlayer player) {
		Minecraft client = Minecraft.getInstance();
		if (client.gameMode == null) {
			return false;
		}
		GameType mode = client.gameMode.getPlayerMode();
		return mode == GameType.CREATIVE || mode == GameType.SPECTATOR;
	}
}
