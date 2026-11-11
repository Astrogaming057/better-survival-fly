package com.forcedflight.flight;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.world.entity.player.Abilities;

import com.forcedflight.ForcedFlight;
import com.forcedflight.ForcedFlightServices;
import com.forcedflight.config.ModConfig;
import com.forcedflight.network.FlightStatePayload;
import com.forcedflight.permission.AbilitySnapshot;
import com.forcedflight.permission.FlightPermissionStore;
import com.forcedflight.permission.FlightPolicy;
import com.forcedflight.permission.FlightSpeed;
import com.forcedflight.permission.PlayerFlightRecord;

/**
 * Server authority for who may fly. Field restores happen at movement and at ability-packet construction.
 * Extra ability packets are sent only after a real change, and never more often than the configured cooldown.
 */
public final class FlightManager {
	public static final int TOGGLE_DENIED = 0;
	public static final int TOGGLE_CHANGED = 1;
	public static final int TOGGLE_VANILLA = 2;

	private static final Set<UUID> ONLINE = ConcurrentHashMap.newKeySet();
	private static final Set<UUID> JOIN_RESEND = ConcurrentHashMap.newKeySet();
	private static final ConcurrentHashMap<UUID, Integer> LAST_RESYNC_TICK = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<UUID, Boolean> CONFLICT_LOGGED = new ConcurrentHashMap<>();
	private static volatile boolean anyoneOnline;
	private static volatile MinecraftServer server;
	private static int sendDepth;

	private FlightManager() {
	}

	public static void bindServer(MinecraftServer next) {
		server = next;
	}

	public static void clearRuntime() {
		ONLINE.clear();
		JOIN_RESEND.clear();
		LAST_RESYNC_TICK.clear();
		CONFLICT_LOGGED.clear();
		anyoneOnline = false;
		sendDepth = 0;
		server = null;
	}

	public static void deliverJoinState(MinecraftServer current) {
		if (JOIN_RESEND.isEmpty()) {
			return;
		}
		for (UUID uuid : List.copyOf(JOIN_RESEND)) {
			JOIN_RESEND.remove(uuid);
			ServerPlayer player = current.getPlayerList().getPlayer(uuid);
			if (player != null) {
				sendState(player, false);
				FlightHelp.tell(player);
			}
		}
	}

	public static void onJoin(ServerPlayer player) {
		PlayerFlightRecord record = FlightPermissionStore.get().get(player.getUUID()).orElse(null);
		if (record != null && record.authorized()) {
			markOnline(player.getUUID(), true);
			pushFlight(player, false);
			JOIN_RESEND.add(player.getUUID());
			return;
		}
		markOnline(player.getUUID(), false);
		if (record != null && !vanillaOwns(player)) {
			clearGrant(player);
			sendAbilities(player);
		}
		sendState(player, false);
		JOIN_RESEND.add(player.getUUID());
	}

	public static void onDisconnect(ServerPlayer player) {
		captureFlying(player);
		markOnline(player.getUUID(), false);
		LAST_RESYNC_TICK.remove(player.getUUID());
		CONFLICT_LOGGED.remove(player.getUUID());
		FlightPermissionStore.get().saveNow();
	}

	/** Called after vanilla op/deop updates the player's permission level. */
	public static void onPermissionLevelChanged(ServerPlayer player) {
		if (player.connection == null) {
			return;
		}
		sendState(player, false);
	}

	public static void onRespawn(ServerPlayer player) {
		PlayerFlightRecord record = FlightPermissionStore.get().get(player.getUUID()).orElse(null);
		if (record != null && record.authorized()) {
			markOnline(player.getUUID(), true);
			pushFlight(player, false);
		} else {
			sendState(player, false);
		}
	}

	public static void onWorldChange(ServerPlayer player) {
		onRespawn(player);
	}

	public static void beforeTravel(ServerPlayer player) {
		if (!anyoneOnline || !ModConfig.get().runtimeEnforcement()) {
			return;
		}
		if (!ONLINE.contains(player.getUUID())) {
			return;
		}
		if (sendDepth > 0 || changedByExternalMod(player)) {
			if (sendDepth > 0) {
				return;
			}
			noteConflict(player);
			if (ModConfig.get().enforcementResync && resyncDue(player)) {
				sendState(player, false);
				sendAbilities(player);
			}
		}
	}

	/** Called immediately before an abilities packet copies the live ability object. */
	public static void beforeAbilityPacket(Abilities abilities) {
		MinecraftServer current = server;
		if (current == null || !anyoneOnline || !ModConfig.get().runtimeEnforcement() || abilities == null) {
			return;
		}
		for (ServerPlayer player : current.getPlayerList().getPlayers()) {
			if (ONLINE.contains(player.getUUID()) && player.getAbilities() == abilities) {
				changedByExternalMod(player);
				return;
			}
		}
	}

	public static void restoreCopiedPlayer(ServerPlayer player) {
		if (!ModConfig.get().runtimeEnforcement()) {
			return;
		}
		changedByExternalMod(player);
	}

	public static void handleClientFlying(ServerPlayer player, boolean requested) {
		PlayerFlightRecord record = FlightPermissionStore.get().get(player.getUUID()).orElse(null);
		if (record == null || !record.authorized() || vanillaOwns(player)) {
			return;
		}
		Abilities abilities = player.getAbilities();
		abilities.mayfly = true;
		abilities.setFlyingSpeed(record.flySpeed());
		abilities.flying = requested && player.isAlive() && !player.isSleeping() && !player.isPassenger();
		record.setFlying(requested);
		FlightPermissionStore.get().markDirty();
		sendState(player, false);
	}

	public static int tryToggleFromClient(ServerPlayer player) {
		if (!ModConfig.get().enabled) {
			return TOGGLE_DENIED;
		}
		if (vanillaOwns(player)) {
			return TOGGLE_VANILLA;
		}
		PlayerFlightRecord existing = FlightPermissionStore.get().get(player.getUUID()).orElse(null);
		if (!FlightPolicy.canToggleSelf(isOperator(player), existing != null && existing.allowControl())) {
			return TOGGLE_DENIED;
		}
		PlayerFlightRecord record = FlightPermissionStore.get().getOrCreate(player.getUUID(), ModConfig.get().defaultSpeed);
		FlightPolicy.toggleAuthorization(record);
		normalizeSpeed(record);
		markOnline(player.getUUID(), record.authorized());
		pushFlight(player, true);
		return TOGGLE_CHANGED;
	}

	/**
	 * Sets this player's saved flight speed from a HUD multiplier. Creative and spectator are left at vanilla speed.
	 *
	 * @return the raw speed stored, or {@link Float#NaN} when it was not applied
	 */
	public static float setOwnSpeed(ServerPlayer player, float multiplier) {
		if (!ModConfig.get().enabled || !Float.isFinite(multiplier) || vanillaOwns(player)) {
			return Float.NaN;
		}
		PlayerFlightRecord record = FlightPermissionStore.get().get(player.getUUID()).orElse(null);
		if (record == null || !record.authorized()) {
			return Float.NaN;
		}
		ModConfig config = ModConfig.get();
		float clamped = FlightSpeed.clamp(multiplier * ForcedFlight.VANILLA_FLY_SPEED, config.minSpeed, config.maxSpeed);
		record.setFlySpeed(clamped);
		player.getAbilities().setFlyingSpeed(clamped);
		FlightPermissionStore.get().markDirty();
		sendState(player, false);
		sendAbilities(player);
		FlightPermissionStore.get().saveNow();
		return clamped;
	}

	public static boolean usesVanillaFlight(ServerPlayer player) {
		return vanillaOwns(player);
	}

	public static void trySetSpeed(ServerPlayer player, float requested) {
		if (!ModConfig.get().enabled || !Float.isFinite(requested)) {
			return;
		}
		PlayerFlightRecord record = FlightPermissionStore.get().get(player.getUUID()).orElse(null);
		if (record == null || !record.authorized() || vanillaOwns(player)) {
			return;
		}
		ModConfig config = ModConfig.get();
		float clamped = FlightSpeed.clamp(requested, config.minSpeed, config.maxSpeed);
		if (Math.abs(clamped - record.flySpeed()) < 0.0001f && Math.abs(player.getAbilities().getFlyingSpeed() - clamped) < 0.0001f) {
			return;
		}
		record.setFlySpeed(clamped);
		player.getAbilities().setFlyingSpeed(clamped);
		FlightPermissionStore.get().markDirty();
		sendState(player, false);
		sendAbilities(player);
	}

	public static void pushFlight(ServerPlayer player, boolean announce) {
		PlayerFlightRecord record = FlightPermissionStore.get().get(player.getUUID()).orElse(null);
		if (record == null) {
			return;
		}
		normalizeSpeed(record);
		markOnline(player.getUUID(), record.authorized());
		if (vanillaOwns(player)) {
			restoreVanillaFlySpeed(player);
		} else if (record.authorized()) {
			write(player, FlightPolicy.reconcile(capture(player), record, false, suppressFlying(player)));
		} else {
			clearGrant(player);
		}
		sendState(player, announce);
		if (!vanillaOwns(player)) {
			sendAbilities(player);
		}
		FlightPermissionStore.get().saveNow();
	}

	/** Pushes the server speed cap to every online player and pulls anyone above it back down. */
	public static void applySpeedLimit() {
		MinecraftServer current = server;
		if (current == null) {
			return;
		}
		ModConfig config = ModConfig.get();
		boolean dirty = false;
		for (ServerPlayer player : current.getPlayerList().getPlayers()) {
			PlayerFlightRecord record = FlightPermissionStore.get().get(player.getUUID()).orElse(null);
			if (record != null) {
				float clamped = FlightSpeed.clamp(record.flySpeed(), config.minSpeed, config.maxSpeed);
				if (Math.abs(clamped - record.flySpeed()) >= 0.0001f) {
					record.setFlySpeed(clamped);
					dirty = true;
					if (record.authorized() && !vanillaOwns(player)) {
						player.getAbilities().setFlyingSpeed(clamped);
						sendAbilities(player);
					}
				}
			}
			sendState(player, false);
		}
		if (dirty) {
			FlightPermissionStore.get().saveNow();
		}
	}

	public static void syncControl(ServerPlayer player) {
		sendState(player, false);
		FlightPermissionStore.get().saveNow();
	}

	public static boolean setKeepFlying(ServerPlayer player, boolean enabled) {
		PlayerFlightRecord record = recordForEdit(player);
		record.setKeepFlying(enabled);
		FlightPermissionStore.get().saveNow();
		sendState(player, false);
		return record.keepFlying();
	}

	public static PlayerFlightRecord recordForEdit(ServerPlayer player) {
		PlayerFlightRecord record = FlightPermissionStore.get().getOrCreate(player.getUUID(), ModConfig.get().defaultSpeed);
		normalizeSpeed(record);
		return record;
	}

	public static boolean isOperator(ServerPlayer player) {
		return player.hasPermissions(ModConfig.get().operatorLevel);
	}

	private static boolean changedByExternalMod(ServerPlayer player) {
		if (vanillaOwns(player)) {
			return false;
		}
		PlayerFlightRecord record = FlightPermissionStore.get().get(player.getUUID()).orElse(null);
		AbilitySnapshot next = FlightPolicy.reconcile(capture(player), record, false, suppressFlying(player));
		AbilitySnapshot current = capture(player);
		if (next == current || next.sameFlightAs(current)) {
			return false;
		}
		write(player, next);
		return true;
	}

	private static void restoreVanillaFlySpeed(ServerPlayer player) {
		Abilities abilities = player.getAbilities();
		if (Math.abs(abilities.getFlyingSpeed() - ForcedFlight.VANILLA_FLY_SPEED) < 0.0001f) {
			return;
		}
		abilities.setFlyingSpeed(ForcedFlight.VANILLA_FLY_SPEED);
		sendAbilities(player);
	}

	private static void clearGrant(ServerPlayer player) {
		if (vanillaOwns(player)) {
			return;
		}
		Abilities abilities = player.getAbilities();
		abilities.mayfly = false;
		abilities.flying = false;
	}

	private static AbilitySnapshot capture(ServerPlayer player) {
		Abilities abilities = player.getAbilities();
		return new AbilitySnapshot(
				abilities.mayfly,
				abilities.flying,
				abilities.getFlyingSpeed(),
				abilities.invulnerable,
				abilities.instabuild,
				abilities.mayBuild,
				abilities.getWalkingSpeed()
		);
	}

	private static void write(ServerPlayer player, AbilitySnapshot snapshot) {
		Abilities abilities = player.getAbilities();
		abilities.mayfly = snapshot.allowFlying();
		abilities.flying = snapshot.flying();
		if (Math.abs(abilities.getFlyingSpeed() - snapshot.flySpeed()) >= 0.0001f) {
			abilities.setFlyingSpeed(snapshot.flySpeed());
		}
	}

	private static void sendAbilities(ServerPlayer player) {
		if (player.connection == null) {
			return;
		}
		sendDepth++;
		try {
			player.onUpdateAbilities();
		} finally {
			sendDepth--;
		}
	}

	private static void sendState(ServerPlayer player, boolean announce) {
		if (player.connection == null || ForcedFlightServices.sendState == null) {
			return;
		}
		PlayerFlightRecord record = FlightPermissionStore.get().get(player.getUUID()).orElse(null);
		ModConfig config = ModConfig.get();
		boolean authorized = record != null && record.authorized();
		boolean allowControl = record != null && record.allowControl();
		float speed = record == null ? config.defaultSpeed : record.flySpeed();
		boolean flying = record != null && record.flying();
		int revision = player.getServer() == null ? 0 : player.getServer().getTickCount();
		ForcedFlightServices.sendState.accept(player, new FlightStatePayload(
				revision,
				authorized,
				flying,
				allowControl,
				FlightPolicy.canToggleSelf(isOperator(player), allowControl),
				speed,
				announce,
				config.minSpeed,
				config.maxSpeed,
				config.speedStep,
				config.flyPenalty,
				isOperator(player),
				config.fallDamage,
				config.joinMessage,
				record != null && record.keepFlying()
		));
	}

	/** Sends the current config, including the mining penalty, to everyone online. */
	public static void broadcastState() {
		MinecraftServer current = server;
		if (current == null) {
			return;
		}
		for (ServerPlayer player : current.getPlayerList().getPlayers()) {
			sendState(player, false);
		}
	}

	private static boolean resyncDue(ServerPlayer player) {
		int now = player.getServer().getTickCount();
		int previous = LAST_RESYNC_TICK.getOrDefault(player.getUUID(), -100000);
		if (now - previous < ModConfig.get().resyncCooldownTicks) {
			return false;
		}
		LAST_RESYNC_TICK.put(player.getUUID(), now);
		return true;
	}

	private static void noteConflict(ServerPlayer player) {
		if (CONFLICT_LOGGED.putIfAbsent(player.getUUID(), Boolean.TRUE) == null) {
			ForcedFlight.LOGGER.info("Restored authorized flight for {} after another change cleared it", player.getName().getString());
		}
	}

	private static void captureFlying(ServerPlayer player) {
		PlayerFlightRecord record = FlightPermissionStore.get().get(player.getUUID()).orElse(null);
		if (record == null || !record.authorized() || vanillaOwns(player)) {
			return;
		}
		if (player.getAbilities().mayfly) {
			record.setFlying(player.getAbilities().flying);
			FlightPermissionStore.get().markDirty();
		}
	}

	private static void normalizeSpeed(PlayerFlightRecord record) {
		ModConfig config = ModConfig.get();
		record.setFlySpeed(FlightSpeed.clamp(record.flySpeed(), config.minSpeed, config.maxSpeed));
	}

	private static boolean suppressFlying(ServerPlayer player) {
		return !player.isAlive() || player.isSleeping() || player.isPassenger();
	}

	private static boolean vanillaOwns(ServerPlayer player) {
		GameType mode = player.gameMode.getGameModeForPlayer();
		return mode == GameType.CREATIVE || mode == GameType.SPECTATOR;
	}

	private static void markOnline(UUID uuid, boolean authorized) {
		if (authorized) {
			ONLINE.add(uuid);
		} else {
			ONLINE.remove(uuid);
		}
		anyoneOnline = !ONLINE.isEmpty();
	}
}
