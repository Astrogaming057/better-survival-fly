package com.forcedflight.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Small pause-menu button, the same size as the Create goggles button. */
public final class ElytraMenuButton extends Button {
	private static final ItemStack ICON = new ItemStack(Items.ELYTRA);

	public ElytraMenuButton(int x, int y, OnPress onPress) {
		super(x, y, 20, 20, Component.empty(), onPress, DEFAULT_NARRATION);
		setTooltip(Tooltip.create(Component.literal("Flight")));
	}

	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.renderWidget(graphics, mouseX, mouseY, partialTick);
		graphics.renderItem(ICON, getX() + 2, getY() + 2);
	}
}
