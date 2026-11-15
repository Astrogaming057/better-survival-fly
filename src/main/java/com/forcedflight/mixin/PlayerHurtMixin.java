package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;

import com.forcedflight.flight.FlightManager;

@Mixin(Player.class)
public abstract class PlayerHurtMixin {
	//? if >=1.21.2 {
	/*@Inject(method = "hurtServer", at = @At("RETURN"))
	private void forcedflight$damageDrop(net.minecraft.server.level.ServerLevel level, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		forcedflight$afterHurt(source, amount, cir.getReturnValueZ());
	}
	*///?} else {
	@Inject(method = "hurt", at = @At("RETURN"))
	private void forcedflight$damageDrop(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
		forcedflight$afterHurt(source, amount, cir.getReturnValueZ());
	}
	//?}

	@Unique
	private void forcedflight$afterHurt(DamageSource source, float amount, boolean applied) {
		if (!applied || amount <= 0.0f || !((Object) this instanceof ServerPlayer player)) {
			return;
		}
		FlightManager.onDamaged(player, source);
	}
}
