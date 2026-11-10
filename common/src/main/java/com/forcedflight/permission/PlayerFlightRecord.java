package com.forcedflight.permission;

import java.util.UUID;

/**
 * Per-player flight permission. Authorization, the airborne flag, and independent control are separate.
 */
public final class PlayerFlightRecord {
	private final UUID uuid;
	private boolean authorized;
	private boolean flying;
	private boolean allowControl;
	private float flySpeed;

	public PlayerFlightRecord(UUID uuid, float flySpeed) {
		this.uuid = uuid;
		this.flySpeed = flySpeed;
	}

	public UUID uuid() {
		return uuid;
	}

	public boolean authorized() {
		return authorized;
	}

	public void setAuthorized(boolean authorized) {
		this.authorized = authorized;
	}

	public boolean flying() {
		return flying;
	}

	public void setFlying(boolean flying) {
		this.flying = flying;
	}

	public boolean allowControl() {
		return allowControl;
	}

	public void setAllowControl(boolean allowControl) {
		this.allowControl = allowControl;
	}

	public float flySpeed() {
		return flySpeed;
	}

	public void setFlySpeed(float flySpeed) {
		this.flySpeed = flySpeed;
	}

	public PlayerFlightRecord copy() {
		PlayerFlightRecord copy = new PlayerFlightRecord(uuid, flySpeed);
		copy.authorized = authorized;
		copy.flying = flying;
		copy.allowControl = allowControl;
		return copy;
	}
}
