/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.client.wheel;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
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
	private final Collection<NativeResource> children = new HashSet<>();

	protected NativeResource() {
	}

	protected PointerBuffer mallocPointers(int count) {
		PointerBuffer pointer = MemoryUtil.memAllocPointer(count);
		logAllocCount(count, pointer);
		this.customBuffers.add(pointer);
		return pointer;
	}

	protected PointerBuffer mallocPointer() {
		return this.mallocPointers(1);
	}

	protected <T extends Struct<T>, B extends StructBuffer<T, B>>
			B allocStructs(int count, IntFunction<B> allocFactory) {
		B b = allocFactory.apply(count);
		logAllocCount(count, b);
		this.customBuffers.add(b);
		return b;
	}

	protected <T extends Struct<T>, B extends StructBuffer<T, B>>
			B allocStructs(IntBuffer count, IntFunction<B> allocFactory) {
		return allocStructs(count.get(0), allocFactory);
	}

	protected <T extends Struct<T>> T allocStruct(Supplier<T> allocFactory) {
		T t = allocFactory.get();
		logAlloc(t);
		this.structs.add(t);
		return t;
	}

	protected FloatBuffer allocFloats(float... values) {
		FloatBuffer buffer = MemoryUtil.memAllocFloat(values.length);
		buffer.put(0, values);
		logAllocCount(values.length, buffer);
		this.buffers.add(buffer);
		return buffer;
	}

	protected <T extends NativeResource> T addChild(T child) {
		this.children.add(child);
		return child;
	}

	protected <T extends NativeResource, C extends Collection<T>> C addChildren(C children) {
		this.children.addAll(children);
		return children;
	}

	/// Called before automatic resources are freed.
	/// @see #onFree()
	protected void onBeforeFree() {
	}

	/// Called after automatic resources are freed.
	/// @see #close()
	protected void onFree() {
	}

	@Override
	public final void close() {
		this.onBeforeFree();
		this.children.forEach(NativeResource::close);
		this.buffers.forEach(r -> {
			logFree(r);
			MemoryUtil.memFree(r);
		});
		this.resources.forEach(r -> {
			logFree(r);
			r.free();
		});
		this.customBuffers.forEach(r -> {
			logFree(r);
			r.free();
		});
		this.structs.forEach(r -> {
			logFree(r);
			r.free();
		});
		this.onFree();
	}

	private static void logAlloc(Object obj) {
		GameRenderer.LOGGER.trace("ALLOC: {}", obj);
	}

	private static void logAllocCount(int count, Object obj) {
		GameRenderer.LOGGER.trace("ALLOC: ({}) {}", count, obj);
	}

	private static void logFree(Object obj) {
		GameRenderer.LOGGER.trace("FREE: {}", obj);
	}
}
