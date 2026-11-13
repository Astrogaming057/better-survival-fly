package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;

import com.forcedflight.client.ClientFlightState;
import com.forcedflight.client.FlightClientController;
import com.forcedflight.permission.FlightPolicy;

@Mixin(value = LocalPlayer.class, priority = 2000)
public abstract class LocalPlayerMixin {
	@Inject(method = "aiStep", at = @At("HEAD"))
	private void forcedflight$enforce(CallbackInfo ci) {
		FlightClientController.enforceLocal((LocalPlayer) (Object) this);
	}

	/**
	 * Vanilla clears flying when you touch the ground. The second isAlwaysFlying check skips that clear.
	 * When keep-flying is on, treat it the same so landing does not cancel flight. Double-tap jump still works.
	 */
	//? if >=1.21.11 {
	/*@Redirect(
			method = "aiStep",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isSpectator()Z",
					ordinal = 1
			)
	)
	private boolean forcedflight$keepFlyingOnLand(MultiPlayerGameMode mode) {
		if (mode.isSpectator()) {
	*///?} else {
	@Redirect(
			method = "aiStep",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isAlwaysFlying()Z",
					ordinal = 1
			)
	)
	private boolean forcedflight$keepFlyingOnLand(MultiPlayerGameMode mode) {
		if (mode.isAlwaysFlying()) {
	//?}
			return true;
		}
		LocalPlayer player = (LocalPlayer) (Object) this;
		ClientFlightState state = ClientFlightState.get();
		return state.hasServerState
				&& FlightPolicy.keepFlyingOnLand(state.keepFlying, state.authorized, FlightClientController.usesVanillaFlight(player));
	}
}
