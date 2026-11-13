package com.forcedflight.forge;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
//? if >=1.21.6 {
/*import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
*///?} else {
import net.minecraftforge.eventbus.api.SubscribeEvent;
//?}
import net.minecraftforge.fml.loading.FMLPaths;

import com.forcedflight.ForcedFlight;
import com.forcedflight.ForcedFlightBootstrap;
import com.forcedflight.ForcedFlightServices;
import com.forcedflight.command.FlightCommand;
import com.forcedflight.flight.FlightManager;
import com.forcedflight.flight.FlightMining;
import com.forcedflight.permission.FlightPermissionStore;

@Mod(ForcedFlight.MOD_ID)
public final class ForcedFlightForge {
	public ForcedFlightForge() {
		ForcedFlightForgeNetwork.bootstrap();
		ForcedFlightServices.use(FMLPaths.CONFIGDIR.get().resolve(ForcedFlight.MOD_ID));
		ForcedFlightServices.sendState = ForcedFlightForgeNetwork::sendToPlayer;
		ForcedFlightServices.sendRoster = ForcedFlightForgeNetwork::sendRoster;
		ForcedFlightBootstrap.init();
	}

	@Mod.EventBusSubscriber(modid = ForcedFlight.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
	public static final class GameEvents {
		private GameEvents() {
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

		//? if >=1.21.9 {
		/*@SubscribeEvent
		public static void tick(TickEvent.ServerTickEvent.Post event) {
			FlightPermissionStore.get().tickAutosave();
			FlightManager.deliverJoinState();
		}
		*///?} else {
		@SubscribeEvent
		public static void tick(TickEvent.ServerTickEvent event) {
			if (event.phase != TickEvent.Phase.END) {
				return;
			}
			FlightPermissionStore.get().tickAutosave();
			FlightManager.deliverJoinState();
		}
		//?}

		@SubscribeEvent
		public static void breakSpeed(PlayerEvent.BreakSpeed event) {
			net.minecraft.world.entity.player.Player player = playerFrom(event);
			if (player != null) {
				event.setNewSpeed(FlightMining.withoutAirPenalty(player, event.getNewSpeed()));
			}
		}

		@SubscribeEvent
		public static void join(PlayerEvent.PlayerLoggedInEvent event) {
			ServerPlayer player = serverPlayer(event);
			if (player != null) {
				FlightManager.onJoin(player);
			}
		}

		@SubscribeEvent
		public static void leave(PlayerEvent.PlayerLoggedOutEvent event) {
			ServerPlayer player = serverPlayer(event);
			if (player != null) {
				FlightManager.onDisconnect(player);
			}
		}

		@SubscribeEvent
		public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
			ServerPlayer player = serverPlayer(event);
			if (player != null) {
				FlightManager.onRespawn(player);
			}
		}

		@SubscribeEvent
		public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event) {
			ServerPlayer player = serverPlayer(event);
			if (player != null) {
				FlightManager.onWorldChange(player);
			}
		}
	}

	private static net.minecraft.world.entity.player.Player playerFrom(PlayerEvent event) {
		//? if >=1.19.2 {
		return event.getEntity();
		//?} else {
		/*return event.getPlayer();
		*///?}
	}

	private static ServerPlayer serverPlayer(PlayerEvent event) {
		return playerFrom(event) instanceof ServerPlayer player ? player : null;
	}
}
