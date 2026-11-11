package com.forcedflight.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

import com.forcedflight.ForcedFlight;
import com.forcedflight.ForcedFlightBootstrap;
import com.forcedflight.ForcedFlightServices;
import com.forcedflight.command.FlightCommand;
import com.forcedflight.flight.FlightManager;
import com.forcedflight.network.FlightMemberPayload;
import com.forcedflight.network.FlightRequestPayload;
import com.forcedflight.network.FlightRequests;
import com.forcedflight.network.FlightRosterPayload;
import com.forcedflight.network.FlightStatePayload;
import com.forcedflight.permission.FlightPermissionStore;

public final class ForcedFlightFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		ForcedFlightServices.use(FabricLoader.getInstance().getConfigDir().resolve(ForcedFlight.MOD_ID));
		ForcedFlightServices.sendState = ServerPlayNetworking::send;
		ForcedFlightServices.sendRoster = ServerPlayNetworking::send;
		ForcedFlightBootstrap.init();
		PayloadTypeRegistry.playC2S().register(FlightRequestPayload.TYPE, FlightRequestPayload.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(FlightStatePayload.TYPE, FlightStatePayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(FlightMemberPayload.TYPE, FlightMemberPayload.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(FlightRosterPayload.TYPE, FlightRosterPayload.STREAM_CODEC);
		ServerPlayNetworking.registerGlobalReceiver(FlightRequestPayload.TYPE, (payload, context) ->
				FlightRequests.handle(context.player(), payload));
		ServerPlayNetworking.registerGlobalReceiver(FlightMemberPayload.TYPE, (payload, context) ->
				FlightRequests.handleMember(context.player(), payload));
		CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) -> FlightCommand.register(dispatcher));
		ServerLifecycleEvents.SERVER_STARTED.register(FlightManager::bindServer);
		ServerLifecycleEvents.SERVER_STOPPING.register(server -> FlightPermissionStore.get().saveNow());
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> FlightManager.clearRuntime());
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			FlightPermissionStore.get().tickAutosave();
			FlightManager.deliverJoinState(server);
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> FlightManager.onJoin(handler.getPlayer()));
		ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> FlightManager.onDisconnect(handler.getPlayer()));
		ServerPlayerEvents.AFTER_RESPAWN.register((oldPlayer, newPlayer, alive) -> FlightManager.onRespawn(newPlayer));
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> FlightManager.onWorldChange(player));
	}
}
