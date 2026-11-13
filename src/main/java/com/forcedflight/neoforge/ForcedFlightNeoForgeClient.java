package com.forcedflight.neoforge;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

import com.forcedflight.ForcedFlight;
import com.forcedflight.ForcedFlightServices;
import com.forcedflight.client.FlightClientController;
import com.forcedflight.client.FlightClientHooks;
import com.forcedflight.client.FlightHud;
//? if >=1.20.5 {
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
//?} else {
/*import net.neoforged.neoforge.event.TickEvent;
*///?}

//? if >=1.21.6 {
/*@EventBusSubscriber(modid = ForcedFlight.MOD_ID, value = Dist.CLIENT)
*///?} elif >=1.20.5 {
@EventBusSubscriber(modid = ForcedFlight.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
//?} else {
/*@net.neoforged.fml.common.Mod.EventBusSubscriber(modid = ForcedFlight.MOD_ID, value = Dist.CLIENT, bus = net.neoforged.fml.common.Mod.EventBusSubscriber.Bus.MOD)
*///?}
public final class ForcedFlightNeoForgeClient {
	static {
		FlightClientController.installMiningHook();
		ForcedFlightServices.acceptClientState = payload ->
				FlightClientController.applyServerState(payload, Minecraft.getInstance().player);
		ForcedFlightServices.acceptRoster = com.forcedflight.client.ClientRoster::apply;
	}

	private ForcedFlightNeoForgeClient() {
	}

	@SubscribeEvent
	public static void keys(RegisterKeyMappingsEvent event) {
		ForcedFlightServices.sendRequest = ForcedFlightNeoForge::sendRequest;
		ForcedFlightServices.sendMember = ForcedFlightNeoForge::sendMember;
		FlightClientHooks.toggle = FlightClientHooks.createToggle();
		FlightClientHooks.createSpeedKeys();
		event.register(FlightClientHooks.toggle);
		event.register(FlightClientHooks.speedModifier);
		event.register(FlightClientHooks.speedUp);
		event.register(FlightClientHooks.speedDown);
		com.forcedflight.client.ClientPreferences.load();
	}
}

//? if >=1.20.5 {
@EventBusSubscriber(modid = ForcedFlight.MOD_ID, value = Dist.CLIENT)
//?} else {
/*@net.neoforged.fml.common.Mod.EventBusSubscriber(modid = ForcedFlight.MOD_ID, value = Dist.CLIENT, bus = net.neoforged.fml.common.Mod.EventBusSubscriber.Bus.FORGE)
*///?}
final class ForcedFlightNeoForgeClientGame {
	private ForcedFlightNeoForgeClientGame() {
	}

	//? if >=1.20.5 {
	@SubscribeEvent
	public static void tick(ClientTickEvent.Post event) {
		FlightClientHooks.tick(Minecraft.getInstance());
	}
	//?} else {
	/*@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			FlightClientHooks.tick(Minecraft.getInstance());
		}
	}
	*///?}

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
