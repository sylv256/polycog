package gay.sylv.polycog.impl.client.wheel.vulkan.core;

import java.util.function.BiFunction;

import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Struct;

public final class VkStructUtil {
	private VkStructUtil() {
	}

	/// Compare boolean members of two structs.
	public static <T extends Struct<T>> boolean compareBooleans(
			int member,
			int sizeof,
			int alignof,
			T first,
			T second,
			BiFunction<Boolean, Boolean, Boolean> comparator
	) {
		boolean ret = true;
		long firstAddress = first.address();
		long secondAddress = second.address();

		for (int i = member; i < sizeof; i += alignof) {
			ret &= comparator.apply(
					VkBool32.fromRaw(MemoryUtil.memGetInt(firstAddress + i)),
					VkBool32.fromRaw(MemoryUtil.memGetInt(secondAddress + i))
			);
		}

		return ret;
	}
}
