package com.forcedflight.mixin;

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;

import com.forcedflight.client.FlightClientController;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
	@Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
	private void forcedflight$flightSpeedScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
		if (vertical == 0.0 || !FlightClientController.consumeScroll() || !altDown(window)) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (window != client.getWindow().getWindow()) {
			return;
		}
		double consumed = vertical;
		client.execute(() -> FlightClientController.adjustFromScroll(consumed));
		ci.cancel();
	}

	private static boolean altDown(long window) {
		return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_ALT) == GLFW.GLFW_PRESS
				|| GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_ALT) == GLFW.GLFW_PRESS;
	}
}
