package com.forcedflight.permission;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import com.forcedflight.ForcedFlight;

/**
 * UUID-keyed permission file. Usernames are never used as keys, so a name change does not drop a grant.
 */
public final class FlightPermissionStore {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
	private static volatile FlightPermissionStore instance;

	private final Path file;
	private final Map<UUID, PlayerFlightRecord> players = new ConcurrentHashMap<>();
	private final float defaultSpeed;
	private boolean dirty;
	private int ticksSinceSave;

	private FlightPermissionStore(Path file, float defaultSpeed) {
		this.file = file;
		this.defaultSpeed = defaultSpeed;
	}

	public static FlightPermissionStore get() {
		FlightPermissionStore current = instance;
		if (current == null) {
			throw new IllegalStateException("Flight permissions are not loaded");
		}
		return current;
	}

	public static void install(FlightPermissionStore store) {
		instance = store;
	}

	public static FlightPermissionStore load(Path file, float defaultSpeed) {
		FlightPermissionStore store = new FlightPermissionStore(file, defaultSpeed);
		store.readFromDisk();
		return store;
	}

	public Optional<PlayerFlightRecord> get(UUID uuid) {
		return Optional.ofNullable(players.get(uuid));
	}

	public PlayerFlightRecord getOrCreate(UUID uuid, float speed) {
		return players.computeIfAbsent(uuid, id -> new PlayerFlightRecord(id, FlightSpeed.clamp(speed, ForcedFlight.HARD_MIN_SPEED, ForcedFlight.HARD_MAX_SPEED)));
	}

	public Collection<PlayerFlightRecord> all() {
		return new ArrayList<>(players.values());
	}

	/**
	 * Seeds UUIDs that have never been saved. Existing command edits are not overwritten on restart.
	 */
	public boolean applyBootstrap(Collection<UUID> authorized, Collection<UUID> allowControl, float speed) {
		java.util.Set<UUID> authorizedSet = authorized == null ? java.util.Set.of() : new java.util.LinkedHashSet<>(authorized);
		java.util.Set<UUID> controlSet = allowControl == null ? java.util.Set.of() : new java.util.LinkedHashSet<>(allowControl);
		java.util.Set<UUID> fresh = new java.util.LinkedHashSet<>();
		fresh.addAll(authorizedSet);
		fresh.addAll(controlSet);
		fresh.removeAll(players.keySet());
		if (fresh.isEmpty()) {
			return false;
		}
		for (UUID uuid : fresh) {
			PlayerFlightRecord record = getOrCreate(uuid, speed);
			if (authorizedSet.contains(uuid)) {
				record.setAuthorized(true);
			}
			if (controlSet.contains(uuid)) {
				record.setAllowControl(true);
			}
		}
		saveNow();
		return true;
	}

	public void markDirty() {
		dirty = true;
	}

	public void tickAutosave() {
		if (!dirty) {
			return;
		}
		ticksSinceSave++;
		if (ticksSinceSave >= 20) {
			saveNow();
		}
	}

	public void saveNow() {
		dirty = false;
		ticksSinceSave = 0;
		FileModel model = new FileModel();
		players.entrySet().stream()
				.sorted(Map.Entry.comparingByKey())
				.forEach(entry -> model.players.put(entry.getKey().toString(), Entry.from(entry.getValue())));
		String json = GSON.toJson(model);
		try {
			Path parent = file.getParent();
			if (parent != null) {
				Files.createDirectories(parent);
			}
			Path temp = file.resolveSibling(file.getFileName().toString() + ".tmp");
			Files.writeString(temp, json + System.lineSeparator(), StandardCharsets.UTF_8);
			try {
				Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
			} catch (AtomicMoveNotSupportedException ex) {
				Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
			}
		} catch (IOException ex) {
			dirty = true;
			ForcedFlight.LOGGER.error("Could not save flight permissions to {}", file, ex);
		}
	}

	private void readFromDisk() {
		if (!Files.exists(file)) {
			return;
		}
		try {
			String json = Files.readString(file, StandardCharsets.UTF_8);
			FileModel model = GSON.fromJson(json, FileModel.class);
			if (model == null || model.players == null) {
				return;
			}
			for (Map.Entry<String, Entry> entry : model.players.entrySet()) {
				UUID uuid;
				try {
					uuid = UUID.fromString(entry.getKey());
				} catch (IllegalArgumentException ex) {
					ForcedFlight.LOGGER.warn("Skipping flight permission key that is not a UUID: {}", entry.getKey());
					continue;
				}
				Entry value = entry.getValue();
				if (value == null) {
					continue;
				}
				PlayerFlightRecord record = new PlayerFlightRecord(uuid, defaultSpeed);
				record.setAuthorized(value.authorized);
				record.setFlying(value.authorized && value.flying);
				record.setAllowControl(value.allowControl);
				float speed = Float.isFinite(value.flySpeed) && value.flySpeed > 0f ? value.flySpeed : defaultSpeed;
				record.setFlySpeed(FlightSpeed.clamp(speed, ForcedFlight.HARD_MIN_SPEED, ForcedFlight.HARD_MAX_SPEED));
				players.put(uuid, record);
			}
		} catch (IOException | JsonParseException ex) {
			Path broken = file.resolveSibling(file.getFileName().toString() + ".corrupt");
			try {
				Files.move(file, broken, StandardCopyOption.REPLACE_EXISTING);
				ForcedFlight.LOGGER.error("Flight permissions could not be read and were moved to {}", broken, ex);
			} catch (IOException moveEx) {
				ForcedFlight.LOGGER.error("Flight permissions could not be read at {}", file, ex);
			}
		}
	}

	private static final class FileModel {
		int version = 1;
		Map<String, Entry> players = new LinkedHashMap<>();
	}

	private static final class Entry {
		boolean authorized;
		boolean flying;
		boolean allowControl;
		float flySpeed;

		static Entry from(PlayerFlightRecord record) {
			Entry entry = new Entry();
			entry.authorized = record.authorized();
			entry.flying = record.flying();
			entry.allowControl = record.allowControl();
			entry.flySpeed = record.flySpeed();
			return entry;
		}
	}
}
