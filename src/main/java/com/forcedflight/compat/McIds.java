package com.forcedflight.compat;

//? if >=1.21.11 {
/*import net.minecraft.resources.Identifier;
*///?} else {
import net.minecraft.resources.ResourceLocation;
//?}

public final class McIds {
	private McIds() {
	}

	//? if >=1.21.11 {
	/*public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("forcedflight", path);
	}
	*///?} else {
	public static ResourceLocation id(String path) {
		//? if >=1.21 {
		return ResourceLocation.fromNamespaceAndPath("forcedflight", path);
		//?} else {
		/*return new ResourceLocation("forcedflight", path);
		*///?}
	}
	//?}
}
