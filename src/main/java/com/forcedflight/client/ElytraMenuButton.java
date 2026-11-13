package com.forcedflight.client;
import com.forcedflight.compat.McText;

import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
//? if >=26.1 {
/*import net.minecraft.client.gui.GuiGraphicsExtractor;
*///?} elif >=1.20 {
import net.minecraft.client.gui.GuiGraphics;
//?} else {
/*import com.mojang.blaze3d.vertex.PoseStack;
*///?}
//? if >=1.19.4 {
import net.minecraft.client.gui.components.Tooltip;
//?}

/** Small pause-menu button, the same size as the Create goggles button. */
public final class ElytraMenuButton extends Button {
	private static final ItemStack ICON = new ItemStack(Items.ELYTRA);

	public ElytraMenuButton(int x, int y, OnPress onPress) {
		//? if >=1.19.4 {
		super(x, y, 20, 20, McText.empty(), onPress, DEFAULT_NARRATION);
		setTooltip(Tooltip.create(McText.literal("Flight")));
		//?} else {
		/*super(x, y, 20, 20, McText.empty(), onPress);
		*///?}
	}

	//? if >=26.1 {
	/*@Override
	public void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
		graphics.item(ICON, getX() + 2, getY() + 2);
	}
	*///?} elif >=1.21.11 {
	/*@Override
	public void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		graphics.renderItem(ICON, getX() + 2, getY() + 2);
	}
	*///?} elif >=1.20 {
	@Override
	public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
		super.renderWidget(graphics, mouseX, mouseY, partialTick);
		graphics.renderItem(ICON, getX() + 2, getY() + 2);
	}
	//?} elif >=1.19.4 {
	/*@Override
	public void renderWidget(PoseStack pose, int mouseX, int mouseY, float partialTick) {
		super.renderWidget(pose, mouseX, mouseY, partialTick);
		net.minecraft.client.Minecraft.getInstance().getItemRenderer().renderGuiItem(pose, ICON, getX() + 2, getY() + 2);
	}
	*///?} else {
	/*@Override
	public void renderButton(PoseStack pose, int mouseX, int mouseY, float partialTick) {
		super.renderButton(pose, mouseX, mouseY, partialTick);
		net.minecraft.client.Minecraft.getInstance().getItemRenderer().renderGuiItem(ICON, this.x + 2, this.y + 2);
	}
	*///?}
}
