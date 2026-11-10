package com.forcedflight.client;

import com.forcedflight.ForcedFlight;
import com.forcedflight.config.ModConfig;
import com.forcedflight.network.FlightStatePayload;

public final class ClientFlightState {
	private static final ClientFlightState INSTANCE = new ClientFlightState();

	public boolean hasServerState;
	public boolean authorized;
	public boolean flying;
	public boolean allowControl;
	public boolean canToggle;
	public float flySpeed = ForcedFlight.VANILLA_FLY_SPEED;
	public float minSpeed = 0.01f;
	public float maxSpeed = ModConfig.DEFAULT_MAX_SPEED;
	public float speedStep = 0.01f;
	public boolean flyPenalty = true;
	public int clientTicks;
	public int hudUntilTick;

	private ClientFlightState() {
	}

	public static ClientFlightState get() {
		return INSTANCE;
	}

	public void reset() {
		hasServerState = false;
		authorized = false;
		flying = false;
		allowControl = false;
		canToggle = false;
		flyPenalty = true;
		hudUntilTick = 0;
	}

	public void apply(FlightStatePayload payload) {
		float previousSpeed = flySpeed;
		boolean wasAuthorized = hasServerState && authorized;
		hasServerState = true;
		authorized = payload.authorized();
		flying = payload.flying();
		allowControl = payload.allowControl();
		canToggle = payload.canToggle();
		flySpeed = payload.flySpeed();
		minSpeed = payload.minSpeed();
		maxSpeed = payload.maxSpeed();
		speedStep = payload.speedStep();
		flyPenalty = payload.flyPenalty();
		if (payload.announce() || wasAuthorized != authorized || Math.abs(previousSpeed - flySpeed) >= 0.0001f) {
			bumpHud();
		}
	}

	public void bumpHud() {
		hudUntilTick = clientTicks + Math.max(0, ModConfig.get().hudDurationTicks);
	}
}
