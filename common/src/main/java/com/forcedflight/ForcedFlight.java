package com.forcedflight;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ForcedFlight {
	public static final String MOD_ID = "forcedflight";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	public static final float VANILLA_FLY_SPEED = 0.05f;
	public static final float HARD_MIN_SPEED = 0.005f;
	public static final float HARD_MAX_SPEED = 1.0f;

	private ForcedFlight() {
	}
}
