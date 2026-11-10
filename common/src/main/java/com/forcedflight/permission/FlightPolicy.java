package com.forcedflight.permission;

/**
 * Pure permission and enforcement rules. Minecraft calls into this so the same decisions can be tested
 * without a running game.
 */
public final class FlightPolicy {
	private FlightPolicy() {
	}

	public static boolean canAdminister(boolean operator) {
		return operator;
	}

	public static boolean canToggleSelf(boolean operator, boolean allowControl) {
		return operator || allowControl;
	}

	/**
	 * Flips authorization. Enabling also lifts the player off; disabling drops them.
	 * Independent-control permission is left as it is.
	 *
	 * @return the new authorization value
	 */
	public static boolean toggleAuthorization(PlayerFlightRecord record) {
		if (record.authorized()) {
			record.setAuthorized(false);
			record.setFlying(false);
			return false;
		}
		record.setAuthorized(true);
		record.setFlying(true);
		return true;
	}

	public static void enableFlight(PlayerFlightRecord record) {
		record.setAuthorized(true);
		record.setFlying(true);
	}

	public static void disableFlight(PlayerFlightRecord record) {
		record.setAuthorized(false);
		record.setFlying(false);
	}

	public static void grantControl(PlayerFlightRecord record) {
		record.setAllowControl(true);
	}

	/** Removes independent control and does not change an existing flight grant. */
	public static void revokeControl(PlayerFlightRecord record) {
		record.setAllowControl(false);
	}

	/**
	 * Restores an authorized player's flight flags. Creative and spectator are left to vanilla.
	 * While sleeping, dead, or riding, the airborne flag is suppressed but the grant stays.
	 * Returns the original snapshot when nothing needs to change.
	 */
	public static AbilitySnapshot reconcile(AbilitySnapshot current, PlayerFlightRecord record, boolean vanillaOwnsFlight, boolean suppressFlying) {
		if (vanillaOwnsFlight || record == null || !record.authorized()) {
			return current;
		}
		AbilitySnapshot next = new AbilitySnapshot(
				true,
				record.flying() && !suppressFlying,
				record.flySpeed(),
				current.invulnerable(),
				current.creativeMode(),
				current.allowModifyWorld(),
				current.walkSpeed()
		);
		return next.sameFlightAs(current) ? current : next;
	}
}
