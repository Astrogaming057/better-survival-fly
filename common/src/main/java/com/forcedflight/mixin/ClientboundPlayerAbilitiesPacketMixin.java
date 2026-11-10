package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.world.entity.player.Abilities;

import com.forcedflight.flight.FlightManager;

@Mixin(value = ClientboundPlayerAbilitiesPacket.class, priority = 2000)
public abstract class ClientboundPlayerAbilitiesPacketMixin {
	@Inject(method = "<init>(Lnet/minecraft/world/entity/player/Abilities;)V", at = @At("HEAD"))
	private static void forcedflight$beforePacket(Abilities abilities, CallbackInfo ci) {
		FlightManager.beforeAbilityPacket(abilities);
	}
}
