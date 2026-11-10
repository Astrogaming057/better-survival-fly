package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;

import com.forcedflight.flight.FlightManager;

@Mixin(value = ServerGamePacketListenerImpl.class, priority = 100)
public abstract class ServerGamePacketListenerImplMixin {
	@Shadow
	public ServerPlayer player;

	@Inject(method = "handlePlayerAbilities", at = @At("TAIL"))
	private void forcedflight$clientFlying(ServerboundPlayerAbilitiesPacket packet, CallbackInfo ci) {
		FlightManager.handleClientFlying(this.player, packet.isFlying());
	}
}
