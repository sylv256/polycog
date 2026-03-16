/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.client.wheel.vulkan.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK11;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VK13;

public record VkResult(int raw, String name) {
	private static final Map<Integer, VkResult> RAW_2_RESULT = new HashMap<>();
	private static final Map<Integer, String> RAW_2_NAME = new HashMap<>();

	public static final VkResult SUCCESS = new VkResult(VK10.VK_SUCCESS, "SUCCESS");
	public static final VkResult NOT_READY = new VkResult(VK10.VK_NOT_READY, "NOT_READY");
	public static final VkResult TIMEOUT = new VkResult(VK10.VK_TIMEOUT, "TIMEOUT");
	public static final VkResult EVENT_SET = new VkResult(VK10.VK_EVENT_SET, "EVENT_SET");
	public static final VkResult EVENT_RESET = new VkResult(VK10.VK_EVENT_RESET, "EVENT_RESET");
	public static final VkResult INCOMPLETE = new VkResult(VK10.VK_INCOMPLETE, "INCOMPLETE");
	public static final VkResult ERROR_OUT_OF_HOST_MEMORY = new VkResult(VK10.VK_ERROR_OUT_OF_HOST_MEMORY, "ERROR_OUT_OF_HOST_MEMORY");
	public static final VkResult ERROR_OUT_OF_DEVICE_MEMORY = new VkResult(VK10.VK_ERROR_OUT_OF_DEVICE_MEMORY, "ERROR_OUT_OF_DEVICE_MEMORY");
	public static final VkResult ERROR_INITIALIZATION_FAILED = new VkResult(VK10.VK_ERROR_INITIALIZATION_FAILED, "ERROR_INITIALIZATION_FAILED");
	public static final VkResult ERROR_DEVICE_LOST = new VkResult(VK10.VK_ERROR_DEVICE_LOST, "ERROR_DEVICE_LOST");
	public static final VkResult ERROR_MEMORY_MAP_FAILED = new VkResult(VK10.VK_ERROR_MEMORY_MAP_FAILED, "ERROR_MEMORY_MAP_FAILED");
	public static final VkResult ERROR_LAYER_NOT_PRESENT = new VkResult(VK10.VK_ERROR_LAYER_NOT_PRESENT, "ERROR_LAYER_NOT_PRESENT");
	public static final VkResult ERROR_EXTENSION_NOT_PRESENT = new VkResult(VK10.VK_ERROR_EXTENSION_NOT_PRESENT, "ERROR_EXTENSION_NOT_PRESENT");
	public static final VkResult ERROR_FEATURE_NOT_PRESENT = new VkResult(VK10.VK_ERROR_FEATURE_NOT_PRESENT, "ERROR_FEATURE_NOT_PRESENT");
	public static final VkResult ERROR_INCOMPATIBLE_DRIVER = new VkResult(VK10.VK_ERROR_INCOMPATIBLE_DRIVER, "ERROR_INCOMPATIBLE_DRIVER");
	public static final VkResult ERROR_TOO_MANY_OBJECTS = new VkResult(VK10.VK_ERROR_TOO_MANY_OBJECTS, "ERROR_TOO_MANY_OBJECTS");
	public static final VkResult ERROR_FORMAT_NOT_SUPPORTED = new VkResult(VK10.VK_ERROR_FORMAT_NOT_SUPPORTED, "ERROR_FORMAT_NOT_SUPPORTED");
	public static final VkResult ERROR_FRAGMENTED_POOL = new VkResult(VK10.VK_ERROR_FRAGMENTED_POOL, "ERROR_FRAGMENTED_POOL");
	public static final VkResult ERROR_VALIDATION_FAILED = new VkResult(VK10.VK_ERROR_VALIDATION_FAILED, "ERROR_VALIDATION_FAILED");
	public static final VkResult ERROR_OUT_OF_POOL_MEMORY = new VkResult(VK11.VK_ERROR_OUT_OF_POOL_MEMORY, "ERROR_OUT_OF_POOL_MEMORY");
	public static final VkResult ERROR_INVALID_EXTERNAL_HANDLE = new VkResult(VK11.VK_ERROR_INVALID_EXTERNAL_HANDLE, "ERROR_INVALID_EXTERNAL_HANDLE");
	public static final VkResult ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS = new VkResult(VK12.VK_ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS, "ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS");
	public static final VkResult ERROR_FRAGMENTATION = new VkResult(VK12.VK_ERROR_FRAGMENTATION, "ERROR_FRAGMENTATION");
	public static final VkResult PIPELINE_COMPILE_REQUIRED = new VkResult(VK13.VK_PIPELINE_COMPILE_REQUIRED, "PIPELINE_COMPILE_REQUIRED");

	public VkResult(int raw, String name) {
		this.raw = raw;
		this.name = name;
		RAW_2_RESULT.put(raw, this);
		RAW_2_NAME.put(raw, name);
	}

	public static String toString(int result) {
		return "VK_" + RAW_2_NAME.computeIfAbsent(result, i -> String.format("UNKNOWN_RESULT_%X", i));
	}

	public static VkResult fromRaw(int raw) {
		return RAW_2_RESULT.computeIfAbsent(raw, i -> new VkResult(i, toString(i)));
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof VkResult vkResult)) return false;
		return raw == vkResult.raw;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(raw);
	}

	@Override
	public String toString() {
		return "VK_" + this.name();
	}
}
