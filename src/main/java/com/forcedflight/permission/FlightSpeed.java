package com.forcedflight.permission;

import com.forcedflight.ForcedFlight;

public final class FlightSpeed {
	private FlightSpeed() {
	}

	public static float clamp(float speed, float min, float max) {
		float low = Math.max(ForcedFlight.HARD_MIN_SPEED, Math.min(min, max));
		float high = Math.min(ForcedFlight.HARD_MAX_SPEED, Math.max(min, max));
		if (low > high) {
			float swap = low;
			low = high;
			high = swap;
		}
		if (!Float.isFinite(speed)) {
			speed = ForcedFlight.VANILLA_FLY_SPEED;
		}
		if (speed < low) {
			speed = low;
		} else if (speed > high) {
			speed = high;
		}
		return Math.round(speed * 10000f) / 10000f;
	}

	public static float step(float current, int direction, float min, float max, float increment) {
		float stepSize = increment;
		if (!Float.isFinite(stepSize) || stepSize <= 0f) {
			stepSize = 0.01f;
		}
		int signed = direction == 0 ? 0 : (direction > 0 ? 1 : -1);
		if (Math.abs(direction) > 1) {
			signed = Math.max(-5, Math.min(5, direction));
		}
		float next = current + (signed * stepSize);
		return clamp(next, min, max);
	}

	/** 1.00 is vanilla creative speed (0.05). */
	public static float multiplier(float flySpeed) {
		return flySpeed / ForcedFlight.VANILLA_FLY_SPEED;
	}
}
