package com.forcedflight.compat;

import net.minecraft.world.entity.Entity;

public final class McPlayer {
	private McPlayer() {
	}

	public static boolean onGround(Entity entity) {
		//? if >=1.20 {
		return entity.onGround();
		//?} else {
		/*return entity.isOnGround();
		*///?}
	}
}
