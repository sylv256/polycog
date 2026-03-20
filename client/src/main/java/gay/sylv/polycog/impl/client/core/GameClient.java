/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.client.core;

import static org.lwjgl.sdl.SDLError.SDL_GetError;
import static org.lwjgl.sdl.SDLHints.SDL_HINT_RENDER_DRIVER;
import static org.lwjgl.sdl.SDLHints.SDL_HINT_VIDEO_DRIVER;
import static org.lwjgl.sdl.SDLHints.SDL_SetHint;
import static org.lwjgl.sdl.SDLInit.SDL_INIT_AUDIO;
import static org.lwjgl.sdl.SDLInit.SDL_INIT_VIDEO;
import static org.lwjgl.sdl.SDLInit.SDL_Init;

import java.util.Objects;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gay.sylv.polycog.api.client.wheel.device.PhysicalDevice;
import gay.sylv.polycog.api.client.wheel.window.Window;
import gay.sylv.polycog.api.core.GameLoop;
import gay.sylv.polycog.impl.client.wheel.GameRenderer;
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
		SDL_SetHint(SDL_HINT_RENDER_DRIVER, "vulkan");

		switch (Platform.get()) {
			case LINUX -> SDL_SetHint(SDL_HINT_VIDEO_DRIVER, "x11");
			case WINDOWS -> {
			}
			default -> throw GameRenderer.unsupported();
		}

		if (!SDL_Init(SDL_INIT_AUDIO | SDL_INIT_VIDEO)) {
			throw handleErrorSDL("Failed to initialize SDL");
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
	}

	public GameRenderer getRenderer() {
		return this.renderer;
	}

	public static SDLException handleErrorSDL(String message) {
		String error = SDL_GetError();

		if (error != null) {
			GameRenderer.LOGGER.error(message);
			return new SDLException(error);
		} else {
			return new SDLException(message);
		}
	}

	public static <X extends Exception> X handleErrorSDL(X exception) {
		String error = SDL_GetError();

		if (error != null) {
			GameRenderer.LOGGER.error("Multiple platform-specific errors have occurred", new SDLException(error));
		}

		return exception;
	}

	public static <T> T handleErrorSDL(T number) {
		if (number == null) {
			throw new SDLException(Objects.requireNonNull(SDL_GetError(), "No SDL error was found, but passed object was null; aborting"));
		}

		return number;
	}

	public static long handleErrorSDL(long number) {
		if (number == 0) {
			throw new SDLException(Objects.requireNonNull(SDL_GetError(), "No SDL error was found, but passed object was null; aborting"));
		}

		return number;
	}
}
