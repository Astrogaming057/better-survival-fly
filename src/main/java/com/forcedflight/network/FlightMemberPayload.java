package com.forcedflight.network;

import java.util.UUID;

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

public record FlightMemberPayload(int action, UUID target)
		//? if >=1.20.2 {
		implements CustomPacketPayload
		//?}
{
	public static final int REQUEST = 0;
	public static final int TOGGLE_FLIGHT = 1;
	public static final int TOGGLE_CONTROL = 2;

	//? if >=1.20.5 {
	public static final Type<FlightMemberPayload> TYPE = new Type<>(McIds.id("member"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FlightMemberPayload> STREAM_CODEC = StreamCodec.of(
			(buf, payload) -> write(buf, payload),
			FlightMemberPayload::read
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
	//?} elif >=1.20.2 {
	/*public static final ResourceLocation ID = McIds.id("member");

	@Override
	public void write(FriendlyByteBuf buf) {
		write(buf, this);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
	*///?}

	public static void write(FriendlyByteBuf buf, FlightMemberPayload payload) {
		buf.writeVarInt(payload.action);
		buf.writeUUID(payload.target);
	}

	public static FlightMemberPayload read(FriendlyByteBuf buf) {
		return new FlightMemberPayload(buf.readVarInt(), buf.readUUID());
	}
}
