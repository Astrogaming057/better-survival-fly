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
		if (this.forcedflight$added || !((PauseScreen) (Object) this).showsPauseMenu()) {
			return;
		}
		this.forcedflight$waits++;
		AbstractWidget mods = null;
		AbstractWidget lowest = null;
		for (GuiEventListener child : this.children()) {
			if (!(child instanceof AbstractWidget widget) || widget.getWidth() < 20) {
				continue;
			}
			if (lowest == null || widget.getY() > lowest.getY()) {
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
		int rowLeft = anchor.getX();
		int rowRight = anchor.getX() + anchor.getWidth();
		for (GuiEventListener child : this.children()) {
			if (child instanceof AbstractWidget widget && widget.getY() == anchor.getY()) {
				rowLeft = Math.min(rowLeft, widget.getX());
				rowRight = Math.max(rowRight, widget.getX() + widget.getWidth());
			}
		}
		int x = rowLeft - 24;
		if (x < 2) {
			x = rowRight + 4;
		}
		this.addRenderableWidget(new ElytraMenuButton(x, anchor.getY(), button ->
				this.minecraft.setScreen(new FlightMenuScreen(this))));
	}

	@Unique
	private static boolean isMods(String text) {
		String lower = text.toLowerCase(java.util.Locale.ROOT);
		return lower.equals("mods") || lower.startsWith("mods ") || lower.startsWith("mods(") || lower.startsWith("mods...");
	}
}
