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
import static org.lwjgl.sdl.SDLInit.SDL_Quit;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

import org.jspecify.annotations.Nullable;
import org.lwjgl.system.Configuration;
import org.lwjgl.system.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gay.sylv.polycog.api.client.wheel.device.GpuDevice;
import gay.sylv.polycog.api.client.wheel.window.Window;
import gay.sylv.polycog.api.core.GameLoop;
import gay.sylv.polycog.impl.client.wheel.GameRenderer;
import gay.sylv.polycog.impl.share.Constants;
import gay.sylv.polycog.impl.share.LazyConstant;

public final class GameClient implements GameLoop, Executor {
	public final Logger logger;
	private @Nullable Window window;
	private final GameRenderer renderer;
	private static @Nullable GameClient INSTANCE;
	private static @Nullable Thread CLIENT_THREAD;
	private static @Nullable Thread RENDER_THREAD;
	private final LazyConstant<Instant> stop = LazyConstant.of();
	private static final List<Runnable> TASKS = new ArrayList<>();

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
		runTasks();

		if (this.isQuitting()) {
			// Wait until the render thread is done before terminating.
			while (getRenderThread().isAlive()) {
				runTasks();
			}

			return Control.BREAK;
		}

		return Control.CONTINUE;
	}

	private static void runTasks() {
		for (Runnable runnable : List.copyOf(TASKS)) {
			runnable.run();
			TASKS.remove(runnable);
		}
	}

	@Override
	public void endLoop() {
		SDL_Quit();
	}

	public boolean isQuitting() {
		return this.stop.get().isBefore(Instant.now());
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

		this.stop.set(Instant.now().plusSeconds(5));
		this.logger.info("Hello, Client!");
		GameClient.getRenderThread().start();
	}

	public void initializeWindow(GpuDevice device) {
		this.window = Window.of(
				device,
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

	/// A version of [#callSDL(Callable, String...)] that fails if `false` is
	/// returned.
	///
	/// @see #callSDL(Callable, String...)
	public static void checkCallSDL(Callable<Boolean> callable, String... messages) throws SDLException {
		callSDL(() -> {
			if (!callable.call()) {
				return null;
			}

			return true;
		}, messages);
	}

	/// A version of [#callSDL(Callable, String...)] that does not return a
	/// value.
	///
	/// @see #callSDL(Callable, String...)
	public static void callSDL(Runnable runnable, String... messages) throws SDLException {
		callSDL(() -> {
			runnable.run();
			return true;
		}, messages);
	}

	/// Perform an SDL call on the main thread.
	///
	/// @param callable a call to an SDL function that must be performed on the
	/// main thread. Returning `null` will result in a failure.
	/// @param messages messages used as the reason for failure in the exception.
	/// @throws SDLException if the operation failed
	/// @see #checkCallSDL(Callable, String...)
	/// @see #callSDL(Runnable, String...)
	public static <T> T callSDL(Callable<@Nullable T> callable, String... messages) throws SDLException {
		try {
			AtomicReference<@Nullable T> value = new AtomicReference<>();
			GameClient.getInstance().executeBlocking(() -> {
				try {
					value.set(callable.call());
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			});
			T val = value.get();

			if (val == null) {
				if (messages.length > 0) {
					throw handleErrorSDL(String.join("\n", messages));
				} else {
					throw handleErrorSDL("An SDL call failed");
				}
			}

			return val;
		} catch (RuntimeException re) {
			Throwable t = re.getCause();

			if (t == null) {
				throw re;
			} else if (t instanceof SDLException se) {
				throw se;
			}

			throw new SDLException(t);
		}
	}

	@Override
	public void execute(Runnable command) {
		if (!Thread.currentThread().equals(getClientThread())) {
			TASKS.add(command);
		} else {
			command.run();
		}
	}

	public void executeBlocking(Runnable command) {
		this.execute(command);

		if (!Thread.currentThread().equals(getClientThread())) {
			// We're waiting until the command is run.
			//noinspection LoopConditionNotUpdatedInsideLoop,StatementWithEmptyBody
			while (TASKS.contains(command)) {
			}
		}
	}
}
