package com.forcedflight.compat;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public final class McWidgets {
	private McWidgets() {
	}

	public static Button button(Component text, Button.OnPress press, int x, int y, int width, int height) {
		//? if >=1.19.4 {
		return Button.builder(text, press).bounds(x, y, width, height).build();
		//?} else {
		/*return new Button(x, y, width, height, text, press);
		*///?}
	}
}
