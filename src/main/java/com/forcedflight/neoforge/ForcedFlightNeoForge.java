package com.forcedflight.neoforge;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import com.forcedflight.ForcedFlight;
import com.forcedflight.ForcedFlightBootstrap;
import com.forcedflight.ForcedFlightServices;
import com.forcedflight.command.FlightCommand;
import com.forcedflight.flight.FlightManager;
import com.forcedflight.flight.FlightMining;
import com.forcedflight.network.FlightMemberPayload;
import com.forcedflight.network.FlightRequestPayload;
import com.forcedflight.network.FlightRequests;
import com.forcedflight.network.FlightRosterPayload;
import com.forcedflight.network.FlightStatePayload;
import com.forcedflight.permission.FlightPermissionStore;
//? if >=1.20.5 {
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
//?} elif >=1.20.4 {
/*import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;
*///?} else {
/*import net.neoforged.neoforge.event.TickEvent;
import net.neoforged.neoforge.network.NetworkEvent;
import net.neoforged.neoforge.network.NetworkRegistry;
import net.neoforged.neoforge.network.simple.SimpleChannel;
*///?}

@Mod(ForcedFlight.MOD_ID)
public final class ForcedFlightNeoForge {
	//? if <1.20.4 {
	/*public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			com.forcedflight.compat.McIds.id("main"),
			() -> "3",
			"3"::equals,
			"3"::equals
	);

	static {
		int id = 0;
		CHANNEL.registerMessage(id++, FlightRequestPayload.class, (payload, buf) -> FlightRequestPayload.write(buf, payload), FlightRequestPayload::read, ForcedFlightNeoForge::onRequest);
		CHANNEL.registerMessage(id++, FlightStatePayload.class, (payload, buf) -> FlightStatePayload.write(buf, payload), FlightStatePayload::read, ForcedFlightNeoForge::onState);
		CHANNEL.registerMessage(id++, FlightMemberPayload.class, (payload, buf) -> FlightMemberPayload.write(buf, payload), FlightMemberPayload::read, ForcedFlightNeoForge::onMember);
		CHANNEL.registerMessage(id++, FlightRosterPayload.class, (payload, buf) -> FlightRosterPayload.write(buf, payload), FlightRosterPayload::read, ForcedFlightNeoForge::onRoster);
	}
	*///?}

	public ForcedFlightNeoForge(IEventBus modBus) {
		ForcedFlightServices.use(FMLPaths.CONFIGDIR.get().resolve(ForcedFlight.MOD_ID));
		ForcedFlightServices.sendState = ForcedFlightNeoForge::sendState;
		ForcedFlightServices.sendRoster = ForcedFlightNeoForge::sendRoster;
		ForcedFlightBootstrap.init();
		//? if >=1.20.5 {
		modBus.addListener(Events::registerPayloads);
		//?} elif >=1.20.4 {
		/*modBus.addListener(Events::registerLegacyPayloads);
		*///?}
	}

	public static void sendState(ServerPlayer player, FlightStatePayload payload) {
		//? if >=1.20.5 {
		PacketDistributor.sendToPlayer(player, payload);
		//?} elif >=1.20.4 {
		/*PacketDistributor.PLAYER.with(player).send(payload);
		*///?} else {
		/*CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
		*///?}
	}

	public static void sendRoster(ServerPlayer player, FlightRosterPayload payload) {
		//? if >=1.20.5 {
		PacketDistributor.sendToPlayer(player, payload);
		//?} elif >=1.20.4 {
		/*PacketDistributor.PLAYER.with(player).send(payload);
		*///?} else {
		/*CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
		*///?}
	}

	public static void sendRequest(FlightRequestPayload payload) {
		//? if >=1.21.7 {
		/*net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(payload);
		*///?} elif >=1.20.5 {
		PacketDistributor.sendToServer(payload);
		//?} elif >=1.20.4 {
		/*PacketDistributor.SERVER.noArg().send(payload);
		*///?} else {
		/*CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
		*///?}
	}

	public static void sendMember(FlightMemberPayload payload) {
		//? if >=1.21.7 {
		/*net.neoforged.neoforge.client.network.ClientPacketDistributor.sendToServer(payload);
		*///?} elif >=1.20.5 {
		PacketDistributor.sendToServer(payload);
		//?} elif >=1.20.4 {
		/*PacketDistributor.SERVER.noArg().send(payload);
		*///?} else {
		/*CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
		*///?}
	}

	//? if <1.20.4 {
	/*private static void onRequest(FlightRequestPayload payload, NetworkEvent.Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player != null) {
				FlightRequests.handle(player, payload);
			}
		});
		context.setPacketHandled(true);
	}

	private static void onMember(FlightMemberPayload payload, NetworkEvent.Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player != null) {
				FlightRequests.handleMember(player, payload);
			}
		});
		context.setPacketHandled(true);
	}

	private static void onState(FlightStatePayload payload, NetworkEvent.Context context) {
		context.enqueueWork(() -> {
			if (ForcedFlightServices.acceptClientState != null) {
				ForcedFlightServices.acceptClientState.accept(payload);
			}
		});
		context.setPacketHandled(true);
	}

	private static void onRoster(FlightRosterPayload payload, NetworkEvent.Context context) {
		context.enqueueWork(() -> {
			if (ForcedFlightServices.acceptRoster != null) {
				ForcedFlightServices.acceptRoster.accept(payload);
			}
		});
		context.setPacketHandled(true);
	}
	*///?}

	//? if >=1.20.5 {
	@EventBusSubscriber(modid = ForcedFlight.MOD_ID)
	//?} else {
	/*@Mod.EventBusSubscriber(modid = ForcedFlight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	*///?}
	public static final class Events {
		private Events() {
		}

		//? if >=1.20.5 {
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
		//?} elif >=1.20.4 {
		/*public static void registerLegacyPayloads(RegisterPayloadHandlerEvent event) {
			IPayloadRegistrar registrar = event.registrar(ForcedFlight.MOD_ID);
			registrar.play(FlightRequestPayload.ID, FlightRequestPayload::read, (payload, context) -> context.workHandler().execute(() -> {
				if (context.player().orElse(null) instanceof ServerPlayer player) {
					FlightRequests.handle(player, payload);
				}
			}));
			registrar.play(FlightStatePayload.ID, FlightStatePayload::read, (payload, context) -> context.workHandler().execute(() -> {
				if (ForcedFlightServices.acceptClientState != null) {
					ForcedFlightServices.acceptClientState.accept(payload);
				}
			}));
			registrar.play(FlightMemberPayload.ID, FlightMemberPayload::read, (payload, context) -> context.workHandler().execute(() -> {
				if (context.player().orElse(null) instanceof ServerPlayer player) {
					FlightRequests.handleMember(player, payload);
				}
			}));
			registrar.play(FlightRosterPayload.ID, FlightRosterPayload::read, (payload, context) -> context.workHandler().execute(() -> {
				if (ForcedFlightServices.acceptRoster != null) {
					ForcedFlightServices.acceptRoster.accept(payload);
				}
			}));
		}
		*///?}

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

		//? if >=1.20.5 {
		@SubscribeEvent
		public static void tick(ServerTickEvent.Post event) {
			FlightPermissionStore.get().tickAutosave();
			FlightManager.deliverJoinState(event.getServer());
		}
		//?} else {
		/*@SubscribeEvent
		public static void tick(TickEvent.ServerTickEvent event) {
			if (event.phase != TickEvent.Phase.END) {
				return;
			}
			FlightPermissionStore.get().tickAutosave();
			FlightManager.deliverJoinState();
		}
		*///?}

		@SubscribeEvent
		public static void breakSpeed(PlayerEvent.BreakSpeed event) {
			if (event.getEntity() instanceof net.minecraft.world.entity.player.Player player) {
				event.setNewSpeed(FlightMining.withoutAirPenalty(player, event.getNewSpeed()));
			}
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
