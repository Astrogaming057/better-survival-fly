package com.forcedflight.forge;

import net.minecraft.server.level.ServerPlayer;

import com.forcedflight.ForcedFlightServices;
import com.forcedflight.compat.McIds;
import com.forcedflight.network.FlightMemberPayload;
import com.forcedflight.network.FlightRequestPayload;
import com.forcedflight.network.FlightRequests;
import com.forcedflight.network.FlightRosterPayload;
import com.forcedflight.network.FlightStatePayload;
//? if >=1.20.5 {
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.Channel;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
//?} elif >=1.20.2 {
/*import net.minecraftforge.event.network.CustomPayloadEvent;
import net.minecraftforge.network.ChannelBuilder;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.SimpleChannel;
*///?} else {
/*import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
*///?}

public final class ForcedFlightForgeNetwork {
	//? if >=1.20.5 {
	public static final Channel<CustomPacketPayload> CHANNEL = ChannelBuilder
			.named(McIds.id("main"))
			.networkProtocolVersion(3)
			.payloadChannel()
			.play()
			.bidirectional()
			.addMain(FlightRequestPayload.TYPE, FlightRequestPayload.STREAM_CODEC, ForcedFlightForgeNetwork::onRequest)
			.addMain(FlightStatePayload.TYPE, FlightStatePayload.STREAM_CODEC, ForcedFlightForgeNetwork::onState)
			.addMain(FlightMemberPayload.TYPE, FlightMemberPayload.STREAM_CODEC, ForcedFlightForgeNetwork::onMember)
			.addMain(FlightRosterPayload.TYPE, FlightRosterPayload.STREAM_CODEC, ForcedFlightForgeNetwork::onRoster)
			.build();

	public static void bootstrap() {
		if (CHANNEL == null) {
			throw new IllegalStateException("forcedflight channel missing");
		}
	}

	private ForcedFlightForgeNetwork() {
	}

	public static void sendToPlayer(ServerPlayer player, FlightStatePayload payload) {
		CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
	}

	public static void sendToServer(FlightRequestPayload payload) {
		CHANNEL.send(payload, PacketDistributor.SERVER.noArg());
	}

	public static void sendRoster(ServerPlayer player, FlightRosterPayload payload) {
		CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
	}

	public static void sendMember(FlightMemberPayload payload) {
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

	private static void onMember(FlightMemberPayload payload, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player != null) {
				FlightRequests.handleMember(player, payload);
			}
		});
		context.setPacketHandled(true);
	}

	private static void onRoster(FlightRosterPayload payload, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			if (ForcedFlightServices.acceptRoster != null) {
				ForcedFlightServices.acceptRoster.accept(payload);
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
	//?} elif >=1.20.2 {
	/*public static final SimpleChannel CHANNEL = ChannelBuilder
			.named(McIds.id("main"))
			.networkProtocolVersion(3)
			.simpleChannel();

	static {
		CHANNEL.messageBuilder(FlightRequestPayload.class, 0)
				.encoder((payload, buf) -> FlightRequestPayload.write(buf, payload))
				.decoder(FlightRequestPayload::read)
				.consumerMainThread(ForcedFlightForgeNetwork::onRequest)
				.add();
		CHANNEL.messageBuilder(FlightStatePayload.class, 1)
				.encoder((payload, buf) -> FlightStatePayload.write(buf, payload))
				.decoder(FlightStatePayload::read)
				.consumerMainThread(ForcedFlightForgeNetwork::onState)
				.add();
		CHANNEL.messageBuilder(FlightMemberPayload.class, 2)
				.encoder((payload, buf) -> FlightMemberPayload.write(buf, payload))
				.decoder(FlightMemberPayload::read)
				.consumerMainThread(ForcedFlightForgeNetwork::onMember)
				.add();
		CHANNEL.messageBuilder(FlightRosterPayload.class, 3)
				.encoder((payload, buf) -> FlightRosterPayload.write(buf, payload))
				.decoder(FlightRosterPayload::read)
				.consumerMainThread(ForcedFlightForgeNetwork::onRoster)
				.add();
	}

	public static void bootstrap() {
	}

	private ForcedFlightForgeNetwork() {
	}

	public static void sendToPlayer(ServerPlayer player, FlightStatePayload payload) {
		CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
	}

	public static void sendToServer(FlightRequestPayload payload) {
		CHANNEL.send(payload, PacketDistributor.SERVER.with(null));
	}

	public static void sendRoster(ServerPlayer player, FlightRosterPayload payload) {
		CHANNEL.send(payload, PacketDistributor.PLAYER.with(player));
	}

	public static void sendMember(FlightMemberPayload payload) {
		CHANNEL.send(payload, PacketDistributor.SERVER.with(null));
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

	private static void onMember(FlightMemberPayload payload, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player != null) {
				FlightRequests.handleMember(player, payload);
			}
		});
		context.setPacketHandled(true);
	}

	private static void onRoster(FlightRosterPayload payload, CustomPayloadEvent.Context context) {
		context.enqueueWork(() -> {
			if (ForcedFlightServices.acceptRoster != null) {
				ForcedFlightServices.acceptRoster.accept(payload);
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
	*///?} else {
	/*private static final String PROTOCOL = "3";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			McIds.id("main"),
			() -> PROTOCOL,
			PROTOCOL::equals,
			PROTOCOL::equals
	);

	static {
		int id = 0;
		CHANNEL.registerMessage(id++, FlightRequestPayload.class, (payload, buf) -> FlightRequestPayload.write(buf, payload), FlightRequestPayload::read, ForcedFlightForgeNetwork::onRequest);
		CHANNEL.registerMessage(id++, FlightStatePayload.class, (payload, buf) -> FlightStatePayload.write(buf, payload), FlightStatePayload::read, ForcedFlightForgeNetwork::onState);
		CHANNEL.registerMessage(id++, FlightMemberPayload.class, (payload, buf) -> FlightMemberPayload.write(buf, payload), FlightMemberPayload::read, ForcedFlightForgeNetwork::onMember);
		CHANNEL.registerMessage(id++, FlightRosterPayload.class, (payload, buf) -> FlightRosterPayload.write(buf, payload), FlightRosterPayload::read, ForcedFlightForgeNetwork::onRoster);
	}

	public static void bootstrap() {
	}

	private ForcedFlightForgeNetwork() {
	}

	public static void sendToPlayer(ServerPlayer player, FlightStatePayload payload) {
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
	}

	public static void sendToServer(FlightRequestPayload payload) {
		CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
	}

	public static void sendRoster(ServerPlayer player, FlightRosterPayload payload) {
		CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), payload);
	}

	public static void sendMember(FlightMemberPayload payload) {
		CHANNEL.send(PacketDistributor.SERVER.noArg(), payload);
	}

	private static void onRequest(FlightRequestPayload payload, Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player != null) {
				FlightRequests.handle(player, payload);
			}
		});
		context.setPacketHandled(true);
	}

	private static void onMember(FlightMemberPayload payload, Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			ServerPlayer player = context.getSender();
			if (player != null) {
				FlightRequests.handleMember(player, payload);
			}
		});
		context.setPacketHandled(true);
	}

	private static void onRoster(FlightRosterPayload payload, Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			if (ForcedFlightServices.acceptRoster != null) {
				ForcedFlightServices.acceptRoster.accept(payload);
			}
		});
		context.setPacketHandled(true);
	}

	private static void onState(FlightStatePayload payload, Supplier<NetworkEvent.Context> supplier) {
		NetworkEvent.Context context = supplier.get();
		context.enqueueWork(() -> {
			if (ForcedFlightServices.acceptClientState != null) {
				ForcedFlightServices.acceptClientState.accept(payload);
			}
		});
		context.setPacketHandled(true);
	}
	*///?}
}
