package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import com.forcedflight.flight.FlightManager;

@Mixin(PlayerList.class)
public abstract class PlayerListMixin {
	@Inject(method = "sendPlayerPermissionLevel(Lnet/minecraft/server/level/ServerPlayer;)V", at = @At("TAIL"))
	private void forcedflight$afterPermissionLevel(ServerPlayer player, CallbackInfo ci) {
		FlightManager.onPermissionLevelChanged(player);
	}
}
