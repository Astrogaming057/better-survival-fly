package com.forcedflight.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record FlightStatePayload(
		int revision,
		boolean authorized,
		boolean flying,
		boolean allowControl,
		boolean canToggle,
		float flySpeed,
		boolean announce,
		float minSpeed,
		float maxSpeed,
		float speedStep,
		boolean flyPenalty
) implements CustomPacketPayload {
	public static final Type<FlightStatePayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("forcedflight", "state"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FlightStatePayload> STREAM_CODEC = StreamCodec.of(
			(buf, payload) -> {
				buf.writeVarInt(payload.revision);
				buf.writeBoolean(payload.authorized);
				buf.writeBoolean(payload.flying);
				buf.writeBoolean(payload.allowControl);
				buf.writeBoolean(payload.canToggle);
				buf.writeFloat(payload.flySpeed);
				buf.writeBoolean(payload.announce);
				buf.writeFloat(payload.minSpeed);
				buf.writeFloat(payload.maxSpeed);
				buf.writeFloat(payload.speedStep);
				buf.writeBoolean(payload.flyPenalty);
			},
			buf -> new FlightStatePayload(
					buf.readVarInt(),
					buf.readBoolean(),
					buf.readBoolean(),
					buf.readBoolean(),
					buf.readBoolean(),
					buf.readFloat(),
					buf.readBoolean(),
					buf.readFloat(),
					buf.readFloat(),
					buf.readFloat(),
					buf.readBoolean()
			)
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
}
