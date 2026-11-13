package com.forcedflight.fabric;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
//? if >=26.1 {
/*import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
*///?} else {
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
//?}

import com.forcedflight.ForcedFlightServices;
import com.forcedflight.client.FlightClientHooks;
import com.forcedflight.client.FlightHud;
import com.forcedflight.client.ClientRoster;
import com.forcedflight.compat.McIds;
import com.forcedflight.network.FlightMemberPayload;
import com.forcedflight.network.FlightRequestPayload;
import com.forcedflight.network.FlightRosterPayload;
import com.forcedflight.network.FlightStatePayload;
//? if <1.20.5 {
/*import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
*///?}

public final class ForcedFlightFabricClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		registerNetwork();
		com.forcedflight.client.FlightClientController.installMiningHook();
		FlightClientHooks.createSpeedKeys();
		com.forcedflight.client.ClientPreferences.load();
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> FlightClientHooks.onJoin());
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> FlightClientHooks.onDisconnect());
		//? if >=26.1 {
		/*FlightClientHooks.toggle = KeyMappingHelper.registerKeyMapping(FlightClientHooks.createToggle());
		KeyMappingHelper.registerKeyMapping(FlightClientHooks.speedModifier);
		KeyMappingHelper.registerKeyMapping(FlightClientHooks.speedUp);
		KeyMappingHelper.registerKeyMapping(FlightClientHooks.speedDown);
		HudElementRegistry.addLast(McIds.id("hud"), (graphics, delta) -> FlightHud.render(graphics));
		*///?} else {
		FlightClientHooks.toggle = KeyBindingHelper.registerKeyBinding(FlightClientHooks.createToggle());
		KeyBindingHelper.registerKeyBinding(FlightClientHooks.speedModifier);
		KeyBindingHelper.registerKeyBinding(FlightClientHooks.speedUp);
		KeyBindingHelper.registerKeyBinding(FlightClientHooks.speedDown);
		//?}
		//? if >=1.20 && <26.1 {
		HudRenderCallback.EVENT.register((graphics, tickCounter) -> FlightHud.render(graphics));
		//?}
		//? if <1.20 {
		/*HudRenderCallback.EVENT.register((pose, tickCounter) -> FlightHud.render(pose));
		*///?}
		ClientTickEvents.END_CLIENT_TICK.register(FlightClientHooks::tick);
	}

	private static void registerNetwork() {
		//? if >=1.20.5 {
		ForcedFlightServices.sendRequest = ClientPlayNetworking::send;
		ForcedFlightServices.sendMember = ClientPlayNetworking::send;
		ClientPlayNetworking.registerGlobalReceiver(FlightStatePayload.TYPE, (payload, context) ->
				com.forcedflight.client.FlightClientController.applyServerState(payload, context.player()));
		ClientPlayNetworking.registerGlobalReceiver(FlightRosterPayload.TYPE, (payload, context) ->
				ClientRoster.apply(payload));
		//?} else {
		/*ForcedFlightServices.sendRequest = ForcedFlightFabricClient::sendRequest;
		ForcedFlightServices.sendMember = ForcedFlightFabricClient::sendMember;
		ClientPlayNetworking.registerGlobalReceiver(McIds.id("state"), (client, handler, buf, responseSender) -> {
			FlightStatePayload payload = FlightStatePayload.read(buf);
			client.execute(() -> com.forcedflight.client.FlightClientController.applyServerState(payload, client.player));
		});
		ClientPlayNetworking.registerGlobalReceiver(McIds.id("roster"), (client, handler, buf, responseSender) -> {
			FlightRosterPayload payload = FlightRosterPayload.read(buf);
			client.execute(() -> ClientRoster.apply(payload));
		});
		*///?}
		ForcedFlightServices.acceptRoster = ClientRoster::apply;
	}

	//? if <1.20.5 {
	/*private static void sendRequest(FlightRequestPayload payload) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		FlightRequestPayload.write(buf, payload);
		ClientPlayNetworking.send(McIds.id("request"), buf);
	}

	private static void sendMember(FlightMemberPayload payload) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		FlightMemberPayload.write(buf, payload);
		ClientPlayNetworking.send(McIds.id("member"), buf);
	}
	*///?}
}
