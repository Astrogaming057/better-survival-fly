package com.forcedflight.permission;

/**
 * Flight-relevant ability fields plus the unrelated flags enforcement must leave alone.
 */
public record AbilitySnapshot(
		boolean allowFlying,
		boolean flying,
		float flySpeed,
		boolean invulnerable,
		boolean creativeMode,
		boolean allowModifyWorld,
		float walkSpeed
) {
	public boolean sameFlightAs(AbilitySnapshot other) {
		return allowFlying == other.allowFlying
				&& flying == other.flying
				&& Math.abs(flySpeed - other.flySpeed) < 0.0001f
				&& invulnerable == other.invulnerable
				&& creativeMode == other.creativeMode
				&& allowModifyWorld == other.allowModifyWorld
				&& Math.abs(walkSpeed - other.walkSpeed) < 0.0001f;
	}
}
