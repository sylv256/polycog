/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.client.wheel;

import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_DEBUG_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_ERROR_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_INFORMATION_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_BUFFER_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_BUFFER_VIEW_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_COMMAND_BUFFER_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_COMMAND_POOL_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_DEBUG_REPORT_CALLBACK_EXT_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_DESCRIPTOR_POOL_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_DESCRIPTOR_SET_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_DESCRIPTOR_SET_LAYOUT_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_DEVICE_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_DEVICE_MEMORY_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_DISPLAY_KHR_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_DISPLAY_MODE_KHR_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_EVENT_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_FENCE_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_FRAMEBUFFER_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_IMAGE_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_IMAGE_VIEW_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_INSTANCE_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_PHYSICAL_DEVICE_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_PIPELINE_CACHE_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_PIPELINE_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_PIPELINE_LAYOUT_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_QUERY_POOL_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_QUEUE_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_RENDER_PASS_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_SAMPLER_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_SEMAPHORE_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_SHADER_MODULE_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_SURFACE_KHR_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_SWAPCHAIN_KHR_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_UNKNOWN_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_OBJECT_TYPE_VALIDATION_CACHE_EXT_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_PERFORMANCE_WARNING_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_DEBUG_REPORT_WARNING_BIT_EXT;
import static org.lwjgl.vulkan.EXTDebugReport.VK_EXT_DEBUG_REPORT_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.LongFunction;

import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;
import org.lwjgl.vulkan.EXTDebugReport;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkAllocationCallbacks;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkDebugReportCallbackCreateInfoEXT;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import gay.sylv.polycog.api.client.wheel.device.GpuQueue;
import gay.sylv.polycog.api.client.wheel.device.PhysicalGpuDevice;
import gay.sylv.polycog.api.core.GameLoop;
import gay.sylv.polycog.impl.client.core.GameClient;
import gay.sylv.polycog.impl.client.wheel.vulkan.core.VkBool32;
import gay.sylv.polycog.impl.client.wheel.vulkan.core.VkResult;
import gay.sylv.polycog.impl.client.wheel.vulkan.core.VulkanException;
import gay.sylv.polycog.impl.client.wheel.vulkan.device.VkPhysicalGpuDevice;
import gay.sylv.polycog.impl.share.Constants;
import gay.sylv.polycog.impl.share.LazyConstant;
import gay.sylv.polycog.impl.share.LazyConstantList;

public final class GameRenderer implements GameLoop {
	public static final Logger LOGGER = LoggerFactory.getLogger("Polycog/Wheel");
	private @Nullable VkInstance vkInstance;
	@SuppressWarnings("FieldMayBeFinal")
	private @Nullable VkAllocationCallbacks allocator = null;
	private final LazyConstantList<PhysicalGpuDevice> physicalDevices = LazyConstant.ofList();
	private final Collection<String> extensions = new ArrayList<>();
	private final LazyConstant<Instant> stop = LazyConstant.of();

	@Override
	public Control runLoop() {
		if (this.stop.get().isBefore(Instant.now())) {
			return Control.BREAK;
		}

		return Control.CONTINUE;
	}

	@Override
	public void initialize() {
		LOGGER.info("Initializing Wheel/Vulkan");

		this.stop.set(Instant.now().plusSeconds(5));

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

			VkApplicationInfo applicationInfo = VkApplicationInfo.calloc(stack)
					.sType$Default();
			applicationInfo.apiVersion(VK13.VK_API_VERSION_1_3);
			applicationInfo.pEngineName(stack.UTF8("Wheel"));
			applicationInfo.engineVersion(VK13.VK_MAKE_API_VERSION(0, 0, 1, 0));
			applicationInfo.pApplicationName(stack.UTF8(Constants.NAME));
			applicationInfo.applicationVersion(VK13.VK_MAKE_API_VERSION(0, 0, 1, 0));

			VkInstanceCreateInfo instanceCreateInfo = VkInstanceCreateInfo.calloc(stack)
					.sType$Default();
			instanceCreateInfo.pApplicationInfo(applicationInfo);
			instanceCreateInfo.ppEnabledLayerNames(stack.pointers(enabledLayersArray));

			PointerBuffer vkInstancePointerBuffer = stack.mallocPointer(1);

			assertSuccess(
					VkResult.fromRaw(VK13.vkCreateInstance(
					instanceCreateInfo,
					null,
					vkInstancePointerBuffer
			)), "Failed to create VkInstance");

			this.vkInstance = new VkInstance(
					vkInstancePointerBuffer.get(0),
					instanceCreateInfo
			);
			this.extensions.addAll(List.of(
					VK_KHR_SWAPCHAIN_EXTENSION_NAME
			));

			if (Constants.WHEEL_GFX_VALIDATION) {
				// Debug-only extensions
				this.extensions.add(VK_EXT_DEBUG_REPORT_EXTENSION_NAME);

				// Use our own logger for validation errors
				VkDebugReportCallbackCreateInfoEXT debugReportCallbackCreateInfo =
						VkDebugReportCallbackCreateInfoEXT.calloc(stack)
								.sType$Default()
								.flags(VK_DEBUG_REPORT_INFORMATION_BIT_EXT
										& VK_DEBUG_REPORT_WARNING_BIT_EXT
										& VK_DEBUG_REPORT_PERFORMANCE_WARNING_BIT_EXT
										& VK_DEBUG_REPORT_ERROR_BIT_EXT
										& VK_DEBUG_REPORT_DEBUG_BIT_EXT)
								.pfnCallback((flags, objectTypeI, object, location, messageCode, pLayerPrefix, pMessage, _) -> {
									String objectType = fromDebugReportObjectType(objectTypeI);
									String layerPrefix = MemoryUtil.memUTF8(pLayerPrefix);
									String message = MemoryUtil.memUTF8(pMessage);

									Level level = switch (flags) {
										case VK_DEBUG_REPORT_INFORMATION_BIT_EXT -> Level.INFO;
										case VK_DEBUG_REPORT_WARNING_BIT_EXT,
										     VK_DEBUG_REPORT_PERFORMANCE_WARNING_BIT_EXT -> Level.WARN;
										case VK_DEBUG_REPORT_ERROR_BIT_EXT -> Level.ERROR;
										case VK_DEBUG_REPORT_DEBUG_BIT_EXT -> Level.DEBUG;
										default -> {
											LOGGER.warn("Unknown VkDebugReportFlagBitsEXT {}", Integer.toHexString(flags).toUpperCase(Locale.ROOT));
											yield Level.DEBUG;
										}
									};
									LOGGER.atLevel(level).log(message);
									return VkBool32.toRaw(true);
								});
				EXTDebugReport.vkCreateDebugReportCallbackEXT(
						this.getVkInstance(),
				);
			}
		}

		this.getPhysicalDevices().forEach(device -> {
			LOGGER.info("Found PhysicalDevice {}", device.name());

			for (GpuQueue queue : device.getLogicalGpuDevice().getQueues()) {
				LOGGER.info("Found GpuQueue of type {} for PhysicalDevice {}", queue.type(), device.name());
			}
		});
	}

	@Override
	public void close() {
		this.physicalDevices.forEach(v -> ((VkPhysicalGpuDevice) v).close());
		VK13.vkDestroyInstance(
				this.getVkInstance(),
				this.allocator
		);
	}

	public @Nullable VkAllocationCallbacks getAllocator() {
		return this.allocator;
	}

	private void assertState() {
		if (!Thread.currentThread().equals(GameClient.getRenderThread())) {
			throw new IllegalStateException("Render calls may only be performed on the Render Thread");
		}
	}

	private static String fromDebugReportObjectType(int objectType) {
		return "VK_DEBUG_REPORT_OBJECT_TYPE_" + switch (objectType) {
			case VK_DEBUG_REPORT_OBJECT_TYPE_UNKNOWN_EXT -> "UNKNOWN";
			case VK_DEBUG_REPORT_OBJECT_TYPE_INSTANCE_EXT -> "INSTANCE";
			case VK_DEBUG_REPORT_OBJECT_TYPE_PHYSICAL_DEVICE_EXT -> "PHYSICAL_DEVICE";
			case VK_DEBUG_REPORT_OBJECT_TYPE_DEVICE_EXT -> "DEVICE";
			case VK_DEBUG_REPORT_OBJECT_TYPE_QUEUE_EXT -> "QUEUE";
			case VK_DEBUG_REPORT_OBJECT_TYPE_SEMAPHORE_EXT -> "SEMAPHORE";
			case VK_DEBUG_REPORT_OBJECT_TYPE_COMMAND_BUFFER_EXT -> "COMMAND_BUFFER";
			case VK_DEBUG_REPORT_OBJECT_TYPE_FENCE_EXT -> "FENCE";
			case VK_DEBUG_REPORT_OBJECT_TYPE_DEVICE_MEMORY_EXT -> "DEVICE_MEMORY";
			case VK_DEBUG_REPORT_OBJECT_TYPE_BUFFER_EXT -> "BUFFER";
			case VK_DEBUG_REPORT_OBJECT_TYPE_IMAGE_EXT -> "IMAGE";
			case VK_DEBUG_REPORT_OBJECT_TYPE_EVENT_EXT -> "EVENT";
			case VK_DEBUG_REPORT_OBJECT_TYPE_QUERY_POOL_EXT -> "QUERY_POOL";
			case VK_DEBUG_REPORT_OBJECT_TYPE_BUFFER_VIEW_EXT -> "BUFFER_VIEW";
			case VK_DEBUG_REPORT_OBJECT_TYPE_IMAGE_VIEW_EXT -> "IMAGE_VIEW";
			case VK_DEBUG_REPORT_OBJECT_TYPE_SHADER_MODULE_EXT -> "SHADER_MODULE";
			case VK_DEBUG_REPORT_OBJECT_TYPE_PIPELINE_CACHE_EXT -> "PIPELINE_CACHE";
			case VK_DEBUG_REPORT_OBJECT_TYPE_PIPELINE_LAYOUT_EXT -> "PIPELINE_LAYOUT";
			case VK_DEBUG_REPORT_OBJECT_TYPE_RENDER_PASS_EXT -> "RENDER_PASS";
			case VK_DEBUG_REPORT_OBJECT_TYPE_PIPELINE_EXT -> "PIPELINE_EXT";
			case VK_DEBUG_REPORT_OBJECT_TYPE_DESCRIPTOR_SET_LAYOUT_EXT -> "DESCRIPTOR_SET_LAYOUT";
			case VK_DEBUG_REPORT_OBJECT_TYPE_SAMPLER_EXT -> "SAMPLER";
			case VK_DEBUG_REPORT_OBJECT_TYPE_DESCRIPTOR_POOL_EXT -> "DESCRIPTOR_POOL";
			case VK_DEBUG_REPORT_OBJECT_TYPE_DESCRIPTOR_SET_EXT -> "DESCRIPTOR_SET";
			case VK_DEBUG_REPORT_OBJECT_TYPE_FRAMEBUFFER_EXT -> "FRAMEBUFFER";
			case VK_DEBUG_REPORT_OBJECT_TYPE_COMMAND_POOL_EXT -> "COMMAND_POOL";
			case VK_DEBUG_REPORT_OBJECT_TYPE_SURFACE_KHR_EXT -> "SURFACE_KHR";
			case VK_DEBUG_REPORT_OBJECT_TYPE_SWAPCHAIN_KHR_EXT -> "SWAPCHAIN_KHR";
			case VK_DEBUG_REPORT_OBJECT_TYPE_DEBUG_REPORT_CALLBACK_EXT_EXT -> "DEBUG_REPORT_CALLBACK_EXT";
			case VK_DEBUG_REPORT_OBJECT_TYPE_DISPLAY_KHR_EXT -> "DISPLAY_KHR";
			case VK_DEBUG_REPORT_OBJECT_TYPE_DISPLAY_MODE_KHR_EXT -> "DISPLAY_MODE_KHR";
			case VK_DEBUG_REPORT_OBJECT_TYPE_VALIDATION_CACHE_EXT_EXT -> "VALIDATION_CACHE_EXT";
			default -> String.format("UNKNOWN_%X", objectType);
		} + "_EXT";
	}

	public static <T> PointerBuffer collectionToBuffer(
			MemoryStack stack,
			Collection<T> collection,
			BiFunction<MemoryStack, T, ByteBuffer> bufferFactory
	) {
		PointerBuffer pointerBuffer = stack.callocPointer(collection.size());
		collection.forEach(t -> pointerBuffer.put(bufferFactory.apply(stack, t)));
		return pointerBuffer;
	}

	public static <T extends Struct<T>, B extends StructBuffer<T, B>> B collectionToStructBuffer(
			MemoryStack stack,
			Collection<T> collection,
			BiFunction<Integer, MemoryStack, B> bufferFactory
	) {
		B b = bufferFactory.apply(collection.size(), stack);
		collection.forEach(b::put);
		return b;
	}

	public static <T> List<T> bufferToList(
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

	public static <T extends Struct<T>, B extends StructBuffer<T, B>> List<T> bufferToList(
			int count,
			StructBuffer<T, B> buffer
	) {
		//noinspection unchecked
		T[] items = (T[]) new Struct[count];

		for (int i = 0; i < count; i++) {
			items[i] = buffer.get(i);
		}

		return List.of(items);
	}

	public static <T extends Struct<T>, B extends StructBuffer<T, B>> List<T> bufferToList(
			IntBuffer intBuffer,
			StructBuffer<T, B> buffer
	) {
		return bufferToList(intBuffer.get(0), buffer);
	}

	public <T> List<T> bufferToList(
			int count,
			PointerBuffer buffer,
			BiFunction<Long, VkInstance, T> factory
	) {
		return bufferToList(count, buffer, handle -> factory.apply(handle, this.getVkInstance()));
	}

	public <T> List<T> bufferToList(
			IntBuffer intBuffer,
			PointerBuffer buffer,
			BiFunction<Long, VkInstance, T> factory
	) {
		return this.bufferToList(intBuffer.get(0), buffer, factory);
	}

	public static void assertResult(
			VkResult result,
			String message,
			VkResult... results
	) {
		for (VkResult other : results) {
			if (result.equals(other)) {
				return;
			}
		}

		throw new VulkanException(result, message);
	}

	public static void assertResult (
			VkResult result,
			VkResult... results
	) {
		for (VkResult other : results) {
			if (result.equals(other)) {
				return;
			}
		}

		throw new VulkanException(result);
	}

	public static void assertSuccess(VkResult result) {
		assertResult(result, VkResult.SUCCESS);
	}

	public static void assertSuccess(VkResult result, String message) {
		assertResult(result, message, VkResult.SUCCESS);
	}

	private VkResult vkEnumeratePhysicalDevices(
			IntBuffer physicalDeviceCount,
			@Nullable PointerBuffer physicalDevices
	) {
		return VkResult.fromRaw(VK13.vkEnumeratePhysicalDevices(
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

	public Collection<PhysicalGpuDevice> getPhysicalDevices() {
		this.assertState();
		return this.physicalDevices.getOrSet(() -> {
			try (MemoryStack stack = stackPush()) {
				// We first have to retrieve the amount of physical devices…
				IntBuffer physicalDeviceCount = stack.mallocInt(1);
				assertSuccess(this.vkEnumeratePhysicalDevices(
						physicalDeviceCount,
						null
				));

				// …then we can get the actual physical devices.
				PointerBuffer physicalDevices = stack.callocPointer(physicalDeviceCount.get(0));
				assertSuccess(this.vkEnumeratePhysicalDevices(
						physicalDeviceCount,
						physicalDevices
				));
				return this.bufferToList(
						physicalDeviceCount,
						physicalDevices,
						(handle, instance) -> new VkPhysicalGpuDevice(
								handle,
								instance,
								this.extensions
						)
				);
			}
		});
	}
}
