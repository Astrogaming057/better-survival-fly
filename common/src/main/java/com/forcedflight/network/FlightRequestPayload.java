package com.forcedflight.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FlightRequestPayload(int action, float flySpeed) implements CustomPacketPayload {
	public static final int TOGGLE = 1;
	public static final int SET_SPEED = 2;
	public static final int SET_MAX_SPEED = 3;
	public static final int SET_FLY_PENALTY = 4;
	public static final int SET_FALL_DAMAGE = 5;
	public static final int SET_JOIN_MESSAGE = 6;
	public static final Type<FlightRequestPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("forcedflight", "request"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FlightRequestPayload> STREAM_CODEC = StreamCodec.of(
			(buf, payload) -> {
				buf.writeVarInt(payload.action);
				buf.writeFloat(payload.flySpeed);
			},
			buf -> new FlightRequestPayload(buf.readVarInt(), buf.readFloat())
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
