package gay.sylv.polycog.impl.client.wheel;

import java.nio.Buffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.IntFunction;
import java.util.function.Supplier;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.CustomBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Struct;
import org.lwjgl.system.StructBuffer;

public abstract class NativeResource implements AutoCloseable {
	private final Collection<Buffer> buffers = new ArrayList<>();
	private final Collection<org.lwjgl.system.NativeResource> resources = new ArrayList<>();
	private final Collection<CustomBuffer<?>> customBuffers = new ArrayList<>();
	private final Collection<Struct<?>> structs = new ArrayList<>();

	protected NativeResource() {
	}

	protected PointerBuffer mallocPointers(int count) {
		PointerBuffer pointer = MemoryUtil.memAllocPointer(count);
		GameRenderer.LOGGER.debug("ALLOC: {} ({})", pointer, count);
		this.customBuffers.add(pointer);
		return pointer;
	}

	protected PointerBuffer mallocPointer() {
		return this.mallocPointers(1);
	}

	protected <T extends Struct<T>, B extends StructBuffer<T, B>>
			B allocStructs(int count, IntFunction<B> allocFactory) {
		B b = allocFactory.apply(count);
		GameRenderer.LOGGER.debug("ALLOC: {} ({})", b, count);
		this.customBuffers.add(b);
		return b;
	}

	protected <T extends Struct<T>, B extends StructBuffer<T, B>>
			B allocStructs(IntBuffer count, IntFunction<B> allocFactory) {
		return allocStructs(count.get(0), allocFactory);
	}

	protected <T extends Struct<T>> T allocStruct(Supplier<T> allocFactory) {
		T t = allocFactory.get();
		GameRenderer.LOGGER.debug("ALLOC: {}", t);
		this.structs.add(t);
		return t;
	}

	/// Called before automatic resources are freed.
	/// @see #onFree()
	protected void beforeFree() {
	}

	/// Called after automatic resources are freed.
	/// @see #close()
	protected abstract void onFree();

	@Override
	public final void close() {
		this.beforeFree();
		this.buffers.forEach(MemoryUtil::memFree);
		this.resources.forEach(r -> {
			GameRenderer.LOGGER.debug("FREE: {}", r);
			r.free();
		});
		this.customBuffers.forEach(r -> {
			GameRenderer.LOGGER.debug("FREE: {}", r);
			r.free();
		});
		this.structs.forEach(r -> {
			GameRenderer.LOGGER.debug("FREE: {}", r);
			r.free();
		});
		this.onFree();
	}
}
