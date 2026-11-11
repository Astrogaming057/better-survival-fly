package com.forcedflight.network;

import java.util.UUID;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FlightMemberPayload(int action, UUID target) implements CustomPacketPayload {
	public static final int REQUEST = 0;
	public static final int TOGGLE_FLIGHT = 1;
	public static final int TOGGLE_CONTROL = 2;
	public static final Type<FlightMemberPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("forcedflight", "member"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FlightMemberPayload> STREAM_CODEC = StreamCodec.of(
			(buf, payload) -> {
				buf.writeVarInt(payload.action);
				buf.writeUUID(payload.target);
			},
			buf -> new FlightMemberPayload(buf.readVarInt(), buf.readUUID())
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
