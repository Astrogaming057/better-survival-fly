package com.forcedflight.forge;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;

import com.forcedflight.ForcedFlight;
import com.forcedflight.ForcedFlightBootstrap;
import com.forcedflight.ForcedFlightServices;
import com.forcedflight.command.FlightCommand;
import com.forcedflight.flight.FlightManager;
import com.forcedflight.permission.FlightPermissionStore;

@Mod(ForcedFlight.MOD_ID)
public final class ForcedFlightForge {
	public ForcedFlightForge() {
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

		@SubscribeEvent
		public static void tick(TickEvent.ServerTickEvent event) {
			if (event.phase != TickEvent.Phase.END) {
				return;
			}
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
