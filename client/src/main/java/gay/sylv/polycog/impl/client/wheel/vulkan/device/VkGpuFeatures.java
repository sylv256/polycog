/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.client.wheel.vulkan.device;

import java.util.Objects;
import java.util.function.BiFunction;

import org.jspecify.annotations.Nullable;
import org.lwjgl.vulkan.VK12;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkBaseOutStructure;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures;
import org.lwjgl.vulkan.VkPhysicalDeviceFeatures2;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan11Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan12Features;
import org.lwjgl.vulkan.VkPhysicalDeviceVulkan13Features;

import gay.sylv.polycog.api.client.wheel.device.GpuFeatures;
import gay.sylv.polycog.impl.client.wheel.AutomaticallyMoved;
import gay.sylv.polycog.impl.client.wheel.NativeResource;
import gay.sylv.polycog.impl.client.wheel.vulkan.core.VkStructUtil;

public final class VkGpuFeatures extends NativeResource<VkPhysicalDeviceFeatures2> implements GpuFeatures {
	@SuppressWarnings("resource") // Moved automatically
	public static final VkGpuFeatures DEFAULT = new VkGpuFeatures(
			VkPhysicalDeviceFeatures.calloc()
					.samplerAnisotropy(true),
			VkPhysicalDeviceVulkan11Features.calloc()
					.sType$Default(),
			VkPhysicalDeviceVulkan12Features.calloc()
					.sType$Default()
					.descriptorIndexing(true)
					.descriptorBindingVariableDescriptorCount(true)
					.runtimeDescriptorArray(true)
					.bufferDeviceAddress(true),
			VkPhysicalDeviceVulkan13Features.calloc()
					.sType$Default()
					.synchronization2(true)
					.dynamicRendering(true)
	);
	private final VkPhysicalDeviceFeatures vk10Features;
	private final VkPhysicalDeviceVulkan11Features vk11Features;
	private final VkPhysicalDeviceVulkan12Features vk12Features;
	private final VkPhysicalDeviceVulkan13Features vk13Features;
	private @Nullable Boolean coreFeatures;

	public VkGpuFeatures(@AutomaticallyMoved VkPhysicalDeviceFeatures2 features2) {
		VkBaseOutStructure next = VkBaseOutStructure.createSafe(features2.pNext());
		VkPhysicalDeviceVulkan11Features vk11Features = null;
		VkPhysicalDeviceVulkan12Features vk12Features = null;
		VkPhysicalDeviceVulkan13Features vk13Features = null;

		while (next != null) {
			switch (next.sType()) {
				case VK12.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_1_FEATURES ->
						vk11Features = VkPhysicalDeviceVulkan11Features.create(next.address());
				case VK12.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_2_FEATURES ->
						vk12Features = VkPhysicalDeviceVulkan12Features.create(next.address());
				case VK13.VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VULKAN_1_3_FEATURES ->
						vk13Features = VkPhysicalDeviceVulkan13Features.create(next.address());
			}

			next = next.pNext();
		}

		this(
				Objects.requireNonNull(features2.features()),
				Objects.requireNonNull(vk11Features),
				Objects.requireNonNull(vk12Features),
				Objects.requireNonNull(vk13Features)
		);
		this.vkHandle = this.move(features2);
	}

	public VkGpuFeatures(
			@AutomaticallyMoved VkPhysicalDeviceFeatures vk10Features,
			@AutomaticallyMoved VkPhysicalDeviceVulkan11Features vk11Features,
			@AutomaticallyMoved VkPhysicalDeviceVulkan12Features vk12Features,
			@AutomaticallyMoved VkPhysicalDeviceVulkan13Features vk13Features
	) {
		this.move(vk10Features, vk11Features, vk12Features, vk13Features);
		this.vk10Features = vk10Features;
		this.vk11Features = vk11Features;
		this.vk12Features = vk12Features;
		this.vk13Features = vk13Features;
	}

	@Override
	public boolean hasCoreFeatures() {
		if (this.coreFeatures == null) {
			this.coreFeatures = this.hasFeatures(DEFAULT);
		}

		return this.coreFeatures;
	}

	@Override
	public boolean hasFeatures(GpuFeatures requestedFeatures) {
		final BiFunction<Boolean, Boolean, Boolean> isSupported =
				(requested, supported) -> !requested || supported;
		VkGpuFeatures vkFeatures = requestedFeatures.wheel$internal();

		return VkStructUtil.compareBooleans(
				VkPhysicalDeviceFeatures.ROBUSTBUFFERACCESS,
				VkPhysicalDeviceFeatures.SIZEOF,
				VkPhysicalDeviceFeatures.ALIGNOF,
				vkFeatures.vk10Features,
				this.vk10Features,
				isSupported
		) && VkStructUtil.compareBooleans(
				VkPhysicalDeviceVulkan11Features.STORAGEBUFFER16BITACCESS,
				VkPhysicalDeviceVulkan11Features.SIZEOF,
				VkPhysicalDeviceVulkan11Features.ALIGNOF,
				vkFeatures.vk11Features,
				this.vk11Features,
				isSupported
		) && VkStructUtil.compareBooleans(
				VkPhysicalDeviceVulkan12Features.SAMPLERMIRRORCLAMPTOEDGE,
				VkPhysicalDeviceVulkan12Features.SIZEOF,
				VkPhysicalDeviceVulkan12Features.ALIGNOF,
				vkFeatures.vk12Features,
				this.vk12Features,
				isSupported
		) && VkStructUtil.compareBooleans(
				VkPhysicalDeviceVulkan13Features.ROBUSTIMAGEACCESS,
				VkPhysicalDeviceVulkan13Features.SIZEOF,
				VkPhysicalDeviceVulkan13Features.ALIGNOF,
				vkFeatures.vk13Features,
				this.vk13Features,
				isSupported
		);
	}

	public VkPhysicalDeviceFeatures2 getFeatures2() {
		if (this.vkHandle == null) {
			this.vkHandle = this.allocStruct(VkPhysicalDeviceFeatures2::calloc)
					.sType$Default()
					.features(this.vk10Features)
					.pNext(this.vk11Features)
					.pNext(this.vk12Features)
					.pNext(this.vk13Features);
		}

		return Objects.requireNonNull(this.vkHandle);
	}

	@Override
	public String toString() {
		return "VkGpuFeatures[" +
				"coreFeatures=" + this.hasCoreFeatures() +
				"]";
	}
}
