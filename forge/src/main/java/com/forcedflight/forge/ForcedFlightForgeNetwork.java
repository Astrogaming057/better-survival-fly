package com.forcedflight.forge;

import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;

import com.forcedflight.ForcedFlight;
import com.forcedflight.ForcedFlightServices;
import com.forcedflight.network.FlightRequestPayload;
import com.forcedflight.network.FlightRequests;
import com.forcedflight.network.FlightStatePayload;

public final class ForcedFlightForgeNetwork {
	public static final Channel<CustomPacketPayload> CHANNEL = ChannelBuilder
			.named(ResourceLocation.fromNamespaceAndPath(ForcedFlight.MOD_ID, "main"))
			.networkProtocolVersion(1)
			.payloadChannel()
			.play()
			.bidirectional()
			.addMain(FlightRequestPayload.TYPE, FlightRequestPayload.STREAM_CODEC, ForcedFlightForgeNetwork::onRequest)
			.addMain(FlightStatePayload.TYPE, FlightStatePayload.STREAM_CODEC, ForcedFlightForgeNetwork::onState)
			.build();

	private ForcedFlightForgeNetwork() {
	}

	public static void sendToPlayer(ServerPlayer player, FlightStatePayload payload) {
		CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
	}

	public static void sendToServer(FlightRequestPayload payload) {
		CHANNEL.send(payload, PacketDistributor.SERVER.noArg());
	}

	private static void onRequest(FlightRequestPayload payload, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player != null) {
				FlightRequests.handle(player, payload);
			}
		});
		context.setPacketHandled(true);
	}

	private static void onState(FlightStatePayload payload, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			if (ForcedFlightServices.acceptClientState != null) {
				ForcedFlightServices.acceptClientState.accept(payload);
			}
		});
		context.setPacketHandled(true);
	}
}
