package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.entity.player.Player;

import com.forcedflight.flight.FlightMining;

@Mixin(Player.class)
public abstract class PlayerDestroySpeedMixin {
	/**
	 * Block breaking calls getDigSpeed, which divides speed by 5 when the player is off the ground.
	 * Reporting the player as on the ground skips that division while mod flight has the penalty turned off.
	 */
	@Redirect(
			method = "getDigSpeed(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/BlockPos;)F",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;onGround()Z")
	)
	private boolean forcedflight$ignoreAirMiningPenalty(Player player) {
		if (FlightMining.shouldCancel(player)) {
			return true;
		}
		return player.onGround();
	}
}
