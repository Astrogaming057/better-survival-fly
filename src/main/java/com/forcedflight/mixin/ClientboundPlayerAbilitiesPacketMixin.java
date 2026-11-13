package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.network.protocol.game.ClientboundPlayerAbilitiesPacket;
import net.minecraft.world.entity.player.Abilities;

import com.forcedflight.flight.FlightManager;

@Mixin(value = ClientboundPlayerAbilitiesPacket.class, priority = 2000)
public abstract class ClientboundPlayerAbilitiesPacketMixin {
	/**
	 * Mixin forbids @Inject at HEAD on constructors. Rewrite the Abilities argument
	 * before the packet copies mayfly/flying so enforcement still wins.
	 */
	@ModifyVariable(
			method = "<init>(Lnet/minecraft/world/entity/player/Abilities;)V",
			at = @At("HEAD"),
			argsOnly = true
	)
	private static Abilities forcedflight$beforePacket(Abilities abilities) {
		FlightManager.beforeAbilityPacket(abilities);
		return abilities;
	}
}
