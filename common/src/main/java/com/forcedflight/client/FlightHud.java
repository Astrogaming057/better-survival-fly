package com.forcedflight.client;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import com.forcedflight.config.ModConfig;
import com.forcedflight.permission.FlightSpeed;

public final class FlightHud {
	private FlightHud() {
	}

	public static void render(GuiGraphics graphics) {
		ModConfig config = ModConfig.get();
		if (!config.hudEnabled) {
			return;
		}
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || client.options.hideGui || client.level == null
				|| FlightClientController.usesVanillaFlight(client.player)) {
			return;
		}
		ClientFlightState state = ClientFlightState.get();
		int remaining = state.hudUntilTick - state.clientTicks;
		if (remaining <= 0) {
			return;
		}
		int fadeTicks = Math.min(20, Math.max(1, config.hudDurationTicks));
		float alpha = remaining >= fadeTicks ? 1.0f : remaining / (float) fadeTicks;
		if (alpha < 0.02f) {
			return;
		}
		boolean flying = state.authorized && client.player.getAbilities().flying;
		Component text;
		if (!state.authorized) {
			text = Component.literal("Flight off");
		} else if (flying) {
			text = Component.literal(String.format(Locale.ROOT, "Flight %.2fx", FlightSpeed.multiplier(state.flySpeed)));
		} else {
			text = Component.literal(String.format(Locale.ROOT, "Flight ready %.2fx", FlightSpeed.multiplier(state.flySpeed)));
		}
		Font renderer = client.font;
		int width = renderer.width(text);
		int x = config.hudOffsetX;
		int y = config.hudOffsetY;
		int screenWidth = client.getWindow().getGuiScaledWidth();
		int screenHeight = client.getWindow().getGuiScaledHeight();
		switch (config.hudAnchor) {
			case "top_left" -> {
				x += 4;
				y += 4;
			}
			case "top_right" -> {
				x += screenWidth - width - 4;
				y += 4;
			}
			case "bottom_left" -> {
				x += 4;
				y += screenHeight - 12;
			}
			case "bottom_right" -> {
				x += screenWidth - width - 4;
				y += screenHeight - 12;
			}
			default -> {
				x += (screenWidth - width) / 2;
				y += screenHeight - 12;
			}
		}
		int textAlpha = Math.max(4, Math.round(alpha * 255f));
		int backgroundAlpha = Math.max(4, Math.round(alpha * 128f));
		int rgb = flying ? 0xFFD27A : 0xFFEFE6;
		graphics.fill(x - 3, y - 2, x + width + 3, y + 10, backgroundAlpha << 24);
		graphics.drawString(renderer, text, x, y, (textAlpha << 24) | rgb, true);
	}
}
