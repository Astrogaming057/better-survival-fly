//? if fabric {
package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

import com.forcedflight.flight.FlightMining;

@Mixin(Player.class)
public abstract class PlayerDestroySpeedMixin {
	/**
	 * Vanilla already divided airborne mining by 5. Undo that after the method returns
	 * so we do not depend on onGround field vs method across versions.
	 */
	@Inject(
			method = "getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F",
			at = @At("RETURN"),
			cancellable = true
	)
	private void forcedflight$undoAirPenalty(BlockState state, CallbackInfoReturnable<Float> cir) {
		cir.setReturnValue(FlightMining.withoutAirPenalty((Player) (Object) this, cir.getReturnValueF()));
	}
}
//?}
