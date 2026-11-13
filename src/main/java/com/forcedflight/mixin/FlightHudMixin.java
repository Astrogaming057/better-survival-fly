package com.forcedflight.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.Gui;
//? if forge && ((>=1.21 && <1.21.1) || (>=1.21.6 && <1.21.8)) {
/*import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.GuiGraphics;
*///?}

/**
 * Forge 1.21, 1.21.6, and 1.21.7 have no HUD layer event.
 * Every other version draws the HUD from the loader callback.
 */
@Mixin(Gui.class)
public class FlightHudMixin {
	//? if forge && ((>=1.21 && <1.21.1) || (>=1.21.6 && <1.21.8)) {
	/*@Inject(method = "render", at = @At("RETURN"))
	private void forcedflight$hud(GuiGraphics graphics, DeltaTracker tick, CallbackInfo ci) {
		com.forcedflight.client.FlightHud.render(graphics);
	}
	*///?}
}
