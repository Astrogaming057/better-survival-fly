package com.forcedflight;

import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.server.level.ServerPlayer;

import com.forcedflight.network.FlightRequestPayload;
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

	private ForcedFlightServices() {
	}

	public static void use(Path configDirectory) {
		directory = configDirectory;
		configFile = configDirectory.resolve("config.json");
		permissionFile = configDirectory.resolve("permissions.json");
	}
}
