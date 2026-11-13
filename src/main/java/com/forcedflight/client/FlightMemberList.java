package com.forcedflight.client;
import com.forcedflight.compat.McText;

import java.util.List;
import java.util.UUID;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.network.chat.Component;
//? if <1.21.11 {
import net.minecraft.resources.ResourceLocation;
//?}

import com.forcedflight.compat.McWidgets;
import com.forcedflight.network.FlightRequests;
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
*///?} elif >=1.20 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
*///?}
//? if >=1.19.4 {
import net.minecraft.client.gui.components.Tooltip;
//?}
//? if >=1.19.2 && <26.1 {
import net.minecraft.client.gui.components.PlayerFaceRenderer;
//?}
//? if >=1.21.9 {
/*import net.minecraft.world.entity.player.PlayerSkin;
*///?} elif >=1.20.2 {
import net.minecraft.client.resources.PlayerSkin;
//?}

public final class FlightMemberList extends ContainerObjectSelectionList<FlightMemberList.Row> {
	static final int FLIGHT_BUTTON_WIDTH = 56;
	static final int CONTROL_BUTTON_WIDTH = 68;
	private static final int BUTTON_GAP = 4;
	private static final int ROW_PAD = 8;

	public FlightMemberList(Minecraft minecraft, int width, int height, int y) {
		//? if >=1.20.4 {
		super(minecraft, width, height, y, 24);
		//?} else {
		/*super(minecraft, width, height, y, y + height, 24);
		*///?}
	}

	public void place(int x) {
		//? if >=1.20.4 {
		setX(x);
		//?} else {
		/*this.x0 = x;
		this.x1 = x + this.width;
		*///?}
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
		//? if >=1.21.4 {
		/*double scroll = scrollAmount();
		*///?} else {
		double scroll = getScrollAmount();
		//?}
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
		return widthOf() - 16;
	}

	@Override
	public int getRowLeft() {
		return xOf() + (widthOf() - getRowWidth()) / 2;
	}

	//? if >=1.21.4 {
	/*@Override
	protected int scrollBarX() {
		return xOf() + widthOf() - 6;
	}
	*///?} else {
	@Override
	protected int getScrollbarPosition() {
		return xOf() + widthOf() - 6;
	}
	//?}

	private int xOf() {
		//? if >=1.20.4 {
		return getX();
		//?} else {
		/*return this.x0;
		*///?}
	}

	private int widthOf() {
		//? if >=1.20.4 {
		return getWidth();
		//?} else {
		/*return this.width;
		*///?}
	}

	//? if >=1.20.6 && <26.1 {
	@Override
	protected void renderListBackground(GuiGraphics graphics) {
	}

	@Override
	protected void renderListSeparators(GuiGraphics graphics) {
	}
	//?}

	//? if >=26.1 {
	/*@Override
	protected void extractListBackground(GuiGraphicsExtractor graphics) {
	}

	@Override
	protected void extractListSeparators(GuiGraphicsExtractor graphics) {
	}

	@Override
	protected void extractSelection(GuiGraphicsExtractor graphics, Row entry, int color) {
	}
	*///?} elif >=1.21.9 {
	/*@Override
	protected void renderSelection(GuiGraphics graphics, Row entry, int color) {
	}
	*///?} elif >=1.20 {
	@Override
	protected void renderSelection(GuiGraphics graphics, int top, int width, int height, int outerX, int innerX) {
	}
	//?} elif >=1.19.2 {
	/*@Override
	protected void renderSelection(PoseStack pose, int top, int width, int height, int outerX, int innerX) {
	}
	*///?}

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
			this.flightButton = McWidgets.button(McText.literal("Off"), button -> {
				if (this.vanilla && !this.authorized) {
					return;
				}
				this.authorized = !this.authorized;
				refreshLabels();
				FlightRequests.toggleMemberFlight(this.id);
			}, 0, 0, FLIGHT_BUTTON_WIDTH, 20);
			this.controlButton = McWidgets.button(McText.literal("Grant"), button -> {
				this.control = !this.control;
				refreshLabels();
				FlightRequests.toggleMemberControl(this.id);
			}, 0, 0, CONTROL_BUTTON_WIDTH, 20);
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
			this.flightButton.setMessage(McText.literal(locked ? "Mode" : this.authorized ? "On" : "Off"));
			this.controlButton.setMessage(McText.literal(this.control ? "Granted" : "Grant"));
			//? if >=1.19.4 {
			this.flightButton.setTooltip(Tooltip.create(McText.literal(locked
					? "Creative and spectator keep vanilla flight."
					: this.authorized ? "Turn their flight off" : "Turn their flight on")));
			this.controlButton.setTooltip(Tooltip.create(McText.literal(this.control
					? "They can toggle their own flight. Click to take that away."
					: "Let them toggle their own flight.")));
			//?}
		}

		//? if >=26.1 {
		/*@Override
		public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int left = getContentX();
			int top = getContentY();
			int width = getContentWidth();
			int height = getContentHeight();
			graphics.fill(left, top, left + width, top + height, hovering ? 0x553C3C3C : 0x33202020);
			int faceY = top + (height - 16) / 2;
			int border = this.authorized ? 0xFFE0B040 : 0xFF000000;
			graphics.fill(left + 3, faceY - 1, left + 21, faceY + 17, border);
			PlayerFaceExtractor.extractRenderState(graphics, skin(), left + 4, faceY, 16);
			drawButtons(left, top, width, height, mouseX, mouseY, partialTick, graphics);
		}
		*///?} elif >=1.21.9 {
		/*@Override
		public void renderContent(GuiGraphics graphics, int mouseX, int mouseY, boolean hovering, float partialTick) {
			int left = getContentX();
			int top = getContentY();
			int width = getContentWidth();
			int height = getContentHeight();
			graphics.fill(left, top, left + width, top + height, hovering ? 0x553C3C3C : 0x33202020);
			int faceY = top + (height - 16) / 2;
			int border = this.authorized ? 0xFFE0B040 : 0xFF000000;
			graphics.fill(left + 3, faceY - 1, left + 21, faceY + 17, border);
			PlayerFaceRenderer.draw(graphics, skin(), left + 4, faceY, 16);
			drawButtons(left, top, width, height, mouseX, mouseY, partialTick, graphics);
		}
		*///?} elif >=1.20 {
		@Override
		public void render(GuiGraphics graphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			graphics.fill(left, top, left + width, top + height, hovering ? 0x553C3C3C : 0x33202020);
			int faceY = top + (height - 16) / 2;
			int border = this.authorized ? 0xFFE0B040 : 0xFF000000;
			graphics.fill(left + 3, faceY - 1, left + 21, faceY + 17, border);
			//? if >=1.20.2 {
			PlayerFaceRenderer.draw(graphics, skin(), left + 4, faceY, 16);
			//?} else {
			/*PlayerFaceRenderer.draw(graphics, skinLocation(), left + 4, faceY, 16);
			*///?}
			drawButtons(left, top, width, height, mouseX, mouseY, partialTick, graphics);
		}
		//?} elif >=1.19.2 {
		/*@Override
		public void render(PoseStack pose, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			net.minecraft.client.gui.GuiComponent.fill(pose, left, top, left + width, top + height, hovering ? 0x553C3C3C : 0x33202020);
			int faceY = top + (height - 16) / 2;
			int border = this.authorized ? 0xFFE0B040 : 0xFF000000;
			net.minecraft.client.gui.GuiComponent.fill(pose, left + 3, faceY - 1, left + 21, faceY + 17, border);
			RenderSystem.setShaderTexture(0, skinLocation());
			PlayerFaceRenderer.draw(pose, left + 4, faceY, 16);
			placeButtons(left, top, width, height);
			this.flightButton.render(pose, mouseX, mouseY, partialTick);
			this.controlButton.render(pose, mouseX, mouseY, partialTick);
			int flightX = flightX(left, width);
			String shown = displayName();
			shown = Minecraft.getInstance().font.plainSubstrByWidth(shown, Math.max(8, flightX - left - 28));
			Minecraft.getInstance().font.draw(pose, shown, left + 26, buttonY(top, height) + 6, this.authorized ? 0xFFE7C56A : 0xFFFFFFFF);
		}
		*///?} else {
		/*@Override
		public void render(PoseStack pose, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovering, float partialTick) {
			net.minecraft.client.gui.GuiComponent.fill(pose, left, top, left + width, top + height, hovering ? 0x553C3C3C : 0x33202020);
			int faceY = top + (height - 16) / 2;
			int border = this.authorized ? 0xFFE0B040 : 0xFF000000;
			net.minecraft.client.gui.GuiComponent.fill(pose, left + 3, faceY - 1, left + 21, faceY + 17, border);
			RenderSystem.setShaderTexture(0, skinLocation());
			net.minecraft.client.gui.GuiComponent.blit(pose, left + 4, faceY, 16, 16, 8.0F, 8.0F, 8, 8, 64, 64);
			placeButtons(left, top, width, height);
			this.flightButton.render(pose, mouseX, mouseY, partialTick);
			this.controlButton.render(pose, mouseX, mouseY, partialTick);
			int flightX = flightX(left, width);
			String shown = displayName();
			shown = Minecraft.getInstance().font.plainSubstrByWidth(shown, Math.max(8, flightX - left - 28));
			Minecraft.getInstance().font.draw(pose, shown, left + 26, buttonY(top, height) + 6, this.authorized ? 0xFFE7C56A : 0xFFFFFFFF);
		}
		*///?}

		//? if >=26.1 {
		/*private void drawButtons(int left, int top, int width, int height, int mouseX, int mouseY, float partialTick, GuiGraphicsExtractor graphics) {
			int controlX = controlX(left, width);
			int flightX = flightX(left, width);
			int buttonY = buttonY(top, height);
			String shown = displayName();
			int nameWidth = Math.max(8, flightX - left - 28);
			shown = Minecraft.getInstance().font.plainSubstrByWidth(shown, nameWidth);
			int nameColor = this.authorized ? 0xFFE7C56A : 0xFFFFFFFF;
			int textY = buttonY + (20 - 8) / 2;
			graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.NONE)
					.accept(left + 26, textY, McText.literal(shown).withColor(nameColor));
			moveButtons(flightX, controlX, buttonY);
			this.flightButton.extractRenderState(graphics, mouseX, mouseY, partialTick);
			this.controlButton.extractRenderState(graphics, mouseX, mouseY, partialTick);
		}
		*///?} elif >=1.20 {
		private void drawButtons(int left, int top, int width, int height, int mouseX, int mouseY, float partialTick, GuiGraphics graphics) {
			int controlX = controlX(left, width);
			int flightX = flightX(left, width);
			int buttonY = buttonY(top, height);
			String shown = displayName();
			int nameWidth = Math.max(8, flightX - left - 28);
			shown = Minecraft.getInstance().font.plainSubstrByWidth(shown, nameWidth);
			int nameColor = this.authorized ? 0xFFE7C56A : 0xFFFFFFFF;
			int textY = buttonY + (20 - 8) / 2;
			graphics.drawString(Minecraft.getInstance().font, shown, left + 26, textY, nameColor);
			moveButtons(flightX, controlX, buttonY);
			this.flightButton.render(graphics, mouseX, mouseY, partialTick);
			this.controlButton.render(graphics, mouseX, mouseY, partialTick);
		}
		//?}

		private static int buttonY(int top, int height) {
			return top + (height - 20) / 2;
		}

		private void placeButtons(int left, int top, int width, int height) {
			moveButtons(flightX(left, width), controlX(left, width), buttonY(top, height));
		}

		private void moveButtons(int flightX, int controlX, int buttonY) {
			//? if >=1.19.4 {
			this.flightButton.setX(flightX);
			this.flightButton.setY(buttonY);
			this.controlButton.setX(controlX);
			this.controlButton.setY(buttonY);
			//?} else {
			/*this.flightButton.x = flightX;
			this.flightButton.y = buttonY;
			this.controlButton.x = controlX;
			this.controlButton.y = buttonY;
			*///?}
		}

		private String displayName() {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.player != null && this.id.equals(minecraft.player.getUUID())) {
				return this.name + " (you)";
			}
			return this.name;
		}

		//? if >=1.20.2 {
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
		//?} else {
		/*private ResourceLocation skinLocation() {
			Minecraft minecraft = Minecraft.getInstance();
			if (minecraft.getConnection() != null) {
				PlayerInfo info = minecraft.getConnection().getPlayerInfo(this.id);
				if (info != null) {
					return info.getSkinLocation();
				}
			}
			return DefaultPlayerSkin.getDefaultSkin(this.id);
		}
		*///?}

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
