package com.forcedflight.client;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import com.forcedflight.ForcedFlight;
import com.forcedflight.ForcedFlightServices;

/** Client-only controls. Keys themselves are saved with the normal Minecraft options. */
public final class ClientPreferences {
	public enum SpeedMode {
		SCROLL,
		KEYS
	}

	private static SpeedMode mode = SpeedMode.SCROLL;

	private ClientPreferences() {
	}

	public static SpeedMode mode() {
		return mode;
	}

	public static void setMode(SpeedMode next) {
		mode = next == null ? SpeedMode.SCROLL : next;
		save();
	}

	public static void load() {
		Path file = file();
		if (file == null || !Files.exists(file)) {
			return;
		}
		try {
			JsonObject root = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8)).getAsJsonObject();
			String saved = root.has("speedMode") ? root.get("speedMode").getAsString() : "scroll";
			mode = "keys".equalsIgnoreCase(saved) ? SpeedMode.KEYS : SpeedMode.SCROLL;
		} catch (Exception ex) {
			ForcedFlight.LOGGER.warn("Could not read flight client settings.", ex);
		}
	}

	public static void save() {
		Path file = file();
		if (file == null) {
			return;
		}
		try {
			Path parent = file.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			JsonObject root = new JsonObject();
			root.addProperty("speedMode", mode == SpeedMode.KEYS ? "keys" : "scroll");
			Files.writeString(file, new GsonBuilder().setPrettyPrinting().create().toJson(root) + System.lineSeparator(), StandardCharsets.UTF_8);
		} catch (IOException ex) {
			ForcedFlight.LOGGER.warn("Could not write flight client settings.", ex);
		}
	}

	private static Path file() {
		if (ForcedFlightServices.directory == null) {
			return null;
		}
		return ForcedFlightServices.directory.resolve("client.json");
	}
}
