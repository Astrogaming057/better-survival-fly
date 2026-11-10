package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

import com.forcedflight.config.ModConfig;
import com.forcedflight.flight.FlightManager;
import com.forcedflight.permission.FlightPermissionStore;
import com.forcedflight.permission.FlightPolicy;
import com.forcedflight.permission.PlayerFlightRecord;

@Mixin(Player.class)
public abstract class PlayerFallMixin {
	@Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
	private void forcedflight$fallDamage(float fallDistance, float multiplier, DamageSource source, CallbackInfoReturnable<Boolean> cir) {
		if (!((Object) this instanceof ServerPlayer player) || FlightManager.usesVanillaFlight(player)) {
			return;
		}
		PlayerFlightRecord record = FlightPermissionStore.get().get(player.getUUID()).orElse(null);
		boolean authorized = record != null && record.authorized();
		if (!FlightPolicy.cancelAuthorizedFallDamage(ModConfig.get().fallDamage, authorized, player.getAbilities().flying, false)) {
			return;
		}
		player.resetFallDistance();
		cir.setReturnValue(false);
	}
}
