package com.forcedflight;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.forcedflight.permission.AbilitySnapshot;
import com.forcedflight.permission.FlightPermissionStore;
import com.forcedflight.permission.FlightPolicy;
import com.forcedflight.permission.FlightSpeed;
import com.forcedflight.permission.PlayerFlightRecord;

class FlightRulesTest {
	private static PlayerFlightRecord record(boolean authorized, boolean flying, boolean control) {
		PlayerFlightRecord record = new PlayerFlightRecord(UUID.randomUUID(), 0.05f);
		record.setAuthorized(authorized);
		record.setFlying(flying);
		record.setAllowControl(control);
		return record;
	}

	@Test
	void operatorCanEnableAndDisableOwnFlight() {
		PlayerFlightRecord record = record(false, false, false);
		assertTrue(FlightPolicy.canToggleSelf(true, false));
		assertTrue(FlightPolicy.toggleAuthorization(record));
		assertTrue(record.authorized());
		assertTrue(record.flying());
		assertFalse(FlightPolicy.toggleAuthorization(record));
		assertFalse(record.authorized());
		assertFalse(record.flying());
	}

	@Test
	void operatorCanEnableAndDisableAnotherPlayerWithoutRemovingControl() {
		PlayerFlightRecord record = record(false, false, true);
		assertTrue(FlightPolicy.canAdminister(true));
		FlightPolicy.enableFlight(record);
		assertTrue(record.authorized());
		assertTrue(record.flying());
		assertTrue(record.allowControl());
		FlightPolicy.disableFlight(record);
		assertFalse(record.authorized());
		assertFalse(record.flying());
		assertTrue(record.allowControl());
	}

	@Test
	void grantAndRevokeControlKeepExistingAuthorization() {
		PlayerFlightRecord record = record(true, true, false);
		FlightPolicy.grantControl(record);
		assertTrue(record.allowControl());
		assertTrue(record.authorized());
		FlightPolicy.revokeControl(record);
		assertFalse(record.allowControl());
		assertTrue(record.authorized());
		assertTrue(record.flying());
	}

	@Test
	void allowControlCanToggleSelfAndKeybind() {
		PlayerFlightRecord record = record(false, false, true);
		assertTrue(FlightPolicy.canToggleSelf(false, true));
		assertTrue(FlightPolicy.toggleAuthorization(record));
		assertTrue(FlightPolicy.toggleAuthorization(record) == false);
	}

	@Test
	void authorizedPlayerWithoutControlCannotToggle() {
		PlayerFlightRecord record = record(true, true, false);
		assertFalse(FlightPolicy.canToggleSelf(false, record.allowControl()));
		assertTrue(record.authorized());
	}

	@Test
	void unauthorizedPlayerCannotGrantSelfOrOthers() {
		assertFalse(FlightPolicy.canToggleSelf(false, false));
		assertFalse(FlightPolicy.canAdminister(false));
		PlayerFlightRecord record = record(false, false, false);
		AbilitySnapshot survival = new AbilitySnapshot(false, false, 0.05f, false, false, true, 0.1f);
		assertTrue(FlightPolicy.reconcile(survival, record, false, false) == survival);
	}

	@Test
	void scrollSpeedStepsAndClamps() {
		assertEquals(0.06f, FlightSpeed.step(0.05f, 1, 0.01f, 0.25f, 0.01f), 0.00001f);
		assertEquals(0.04f, FlightSpeed.step(0.05f, -1, 0.01f, 0.25f, 0.01f), 0.00001f);
		assertEquals(0.01f, FlightSpeed.step(0.01f, -3, 0.01f, 0.25f, 0.01f), 0.00001f);
		assertEquals(0.25f, FlightSpeed.step(0.25f, 4, 0.01f, 0.25f, 0.01f), 0.00001f);
		assertEquals(1.0f, FlightSpeed.clamp(5f, 0.01f, 2f), 0.00001f);
	}

	@Test
	void externalClearRestoresFlightAndLeavesOtherAbilities() {
		PlayerFlightRecord record = record(true, true, false);
		record.setFlySpeed(0.08f);
		AbilitySnapshot wiped = new AbilitySnapshot(false, false, 0.05f, true, false, false, 0.2f);
		AbilitySnapshot restored = FlightPolicy.reconcile(wiped, record, false, false);
		assertTrue(restored.allowFlying());
		assertTrue(restored.flying());
		assertEquals(0.08f, restored.flySpeed(), 0.00001f);
		assertTrue(restored.invulnerable());
		assertFalse(restored.creativeMode());
		assertFalse(restored.allowModifyWorld());
		assertEquals(0.2f, restored.walkSpeed(), 0.00001f);
	}

	@Test
	void creativeAndSpectatorAreNotOverwritten() {
		PlayerFlightRecord record = record(true, true, true);
		AbilitySnapshot creative = new AbilitySnapshot(true, false, 0.05f, true, true, true, 0.1f);
		assertTrue(FlightPolicy.reconcile(creative, record, true, false) == creative);
	}

	@Test
	void vehicleSuppressesAirborneFlagWithoutRevokingGrant() {
		PlayerFlightRecord record = record(true, true, false);
		AbilitySnapshot grounded = FlightPolicy.reconcile(
				new AbilitySnapshot(true, true, 0.05f, false, false, true, 0.1f),
				record,
				false,
				true
		);
		assertTrue(grounded.allowFlying());
		assertFalse(grounded.flying());
		assertTrue(record.authorized());
		assertTrue(record.flying());
	}

	@Test
	void permissionsSurviveReloadAndUsernameIsNotTheKey(@TempDir Path dir) {
		Path file = dir.resolve("permissions.json");
		UUID steve = UUID.fromString("11111111-1111-1111-1111-111111111111");
		UUID alex = UUID.fromString("22222222-2222-2222-2222-222222222222");
		FlightPermissionStore store = FlightPermissionStore.load(file, 0.05f);
		PlayerFlightRecord first = store.getOrCreate(steve, 0.05f);
		FlightPolicy.enableFlight(first);
		first.setAllowControl(true);
		first.setFlySpeed(0.07f);
		PlayerFlightRecord second = store.getOrCreate(alex, 0.05f);
		second.setAllowControl(true);
		store.saveNow();

		FlightPermissionStore reloaded = FlightPermissionStore.load(file, 0.05f);
		PlayerFlightRecord loaded = reloaded.get(steve).orElseThrow();
		assertTrue(loaded.authorized());
		assertTrue(loaded.flying());
		assertTrue(loaded.allowControl());
		assertEquals(0.07f, loaded.flySpeed(), 0.00001f);
		assertEquals(steve, loaded.uuid());
		PlayerFlightRecord other = reloaded.get(alex).orElseThrow();
		assertFalse(other.authorized());
		assertTrue(other.allowControl());
		assertEquals(2, reloaded.all().size());
	}

	@Test
	void bootstrapDoesNotOverrideSavedRevocation(@TempDir Path dir) {
		UUID id = UUID.fromString("33333333-3333-3333-3333-333333333333");
		Path file = dir.resolve("permissions.json");
		FlightPermissionStore store = FlightPermissionStore.load(file, 0.05f);
		PlayerFlightRecord record = store.getOrCreate(id, 0.05f);
		record.setAuthorized(false);
		record.setAllowControl(true);
		store.saveNow();

		FlightPermissionStore reloaded = FlightPermissionStore.load(file, 0.05f);
		assertFalse(reloaded.applyBootstrap(List.of(id), List.of(id), 0.05f));
		PlayerFlightRecord loaded = reloaded.get(id).orElseThrow();
		assertFalse(loaded.authorized());
		assertTrue(loaded.allowControl());
	}

	@Test
	void freshBootstrapCanGrantBothFlags(@TempDir Path dir) {
		UUID id = UUID.fromString("44444444-4444-4444-4444-444444444444");
		FlightPermissionStore store = FlightPermissionStore.load(dir.resolve("permissions.json"), 0.05f);
		assertTrue(store.applyBootstrap(List.of(id), List.of(id), 0.05f));
		PlayerFlightRecord record = store.get(id).orElseThrow();
		assertTrue(record.authorized());
		assertTrue(record.allowControl());
		assertFalse(record.flying());
		assertTrue(Files.exists(dir.resolve("permissions.json")));
	}
}
