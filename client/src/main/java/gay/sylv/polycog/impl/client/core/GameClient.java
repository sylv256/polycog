/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.client.core;

import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;

import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gay.sylv.polycog.api.core.GameLoop;
import gay.sylv.polycog.impl.client.wheel.GameRenderer;
import gay.sylv.polycog.impl.share.Constants;

public final class GameClient implements GameLoop {
	public final Logger logger;
	private long windowHandle;
	private final GameRenderer renderer;
	private static @Nullable GameClient INSTANCE;
	private static @Nullable Thread CLIENT_THREAD;
	private static @Nullable Thread RENDER_THREAD;

	private GameClient() {
		this.logger = LoggerFactory.getLogger("Polycog/Client");
		Configuration.MEMORY_ALLOCATOR.set("system"); // FIXME: add jemalloc bindings
		this.renderer = new GameRenderer();
	}

	public static void createInstance() {
		if (INSTANCE != null) {
			throw new IllegalStateException("GameClient is already initialized");
		}

		INSTANCE = new GameClient();
		CLIENT_THREAD = Thread.ofPlatform()
			.name("Client Thread")
			.start(INSTANCE);
		RENDER_THREAD = Thread.ofPlatform()
			.name("Render Thread")
			.unstarted(INSTANCE.renderer);
	}

	public static GameClient getInstance() {
		return Objects.requireNonNull(INSTANCE);
	}

	public static Thread getClientThread() {
		return Objects.requireNonNull(CLIENT_THREAD);
	}

	public static Thread getRenderThread() {
		return Objects.requireNonNull(RENDER_THREAD);
	}

	public void setWindowHandle(long windowHandle) {
		this.windowHandle = windowHandle;
	}

	public long getWindowHandle() {
		return this.windowHandle;
	}

	@Override
	public Control runLoop() {
		this.logger.info("Hello, Client!");
		return Control.BREAK;
	}

	@Override
	public void initialize() {
		GLFWErrorCallback.createPrint(System.err).set();

		if (!glfwInit()) {
			throw new IllegalStateException("Failed to initialize GLFW");
		}

		long windowHandle = glfwCreateWindow(
				1280,
				720,
				Constants.NAME,
				0,
				0
		);

		if (windowHandle == 0) {
			throw new IllegalStateException("Failed to create GLFW window");
		}

		GameClient.getInstance().setWindowHandle(windowHandle);
		glfwSetKeyCallback(windowHandle, this::parseInput);

		GameClient.getRenderThread().start();
	}

	public GameRenderer getRenderer() {
		return this.renderer;
	}

	public void parseInput(long window, int key, int scancode, int action, int mods) {
	}
}
