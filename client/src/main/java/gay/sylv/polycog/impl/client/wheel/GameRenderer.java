/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.client.wheel;

import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;
import static org.lwjgl.system.MemoryStack.stackPush;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.LongFunction;

import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gay.sylv.polycog.api.client.wheel.device.PhysicalDevice;
import gay.sylv.polycog.api.core.GameLoop;
import gay.sylv.polycog.impl.client.core.GameClient;
import gay.sylv.polycog.impl.client.wheel.device.PhysicalDeviceImpl;
import gay.sylv.polycog.impl.share.Constants;

public final class GameRenderer implements GameLoop {
	private static final Logger LOGGER = LoggerFactory.getLogger("Polycog/Wheel");
	private @Nullable VkInstance vkInstance;
	private @Nullable VkAllocationCallbacks allocator;

	@Override
	public Control runLoop() {
		return Control.BREAK;
	}

	@Override
	public void initialize() {
		GameClient client = GameClient.getInstance();
		LOGGER.info("Initializing Wheel/Vulkan");

		if (!glfwVulkanSupported()) {
			throw new IllegalStateException("""
				Vulkan is not supported on your platform.
				This means your computer is either too old or not officially supported.
				Do not open support tickets or create issues; they will be closed.""");
		}

		try (MemoryStack stack = stackPush()) {
			List<ByteBuffer> enabledLayers = new ArrayList<>();

			if (Constants.WHEEL_GFX_VALIDATION) {
				enabledLayers.add(stack.UTF8("VK_LAYER_KHRONOS_validation"));
			}

			ByteBuffer[] enabledLayersArray =
					enabledLayers.toArray(ByteBuffer[]::new);

			VkApplicationInfo applicationInfo = VkApplicationInfo.calloc(stack);
			applicationInfo.sType(VK13.VK_STRUCTURE_TYPE_APPLICATION_INFO);
			applicationInfo.apiVersion(VK13.VK_API_VERSION_1_3);
			applicationInfo.pEngineName(stack.UTF8("Wheel"));
			applicationInfo.engineVersion(VK13.VK_MAKE_API_VERSION(0, 0, 1, 0));
			applicationInfo.pApplicationName(stack.UTF8(Constants.NAME));
			applicationInfo.applicationVersion(VK13.VK_MAKE_API_VERSION(0, 0, 1, 0));

			VkInstanceCreateInfo instanceCreateInfo = VkInstanceCreateInfo.calloc(stack);
			instanceCreateInfo.sType(VK13.VK_STRUCTURE_TYPE_INSTANCE_CREATE_INFO);
			instanceCreateInfo.pApplicationInfo(applicationInfo);
			instanceCreateInfo.ppEnabledLayerNames(stack.pointers(enabledLayersArray));

			PointerBuffer vkInstancePointerBuffer = stack.mallocPointer(1);

			int result = VK13.vkCreateInstance(
					instanceCreateInfo,
					null,
					vkInstancePointerBuffer
			);

			if (result != VK13.VK_SUCCESS) {
				throw new RuntimeException("Failed to create VkInstance");
			}

			this.vkInstance = new VkInstance(
					vkInstancePointerBuffer.get(0),
					instanceCreateInfo
			);
		}
	}

	@Override
	public void close() {
		VK13.vkDestroyInstance(
				Objects.requireNonNull(this.vkInstance),
				this.allocator
		);
	}

	private void assertState() {
		if (!Thread.currentThread().equals(GameClient.getRenderThread())) {
			throw new IllegalStateException("Render calls may only be performed on the Render Thread");
		}
	}

	private static <T> Collection<T> bufferToCollection(
			int count,
			PointerBuffer buffer,
			LongFunction<T> factory
	) {
		//noinspection unchecked
		T[] items = (T[]) new Object[count];

		for (int i = 0; i < count; i++) {
			items[i] = factory.apply(buffer.get(i));
		}

		return List.of(items);
	}

	private static <T> Collection<T> bufferToCollection(
			IntBuffer intBuffer,
			PointerBuffer buffer,
			LongFunction<T> factory
	) {
		return bufferToCollection(intBuffer.get(0), buffer, factory);
	}

	public static String stringVkResult(int result) {
		return "VK_" + switch (result) {
			case VK13.VK_SUCCESS -> "SUCCESS";
			case VK13.VK_NOT_READY -> "NOT_READY";
			case VK13.VK_TIMEOUT -> "TIMEOUT";
			case VK13.VK_EVENT_SET -> "EVENT_SET";
			case VK13.VK_EVENT_RESET -> "EVENT_RESET";
			case VK13.VK_INCOMPLETE -> "INCOMPLETE";
			case VK13.VK_ERROR_OUT_OF_HOST_MEMORY -> "ERROR_OUT_OF_HOST_MEMORY";
			case VK13.VK_ERROR_OUT_OF_DEVICE_MEMORY -> "ERROR_OUT_OF_DEVICE_MEMORY";
			case VK13.VK_ERROR_INITIALIZATION_FAILED -> "ERROR_INITIALIZATION_FAILED";
			case VK13.VK_ERROR_DEVICE_LOST -> "ERROR_DEVICE_LOST";
			case VK13.VK_ERROR_MEMORY_MAP_FAILED -> "ERROR_MEMORY_MAP_FAILED";
			case VK13.VK_ERROR_LAYER_NOT_PRESENT -> "ERROR_LAYER_NOT_PRESENT";
			case VK13.VK_ERROR_EXTENSION_NOT_PRESENT -> "ERROR_EXTENSION_NOT_PRESENT";
			case VK13.VK_ERROR_FEATURE_NOT_PRESENT -> "ERROR_FEATURE_NOT_PRESENT";
			case VK13.VK_ERROR_INCOMPATIBLE_DRIVER -> "ERROR_INCOMPATIBLE_DRIVER";
			case VK13.VK_ERROR_TOO_MANY_OBJECTS -> "ERROR_TOO_MANY_OBJECTS";
			case VK13.VK_ERROR_FORMAT_NOT_SUPPORTED -> "ERROR_FORMAT_NOT_SUPPORTED";
			case VK13.VK_ERROR_FRAGMENTED_POOL -> "ERROR_FRAGMENTED_POOL";
			case VK13.VK_ERROR_VALIDATION_FAILED -> "ERROR_VALIDATION_FAILED";
			case VK13.VK_ERROR_OUT_OF_POOL_MEMORY -> "ERROR_OUT_OF_POOL_MEMORY";
			case VK13.VK_ERROR_INVALID_EXTERNAL_HANDLE -> "ERROR_INVALID_EXTERNAL_HANDLE";
			case VK13.VK_ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS -> "ERROR_INVALID_OPAQUE_CAPTURE_ADDRESS";
			case VK13.VK_ERROR_FRAGMENTATION -> "ERROR_FRAGMENTATION";
			case VK13.VK_PIPELINE_COMPILE_REQUIRED -> "PIPELINE_COMPILE_REQUIRED";
			default -> String.format("UNKNOWN_%X", result);
		};
	}

	private static void checkResult(int result) {
		if (result != VK13.VK_SUCCESS) {
			throw new VulkanException(result);
		}
	}

	private void vkEnumeratePhysicalDevices(
			IntBuffer physicalDeviceCount,
			@Nullable PointerBuffer physicalDevices
	) {
		checkResult(VK13.vkEnumeratePhysicalDevices(
				this.getVkInstance(),
				physicalDeviceCount,
				physicalDevices
		));
	}

	public VkInstance getVkInstance() {
		return Objects.requireNonNull(
			this.vkInstance,
			"Vulkan has not yet been initialized"
		);
	}

	public Collection<PhysicalDevice> getPhysicalDevices() {
		this.assertState();

		try (MemoryStack stack = stackPush()) {
			// The vulkan spec sucks so we have to do this lmao.
			IntBuffer physicalDeviceCount = stack.callocInt(1);
			this.vkEnumeratePhysicalDevices(
					physicalDeviceCount,
					null
			);
			PointerBuffer physicalDevices = stack.callocPointer(physicalDeviceCount.get());
			this.vkEnumeratePhysicalDevices(
					physicalDeviceCount,
					physicalDevices
			);
			return bufferToCollection(
					physicalDeviceCount,
					physicalDevices,
					handle -> new PhysicalDeviceImpl(handle, this.getVkInstance())
			);
		}
	}
}
