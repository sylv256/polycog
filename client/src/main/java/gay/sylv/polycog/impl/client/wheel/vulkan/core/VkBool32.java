/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.client.wheel.vulkan.core;

import org.lwjgl.vulkan.VK10;

/// Why the hell doesn't LWJGL just use `true` or `false`?
public enum VkBool32 {
	FALSE(false, VK10.VK_FALSE),
	TRUE(true, VK10.VK_TRUE);

	private final boolean value;
	private final int raw;

	VkBool32(boolean value, int raw) {
		this.value = value;
		this.raw = raw;
	}

	public static VkBool32 vkFromRaw(int raw) {
		return VkBool32.values()[raw];
	}

	public static boolean fromRaw(int raw) {
		return vkFromRaw(raw).asBool();
	}

	public static int toRaw(boolean value) {
		if (value) {
			return TRUE.raw;
		} else {
			return FALSE.raw;
		}
	}

	public boolean asBool() {
		return this.value;
	}

	public int asRaw() {
		return this.raw;
	}
}
