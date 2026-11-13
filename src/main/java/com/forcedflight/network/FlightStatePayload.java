package com.forcedflight.network;

import net.minecraft.network.FriendlyByteBuf;
import com.forcedflight.compat.McIds;
//? if >=1.20.5 {
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
//?} elif >=1.20.2 {
/*import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
*///?}

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
		boolean flyPenalty,
		boolean operator,
		boolean fallDamage,
		boolean joinMessage,
		boolean keepFlying
		//? if >=1.20.2 {
		) implements CustomPacketPayload {
		//?} else {
		/*) {
		*///?}
	//? if >=1.20.5 {
	public static final Type<FlightStatePayload> TYPE = new Type<>(McIds.id("state"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FlightStatePayload> STREAM_CODEC = StreamCodec.of(
			(buf, payload) -> write(buf, payload),
			FlightStatePayload::read
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
	//?} elif >=1.20.2 {
	/*public static final ResourceLocation ID = McIds.id("state");

	@Override
	public void write(FriendlyByteBuf buf) {
		write(buf, this);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
	*///?}

	public static void write(FriendlyByteBuf buf, FlightStatePayload payload) {
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
		buf.writeBoolean(payload.operator);
		buf.writeBoolean(payload.fallDamage);
		buf.writeBoolean(payload.joinMessage);
		buf.writeBoolean(payload.keepFlying);
	}

	public static FlightStatePayload read(FriendlyByteBuf buf) {
		return new FlightStatePayload(
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
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean(),
				buf.readBoolean()
		);
	}
}
