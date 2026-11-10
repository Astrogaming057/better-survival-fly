package com.forcedflight.forge;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.forcedflight.ForcedFlight;
import com.forcedflight.ForcedFlightServices;
import com.forcedflight.client.FlightClientController;
import com.forcedflight.client.FlightClientHooks;
import com.forcedflight.client.FlightHud;

@Mod.EventBusSubscriber(modid = ForcedFlight.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ForcedFlightForgeClient {
	static {
		ForcedFlightServices.acceptClientState = payload ->
				FlightClientController.applyServerState(payload, Minecraft.getInstance().player);
	}

	private ForcedFlightForgeClient() {
	}

	@SubscribeEvent
	public static void keys(RegisterKeyMappingsEvent event) {
		ForcedFlightServices.sendRequest = ForcedFlightForgeNetwork::sendToServer;
		FlightClientHooks.toggle = FlightClientHooks.createToggle();
		event.register(FlightClientHooks.toggle);
	}

	@SubscribeEvent
	public static void hud(AddGuiOverlayLayersEvent event) {
		event.getLayeredDraw().add(
				ResourceLocation.fromNamespaceAndPath(ForcedFlight.MOD_ID, "hud"),
				(graphics, delta) -> FlightHud.render(graphics)
		);
	}
}

@Mod.EventBusSubscriber(modid = ForcedFlight.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
final class ForcedFlightForgeClientGame {
	private ForcedFlightForgeClientGame() {
	}

	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			FlightClientHooks.tick(Minecraft.getInstance());
		}
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
