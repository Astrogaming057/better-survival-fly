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

public record FlightRequestPayload(int action, float flySpeed)
		//? if >=1.20.2 {
		implements CustomPacketPayload
		//?}
{
	public static final int TOGGLE = 1;
	public static final int SET_SPEED = 2;
	public static final int SET_MAX_SPEED = 3;
	public static final int SET_FLY_PENALTY = 4;
	public static final int SET_FALL_DAMAGE = 5;
	public static final int SET_JOIN_MESSAGE = 6;
	public static final int SET_KEEP_FLYING = 7;

	//? if >=1.20.5 {
	public static final Type<FlightRequestPayload> TYPE = new Type<>(McIds.id("request"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FlightRequestPayload> STREAM_CODEC = StreamCodec.of(
			(buf, payload) -> write(buf, payload),
			FlightRequestPayload::read
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
	//?} elif >=1.20.2 {
	/*public static final ResourceLocation ID = McIds.id("request");

	@Override
	public void write(FriendlyByteBuf buf) {
		write(buf, this);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
	*///?}

	public static void write(FriendlyByteBuf buf, FlightRequestPayload payload) {
		buf.writeVarInt(payload.action);
		buf.writeFloat(payload.flySpeed);
	}

	public static FlightRequestPayload read(FriendlyByteBuf buf) {
		return new FlightRequestPayload(buf.readVarInt(), buf.readFloat());
	}
}
