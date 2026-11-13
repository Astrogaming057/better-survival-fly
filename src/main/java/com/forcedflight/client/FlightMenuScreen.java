package com.forcedflight.client;
import com.forcedflight.compat.McText;

import java.util.Locale;

import net.minecraft.client.KeyMapping;
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} elif >=1.20 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import com.forcedflight.ForcedFlight;
import com.forcedflight.compat.McClient;
import com.forcedflight.compat.McWidgets;
import com.forcedflight.config.ModConfig;
import com.forcedflight.network.FlightRequests;
import com.forcedflight.permission.FlightSpeed;
import com.mojang.blaze3d.platform.InputConstants;

public final class FlightMenuScreen extends Screen {
	private static final int ROW = 24;

	private final Screen parent;
	private int tab;
	private BindTarget listening;
	private boolean ignoreNextMouse;
	private Component hint = McText.empty();
	private Button flightToggle;
	private ReleaseSlider speedSlider;
	private FlightMemberList memberList;
	private boolean membersTabShown;
	private int rosterRevision = -1;
	private int rosterPoll;

	public FlightMenuScreen(Screen parent) {
		super(McText.literal("Forced Creative Flight"));
		this.parent = parent;
	}

	@Override
	protected void init() {
		ClientPreferences.load();
		this.flightToggle = null;
		this.speedSlider = null;
		this.memberList = null;
		this.membersTabShown = ClientFlightState.get().operator;
		if (!this.membersTabShown && this.tab == 3) {
			this.tab = 0;
		}
		int tabs = this.membersTabShown ? 4 : 3;
		int tabWidth = this.membersTabShown ? 68 : 78;
		int tabX = this.width / 2 - (tabWidth * tabs + 4 * (tabs - 1)) / 2;
		addRenderableWidget(tabButton(tabX, 32, tabWidth, 0, "Flight"));
		addRenderableWidget(tabButton(tabX + tabWidth + 4, 32, tabWidth, 1, "Controls"));
		addRenderableWidget(tabButton(tabX + (tabWidth + 4) * 2, 32, tabWidth, 2, "Server"));
		if (this.membersTabShown) {
			addRenderableWidget(tabButton(tabX + (tabWidth + 4) * 3, 32, tabWidth, 3, "Members"));
		}
		int x = this.width / 2 - 120;
		int y = 64;
		if (tab == 0) {
			initFlight(x, y);
		} else if (tab == 1) {
			initControls(x, y);
		} else if (tab == 2) {
			initServer(x, y);
		} else {
			initMembers();
		}
		addRenderableWidget(McWidgets.button(McText.literal("Done"), button -> onClose(), this.width / 2 - 100, this.height - 28, 200, 20));
	}

	private Button tabButton(int x, int y, int width, int index, String label) {
		String text = tab == index ? "[" + label + "]" : label;
		return McWidgets.button(McText.literal(text), button -> {
			tab = index;
			listening = null;
			refreshScreen();
		}, x, y, width, 20);
	}

	private void initFlight(int x, int y) {
		ClientFlightState state = ClientFlightState.get();
		boolean vanilla = vanillaFlight();
		this.flightToggle = McWidgets.button(McText.literal(flightLabel(state, vanilla)), button -> {
			FlightRequests.sendToggle();
			state.authorized = !state.authorized;
			refreshFlightWidgets();
		}, x, y, 240, 20);
		this.flightToggle.active = state.canToggle && !vanilla && state.hasServerState;
		addRenderableWidget(this.flightToggle);
		y += ROW;
		if (state.hasServerState && state.maxSpeed > state.minSpeed) {
			float current = Mth.clamp(state.flySpeed, state.minSpeed, state.maxSpeed);
			double slider = (current - state.minSpeed) / (state.maxSpeed - state.minSpeed);
			this.speedSlider = new ReleaseSlider(x, y, 190, 20, slider, value -> {
				float raw = state.minSpeed + (float) value * (state.maxSpeed - state.minSpeed);
				return String.format(Locale.ROOT, "Your speed: %.2fx", FlightSpeed.multiplier(raw));
			}, value -> {
				float raw = state.minSpeed + (float) value * (state.maxSpeed - state.minSpeed);
				state.flySpeed = raw;
				FlightRequests.sendSpeed(raw);
			});
			this.speedSlider.active = state.authorized && !vanilla;
			addRenderableWidget(this.speedSlider);
			addRenderableWidget(resetButton(x + 194, y, () -> {
				FlightRequests.sendSpeed(ForcedFlight.VANILLA_FLY_SPEED);
				state.flySpeed = ForcedFlight.VANILLA_FLY_SPEED;
				refreshScreen();
			}));
			y += ROW;
		}
		addRenderableWidget(CycleButton.onOffBuilder(state.keepFlying)
				.create(x, y, 190, 20, McText.literal("Keep flying on land"), (button, value) -> {
					state.keepFlying = value;
					FlightRequests.sendKeepFlying(value);
				}));
		addRenderableWidget(resetButton(x + 194, y, () -> {
			FlightRequests.sendKeepFlying(false);
			state.keepFlying = false;
			refreshScreen();
		}));
		hint = McText.literal(vanilla
				? "Creative and spectator keep vanilla flight."
				: String.format(Locale.ROOT, "Server cap %.2fx. Keep flying is only for you.", FlightSpeed.multiplier(state.maxSpeed)));
	}

	private void initControls(int x, int y) {
		addControlRow(x, y, bindButton(x, y, BindTarget.TOGGLE, "Toggle flight", FlightClientHooks.toggle), () -> resetKey(FlightClientHooks.toggle));
		y += ROW;
		addRenderableWidget(CycleButton.<ClientPreferences.SpeedMode>builder(mode -> McText.literal(
						mode == ClientPreferences.SpeedMode.KEYS ? "Up and down keys" : "Hold key + scroll")
				//? if >=1.21.11 {
				/*, () -> ClientPreferences.mode()
				*///?}
				)
				.withValues(ClientPreferences.SpeedMode.SCROLL, ClientPreferences.SpeedMode.KEYS)
				//? if <1.21.11 {
				.withInitialValue(ClientPreferences.mode())
				//?}
				.create(x, y, 190, 20, McText.literal("Speed control"), (button, value) -> {
					ClientPreferences.setMode(value);
					refreshScreen();
				}));
		addRenderableWidget(resetButton(x + 194, y, () -> {
			ClientPreferences.setMode(ClientPreferences.SpeedMode.SCROLL);
			refreshScreen();
		}));
		y += ROW;
		if (ClientPreferences.mode() == ClientPreferences.SpeedMode.SCROLL) {
			addControlRow(x, y, bindButton(x, y, BindTarget.MODIFIER, "Hold key", FlightClientHooks.speedModifier), () -> resetKey(FlightClientHooks.speedModifier));
		} else {
			addControlRow(x, y, bindButton(x, y, BindTarget.UP, "Speed up", FlightClientHooks.speedUp), () -> resetKey(FlightClientHooks.speedUp));
			y += ROW;
			addControlRow(x, y, bindButton(x, y, BindTarget.DOWN, "Speed down", FlightClientHooks.speedDown), () -> resetKey(FlightClientHooks.speedDown));
		}
		hint = McText.literal("Click a key button, then press a key or mouse button. Escape cancels.");
	}

	private void initServer(int x, int y) {
		ClientFlightState state = ClientFlightState.get();
		if (!state.operator) {
			hint = McText.literal("Only operators can change server settings.");
			return;
		}
		double maxSlider = Mth.clamp((FlightSpeed.multiplier(state.maxSpeed) - 0.1f) / 19.9f, 0.0f, 1.0f);
		addRenderableWidget(new ReleaseSlider(x, y, 190, 20, maxSlider, value -> {
			float multiplier = 0.1f + (float) value * 19.9f;
			return String.format(Locale.ROOT, "Server cap: %.2fx", multiplier);
		}, value -> FlightRequests.sendMaxSpeed(0.1f + (float) value * 19.9f)));
		addRenderableWidget(resetButton(x + 194, y, () -> {
			FlightRequests.sendMaxSpeed(FlightSpeed.multiplier(ModConfig.DEFAULT_MAX_SPEED));
			state.maxSpeed = ModConfig.DEFAULT_MAX_SPEED;
			refreshScreen();
		}));
		y += ROW;
		addRenderableWidget(CycleButton.onOffBuilder(state.flyPenalty)
				.create(x, y, 190, 20, McText.literal("Mining slowdown"), (button, value) -> FlightRequests.sendFlyPenalty(value)));
		addRenderableWidget(resetButton(x + 194, y, () -> {
			FlightRequests.sendFlyPenalty(true);
			state.flyPenalty = true;
			refreshScreen();
		}));
		y += ROW;
		addRenderableWidget(CycleButton.onOffBuilder(state.fallDamage)
				.create(x, y, 190, 20, McText.literal("Fall damage"), (button, value) -> FlightRequests.sendFallDamage(value)));
		addRenderableWidget(resetButton(x + 194, y, () -> {
			FlightRequests.sendFallDamage(true);
			state.fallDamage = true;
			refreshScreen();
		}));
		y += ROW;
		addRenderableWidget(CycleButton.onOffBuilder(state.joinMessage)
				.create(x, y, 190, 20, McText.literal("Join message"), (button, value) -> FlightRequests.sendJoinMessage(value)));
		addRenderableWidget(resetButton(x + 194, y, () -> {
			FlightRequests.sendJoinMessage(true);
			state.joinMessage = true;
			refreshScreen();
		}));
		hint = McText.literal("Server settings save immediately and apply to everyone.");
	}

	private void initMembers() {
		int listWidth = Math.min(300, this.width - 20);
		int listTop = 76;
		int listHeight = Math.max(40, this.height - listTop - 48);
		this.memberList = new FlightMemberList(this.minecraft, listWidth, listHeight, listTop);
		this.memberList.place(this.width / 2 - listWidth / 2);
		this.memberList.sync(ClientRoster.members());
		addRenderableWidget(this.memberList);
		this.rosterRevision = ClientRoster.revision();
		this.rosterPoll = 1;
		FlightRequests.requestRoster();
		hint = McText.literal("Loading players...");
	}

	private void addControlRow(int x, int y, Button control, Runnable reset) {
		addRenderableWidget(control);
		addRenderableWidget(resetButton(x + 194, y, reset));
	}

	private Button resetButton(int x, int y, Runnable action) {
		return McWidgets.button(McText.literal("Reset"), button -> action.run(), x, y, 46, 20);
	}

	private void resetKey(KeyMapping mapping) {
		if (mapping == null) {
			return;
		}
		mapping.setKey(mapping.getDefaultKey());
		KeyMapping.resetMapping();
		if (this.minecraft.options != null) {
			this.minecraft.options.save();
		}
		refreshScreen();
	}

	@Override
	public void tick() {
		super.tick();
		refreshFlightWidgets();
		boolean operator = ClientFlightState.get().operator;
		if (operator != this.membersTabShown) {
			if (!operator && this.tab == 3) {
				this.tab = 0;
			}
			refreshScreen();
			return;
		}
		if (this.tab != 3 || this.memberList == null) {
			return;
		}
		if (this.rosterPoll++ % 40 == 0) {
			FlightRequests.requestRoster();
		}
		if (ClientRoster.revision() != this.rosterRevision) {
			this.rosterRevision = ClientRoster.revision();
			this.memberList.sync(ClientRoster.members());
		}
		if (!ClientRoster.received()) {
			hint = McText.literal("Loading players...");
		} else if (ClientRoster.members().isEmpty()) {
			hint = McText.literal("No players online.");
		} else {
			hint = McText.literal("Flight turns theirs on or off. Control lets them toggle it themselves.");
		}
	}

	private void refreshFlightWidgets() {
		if (this.flightToggle == null && this.speedSlider == null) {
			return;
		}
		ClientFlightState state = ClientFlightState.get();
		boolean vanilla = vanillaFlight();
		if (this.flightToggle != null) {
			this.flightToggle.setMessage(McText.literal(flightLabel(state, vanilla)));
			this.flightToggle.active = state.canToggle && !vanilla && state.hasServerState;
		}
		if (this.speedSlider != null && state.maxSpeed > state.minSpeed) {
			boolean unlocked = state.authorized && !vanilla && state.hasServerState;
			this.speedSlider.active = unlocked;
			if (unlocked && !this.speedSlider.isFocused()) {
				float current = Mth.clamp(state.flySpeed, state.minSpeed, state.maxSpeed);
				this.speedSlider.setNormalized((current - state.minSpeed) / (state.maxSpeed - state.minSpeed));
			}
		}
	}

	private boolean vanillaFlight() {
		return this.minecraft.player != null && FlightClientController.usesVanillaFlight(this.minecraft.player);
	}

	private Button bindButton(int x, int y, BindTarget target, String label, KeyMapping mapping) {
		Component text = listening == target
				? McText.literal("> Press a key <")
				: McText.literal(label + ": " + (mapping == null ? "?" : mapping.getTranslatedKeyMessage().getString()));
		return McWidgets.button(text, button -> {
			listening = target;
			ignoreNextMouse = true;
			refreshScreen();
		}, x, y, 190, 20);
	}

	private static String flightLabel(ClientFlightState state, boolean vanilla) {
		if (!state.hasServerState) {
			return "Waiting for the server";
		}
		if (vanilla) {
			return "Vanilla flight is in charge";
		}
		if (!state.canToggle) {
			return state.authorized ? "Flight is on" : "Flight is off";
		}
		return state.authorized ? "Flight: On" : "Flight: Off";
	}

	//? if >=1.21.9 {
	/*@Override
	public boolean keyPressed(net.minecraft.client.input.KeyEvent event) {
		if (listening != null) {
			if (event.key() != InputConstants.KEY_ESCAPE) {
				applyBind(McClient.keyboardKey(event.key()));
			} else {
				listening = null;
				refreshScreen();
			}
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	public boolean mouseClicked(net.minecraft.client.input.MouseButtonEvent event, boolean doubled) {
		if (listening != null && !ignoreNextMouse) {
			applyBind(InputConstants.Type.MOUSE.getOrCreate(event.button()));
			return true;
		}
		boolean handled = super.mouseClicked(event, doubled);
		ignoreNextMouse = false;
		return handled;
	}
	*///?} else {
	@Override
	public boolean keyPressed(int key, int scanCode, int modifiers) {
		if (listening != null) {
			if (key != InputConstants.KEY_ESCAPE) {
				applyBind(McClient.keyboardKey(key));
			} else {
				listening = null;
				refreshScreen();
			}
			return true;
		}
		return super.keyPressed(key, scanCode, modifiers);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (listening != null && !ignoreNextMouse) {
			applyBind(InputConstants.Type.MOUSE.getOrCreate(button));
			return true;
		}
		boolean handled = super.mouseClicked(mouseX, mouseY, button);
		ignoreNextMouse = false;
		return handled;
	}
	//?}

	private void applyBind(InputConstants.Key key) {
		KeyMapping mapping = switch (listening) {
			case TOGGLE -> FlightClientHooks.toggle;
			case MODIFIER -> FlightClientHooks.speedModifier;
			case UP -> FlightClientHooks.speedUp;
			case DOWN -> FlightClientHooks.speedDown;
		};
		listening = null;
		if (mapping != null && key != null && key != InputConstants.UNKNOWN) {
			mapping.setKey(key);
			KeyMapping.resetMapping();
			if (this.minecraft.options != null) {
				this.minecraft.options.save();
			}
		}
		refreshScreen();
	}

	private void refreshScreen() {
		//? if >=1.19.2 {
		this.rebuildWidgets();
		//?} else {
		/*this.init(this.minecraft, this.width, this.height);
		*///?}
	}

	@Override
	//? if >=26.1 {
	/*public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		super.extractRenderState(graphics, mouseX, mouseY, partialTick);
		net.minecraft.client.gui.ActiveTextCollector text = graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.NONE);
		text.accept(net.minecraft.client.gui.TextAlignment.CENTER, this.width / 2, 12, this.title.copy().withColor(0xFFFFFF));
		if (this.memberList != null) {
			int left = this.memberList.getRowLeft();
			int rowWidth = this.memberList.getRowWidth();
			int flightX = FlightMemberList.flightX(left, rowWidth);
			int controlX = FlightMemberList.controlX(left, rowWidth);
			text.accept(left + 26, 62, McText.literal("Player").withColor(0xA0A0A0));
			drawColumnHeader(graphics, "Flight", flightX, FlightMemberList.FLIGHT_BUTTON_WIDTH);
			drawColumnHeader(graphics, "Control", controlX, FlightMemberList.CONTROL_BUTTON_WIDTH);
		}
		text.accept(net.minecraft.client.gui.TextAlignment.CENTER, this.width / 2, this.height - 46, hint.copy().withColor(0xA0A0A0));
	}

	private void drawColumnHeader(GuiGraphicsExtractor graphics, String label, int x, int width) {
		graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.NONE).accept(
				net.minecraft.client.gui.TextAlignment.CENTER, x + width / 2, 62, McText.literal(label).withColor(0xA0A0A0));
	}
	*///?} elif >=1.20 {
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.render(graphics, mouseX, mouseY, partialTick);
		graphics.drawCenteredString(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
		if (this.memberList != null) {
			int left = this.memberList.getRowLeft();
			int rowWidth = this.memberList.getRowWidth();
			int flightX = FlightMemberList.flightX(left, rowWidth);
			int controlX = FlightMemberList.controlX(left, rowWidth);
			graphics.drawString(this.font, "Player", left + 26, 62, 0xA0A0A0);
			drawColumnHeader(graphics, "Flight", flightX, FlightMemberList.FLIGHT_BUTTON_WIDTH);
			drawColumnHeader(graphics, "Control", controlX, FlightMemberList.CONTROL_BUTTON_WIDTH);
		}
		graphics.drawCenteredString(this.font, hint, this.width / 2, this.height - 46, 0xA0A0A0);
	}

	private void drawColumnHeader(GuiGraphics graphics, String text, int x, int width) {
		graphics.drawString(this.font, text, x + (width - this.font.width(text)) / 2, 62, 0xA0A0A0);
	}
	//?} else {
	/*public void render(PoseStack pose, int mouseX, int mouseY, float partialTick) {
		super.render(pose, mouseX, mouseY, partialTick);
		drawCenteredString(pose, this.font, this.title, this.width / 2, 12, 0xFFFFFF);
		if (this.memberList != null) {
			int left = this.memberList.getRowLeft();
			int rowWidth = this.memberList.getRowWidth();
			int flightX = FlightMemberList.flightX(left, rowWidth);
			int controlX = FlightMemberList.controlX(left, rowWidth);
			this.font.draw(pose, "Player", left + 26, 62, 0xA0A0A0);
			this.font.draw(pose, "Flight", flightX + (FlightMemberList.FLIGHT_BUTTON_WIDTH - this.font.width("Flight")) / 2, 62, 0xA0A0A0);
			this.font.draw(pose, "Control", controlX + (FlightMemberList.CONTROL_BUTTON_WIDTH - this.font.width("Control")) / 2, 62, 0xA0A0A0);
		}
		drawCenteredString(pose, this.font, hint, this.width / 2, this.height - 46, 0xA0A0A0);
	}
	*///?}

	@Override
	public void onClose() {
		McClient.setScreen(this.minecraft, parent);
	}

	private enum BindTarget {
		TOGGLE,
		MODIFIER,
		UP,
		DOWN
	}

	private static final class ReleaseSlider extends AbstractSliderButton {
		private final java.util.function.DoubleFunction<String> label;
		private final java.util.function.DoubleConsumer apply;

		private ReleaseSlider(int x, int y, int width, int height, double value, java.util.function.DoubleFunction<String> label, java.util.function.DoubleConsumer apply) {
			super(x, y, width, height, McText.empty(), value);
			this.label = label;
			this.apply = apply;
			updateMessage();
		}

		@Override
		protected void updateMessage() {
			setMessage(McText.literal(label.apply(this.value)));
		}

		@Override
		protected void applyValue() {
		}

		//? if >=1.21.9 {
		/*@Override
		public void onRelease(net.minecraft.client.input.MouseButtonEvent event) {
			super.onRelease(event);
			apply.accept(this.value);
		}
		*///?} else {
		@Override
		public void onRelease(double mouseX, double mouseY) {
			super.onRelease(mouseX, mouseY);
			apply.accept(this.value);
		}
		//?}

		private void setNormalized(double next) {
			double clamped = Mth.clamp(next, 0.0, 1.0);
			if (Math.abs(clamped - this.value) < 0.0001) {
				return;
			}
			this.value = clamped;
			updateMessage();
		}
	}
}
