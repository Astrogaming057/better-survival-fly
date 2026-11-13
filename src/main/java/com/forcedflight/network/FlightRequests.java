package com.forcedflight.network;
import com.forcedflight.compat.McText;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import com.forcedflight.ForcedFlight;
import com.forcedflight.ForcedFlightServices;
import com.forcedflight.compat.McServer;
import com.forcedflight.config.ModConfig;
import com.forcedflight.flight.FlightManager;
import com.forcedflight.permission.FlightPermissionStore;
import com.forcedflight.permission.FlightPolicy;
import com.forcedflight.permission.FlightSpeed;
import com.forcedflight.permission.PlayerFlightRecord;

public final class FlightRequests {
	private FlightRequests() {
	}

	public static void handle(ServerPlayer player, FlightRequestPayload payload) {
		if (payload.action() == FlightRequestPayload.TOGGLE) {
			int result = FlightManager.tryToggleFromClient(player);
			if (result == FlightManager.TOGGLE_VANILLA) {
				McText.tell(player, McText.literal(
						"Forced Creative Flight is for survival only. Creative and spectator keep vanilla flight."), false);
			} else if (result != FlightManager.TOGGLE_CHANGED) {
				McText.tell(player, McText.literal("You do not have permission to toggle flight."), false);
			}
			return;
		}
		if (payload.action() == FlightRequestPayload.SET_SPEED) {
			FlightManager.trySetSpeed(player, payload.flySpeed());
			return;
		}
		if (payload.action() == FlightRequestPayload.SET_KEEP_FLYING) {
			boolean enabled = FlightManager.setKeepFlying(player, payload.flySpeed() >= 0.5f);
			McText.tell(player, McText.literal(enabled
					? "Keep flying enabled for you. Landing no longer cancels your flight until you double-tap jump."
					: "Keep flying disabled for you. Landing cancels your flight like normal."), false);
			return;
		}
		if (!FlightManager.isOperator(player)) {
			McText.tell(player, McText.literal("Only operators can change flight server settings."), false);
			return;
		}
		if (payload.action() == FlightRequestPayload.SET_MAX_SPEED) {
			float saved = ModConfig.saveMaxSpeed(payload.flySpeed() * ForcedFlight.VANILLA_FLY_SPEED);
			FlightManager.applySpeedLimit();
			McText.tell(player, McText.literal(String.format(Locale.ROOT,
					"Max flight speed set to %.2fx.", FlightSpeed.multiplier(saved))), false);
			return;
		}
		if (payload.action() == FlightRequestPayload.SET_FLY_PENALTY) {
			boolean enabled = ModConfig.saveFlyPenalty(payload.flySpeed() >= 0.5f);
			FlightManager.broadcastState();
			McText.tell(player, McText.literal(enabled
					? "Fly mining penalty enabled. Breaking blocks while flying is slower."
					: "Fly mining penalty disabled. Breaking blocks while flying uses normal speed."), false);
			return;
		}
		if (payload.action() == FlightRequestPayload.SET_FALL_DAMAGE) {
			boolean enabled = ModConfig.saveFallDamage(payload.flySpeed() >= 0.5f);
			FlightManager.broadcastState();
			McText.tell(player, McText.literal(enabled
					? "Fall damage enabled while flight is toggled off."
					: "Fall damage disabled while flight is granted and toggled off."), false);
			return;
		}
		if (payload.action() == FlightRequestPayload.SET_JOIN_MESSAGE) {
			boolean enabled = ModConfig.saveJoinMessage(payload.flySpeed() >= 0.5f);
			FlightManager.broadcastState();
			McText.tell(player, McText.literal(enabled
					? "Join message enabled."
					: "Join message disabled."), false);
		}
	}

	public static void sendToggle() {
		send(FlightRequestPayload.TOGGLE, 0f);
	}

	public static void sendSpeed(float flySpeed) {
		send(FlightRequestPayload.SET_SPEED, flySpeed);
	}

	public static void sendMaxSpeed(float multiplier) {
		send(FlightRequestPayload.SET_MAX_SPEED, multiplier);
	}

	public static void sendFlyPenalty(boolean enabled) {
		send(FlightRequestPayload.SET_FLY_PENALTY, enabled ? 1f : 0f);
	}

	public static void sendFallDamage(boolean enabled) {
		send(FlightRequestPayload.SET_FALL_DAMAGE, enabled ? 1f : 0f);
	}

	public static void sendJoinMessage(boolean enabled) {
		send(FlightRequestPayload.SET_JOIN_MESSAGE, enabled ? 1f : 0f);
	}

	public static void sendKeepFlying(boolean enabled) {
		send(FlightRequestPayload.SET_KEEP_FLYING, enabled ? 1f : 0f);
	}

	public static void handleMember(ServerPlayer operator, FlightMemberPayload payload) {
		if (!FlightManager.isOperator(operator)) {
			return;
		}
		if (payload.action() == FlightMemberPayload.REQUEST || McServer.server(operator) == null) {
			sendRoster(operator);
			return;
		}
		ServerPlayer target = McServer.server(operator).getPlayerList().getPlayer(payload.target());
		if (target == null) {
			sendRoster(operator);
			return;
		}
		if (payload.action() == FlightMemberPayload.TOGGLE_FLIGHT) {
			PlayerFlightRecord existing = FlightPermissionStore.get().get(target.getUUID()).orElse(null);
			if (FlightManager.usesVanillaFlight(target) && (existing == null || !existing.authorized())) {
				McText.tell(operator, McText.literal(
						target.getName().getString() + " is in creative or spectator. This mod only enables flight in survival."), false);
			} else {
				PlayerFlightRecord record = FlightManager.recordForEdit(target);
				FlightPolicy.toggleAuthorization(record);
				FlightManager.pushFlight(target, true);
			}
		} else if (payload.action() == FlightMemberPayload.TOGGLE_CONTROL) {
			PlayerFlightRecord record = FlightManager.recordForEdit(target);
			if (record.allowControl()) {
				FlightPolicy.revokeControl(record);
			} else {
				FlightPolicy.grantControl(record);
			}
			FlightManager.syncControl(target);
		}
		sendRoster(operator);
	}

	public static void requestRoster() {
		sendMember(FlightMemberPayload.REQUEST, new UUID(0L, 0L));
	}

	public static void toggleMemberFlight(UUID target) {
		sendMember(FlightMemberPayload.TOGGLE_FLIGHT, target);
	}

	public static void toggleMemberControl(UUID target) {
		sendMember(FlightMemberPayload.TOGGLE_CONTROL, target);
	}

	private static void sendRoster(ServerPlayer viewer) {
		if (ForcedFlightServices.sendRoster == null || McServer.server(viewer) == null) {
			return;
		}
		List<FlightRosterPayload.FlightMemberEntry> entries = new ArrayList<>();
		for (ServerPlayer player : McServer.server(viewer).getPlayerList().getPlayers()) {
			PlayerFlightRecord record = FlightPermissionStore.get().get(player.getUUID()).orElse(null);
			entries.add(new FlightRosterPayload.FlightMemberEntry(
					player.getUUID(),
					player.getName().getString(),
					record != null && record.authorized(),
					record != null && record.allowControl(),
					FlightManager.usesVanillaFlight(player)));
		}
		entries.sort(Comparator.comparing(FlightRosterPayload.FlightMemberEntry::name, String.CASE_INSENSITIVE_ORDER));
		ForcedFlightServices.sendRoster.accept(viewer, new FlightRosterPayload(List.copyOf(entries)));
	}

	private static void sendMember(int action, UUID target) {
		if (ForcedFlightServices.sendMember != null && target != null) {
			ForcedFlightServices.sendMember.accept(new FlightMemberPayload(action, target));
		}
	}

	private static void send(int action, float value) {
		if (ForcedFlightServices.sendRequest != null) {
			ForcedFlightServices.sendRequest.accept(new FlightRequestPayload(action, value));
		}
	}
}
