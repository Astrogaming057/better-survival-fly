package com.forcedflight.compat;

import com.mojang.blaze3d.platform.InputConstants;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/** Client calls that moved between 1.21, 26.2, and 26.3. */
public final class McClient {
	private McClient() {
	}

	public static Screen screen(Minecraft client) {
		//? if >=26.2 {
		/*return client.gui.screen();
		*///?} else {
		return client.screen;
		//?}
	}

	public static void setScreen(Minecraft client, Screen screen) {
		//? if >=26.2 {
		/*client.gui.setScreen(screen);
		*///?} else {
		client.setScreen(screen);
		//?}
	}

	public static boolean hideGui(Minecraft client) {
		//? if >=26.2 {
		/*return client.gui.hud.isHidden();
		*///?} else {
		return client.options.hideGui;
		//?}
	}

	public static InputConstants.Type keyboard() {
		//? if >=26.3 {
		/*return InputConstants.Type.KEYBOARD;
		*///?} else {
		return InputConstants.Type.KEYSYM;
		//?}
	}

	public static InputConstants.Key keyboardKey(int code) {
		return keyboard().getOrCreate(code);
	}
}
