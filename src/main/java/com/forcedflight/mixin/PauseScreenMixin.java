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
	private int forcedflight$waits;

	protected PauseScreenMixin(Component title) {
		super(title);
	}

	@Inject(method = "init", at = @At("TAIL"))
	private void forcedflight$resetButton(CallbackInfo ci) {
		this.forcedflight$added = false;
		this.forcedflight$waits = 0;
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void forcedflight$elytraButton(CallbackInfo ci) {
		if (this.forcedflight$added || !showsMenu()) {
			return;
		}
		this.forcedflight$waits++;
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
		if (anchor == null || (mods == null && this.forcedflight$waits < 5)) {
			return;
		}
		this.forcedflight$added = true;
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
		this.addRenderableWidget(new ElytraMenuButton(x, yOf(anchor), button ->
				com.forcedflight.compat.McClient.setScreen(this.minecraft, new FlightMenuScreen(this))));
	}

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
		return lower.equals("mods") || lower.startsWith("mods ") || lower.startsWith("mods(") || lower.startsWith("mods...");
	}
}
