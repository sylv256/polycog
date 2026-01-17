package gay.sylv.polycog.impl.client.wheel;

public final class VulkanException extends RuntimeException {
	public VulkanException(int result) {
		super(String.format("A Vulkan error has occurred: %X", result));
	}
}
