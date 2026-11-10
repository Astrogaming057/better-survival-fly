package com.forcedflight.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

import com.forcedflight.ForcedFlightServices;
import com.forcedflight.client.FlightClientHooks;
import com.forcedflight.client.FlightHud;
import com.forcedflight.network.FlightStatePayload;

public final class ForcedFlightFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ForcedFlightServices.sendRequest = ClientPlayNetworking::send;
		com.forcedflight.client.FlightClientController.installMiningHook();
		FlightClientHooks.toggle = KeyBindingHelper.registerKeyBinding(FlightClientHooks.createToggle());
		ClientPlayNetworking.registerGlobalReceiver(FlightStatePayload.TYPE, (payload, context) ->
				com.forcedflight.client.FlightClientController.applyServerState(payload, context.player()));
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> FlightClientHooks.onJoin());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FlightClientHooks.onDisconnect());
		HudRenderCallback.EVENT.register((graphics, tickCounter) -> FlightHud.render(graphics));
		ClientTickEvents.END_CLIENT_TICK.register(FlightClientHooks::tick);
	}
}
