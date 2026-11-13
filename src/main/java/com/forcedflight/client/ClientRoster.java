package com.forcedflight.client;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.forcedflight.network.FlightRosterPayload;

public final class ClientRoster {
	private static final List<Member> MEMBERS = new ArrayList<>();
	private static int revision;
	private static boolean received;

	private ClientRoster() {
	}

	public static void apply(FlightRosterPayload payload) {
		MEMBERS.clear();
		for (FlightRosterPayload.FlightMemberEntry entry : payload.members()) {
			MEMBERS.add(new Member(entry.id(), entry.name(), entry.authorized(), entry.control(), entry.vanilla()));
		}
		received = true;
		revision++;
	}

	public static void clear() {
		MEMBERS.clear();
		received = false;
		revision++;
	}

	public static List<Member> members() {
		return MEMBERS;
	}

	public static int revision() {
		return revision;
	}

	public static boolean received() {
		return received;
	}

	public static final class Member {
		public final UUID id;
		public final String name;
		public boolean authorized;
		public boolean control;
		public boolean vanilla;

		public Member(UUID id, String name, boolean authorized, boolean control, boolean vanilla) {
			this.id = id;
			this.name = name == null || name.isBlank() ? "player" : name;
			this.authorized = authorized;
			this.control = control;
			this.vanilla = vanilla;
		}
	}
}
