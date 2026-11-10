package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;

import com.forcedflight.client.ClientFlightState;
import com.forcedflight.client.FlightClientController;

@Mixin(value = ClientPacketListener.class, priority = 100)
public abstract class ClientPacketListenerMixin {
	@Inject(method = "handlePlayerAbilities", at = @At("TAIL"))
	private void forcedflight$afterAbilities(ClientboundPlayerAbilitiesPacket packet, CallbackInfo ci) {
		if (Minecraft.getInstance().player != null && ClientFlightState.get().authorized) {
			FlightClientController.enforceLocal(Minecraft.getInstance().player);
		}
	}
}
