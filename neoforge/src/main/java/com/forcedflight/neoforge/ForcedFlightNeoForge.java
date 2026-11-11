package com.forcedflight.neoforge;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

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

@Mod(ForcedFlight.MOD_ID)
public final class ForcedFlightNeoForge {
	public ForcedFlightNeoForge(IEventBus modBus) {
		ForcedFlightServices.use(FMLPaths.CONFIGDIR.get().resolve(ForcedFlight.MOD_ID));
		ForcedFlightServices.sendState = PacketDistributor::sendToPlayer;
		ForcedFlightServices.sendRoster = PacketDistributor::sendToPlayer;
		ForcedFlightBootstrap.init();
		modBus.addListener(Events::registerPayloads);
	}

	@EventBusSubscriber(modid = ForcedFlight.MOD_ID)
	public static final class Events {
		private Events() {
		}

		public static void registerPayloads(RegisterPayloadHandlersEvent event) {
			PayloadRegistrar registrar = event.registrar("3");
			registrar.playToServer(FlightRequestPayload.TYPE, FlightRequestPayload.STREAM_CODEC, (payload, context) ->
					context.enqueueWork(() -> {
						if (context.player() instanceof ServerPlayer player) {
							FlightRequests.handle(player, payload);
						}
					}));
			registrar.playToClient(FlightStatePayload.TYPE, FlightStatePayload.STREAM_CODEC, (payload, context) ->
					context.enqueueWork(() -> {
						if (ForcedFlightServices.acceptClientState != null) {
							ForcedFlightServices.acceptClientState.accept(payload);
						}
					}));
			registrar.playToServer(FlightMemberPayload.TYPE, FlightMemberPayload.STREAM_CODEC, (payload, context) ->
					context.enqueueWork(() -> {
						if (context.player() instanceof ServerPlayer player) {
							FlightRequests.handleMember(player, payload);
						}
					}));
			registrar.playToClient(FlightRosterPayload.TYPE, FlightRosterPayload.STREAM_CODEC, (payload, context) ->
					context.enqueueWork(() -> {
						if (ForcedFlightServices.acceptRoster != null) {
							ForcedFlightServices.acceptRoster.accept(payload);
						}
					}));
		}

		@SubscribeEvent
		public static void registerCommands(RegisterCommandsEvent event) {
			FlightCommand.register(event.getDispatcher());
		}

		@SubscribeEvent
		public static void started(ServerStartedEvent event) {
			FlightManager.bindServer(event.getServer());
		}

		@SubscribeEvent
		public static void stopping(ServerStoppingEvent event) {
			FlightPermissionStore.get().saveNow();
		}

		@SubscribeEvent
		public static void stopped(ServerStoppedEvent event) {
			FlightManager.clearRuntime();
		}

		@SubscribeEvent
		public static void tick(ServerTickEvent.Post event) {
			FlightPermissionStore.get().tickAutosave();
			FlightManager.deliverJoinState(event.getServer());
		}

		@SubscribeEvent
		public static void join(PlayerEvent.PlayerLoggedInEvent event) {
			if (event.getEntity() instanceof ServerPlayer player) {
				FlightManager.onJoin(player);
			}
		}

		@SubscribeEvent
		public static void leave(PlayerEvent.PlayerLoggedOutEvent event) {
			if (event.getEntity() instanceof ServerPlayer player) {
				FlightManager.onDisconnect(player);
			}
		}

		@SubscribeEvent
		public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
			if (event.getEntity() instanceof ServerPlayer player) {
				FlightManager.onRespawn(player);
			}
		}

		@SubscribeEvent
		public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
			if (event.getEntity() instanceof ServerPlayer player) {
				FlightManager.onWorldChange(player);
			}
		}
	}
}
