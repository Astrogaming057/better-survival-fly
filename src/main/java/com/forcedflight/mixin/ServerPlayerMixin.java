package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;

import com.forcedflight.flight.FlightManager;

@Mixin(value = ServerPlayer.class, priority = 2000)
public abstract class ServerPlayerMixin {
	@Inject(method = "onUpdateAbilities", at = @At("HEAD"))
	private void forcedflight$restoreBeforeSync(CallbackInfo ci) {
		FlightManager.restoreCopiedPlayer((ServerPlayer) (Object) this);
	}
}
