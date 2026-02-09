package gay.sylv.polycog.impl.client.wheel.vulkan.core;

public final class VulkanException extends RuntimeException {
	public VulkanException(VkResult result) {
		this(result, "A Vulkan error has occurred");
	}

	public VulkanException(VkResult result, String message) {
		super(String.format("%s: %s", message, result));
	}
}
