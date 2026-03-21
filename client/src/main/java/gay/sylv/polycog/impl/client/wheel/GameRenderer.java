/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.client.wheel;

import static gay.sylv.polycog.impl.client.core.GameClient.handleErrorSDL;
import static org.lwjgl.sdl.SDLVulkan.SDL_Vulkan_GetInstanceExtensions;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.vma.Vma.VMA_ALLOCATOR_CREATE_BUFFER_DEVICE_ADDRESS_BIT;
import static org.lwjgl.vulkan.KHRSurface.VK_KHR_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRSwapchain.VK_KHR_SWAPCHAIN_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRWin32Surface.VK_KHR_WIN32_SURFACE_EXTENSION_NAME;
import static org.lwjgl.vulkan.KHRXlibSurface.VK_KHR_XLIB_SURFACE_EXTENSION_NAME;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.LongFunction;

import org.jspecify.annotations.Nullable;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Platform;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;
import org.lwjgl.util.vma.Vma;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;
import org.lwjgl.vulkan.VK10;
import org.lwjgl.vulkan.VK13;
import org.lwjgl.vulkan.VkApplicationInfo;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkInstanceCreateInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gay.sylv.polycog.api.client.wheel.device.GpuDevice;
import gay.sylv.polycog.api.client.wheel.device.GpuFeatures;
import gay.sylv.polycog.api.client.wheel.device.GpuQueue;
import gay.sylv.polycog.api.client.wheel.device.PhysicalDevice;
import gay.sylv.polycog.api.core.GameLoop;
import gay.sylv.polycog.impl.client.core.GameClient;
import gay.sylv.polycog.impl.client.wheel.vulkan.core.DeviceUnsupportedException;
import gay.sylv.polycog.impl.client.wheel.vulkan.core.VkResult;
import gay.sylv.polycog.impl.client.wheel.vulkan.core.VulkanException;
import gay.sylv.polycog.impl.client.wheel.vulkan.device.VkGpuDevice;
import gay.sylv.polycog.impl.client.wheel.vulkan.device.VkPhysicalGpuDevice;
import gay.sylv.polycog.impl.share.Constants;
import gay.sylv.polycog.impl.share.LazyConstant;
import gay.sylv.polycog.impl.share.LazyConstantList;

public final class GameRenderer extends NativeResource<VkInstance> implements GameLoop {
	public static final Logger LOGGER = LoggerFactory.getLogger("Polycog/Wheel");
	private final LazyConstantList<PhysicalDevice> physicalDevices = LazyConstant.ofList();
	private final Collection<String> extensions = new ArrayList<>();
	private final Collection<String> instanceExtensions = new ArrayList<>();
	private final LazyConstant<PhysicalDevice> physicalGpuDevice = LazyConstant.of();

	public static GameRenderer getInstance() {
		return GameClient.getInstance().getRenderer();
	}

	@Override
	public Control runLoop() {
		if (GameClient.getInstance().isQuitting()) {
			return Control.BREAK;
		}

		return Control.CONTINUE;
	}

	@Override
	public void initialize() {
		LOGGER.info("Initializing Wheel/Vulkan");

		PointerBuffer supportedInstanceExtensionsBuffer = SDL_Vulkan_GetInstanceExtensions();

		if (supportedInstanceExtensionsBuffer == null) {
			throw handleErrorSDL(new DeviceUnsupportedException());
		}

		Set<String> supportedInstanceExtensions = new HashSet<>();

		for (int i = 0; i < supportedInstanceExtensionsBuffer.limit(); i++) {
			supportedInstanceExtensions.add(supportedInstanceExtensionsBuffer.getStringASCII(i));
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

			this.instanceExtensions.add(switch (Platform.get()) {
				case LINUX -> VK_KHR_XLIB_SURFACE_EXTENSION_NAME;
				case WINDOWS -> VK_KHR_WIN32_SURFACE_EXTENSION_NAME;
				default -> throw unsupported();
			});

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

			List<String> unsupportedInstanceExtensions = new ArrayList<>();

			for (String extension : this.instanceExtensions) {
				if (!supportedInstanceExtensions.contains(extension)) {
					unsupportedInstanceExtensions.add(extension);
				}
			}

			if (!unsupportedInstanceExtensions.isEmpty()) {
				throw new DeviceUnsupportedException("The required Vulkan instance extensions are missing: " + unsupportedInstanceExtensions);
			}

			assertSuccess(
					VkResult.fromRaw(VK10.vkCreateInstance(
					instanceCreateInfo,
					null,
					vkInstancePointerBuffer
			)), "Failed to create VkInstance");

			this.vkHandle = new VkInstance(
					vkInstancePointerBuffer.get(0),
					instanceCreateInfo
			);
		}

		LOGGER.debug("Instance Extensions: {}", this.instanceExtensions);
		LOGGER.debug("Device Extensions: {}", this.extensions);

		this.getPhysicalDevices().forEach(device -> {
			LOGGER.debug("Found PhysicalDevice {}", device.name());

			for (GpuQueue queue : device.getLogicalDevice(GpuFeatures.CORE).getQueues()) {
				LOGGER.debug("Found GpuQueue of type {} for PhysicalDevice {}", queue.type(), device.name());
			}
		});

		VkGpuDevice device = GpuDevice.get().wheel$impl();
		VkPhysicalGpuDevice physicalDevice = PhysicalDevice.get().wheel$impl();
		VmaVulkanFunctions vmaFunctions = this.allocStruct(VmaVulkanFunctions::calloc)
				.set(this.getVkInstance(), device.getVkHandle());
		VmaAllocatorCreateInfo allocatorCreateInfo = this.allocStruct(VmaAllocatorCreateInfo::calloc)
				.set(
						VMA_ALLOCATOR_CREATE_BUFFER_DEVICE_ADDRESS_BIT,
						physicalDevice.getVkHandle(),
						device.getVkHandle(),
						0,
						null,
						null,
						null,
						vmaFunctions,
						this.getVkInstance(),
						VK13.VK_API_VERSION_1_3,
						null
				);
		PointerBuffer allocatorPointer = this.mallocPointer();
		assertSuccess(Vma.vmaCreateAllocator(allocatorCreateInfo, allocatorPointer));

		GameClient.getInstance().initializeWindow(device);
	}

	public static DeviceUnsupportedException unsupported() {
		return new DeviceUnsupportedException("WheelVK only supports Linux and Windows");
		// If you're a graphics programmer, pretend you don't see that ^
	}

	public PhysicalDevice getPhysicalGpuDevice() {
		// TODO: configurable selection (automatically chosen first time)
		return this.physicalGpuDevice.getOrSet(() -> {
			for (PhysicalDevice apiPhysicalDevice : this.getPhysicalDevices()) {
				VkPhysicalGpuDevice physicalDevice = (VkPhysicalGpuDevice) apiPhysicalDevice;

				if (physicalDevice.getVkProperties().apiVersion() >= VK13.VK_API_VERSION_1_3) {
					return physicalDevice;
				}
			}

			throw new DeviceUnsupportedException("There is no device that supports at least Vulkan 1.3");
		});
	}

	@Override
	public void onFree() {
		this.physicalDevices.forEach(v -> ((VkPhysicalGpuDevice) v).close());
		VK10.vkDestroyInstance(this.getVkInstance(), null);
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

	public static void assertSuccess(int result) {
		assertSuccess(VkResult.fromRaw(result));
	}

	public static void assertSuccess(int result, String message) {
		assertSuccess(VkResult.fromRaw(result), message);
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
			this.vkHandle,
			"Vulkan has not yet been initialized"
		);
	}

	public List<PhysicalDevice> getPhysicalDevices() {
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
