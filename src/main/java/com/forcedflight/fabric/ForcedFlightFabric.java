package com.forcedflight.fabric;

import net.fabricmc.api.ModInitializer;
//? if >=26.1 {
/*import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
*///?} else {
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
//?}
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.level.ServerPlayer;
//? if >=1.19.2 {
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
//?}

import com.forcedflight.ForcedFlight;
import com.forcedflight.ForcedFlightBootstrap;
import com.forcedflight.ForcedFlightServices;
import com.forcedflight.command.FlightCommand;
import com.forcedflight.compat.McIds;
import com.forcedflight.flight.FlightManager;
import com.forcedflight.network.FlightMemberPayload;
import com.forcedflight.network.FlightRequestPayload;
import com.forcedflight.network.FlightRequests;
import com.forcedflight.network.FlightRosterPayload;
import com.forcedflight.network.FlightStatePayload;
import com.forcedflight.permission.FlightPermissionStore;
//? if >=1.20.5 {
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//?} else {
/*import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.minecraft.network.FriendlyByteBuf;
*///?}

public final class ForcedFlightFabric implements ModInitializer {
	@Override
	public void onInitialize() {
		ForcedFlightServices.use(FabricLoader.getInstance().getConfigDir().resolve(ForcedFlight.MOD_ID));
		ForcedFlightBootstrap.init();
		registerNetwork();
		//? if >=1.19.2 {
		CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) -> FlightCommand.register(dispatcher));
		//?} else {
		/*net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> FlightCommand.register(dispatcher));
		*///?}
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
		//? if >=26.1 {
		/*ServerEntityLevelChangeEvents.AFTER_PLAYER_CHANGE_LEVEL.register((player, origin, destination) -> FlightManager.onWorldChange(player));
		*///?} else {
		ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register((player, origin, destination) -> FlightManager.onWorldChange(player));
		//?}
	}

	private static void registerNetwork() {
		//? if >=1.20.5 {
		ForcedFlightServices.sendState = ServerPlayNetworking::send;
		ForcedFlightServices.sendRoster = ServerPlayNetworking::send;
		//? if >=26.1 {
		/*PayloadTypeRegistry.serverboundPlay().register(FlightRequestPayload.TYPE, FlightRequestPayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(FlightStatePayload.TYPE, FlightStatePayload.STREAM_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(FlightMemberPayload.TYPE, FlightMemberPayload.STREAM_CODEC);
		PayloadTypeRegistry.clientboundPlay().register(FlightRosterPayload.TYPE, FlightRosterPayload.STREAM_CODEC);
		*///?} else {
		PayloadTypeRegistry.playC2S().register(FlightRequestPayload.TYPE, FlightRequestPayload.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(FlightStatePayload.TYPE, FlightStatePayload.STREAM_CODEC);
		PayloadTypeRegistry.playC2S().register(FlightMemberPayload.TYPE, FlightMemberPayload.STREAM_CODEC);
		PayloadTypeRegistry.playS2C().register(FlightRosterPayload.TYPE, FlightRosterPayload.STREAM_CODEC);
		//?}
		ServerPlayNetworking.registerGlobalReceiver(FlightRequestPayload.TYPE, (payload, context) ->
				FlightRequests.handle(context.player(), payload));
		ServerPlayNetworking.registerGlobalReceiver(FlightMemberPayload.TYPE, (payload, context) ->
				FlightRequests.handleMember(context.player(), payload));
		//?} else {
		/*ForcedFlightServices.sendState = ForcedFlightFabric::sendState;
		ForcedFlightServices.sendRoster = ForcedFlightFabric::sendRoster;
		ServerPlayNetworking.registerGlobalReceiver(McIds.id("request"), (server, player, handler, buf, responseSender) -> {
			FlightRequestPayload payload = FlightRequestPayload.read(buf);
			server.execute(() -> FlightRequests.handle(player, payload));
		});
		ServerPlayNetworking.registerGlobalReceiver(McIds.id("member"), (server, player, handler, buf, responseSender) -> {
			FlightMemberPayload payload = FlightMemberPayload.read(buf);
			server.execute(() -> FlightRequests.handleMember(player, payload));
		});
		*///?}
	}

	//? if <1.20.5 {
	/*private static void sendState(ServerPlayer player, FlightStatePayload payload) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		FlightStatePayload.write(buf, payload);
		ServerPlayNetworking.send(player, McIds.id("state"), buf);
	}

	private static void sendRoster(ServerPlayer player, FlightRosterPayload payload) {
		FriendlyByteBuf buf = PacketByteBufs.create();
		FlightRosterPayload.write(buf, payload);
		ServerPlayNetworking.send(player, McIds.id("roster"), buf);
	}
	*///?}
}
