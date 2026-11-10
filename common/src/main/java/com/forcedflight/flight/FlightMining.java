package com.forcedflight.flight;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import com.forcedflight.ForcedFlightServices;
import com.forcedflight.config.ModConfig;
import com.forcedflight.permission.FlightPolicy;

/**
 * Decides when to undo vanilla's airborne mining slowdown. The client hook is separate so a dedicated
 * server never loads client classes.
 */
public final class FlightMining {
	private FlightMining() {
	}

	public static boolean shouldCancel(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			return FlightPolicy.cancelFlyMiningPenalty(
					ModConfig.get().flyPenalty,
					serverPlayer.getAbilities().flying,
					serverPlayer.onGround(),
					FlightManager.usesVanillaFlight(serverPlayer)
			);
		}
		return ForcedFlightServices.cancelClientFlyPenalty != null
				&& ForcedFlightServices.cancelClientFlyPenalty.test(player);
	}
}
