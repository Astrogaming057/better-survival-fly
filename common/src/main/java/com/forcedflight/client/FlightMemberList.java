package com.forcedflight.client;

import java.util.List;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.network.chat.Component;

import com.forcedflight.network.FlightRequests;

public final class FlightMemberList extends ContainerObjectSelectionList<FlightMemberList.Row> {
	static final int FLIGHT_BUTTON_WIDTH = 56;
	static final int CONTROL_BUTTON_WIDTH = 68;
	private static final int BUTTON_GAP = 4;
	private static final int ROW_PAD = 8;

	public FlightMemberList(Minecraft minecraft, int width, int height, int y) {
		super(minecraft, width, height, y, 24);
	}

	public static int controlX(int left, int rowWidth) {
		return left + rowWidth - ROW_PAD - CONTROL_BUTTON_WIDTH;
	}

	public static int flightX(int left, int rowWidth) {
		return controlX(left, rowWidth) - BUTTON_GAP - FLIGHT_BUTTON_WIDTH;
	}

	public void sync(List<ClientRoster.Member> members) {
		if (samePeople(members)) {
			for (int i = 0; i < members.size(); i++) {
				this.children().get(i).copyFrom(members.get(i));
			}
			return;
		}
		double scroll = getScrollAmount();
		clearEntries();
		for (ClientRoster.Member member : members) {
			addEntry(new Row(member));
		}
		setScrollAmount(scroll);
	}

	private boolean samePeople(List<ClientRoster.Member> members) {
		if (members.size() != this.children().size()) {
			return false;
		}
		for (int i = 0; i < members.size(); i++) {
			if (!members.get(i).id.equals(this.children().get(i).id)) {
				return false;
			}
		}
		return true;
	}

	@Override
	public int getRowWidth() {
		return getWidth() - 16;
	}

	@Override
	public int getRowLeft() {
		return getX() + (getWidth() - getRowWidth()) / 2;
	}

	@Override
	protected int getScrollbarPosition() {
		return getX() + getWidth() - 6;
	}

	@Override
	protected void renderSelection(GuiGraphics graphics, int top, int width, int height, int outerX, int innerX) {
	}

	@Override
	protected void renderListBackground(GuiGraphics graphics) {
	}

	@Override
	protected void renderListSeparators(GuiGraphics graphics) {
	}

	public static final class Row extends ContainerObjectSelectionList.Entry<Row> {
		private final UUID id;
		private String name;
		private boolean authorized;
		private boolean control;
		private boolean vanilla;
		private final Button flightButton;
		private final Button controlButton;

		private Row(ClientRoster.Member member) {
			this.id = member.id;
			this.name = member.name;
			this.authorized = member.authorized;
			this.control = member.control;
			this.vanilla = member.vanilla;
			this.flightButton = Button.builder(Component.literal("Off"), button -> {
				if (this.vanilla && !this.authorized) {
					return;
				}
				this.authorized = !this.authorized;
				refreshLabels();
				FlightRequests.toggleMemberFlight(this.id);
			}).bounds(0, 0, FLIGHT_BUTTON_WIDTH, 20).build();
			this.controlButton = Button.builder(Component.literal("Grant"), button -> {
				this.control = !this.control;
				refreshLabels();
				FlightRequests.toggleMemberControl(this.id);
			}).bounds(0, 0, CONTROL_BUTTON_WIDTH, 20).build();
			refreshLabels();
		}

		private void copyFrom(ClientRoster.Member member) {
			this.name = member.name;
			this.authorized = member.authorized;
			this.control = member.control;
			this.vanilla = member.vanilla;
			refreshLabels();
		}

		private void refreshLabels() {
			boolean locked = this.vanilla && !this.authorized;
			this.flightButton.active = !locked;
			this.flightButton.setMessage(Component.literal(locked ? "Mode" : this.authorized ? "On" : "Off"));
			this.flightButton.setTooltip(Tooltip.create(Component.literal(locked
					? "Creative and spectator keep vanilla flight."
					: this.authorized ? "Turn their flight off" : "Turn their flight on")));
			this.controlButton.setMessage(Component.literal(this.control ? "Granted" : "Grant"));
			this.controlButton.setTooltip(Tooltip.create(Component.literal(this.control
					? "They can toggle their own flight. Click to take that away."
					: "Let them toggle their own flight.")));
		}

		@Override
		public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			graphics.fill(left, top, left + width, top + height, hovering ? 0x553C3C3C : 0x33202020);
			int faceY = top + (height - 16) / 2;
			int border = this.authorized ? 0xFFE0B040 : 0xFF000000;
			graphics.fill(left + 3, faceY - 1, left + 21, faceY + 17, border);
			PlayerFaceRenderer.draw(graphics, skin(), left + 4, faceY, 16);
			int controlX = controlX(left, width);
			int flightX = flightX(left, width);
			int buttonY = top + (height - 20) / 2;
			String shown = displayName();
			int nameWidth = Math.max(8, flightX - left - 28);
			shown = Minecraft.getInstance().font.plainSubstrByWidth(shown, nameWidth);
			int nameColor = this.authorized ? 0xFFE7C56A : 0xFFFFFFFF;
			int textY = buttonY + (20 - 8) / 2;
			graphics.drawString(Minecraft.getInstance().font, shown, left + 26, textY, nameColor);
			this.flightButton.setX(flightX);
			this.flightButton.setY(buttonY);
			this.controlButton.setX(controlX);
			this.controlButton.setY(buttonY);
			this.flightButton.render(graphics, mouseX, mouseY, partialTick);
			this.controlButton.render(graphics, mouseX, mouseY, partialTick);
		}

		private String displayName() {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.player != null && this.id.equals(minecraft.player.getUUID())) {
				return this.name + " (you)";
			}
			return this.name;
		}

		private PlayerSkin skin() {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.getConnection() != null) {
				PlayerInfo info = minecraft.getConnection().getPlayerInfo(this.id);
				if (info != null && info.getSkin() != null) {
					return info.getSkin();
				}
			}
			return DefaultPlayerSkin.get(this.id);
		}

		@Override
		public List<? extends GuiEventListener> children() {
			return List.of(this.flightButton, this.controlButton);
		}

		@Override
		public List<? extends NarratableEntry> narratables() {
			return List.of(this.flightButton, this.controlButton);
		}
	}
}
