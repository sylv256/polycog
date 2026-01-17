/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.bootstrap.sprocket;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/// Cursed hacks for bootstrapping that will make you cry.
public final class Sprocket {
	/// Initializes [SprocketClassLoader] and loads all Jar-in-Jar dependencies
	/// via [#loadJarsInJar()].
	public static void bootstrap(Class<?> mainClass) {
		try {
			// Don't load Jar-in-Jar dependencies in development environments
			// as they are already loaded.
			if (!"1".equals(System.getProperty("polycog.debug.ide"))) {
				Sprocket.loadJarsInJar();
			}
		} catch (NoClassDefFoundError | RuntimeException e) {
			IO.println("The game's dependencies are not loaded correctly; the game JAR is either corrupted or malformed!");
			IO.println("This should never happen, but if you see this, please report this bug on our bug tracker.");
			IO.println("https://github.com/sylv256/polycog/issues\n");

			if (e instanceof RuntimeException runtimeException) {
				throw runtimeException;
			} else {
				throw new RuntimeException(e);
			}
		}

		try {
			Thread.currentThread().setName("Common Thread");
			SprocketClassLoader.INSTANCE.loadClass(mainClass.getName())
					.getDeclaredMethod("postMain")
					.invoke(null);
		} catch (IllegalAccessException | InvocationTargetException
				| NoSuchMethodException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	/// Loads all nested JARs into the [SprocketClassLoader].
	public static void loadJarsInJar() {
		final Map<String, byte[]> classBytes = new HashMap<>();

		for (String jar : findNestedJars()) {
			scanNestedJar("META-INF/jars/" + jar, classBytes);
		}

		SprocketClassLoader.INSTANCE.addClasses(classBytes);
	}

	/// Finds all JARs nested in this JAR.
	/// The JARs are located in `META-INF/jars`, and they are defined in a
	/// LF-separated UTF-8 file of JAR file names located at
	/// `META-INF/jars/files.txt`.
	public static List<String> findNestedJars() {
		List<String> nestedJars;
		ClassLoader classLoader = Sprocket.class.getClassLoader();

		try (InputStream inputStream =
				classLoader.getResourceAsStream("META-INF/jars/files.txt")) {
			Objects.requireNonNull(inputStream);
			String content = new String(inputStream.readAllBytes());
			nestedJars = new ArrayList<>(Arrays.asList(content.split("\\n")));

			if (nestedJars.getLast().isEmpty()) {
				nestedJars.removeLast(); // remove trailing LF
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} catch (NullPointerException _) {
			return List.of();
		}

		return List.copyOf(nestedJars);
	}

	/// Scans a nested JAR for classes and adds them to
	/// [SprocketClassLoader#classesToLoad].
	public static void scanNestedJar(
			String nestedJar,
			Map<String, byte[]> classBytes
	) {
		// Get JAR File
		ClassLoader classLoader = Sprocket.class.getClassLoader();

		try (JarInputStream jis = new JarInputStream(
				Objects.requireNonNull(classLoader.getResourceAsStream(nestedJar)))) {
			// Extract Classes
			JarEntry jarEntry = jis.getNextJarEntry();

			while (jarEntry != null) {
				String name = jarEntry.getName();

				if (name.endsWith(".class") && !name.equals("module-info.class")) {
					classBytes.put(
							name
								.replace(".class", "")
								.replace("/", "."),
							jis.readAllBytes()
					);
				}

				jarEntry = jis.getNextJarEntry();
			}
		} catch (IOException e) {
			IO.println("Error reading Nested Jar " + nestedJar);
			throw new RuntimeException(e);
		}

		SprocketClassLoader.INSTANCE.classesToLoad = classBytes.keySet();
	}
}
