package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;

import com.forcedflight.flight.FlightManager;

@Mixin(value = ServerPlayer.class, priority = 100)
public abstract class ServerPlayerCopyMixin {
	@Inject(method = "restoreFrom", at = @At("TAIL"))
	private void forcedflight$afterCopy(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
		FlightManager.restoreCopiedPlayer((ServerPlayer) (Object) this);
	}
}
