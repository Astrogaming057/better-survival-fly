package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import com.forcedflight.client.ElytraMenuButton;
import com.forcedflight.client.FlightMenuScreen;

@Mixin(PauseScreen.class)
public abstract class PauseScreenMixin extends Screen {
	@Unique
	private boolean forcedflight$added;
	@Unique
	private boolean forcedflight$logged;
	@Unique
	private int forcedflight$waits;
	@Unique
	private int forcedflight$buttonX;
	@Unique
	private int forcedflight$buttonY;
	@Unique
	private int forcedflight$span = 204;

	protected PauseScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void forcedflight$resetButton(CallbackInfo ci) {
		this.forcedflight$added = false;
		this.forcedflight$waits = 0;
		forcedflight$placeElytra(true);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void forcedflight$elytraButton(CallbackInfo ci) {
		forcedflight$placeElytra(false);
	}

	//? if >=26.1 {
	/*@Inject(method = "extractRenderState", at = @At("TAIL"))
	private void forcedflight$drawElytra(net.minecraft.client.gui.GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
		int x = this.forcedflight$added ? this.forcedflight$buttonX : Math.max(2, this.width / 2 - 102);
		int y = this.forcedflight$added ? this.forcedflight$buttonY : Math.max(2, this.height / 4);
		int span = this.forcedflight$added ? this.forcedflight$span : 204;
		if (!this.forcedflight$logged) {
			this.forcedflight$logged = true;
			com.forcedflight.ForcedFlight.LOGGER.info(
					"Pause flight button added={} at {},{} span={} children={} screen={}x{}",
					this.forcedflight$added, x, y, span, this.children().size(), this.width, this.height);
		}
		graphics.nextStratum();
		graphics.fill(x, y, x + span, y + 20, 0xFFE6E6E6);
		graphics.item(ElytraMenuButton.icon(), x + 6, y + 2);
		graphics.text(this.font, "Flight", x + 28, y + 6, 0xFF202020);
	}
	*///?}

	@Unique
	private void forcedflight$placeElytra(boolean fromInit) {
		if (this.forcedflight$added) {
			return;
		}
		//? if <26.1 {
		if (!showsMenu()) {
			return;
		}
		//?}
		if (!fromInit) {
			this.forcedflight$waits++;
		}
		AbstractWidget mods = null;
		AbstractWidget lowest = null;
		for (GuiEventListener child : this.children()) {
			if (!(child instanceof AbstractWidget widget) || widthOf(widget) < 20) {
				continue;
			}
			if (lowest == null || yOf(widget) > yOf(lowest)) {
				lowest = widget;
			}
			if (isMods(widget.getMessage().getString())) {
				mods = widget;
			}
		}
		AbstractWidget anchor = mods != null ? mods : lowest;
		//? if >=26.1 {
		/*forcedflight$placeWide(anchor);
		*///?} else {
		if (anchor == null || (mods == null && this.forcedflight$waits < 5)) {
			return;
		}
		int rowLeft = xOf(anchor);
		int rowRight = xOf(anchor) + widthOf(anchor);
		for (GuiEventListener child : this.children()) {
			if (child instanceof AbstractWidget widget && yOf(widget) == yOf(anchor)) {
				rowLeft = Math.min(rowLeft, xOf(widget));
				rowRight = Math.max(rowRight, xOf(widget) + widthOf(widget));
			}
		}
		int x = rowLeft - 24;
		if (x < 2) {
			x = rowRight + 4;
		}
		if (x > this.width - 22) {
			x = Math.max(2, this.width - 22);
		}
		int y = yOf(anchor);
		if (y < 2) {
			y = 2;
		} else if (y > this.height - 22) {
			y = this.height - 22;
		}
		this.forcedflight$added = true;
		this.forcedflight$buttonX = x;
		this.forcedflight$buttonY = y;
		this.addRenderableWidget(new ElytraMenuButton(x, y, button ->
				com.forcedflight.compat.McClient.setScreen(this.minecraft, new FlightMenuScreen(this))));
		//?}
	}

	//? if >=26.1 {
	/*@Unique
	private void forcedflight$placeWide(AbstractWidget anchor) {
		int span = 204;
		int x = Math.max(2, this.width / 2 - span / 2);
		int y = Math.max(2, this.height / 4);
		AbstractWidget bottom = anchor;
		int top = Integer.MAX_VALUE;
		for (GuiEventListener child : this.children()) {
			if (!(child instanceof AbstractWidget widget) || widthOf(widget) < 20) {
				continue;
			}
			top = Math.min(top, yOf(widget));
			if (bottom == null || yOf(widget) + widget.getHeight() > yOf(bottom) + bottom.getHeight()) {
				bottom = widget;
			}
		}
		if (bottom != null) {
			int rowLeft = xOf(bottom);
			int rowRight = rowLeft + widthOf(bottom);
			for (GuiEventListener child : this.children()) {
				if (child instanceof AbstractWidget widget && yOf(widget) == yOf(bottom)) {
					rowLeft = Math.min(rowLeft, xOf(widget));
					rowRight = Math.max(rowRight, xOf(widget) + widthOf(widget));
				}
			}
			span = Math.max(120, rowRight - rowLeft);
			x = rowLeft;
			y = yOf(bottom) + bottom.getHeight() + 4;
			if (x < 2) {
				x = 2;
			}
			if (x + span > this.width - 2) {
				x = Math.max(2, this.width - span - 2);
			}
			if (y > this.height - 22) {
				y = Math.max(2, top - 24);
			}
		}
		this.forcedflight$added = true;
		this.forcedflight$buttonX = x;
		this.forcedflight$buttonY = y;
		this.forcedflight$span = span;
		ElytraMenuButton button = new ElytraMenuButton(x, y, pressed ->
				com.forcedflight.compat.McClient.setScreen(this.minecraft, new FlightMenuScreen(this)));
		button.setWidth(span);
		this.addRenderableWidget(button);
	}
	*///?}

	private boolean showsMenu() {
		//? if >=1.20.2 {
		return ((PauseScreen) (Object) this).showsPauseMenu();
		//?} else {
		/*return true;
		*///?}
	}

	private static int xOf(AbstractWidget widget) {
		//? if >=1.19.4 {
		return widget.getX();
		//?} else {
		/*return widget.x;
		*///?}
	}

	private static int yOf(AbstractWidget widget) {
		//? if >=1.19.4 {
		return widget.getY();
		//?} else {
		/*return widget.y;
		*///?}
	}

	private static int widthOf(AbstractWidget widget) {
		//? if >=1.19.4 {
		return widget.getWidth();
		//?} else {
		/*return widget.width;
		*///?}
	}

	@Unique
	private static boolean isMods(String text) {
		String lower = text.toLowerCase(java.util.Locale.ROOT);
		return lower.equals("mods") || lower.startsWith("mods ") || lower.startsWith("mods(") || lower.startsWith("mods...") || lower.contains("mod");
	}
}
