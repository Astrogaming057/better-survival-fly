package com.forcedflight;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import com.forcedflight.network.FlightMemberPayload;
import com.forcedflight.network.FlightRequestPayload;
import com.forcedflight.network.FlightRosterPayload;
import com.forcedflight.network.FlightStatePayload;

/**
 * Loader-specific hooks. Each platform sets these before {@link ForcedFlightBootstrap#init()}.
 */
public final class ForcedFlightServices {
	public static Path directory;
	public static Path configFile;
	public static Path permissionFile;
	public static BiConsumer<ServerPlayer, FlightStatePayload> sendState;
	public static Consumer<FlightRequestPayload> sendRequest;
	public static Consumer<FlightStatePayload> acceptClientState;
	public static BiConsumer<ServerPlayer, FlightRosterPayload> sendRoster;
	public static Consumer<FlightMemberPayload> sendMember;
	public static Consumer<FlightRosterPayload> acceptRoster;
	/** Set by the client. Dedicated servers leave this empty and decide from the server player. */
	public static Predicate<Player> cancelClientFlyPenalty;

	private ForcedFlightServices() {
	}

	public static void use(Path configDirectory) {
		directory = configDirectory;
		configFile = configDirectory.resolve("config.json");
		permissionFile = configDirectory.resolve("permissions.json");
	}
}
