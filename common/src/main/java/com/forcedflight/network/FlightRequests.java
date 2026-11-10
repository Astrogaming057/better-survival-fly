package com.forcedflight.network;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.forcedflight.ForcedFlightServices;
import com.forcedflight.flight.FlightManager;

public final class FlightRequests {
	private FlightRequests() {
	}

	public static void handle(ServerPlayer player, FlightRequestPayload payload) {
		if (payload.action() == FlightRequestPayload.TOGGLE) {
			if (!FlightManager.tryToggleFromClient(player)) {
				player.sendSystemMessage(Component.literal("You do not have permission to toggle flight."), false);
			}
			return;
		}
		if (payload.action() == FlightRequestPayload.SET_SPEED) {
			FlightManager.trySetSpeed(player, payload.flySpeed());
		}
	}

	public static void sendToggle() {
		if (ForcedFlightServices.sendRequest != null) {
			ForcedFlightServices.sendRequest.accept(new FlightRequestPayload(FlightRequestPayload.TOGGLE, 0f));
		}
	}

	public static void sendSpeed(float flySpeed) {
		if (ForcedFlightServices.sendRequest != null) {
			ForcedFlightServices.sendRequest.accept(new FlightRequestPayload(FlightRequestPayload.SET_SPEED, flySpeed));
		}
	}
}
