package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.level.GameType;

import com.forcedflight.flight.FlightManager;

@Mixin(value = ServerPlayerGameMode.class, priority = 100)
public abstract class ServerPlayerGameModeMixin {
	@Shadow
	protected ServerPlayer player;

	@Inject(method = "setGameModeForPlayer", at = @At("TAIL"))
	private void forcedflight$gameMode(GameType mode, GameType previous, CallbackInfo ci) {
		FlightManager.onWorldChange(this.player);
	}
}
