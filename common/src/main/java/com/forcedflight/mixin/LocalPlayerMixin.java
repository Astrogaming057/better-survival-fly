package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.player.LocalPlayer;

import com.forcedflight.client.FlightClientController;

@Mixin(value = LocalPlayer.class, priority = 2000)
public abstract class LocalPlayerMixin {
	@Inject(method = "aiStep", at = @At("HEAD"))
	private void forcedflight$enforce(CallbackInfo ci) {
		FlightClientController.enforceLocal((LocalPlayer) (Object) this);
	}
}
