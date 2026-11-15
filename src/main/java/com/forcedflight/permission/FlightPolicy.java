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

	/**
	 * The integrated-world host can administer flight even when cheats are off.
	 * Dedicated-server players and LAN guests still need a vanilla operator flag.
	 */
	public static boolean canAdminister(boolean operator, boolean singleplayerHost) {
		return operator || singleplayerHost;
	}

	/**
	 * Vanilla divides mining speed by 5 while the player is off the ground.
	 * That slowdown can be removed for this mod's flight. Jumping, and creative or spectator flight, stay vanilla.
	 */
	public static boolean cancelFlyMiningPenalty(boolean penaltyEnabled, boolean flying, boolean onGround, boolean vanillaOwns) {
		return !penaltyEnabled && flying && !onGround && !vanillaOwns;
	}

	/**
	 * Fall damage can be removed when flight is granted but the player is not currently flying, so a fall
	 * with the toggle off does not hurt them. Creative and spectator stay vanilla.
	 */
	public static boolean cancelAuthorizedFallDamage(boolean fallDamageEnabled, boolean authorized, boolean flying, boolean vanillaOwns) {
		return !fallDamageEnabled && authorized && !flying && !vanillaOwns;
	}

	/**
	 * When enabled, landing on the ground does not clear creative-style flying.
	 * Double-tapping jump still turns flying off. Creative and spectator stay vanilla.
	 */
	public static boolean keepFlyingOnLand(boolean keepFlyingEnabled, boolean authorized, boolean vanillaOwns) {
		return keepFlyingEnabled && authorized && !vanillaOwns;
	}

	public static final int DAMAGE_FROM_PLAYERS = 0;
	public static final int DAMAGE_FROM_MOBS = 1;
	public static final int DAMAGE_FROM_BOTH = 2;
	public static final int DAMAGE_DROP_MIN_SECONDS = 1;
	public static final int DAMAGE_DROP_MAX_SECONDS = 300;
	public static final int DAMAGE_DROP_DEFAULT_SECONDS = 30;

	/** Players, mobs, or both. Anything else becomes both. */
	public static int clampDamageDropFrom(int from) {
		if (from == DAMAGE_FROM_PLAYERS || from == DAMAGE_FROM_MOBS) {
			return from;
		}
		return DAMAGE_FROM_BOTH;
	}

	/** One second through five minutes. */
	public static int clampDamageDropSeconds(int seconds) {
		if (seconds < DAMAGE_DROP_MIN_SECONDS) {
			return DAMAGE_DROP_MIN_SECONDS;
		}
		if (seconds > DAMAGE_DROP_MAX_SECONDS) {
			return DAMAGE_DROP_MAX_SECONDS;
		}
		return seconds;
	}

	public static double damageDropSlider(int seconds) {
		return (clampDamageDropSeconds(seconds) - 1) / (double) (DAMAGE_DROP_MAX_SECONDS - 1);
	}

	public static int damageDropSecondsFromSlider(double value) {
		double clamped = value < 0.0 ? 0.0 : (value > 1.0 ? 1.0 : value);
		return clampDamageDropSeconds(1 + (int) Math.round(clamped * (DAMAGE_DROP_MAX_SECONDS - 1)));
	}

	/**
	 * Environment damage never counts. A player attacker and a mob attacker are separate,
	 * so a hit matches only the source the server selected.
	 */
	public static boolean damageDropsFlight(boolean enabled, int from, boolean attackerIsPlayer, boolean attackerIsMob) {
		if (!enabled || (!attackerIsPlayer && !attackerIsMob)) {
			return false;
		}
		int source = clampDamageDropFrom(from);
		if (source == DAMAGE_FROM_PLAYERS) {
			return attackerIsPlayer;
		}
		if (source == DAMAGE_FROM_MOBS) {
			return attackerIsMob;
		}
		return true;
	}

	public static String damageDropFromName(int from) {
		int source = clampDamageDropFrom(from);
		if (source == DAMAGE_FROM_PLAYERS) {
			return "Players";
		}
		if (source == DAMAGE_FROM_MOBS) {
			return "Mobs";
		}
		return "Players & mobs";
	}

	public static String damageDropFromId(int from) {
		int source = clampDamageDropFrom(from);
		if (source == DAMAGE_FROM_PLAYERS) {
			return "players";
		}
		if (source == DAMAGE_FROM_MOBS) {
			return "mobs";
		}
		return "both";
	}

	public static int damageDropFromId(String raw) {
		if (raw == null) {
			return DAMAGE_FROM_BOTH;
		}
		String value = raw.trim().toLowerCase(java.util.Locale.ROOT);
		if (value.equals("players") || value.equals("player") || value.equals("0")) {
			return DAMAGE_FROM_PLAYERS;
		}
		if (value.equals("mobs") || value.equals("mob") || value.equals("1")) {
			return DAMAGE_FROM_MOBS;
		}
		return DAMAGE_FROM_BOTH;
	}

	/** "30 seconds", "1 minute", or "1 minute 30 seconds". */
	public static String damageDropDuration(int seconds) {
		int clamped = clampDamageDropSeconds(seconds);
		if (clamped < 60) {
			return clamped == 1 ? "1 second" : clamped + " seconds";
		}
		int minutes = clamped / 60;
		int remainder = clamped % 60;
		String minutesText = minutes == 1 ? "1 minute" : minutes + " minutes";
		if (remainder == 0) {
			return minutesText;
		}
		String secondsText = remainder == 1 ? "1 second" : remainder + " seconds";
		return minutesText + " " + secondsText;
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
