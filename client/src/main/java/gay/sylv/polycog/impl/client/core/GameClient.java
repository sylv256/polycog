/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.client.core;

import static org.lwjgl.glfw.GLFW.GLFW_PLATFORM;
import static org.lwjgl.glfw.GLFW.GLFW_PLATFORM_WIN32;
import static org.lwjgl.glfw.GLFW.GLFW_PLATFORM_X11;
import static org.lwjgl.glfw.GLFW.GLFW_TRUE;
import static org.lwjgl.glfw.GLFW.GLFW_X11_XCB_VULKAN_SURFACE;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwInitHint;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gay.sylv.polycog.api.client.wheel.device.PhysicalDevice;
import gay.sylv.polycog.api.client.wheel.window.Window;
import gay.sylv.polycog.api.core.GameLoop;
import gay.sylv.polycog.impl.client.wheel.GameRenderer;
import gay.sylv.polycog.impl.client.wheel.vulkan.window.VkWindow;
import gay.sylv.polycog.impl.share.Constants;

public final class GameClient implements GameLoop {
	public final Logger logger;
	private @Nullable Window window;
	private final GameRenderer renderer;
	private static @Nullable GameClient INSTANCE;
	private static @Nullable Thread CLIENT_THREAD;
	private static @Nullable Thread RENDER_THREAD;

	private GameClient() {
		this.logger = LoggerFactory.getLogger("Polycog/Client");
		Configuration.MEMORY_ALLOCATOR.set("jemalloc");
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

	public Window getWindow() {
		return Objects.requireNonNull(this.window, "Window has not yet been initialized");
	}

	@Override
	public Control runLoop() {
		this.logger.info("Hello, Client!");
		return Control.BREAK;
	}

	@Override
	public void initialize() {
		GLFWErrorCallback.createPrint(new PrintStream(new OutputStream() {
			@Override
			public void write(
					byte[] b,
					int off,
					int len
			) {
				GameClient.this.logger.error(new String(b, off, len, StandardCharsets.UTF_8));
			}

			@Override
			public void write(int b) {
				GameClient.this.logger.atError()
						.setMessage(Character.toString(b))
						.log();
			}
		})).set();

		switch (Platform.get()) {
			case LINUX -> {
				glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_X11);
				glfwInitHint(GLFW_X11_XCB_VULKAN_SURFACE, GLFW_TRUE);
			}
			case WINDOWS -> glfwInitHint(GLFW_PLATFORM, GLFW_PLATFORM_WIN32);
			default -> throw GameRenderer.unsupported();
		}

		if (!glfwInit()) {
			throw new IllegalStateException("Failed to initialize GLFW");
		}

		GameClient.getRenderThread().start();
	}

	public void initializeWindow(PhysicalDevice physicalDevice) {
		this.window = Window.of(
				physicalDevice,
				1280,
				720,
				Constants.NAME
		);
		glfwSetKeyCallback(
				this.window.<VkWindow>wheel$impl().getVkHandle(),
				this::parseInput
		);
	}

	public GameRenderer getRenderer() {
		return this.renderer;
	}

	public void parseInput(long window, int key, int scancode, int action, int mods) {
	}
}
