package com.forcedflight.forge;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
//? if >=1.21.6 {
/*import net.minecraftforge.eventbus.api.listener.SubscribeEvent;
*///?} else {
import net.minecraftforge.eventbus.api.SubscribeEvent;
//?}
//? if (>=1.20.6 && <1.21) || (>=1.21.1 && <1.21.6) || >=1.21.8 {
import net.minecraftforge.client.event.AddGuiOverlayLayersEvent;
//?}
//? if >=1.20.6 {
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
//?} elif >=1.19.2 {
/*import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
*///?} else {
/*import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
*///?}
import com.forcedflight.compat.McIds;

import com.forcedflight.ForcedFlight;
import com.forcedflight.ForcedFlightServices;
import com.forcedflight.client.FlightClientController;
import com.forcedflight.client.FlightClientHooks;
import com.forcedflight.client.FlightHud;

// RegisterKeyMappingsEvent and AddGuiOverlayLayersEvent are mod-bus events through 1.21.8.
// From 1.21.9 they no longer implement IModBusEvent, so they must use the game bus.
//? if >=1.21.9 {
/*@Mod.EventBusSubscriber(modid = ForcedFlight.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
*///?} else {
@Mod.EventBusSubscriber(modid = ForcedFlight.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
//?}
public final class ForcedFlightForgeClient {
	static {
		FlightClientController.installMiningHook();
		ForcedFlightServices.acceptClientState = payload ->
				FlightClientController.applyServerState(payload, Minecraft.getInstance().player);
		ForcedFlightServices.acceptRoster = com.forcedflight.client.ClientRoster::apply;
	}

	private ForcedFlightForgeClient() {
	}

	//? if >=1.19.2 {
	@SubscribeEvent
	public static void keys(RegisterKeyMappingsEvent event) {
		ForcedFlightServices.sendRequest = ForcedFlightForgeNetwork::sendToServer;
		ForcedFlightServices.sendMember = ForcedFlightForgeNetwork::sendMember;
		FlightClientHooks.toggle = FlightClientHooks.createToggle();
		FlightClientHooks.createSpeedKeys();
		event.register(FlightClientHooks.toggle);
		event.register(FlightClientHooks.speedModifier);
		event.register(FlightClientHooks.speedUp);
		event.register(FlightClientHooks.speedDown);
		com.forcedflight.client.ClientPreferences.load();
	}
	//?}

	//? if (>=1.20.6 && <1.21) || (>=1.21.1 && <1.21.6) || >=1.21.8 {
	@SubscribeEvent
	public static void hud(AddGuiOverlayLayersEvent event) {
		event.getLayeredDraw().add(
				McIds.id("hud"),
				(graphics, delta) -> FlightHud.render(graphics)
		);
	}
	//?} elif <1.19.2 {
	/*@SubscribeEvent
	public static void setup(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			ForcedFlightServices.sendRequest = ForcedFlightForgeNetwork::sendToServer;
			ForcedFlightServices.sendMember = ForcedFlightForgeNetwork::sendMember;
			FlightClientHooks.toggle = FlightClientHooks.createToggle();
			FlightClientHooks.createSpeedKeys();
			ClientRegistry.registerKeyBinding(FlightClientHooks.toggle);
			ClientRegistry.registerKeyBinding(FlightClientHooks.speedModifier);
			ClientRegistry.registerKeyBinding(FlightClientHooks.speedUp);
			ClientRegistry.registerKeyBinding(FlightClientHooks.speedDown);
			com.forcedflight.client.ClientPreferences.load();
		});
	}
	*///?}
}

@Mod.EventBusSubscriber(modid = ForcedFlight.MOD_ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
final class ForcedFlightForgeClientGame {
	private ForcedFlightForgeClientGame() {
	}

	//? if >=1.21.9 {
	/*@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent.Post event) {
		FlightClientHooks.tick(Minecraft.getInstance());
	}
	*///?} else {
	@SubscribeEvent
	public static void tick(TickEvent.ClientTickEvent event) {
		if (event.phase == TickEvent.Phase.END) {
			FlightClientHooks.tick(Minecraft.getInstance());
		}
	}
	//?}

	//? if >=1.20 && <1.20.6 {
	/*@SubscribeEvent
	public static void hud(net.minecraftforge.client.event.RenderGuiEvent.Post event) {
		FlightHud.render(event.getGuiGraphics());
	}
	*///?}
	//? if >=1.19.2 && <1.20 {
	/*@SubscribeEvent
	public static void hud(net.minecraftforge.client.event.RenderGuiEvent.Post event) {
		FlightHud.render(event.getPoseStack());
	}
	*///?}
	//? if <1.19.2 {
	/*@SubscribeEvent
	public static void hud(net.minecraftforge.client.event.RenderGameOverlayEvent.Post event) {
		if (event.getType() == net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType.ALL) {
			FlightHud.render(event.getMatrixStack());
		}
	}
	*///?}

	//? if >=1.19.2 {
	@SubscribeEvent
	public static void join(ClientPlayerNetworkEvent.LoggingIn event) {
		FlightClientHooks.onJoin();
	}

	@SubscribeEvent
	public static void leave(ClientPlayerNetworkEvent.LoggingOut event) {
		FlightClientHooks.onDisconnect();
	}
	//?} else {
	/*@SubscribeEvent
	public static void join(ClientPlayerNetworkEvent.LoggedInEvent event) {
		FlightClientHooks.onJoin();
	}

	@SubscribeEvent
	public static void leave(ClientPlayerNetworkEvent.LoggedOutEvent event) {
		FlightClientHooks.onDisconnect();
	}
	*///?}
}
