package com.forcedflight.neoforge;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import com.forcedflight.ForcedFlight;
import com.forcedflight.ForcedFlightServices;
import com.forcedflight.client.FlightClientController;
import com.forcedflight.client.FlightClientHooks;
import com.forcedflight.client.FlightHud;
import net.minecraft.client.Minecraft;

@EventBusSubscriber(modid = ForcedFlight.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ForcedFlightNeoForgeClient {
	static {
		FlightClientController.installMiningHook();
		ForcedFlightServices.acceptClientState = payload ->
				FlightClientController.applyServerState(payload, Minecraft.getInstance().player);
	}

	private ForcedFlightNeoForgeClient() {
	}

	@SubscribeEvent
	public static void keys(RegisterKeyMappingsEvent event) {
		ForcedFlightServices.sendRequest = PacketDistributor::sendToServer;
		FlightClientHooks.toggle = FlightClientHooks.createToggle();
		event.register(FlightClientHooks.toggle);
	}
}

@EventBusSubscriber(modid = ForcedFlight.MOD_ID, value = Dist.CLIENT)
final class ForcedFlightNeoForgeClientGame {
	private ForcedFlightNeoForgeClientGame() {
	}

	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		FlightClientHooks.tick(Minecraft.getInstance());
	}

	@SubscribeEvent
	public static void hud(RenderGuiEvent.Post event) {
		FlightHud.render(event.getGuiGraphics());
	}

	@SubscribeEvent
	public static void join(ClientPlayerNetworkEvent.LoggingIn event) {
		FlightClientHooks.onJoin();
	}

	@SubscribeEvent
	public static void leave(ClientPlayerNetworkEvent.LoggingOut event) {
		FlightClientHooks.onDisconnect();
	}
}
