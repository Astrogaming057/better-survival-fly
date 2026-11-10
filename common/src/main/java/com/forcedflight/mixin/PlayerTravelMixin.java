package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import com.forcedflight.flight.FlightManager;

@Mixin(value = Player.class, priority = 2000)
public abstract class PlayerTravelMixin {
	@Inject(method = "travel", at = @At("HEAD"))
	private void forcedflight$beforeTravel(Vec3 movementInput, CallbackInfo ci) {
		if ((Object) this instanceof ServerPlayer player) {
			FlightManager.beforeTravel(player);
		}
	}
}
