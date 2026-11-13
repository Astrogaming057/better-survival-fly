package com.forcedflight.network;

import java.util.ArrayList;
import java.util.List;
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

public record FlightRosterPayload(List<FlightMemberEntry> members)
		//? if >=1.20.2 {
		implements CustomPacketPayload
		//?}
{
	//? if >=1.20.5 {
	public static final Type<FlightRosterPayload> TYPE = new Type<>(McIds.id("roster"));
	public static final StreamCodec<RegistryFriendlyByteBuf, FlightRosterPayload> STREAM_CODEC = StreamCodec.of(
			(buf, payload) -> write(buf, payload),
			FlightRosterPayload::read
	);

	@Override
	public Type<? extends CustomPacketPayload> type() {
		return TYPE;
	}
	//?} elif >=1.20.2 {
	/*public static final ResourceLocation ID = McIds.id("roster");

	@Override
	public void write(FriendlyByteBuf buf) {
		write(buf, this);
	}

	@Override
	public ResourceLocation id() {
		return ID;
	}
	*///?}

	public static void write(FriendlyByteBuf buf, FlightRosterPayload payload) {
		buf.writeVarInt(payload.members.size());
		for (FlightMemberEntry entry : payload.members) {
			buf.writeUUID(entry.id());
			buf.writeUtf(entry.name(), 32);
			buf.writeBoolean(entry.authorized());
			buf.writeBoolean(entry.control());
			buf.writeBoolean(entry.vanilla());
		}
	}

	public static FlightRosterPayload read(FriendlyByteBuf buf) {
		int count = buf.readVarInt();
		if (count < 0 || count > 1024) {
			throw new IllegalArgumentException("Flight roster is too large");
		}
		List<FlightMemberEntry> members = new ArrayList<>(count);
		for (int i = 0; i < count; i++) {
			members.add(new FlightMemberEntry(
					buf.readUUID(),
					buf.readUtf(32),
					buf.readBoolean(),
					buf.readBoolean(),
					buf.readBoolean()));
		}
		return new FlightRosterPayload(List.copyOf(members));
	}

	public record FlightMemberEntry(UUID id, String name, boolean authorized, boolean control, boolean vanilla) {
	}
}
