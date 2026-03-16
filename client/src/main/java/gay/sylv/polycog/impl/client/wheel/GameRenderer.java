/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.client.wheel;

import static org.lwjgl.glfw.GLFWVulkan.glfwVulkanSupported;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.VK_KHR_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.LongFunction;

import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gay.sylv.polycog.api.client.wheel.device.GpuQueue;
import gay.sylv.polycog.api.client.wheel.device.PhysicalGpuDevice;
import gay.sylv.polycog.api.core.GameLoop;
import gay.sylv.polycog.impl.client.core.GameClient;
import gay.sylv.polycog.impl.client.wheel.vulkan.core.VkResult;
import gay.sylv.polycog.impl.client.wheel.vulkan.core.VulkanException;
import gay.sylv.polycog.impl.client.wheel.vulkan.device.VkPhysicalGpuDevice;
import gay.sylv.polycog.impl.share.Constants;
import gay.sylv.polycog.impl.share.LazyConstant;
import gay.sylv.polycog.impl.share.LazyConstantList;

public final class GameRenderer implements GameLoop {
	public static final Logger LOGGER = LoggerFactory.getLogger("Polycog/Wheel");
	private @Nullable VkInstance vkInstance;
	private final LazyConstantList<PhysicalGpuDevice> physicalDevices = LazyConstant.ofList();
	private final Collection<String> extensions = new ArrayList<>();
	private final Collection<String> instanceExtensions = new ArrayList<>();
	private final LazyConstant<Instant> stop = LazyConstant.of();
	private final LazyConstant<PhysicalGpuDevice> physicalGpuDevice = LazyConstant.of();

	public static GameRenderer getInstance() {
		return GameClient.getInstance().getRenderer();
	}

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

			this.extensions.addAll(List.of(
					VK_KHR_SWAPCHAIN_EXTENSION_NAME
			));
			this.instanceExtensions.addAll(List.of(
					VK_KHR_SURFACE_EXTENSION_NAME
			));

			ByteBuffer[] enabledLayersArray =
					enabledLayers.toArray(ByteBuffer[]::new);

			VkApplicationInfo applicationInfo = VkApplicationInfo.calloc(stack)
					.sType$Default()
					.apiVersion(VK13.VK_API_VERSION_1_3)
					.pEngineName(stack.UTF8("Wheel"))
					.engineVersion(VK10.VK_MAKE_API_VERSION(0, 0, 1, 0))
					.pApplicationName(stack.UTF8(Constants.NAME))
					.applicationVersion(VK10.VK_MAKE_API_VERSION(0, 0, 1, 0));

			VkInstanceCreateInfo instanceCreateInfo = VkInstanceCreateInfo.calloc(stack)
					.sType$Default()
					.pApplicationInfo(applicationInfo)
					.ppEnabledLayerNames(stack.pointers(enabledLayersArray))
					.ppEnabledExtensionNames(collectionToBuffer(
							stack,
							this.instanceExtensions,
							MemoryStack::ASCII
					));

			PointerBuffer vkInstancePointerBuffer = stack.mallocPointer(1);

			assertSuccess(
					VkResult.fromRaw(VK10.vkCreateInstance(
					instanceCreateInfo,
					null,
					vkInstancePointerBuffer
			)), "Failed to create VkInstance");

			this.vkInstance = new VkInstance(
					vkInstancePointerBuffer.get(0),
					instanceCreateInfo
			);
		}

		LOGGER.debug("Instance Extensions: {}", this.instanceExtensions);
		LOGGER.debug("Device Extensions: {}", this.extensions);

		this.getPhysicalDevices().forEach(device -> {
			LOGGER.debug("Found PhysicalDevice {}", device.name());

			for (GpuQueue queue : device.getLogicalDevice(null).getQueues()) {
				LOGGER.debug("Found GpuQueue of type {} for PhysicalDevice {}", queue.type(), device.name());
			}
		});
	}

	public PhysicalGpuDevice getPhysicalGpuDevice() {
		// TODO: configurable selection (automatically chosen first time)
		return this.physicalGpuDevice.getOrSet(() -> this.getPhysicalDevices().getFirst());
	}

	@Override
	public void close() {
		this.physicalDevices.forEach(v -> ((VkPhysicalGpuDevice) v).close());
		VK13.vkDestroyInstance(this.getVkInstance(), null);
	}

	private void assertState() {
		if (!Thread.currentThread().equals(GameClient.getRenderThread())) {
			throw new IllegalStateException("Render calls may only be performed on the Render Thread");
		}
	}

	public static <T> PointerBuffer collectionToBuffer(
			MemoryStack stack,
			Collection<T> collection,
			BiFunction<MemoryStack, T, ByteBuffer> bufferFactory
	) {
		PointerBuffer pointerBuffer = stack.callocPointer(collection.size());
		collection.forEach(t -> pointerBuffer.put(bufferFactory.apply(stack, t)));
		return pointerBuffer.flip();
	}

	public static <T extends Struct<T>, B extends StructBuffer<T, B>> B collectionToStructBuffer(
			MemoryStack stack,
			Collection<T> collection,
			BiFunction<Integer, MemoryStack, B> bufferFactory
	) {
		B b = bufferFactory.apply(collection.size(), stack);
		collection.forEach(b::put);
		return b.flip();
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
		return VkResult.fromRaw(VK10.vkEnumeratePhysicalDevices(
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

	public List<PhysicalGpuDevice> getPhysicalDevices() {
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
