//? if fabric {
package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.world.entity.player.Player;

import com.forcedflight.flight.FlightMining;

@Mixin(Player.class)
public abstract class PlayerDestroySpeedMixin {
	/**
	 * Block breaking divides speed by 5 while the player is off the ground.
	 * Reporting the player as on the ground skips that division while mod flight has the penalty turned off.
	 */
	//? if >=1.20 {
	@Redirect(
			method = "getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;onGround()Z")
	)
	//?} else {
	/*@Redirect(
			method = "getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/world/entity/Entity;onGround:Z",
					opcode = org.objectweb.asm.Opcodes.GETFIELD
			)
	)
	*///?}
	private boolean forcedflight$ignoreAirMiningPenalty(Player player) {
		if (FlightMining.shouldCancel(player)) {
			return true;
		}
		return com.forcedflight.compat.McPlayer.onGround(player);
	}
}
//?}
