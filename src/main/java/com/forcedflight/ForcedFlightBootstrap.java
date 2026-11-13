package com.forcedflight;

import java.io.IOException;
import java.nio.file.Files;

import com.forcedflight.config.ModConfig;
import com.forcedflight.permission.FlightPermissionStore;

public final class ForcedFlightBootstrap {
	private ForcedFlightBootstrap() {
	}

	public static void init() {
		try {
			Files.createDirectories(ForcedFlightServices.directory);
		} catch (IOException ex) {
			ForcedFlight.LOGGER.error("Could not create {}", ForcedFlightServices.directory, ex);
		}
		reload();
		ForcedFlight.LOGGER.info("Forced Creative Flight ready");
	}

	public static void reload() {
		ModConfig.load(ForcedFlightServices.configFile);
		ModConfig config = ModConfig.get();
		FlightPermissionStore store = FlightPermissionStore.load(ForcedFlightServices.permissionFile, config.defaultSpeed);
		store.applyBootstrap(config.bootstrapAuthorized, config.bootstrapAllowControl, config.defaultSpeed);
		FlightPermissionStore.install(store);
	}
}
