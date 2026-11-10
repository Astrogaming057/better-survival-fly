package com.forcedflight.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import com.forcedflight.ForcedFlight;
import com.forcedflight.ForcedFlightServices;
import com.forcedflight.permission.FlightSpeed;

public final class ModConfig {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static volatile ModConfig current = defaults();

	public final boolean enabled;
	public final int operatorLevel;
	public final String defaultToggleKey;
	public final float defaultSpeed;
	public final float minSpeed;
	public final float maxSpeed;
	public final float speedStep;
	public final boolean hudEnabled;
	public final String hudAnchor;
	public final int hudOffsetX;
	public final int hudOffsetY;
	public final int hudDurationTicks;
	public final boolean hudShowWhileFlying;
	public final boolean enforcementEnabled;
	public final boolean enforcementResync;
	public final int resyncCooldownTicks;
	public final List<UUID> bootstrapAuthorized;
	public final List<UUID> bootstrapAllowControl;

	private ModConfig(
			boolean enabled,
			int operatorLevel,
			String defaultToggleKey,
			float defaultSpeed,
			float minSpeed,
			float maxSpeed,
			float speedStep,
			boolean hudEnabled,
			String hudAnchor,
			int hudOffsetX,
			int hudOffsetY,
			int hudDurationTicks,
			boolean hudShowWhileFlying,
			boolean enforcementEnabled,
			boolean enforcementResync,
			int resyncCooldownTicks,
			List<UUID> bootstrapAuthorized,
			List<UUID> bootstrapAllowControl
	) {
		this.enabled = enabled;
		this.operatorLevel = operatorLevel;
		this.defaultToggleKey = defaultToggleKey;
		this.defaultSpeed = defaultSpeed;
		this.minSpeed = minSpeed;
		this.maxSpeed = maxSpeed;
		this.speedStep = speedStep;
		this.hudEnabled = hudEnabled;
		this.hudAnchor = hudAnchor;
		this.hudOffsetX = hudOffsetX;
		this.hudOffsetY = hudOffsetY;
		this.hudDurationTicks = hudDurationTicks;
		this.hudShowWhileFlying = hudShowWhileFlying;
		this.enforcementEnabled = enforcementEnabled;
		this.enforcementResync = enforcementResync;
		this.resyncCooldownTicks = resyncCooldownTicks;
		this.bootstrapAuthorized = bootstrapAuthorized;
		this.bootstrapAllowControl = bootstrapAllowControl;
	}

	public static ModConfig get() {
		return current;
	}

	public static final float DEFAULT_MAX_SPEED = 0.375f;
	private static final float PREVIOUS_DEFAULT_MAX_SPEED = 0.25f;

	public static ModConfig defaults() {
		float min = 0.01f;
		float max = DEFAULT_MAX_SPEED;
		return new ModConfig(
				true,
				2,
				"I",
				FlightSpeed.clamp(ForcedFlight.VANILLA_FLY_SPEED, min, max),
				min,
				max,
				0.01f,
				true,
				"bottom_center",
				0,
				-58,
				100,
				false,
				true,
				true,
				20,
				List.of(),
				List.of()
		);
	}

	public static void load(Path file) {
		if (!Files.exists(file)) {
			current = defaults();
			write(file, current);
			return;
		}
		try {
			String json = Files.readString(file, StandardCharsets.UTF_8);
			JsonElement parsed = JsonParser.parseString(json);
			if (!parsed.isJsonObject()) {
				throw new IOException("config root is not an object");
			}
			current = fromJson(parsed.getAsJsonObject());
			if (Math.abs(current.maxSpeed - PREVIOUS_DEFAULT_MAX_SPEED) < 0.00015f) {
				current = current.withMaxSpeed(DEFAULT_MAX_SPEED);
				write(file, current);
			}
		} catch (Exception ex) {
			ForcedFlight.LOGGER.error("Could not read {}. Defaults will be used for this launch.", file, ex);
			current = defaults();
		}
	}

	public static void write(Path file, ModConfig config) {
		try {
			Path parent = file.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Files.writeString(file, GSON.toJson(toJson(config)) + System.lineSeparator(), StandardCharsets.UTF_8);
		} catch (IOException ex) {
			ForcedFlight.LOGGER.error("Could not write flight config {}", file, ex);
		}
	}

	public boolean runtimeEnforcement() {
		return enabled && enforcementEnabled;
	}

	/** Saves a new server-wide maximum and returns the raw speed actually stored. */
	public static float saveMaxSpeed(float rawMax) {
		current = current.withMaxSpeed(rawMax);
		if (ForcedFlightServices.configFile != null) {
			write(ForcedFlightServices.configFile, current);
		}
		return current.maxSpeed;
	}

	public ModConfig withMaxSpeed(float nextMax) {
		float max = FlightSpeed.clamp(nextMax, this.minSpeed, ForcedFlight.HARD_MAX_SPEED);
		return new ModConfig(
				enabled,
				operatorLevel,
				defaultToggleKey,
				FlightSpeed.clamp(defaultSpeed, minSpeed, max),
				minSpeed,
				max,
				speedStep,
				hudEnabled,
				hudAnchor,
				hudOffsetX,
				hudOffsetY,
				hudDurationTicks,
				hudShowWhileFlying,
				enforcementEnabled,
				enforcementResync,
				resyncCooldownTicks,
				bootstrapAuthorized,
				bootstrapAllowControl
		);
	}

	private static ModConfig fromJson(JsonObject root) {
		ModConfig base = defaults();
		float min = sanitizeSpeed(floatOf(objectOf(root, "flight"), "minSpeed", base.minSpeed), base.minSpeed);
		float max = sanitizeSpeed(floatOf(objectOf(root, "flight"), "maxSpeed", base.maxSpeed), base.maxSpeed);
		if (min > max) {
			float swap = min;
			min = max;
			max = swap;
		}
		float step = floatOf(objectOf(root, "flight"), "speedStep", base.speedStep);
		if (!Float.isFinite(step) || step <= 0f) {
			step = base.speedStep;
		}
		if (step > (max - min) && max > min) {
			step = max - min;
		}
		step = Math.round(step * 10000f) / 10000f;
		float fallback = FlightSpeed.clamp(floatOf(objectOf(root, "flight"), "defaultSpeed", base.defaultSpeed), min, max);
		int operatorLevel = intOf(root, "operatorLevel", base.operatorLevel);
		if (operatorLevel < 1) {
			operatorLevel = 1;
		} else if (operatorLevel > 4) {
			operatorLevel = 4;
		}
		String key = stringOf(root, "defaultToggleKey", base.defaultToggleKey);
		if (key.isBlank()) {
			key = "I";
		}
		JsonObject hud = objectOf(root, "hud");
		String anchor = stringOf(hud, "anchor", base.hudAnchor).toLowerCase(Locale.ROOT);
		if (!anchor.equals("top_left") && !anchor.equals("top_right") && !anchor.equals("bottom_left")
				&& !anchor.equals("bottom_right") && !anchor.equals("bottom_center")) {
			anchor = "bottom_center";
		}
		JsonObject enforcement = objectOf(root, "enforcement");
		String mode = stringOf(enforcement, "mode", "resync").toLowerCase(Locale.ROOT);
		int cooldown = intOf(enforcement, "resyncCooldownTicks", base.resyncCooldownTicks);
		if (cooldown < 5) {
			cooldown = 5;
		}
		int duration = intOf(hud, "durationTicks", base.hudDurationTicks);
		if (duration < 0) {
			duration = 0;
		}
		JsonObject bootstrap = objectOf(root, "bootstrap");
		return new ModConfig(
				boolOf(root, "enabled", base.enabled),
				operatorLevel,
				key.trim(),
				fallback,
				min,
				max,
				step,
				boolOf(hud, "enabled", base.hudEnabled),
				anchor,
				intOf(hud, "offsetX", base.hudOffsetX),
				intOf(hud, "offsetY", base.hudOffsetY),
				duration,
				boolOf(hud, "showWhileFlying", base.hudShowWhileFlying),
				boolOf(enforcement, "enabled", base.enforcementEnabled),
				!"silent".equals(mode),
				cooldown,
				readUuids(bootstrap, "authorized"),
				readUuids(bootstrap, "allowControl")
		);
	}

	private static float sanitizeSpeed(float value, float fallback) {
		if (!Float.isFinite(value) || value <= 0f) {
			return fallback;
		}
		return FlightSpeed.clamp(value, ForcedFlight.HARD_MIN_SPEED, ForcedFlight.HARD_MAX_SPEED);
	}

	private static List<UUID> readUuids(JsonObject object, String name) {
		List<UUID> ids = new ArrayList<>();
		if (object == null || !object.has(name) || !object.get(name).isJsonArray()) {
			return List.copyOf(ids);
		}
		for (JsonElement element : object.getAsJsonArray(name)) {
			if (!element.isJsonPrimitive()) {
				continue;
			}
			try {
				ids.add(UUID.fromString(element.getAsString().trim()));
			} catch (IllegalArgumentException ex) {
				ForcedFlight.LOGGER.warn("Ignoring invalid UUID in config {}.{}", "bootstrap", name);
			}
		}
		return List.copyOf(ids);
	}

	private static JsonObject toJson(ModConfig config) {
		JsonObject root = new JsonObject();
		root.addProperty("configVersion", 1);
		root.addProperty("enabled", config.enabled);
		root.addProperty("operatorLevel", config.operatorLevel);
		root.addProperty("defaultToggleKey", config.defaultToggleKey);
		JsonObject flight = new JsonObject();
		flight.addProperty("defaultSpeed", config.defaultSpeed);
		flight.addProperty("minSpeed", config.minSpeed);
		flight.addProperty("maxSpeed", config.maxSpeed);
		flight.addProperty("speedStep", config.speedStep);
		root.add("flight", flight);
		JsonObject hud = new JsonObject();
		hud.addProperty("enabled", config.hudEnabled);
		hud.addProperty("anchor", config.hudAnchor);
		hud.addProperty("offsetX", config.hudOffsetX);
		hud.addProperty("offsetY", config.hudOffsetY);
		hud.addProperty("durationTicks", config.hudDurationTicks);
		hud.addProperty("showWhileFlying", config.hudShowWhileFlying);
		root.add("hud", hud);
		JsonObject enforcement = new JsonObject();
		enforcement.addProperty("enabled", config.enforcementEnabled);
		enforcement.addProperty("mode", config.enforcementResync ? "resync" : "silent");
		enforcement.addProperty("resyncCooldownTicks", config.resyncCooldownTicks);
		root.add("enforcement", enforcement);
		JsonObject bootstrap = new JsonObject();
		bootstrap.add("authorized", uuids(config.bootstrapAuthorized));
		bootstrap.add("allowControl", uuids(config.bootstrapAllowControl));
		root.add("bootstrap", bootstrap);
		return root;
	}

	private static JsonArray uuids(List<UUID> ids) {
		JsonArray array = new JsonArray();
		for (UUID id : ids) {
			array.add(new JsonPrimitive(id.toString()));
		}
		return array;
	}

	private static JsonObject objectOf(JsonObject object, String name) {
		if (object != null && object.has(name) && object.get(name).isJsonObject()) {
			return object.getAsJsonObject(name);
		}
		return new JsonObject();
	}

	private static boolean boolOf(JsonObject object, String name, boolean fallback) {
		if (object != null && object.has(name) && object.get(name).isJsonPrimitive()) {
			JsonPrimitive primitive = object.getAsJsonPrimitive(name);
			if (primitive.isBoolean()) {
				return primitive.getAsBoolean();
			}
		}
		return fallback;
	}

	private static int intOf(JsonObject object, String name, int fallback) {
		if (object != null && object.has(name) && object.get(name).isJsonPrimitive() && object.get(name).getAsJsonPrimitive().isNumber()) {
			return object.get(name).getAsInt();
		}
		return fallback;
	}

	private static float floatOf(JsonObject object, String name, float fallback) {
		if (object != null && object.has(name) && object.get(name).isJsonPrimitive() && object.get(name).getAsJsonPrimitive().isNumber()) {
			return object.get(name).getAsFloat();
		}
		return fallback;
	}

	private static String stringOf(JsonObject object, String name, String fallback) {
		if (object != null && object.has(name) && object.get(name).isJsonPrimitive()) {
			return object.get(name).getAsString();
		}
		return fallback;
	}
}
