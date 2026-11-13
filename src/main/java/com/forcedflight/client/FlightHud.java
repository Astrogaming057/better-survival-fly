package com.forcedflight.client;
import com.forcedflight.compat.McText;

import java.util.Locale;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} elif >=1.20 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}

import com.forcedflight.config.ModConfig;
import com.forcedflight.permission.FlightSpeed;

public final class FlightHud {
	private FlightHud() {
	}

	//? if >=26.1 {
	/*public static void render(GuiGraphicsExtractor graphics) {
		HudDraw draw = prepare();
		if (draw == null) {
			return;
		}
		graphics.fill(draw.x() - 3, draw.y() - 2, draw.x() + draw.width() + 3, draw.y() + 10, draw.background());
		graphics.textRenderer(GuiGraphicsExtractor.HoveredTextEffects.NONE)
				.accept(draw.x(), draw.y(), draw.text().copy().withColor(draw.color()));
	}
	*///?} elif >=1.20 {
	public static void render(GuiGraphics graphics) {
		HudDraw draw = prepare();
		if (draw == null) {
			return;
		}
		graphics.fill(draw.x() - 3, draw.y() - 2, draw.x() + draw.width() + 3, draw.y() + 10, draw.background());
		graphics.drawString(draw.font(), draw.text(), draw.x(), draw.y(), draw.color(), true);
	}
	//?} else {
	/*public static void render(PoseStack pose) {
		HudDraw draw = prepare();
		if (draw == null) {
			return;
		}
		net.minecraft.client.gui.GuiComponent.fill(pose, draw.x() - 3, draw.y() - 2, draw.x() + draw.width() + 3, draw.y() + 10, draw.background());
		draw.font().drawShadow(pose, draw.text(), draw.x(), draw.y(), draw.color());
	}
	*///?}

	private static HudDraw prepare() {
		ModConfig config = ModConfig.get();
		if (!config.hudEnabled) {
			return null;
		}
		Minecraft client = Minecraft.getInstance();
		if (client.player == null || com.forcedflight.compat.McClient.hideGui(client) || client.level == null
				|| FlightClientController.usesVanillaFlight(client.player)) {
			return null;
		}
		ClientFlightState state = ClientFlightState.get();
		int remaining = state.hudUntilTick - state.clientTicks;
		if (remaining <= 0) {
			return null;
		}
		int fadeTicks = Math.min(20, Math.max(1, config.hudDurationTicks));
		float alpha = remaining >= fadeTicks ? 1.0f : remaining / (float) fadeTicks;
		if (alpha < 0.02f) {
			return null;
		}
		boolean flying = state.authorized && client.player.getAbilities().flying;
		Component text;
		if (!state.authorized) {
			text = McText.literal("Flight off");
		} else if (flying) {
			text = McText.literal(String.format(Locale.ROOT, "Flight %.2fx", FlightSpeed.multiplier(state.flySpeed)));
		} else {
			text = McText.literal(String.format(Locale.ROOT, "Flight ready %.2fx", FlightSpeed.multiplier(state.flySpeed)));
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
		return new HudDraw(renderer, text, x, y, width, (textAlpha << 24) | rgb, backgroundAlpha << 24);
	}

	private record HudDraw(Font font, Component text, int x, int y, int width, int color, int background) {
	}
}
