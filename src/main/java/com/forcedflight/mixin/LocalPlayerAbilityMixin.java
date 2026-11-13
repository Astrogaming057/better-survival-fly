package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.player.LocalPlayer;

import com.forcedflight.client.FlightClientController;

@Mixin(value = LocalPlayer.class, priority = 100)
public abstract class LocalPlayerAbilityMixin {
	@Inject(method = "onUpdateAbilities", at = @At("TAIL"))
	private void forcedflight$sampleFlying(CallbackInfo ci) {
		FlightClientController.onLocalFlyingSample((LocalPlayer) (Object) this);
	}
}
