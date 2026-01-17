/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
*/

package gay.sylv.polycog.impl.bootstrap.sprocket;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Stack;

/// A [ClassLoader] that lets you do awful things like loading new classes with
/// arbitrary bytecode.
///
/// This will go perfectly fine, and there will be zero security vulnerabilities
/// related to this class. :)
public final class SprocketClassLoader extends ClassLoader {
	public static final SprocketClassLoader INSTANCE =
		new SprocketClassLoader(
				"Sprocket Class Loader",
				SprocketClassLoader.class.getClassLoader()
		);

	private final Map<String, Class<?>> loadedClasses = new HashMap<>();
	Set<String> classesToLoad = Set.of();

	private SprocketClassLoader(String name, ClassLoader parent) {
		super(name, parent);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		// Ensure we load PolyCog's own classes in Sprocket
		if (this.findLoadedClass(name) == null && name.startsWith("gay.sylv.polycog")) {
			String path = name.replace(".", "/") + ".class";

			try (InputStream inputStream =
						this.getParent().getResourceAsStream(path)) {
				Objects.requireNonNull(inputStream);
				return this.addClass(name, inputStream.readAllBytes());
			} catch (IOException | NullPointerException e) {
				throw new ClassNotFoundException(name, e);
			}
		}

		return super.loadClass(name);
	}

	private Class<?> addClass(String name, byte[] bytes) {
		Class<?> cls = this.defineClass(name, bytes, 0, bytes.length);
		this.loadedClasses.put(name, cls);
		return cls;
	}

	/// @see #defineClass(String, byte[], int, int)
	private void defineClasses(String name, byte[] bytes, Map<String, byte[]> classes)
			throws ClassFormatError {
		// #BanRecursion
		Stack<Map.Entry<String, byte[]>> stack = new Stack<>();
		stack.push(Map.entry(name, bytes));

		while (!stack.empty()) {
			Map.Entry<String, byte[]> entry = stack.pop();
			final String versionedPattern = "^META-INF\\.versions\\.[0-9]+\\.";
			String entryName = entry.getKey().replaceAll(
					versionedPattern,
					""
			);

			if (entry.getKey().endsWith("module-info")) {
				continue;
			}

			try {
				this.addClass(entryName, entry.getValue());
			} catch (NoClassDefFoundError error) {
				String otherName = error.getMessage()
						.replace("/", ".")
						.replaceAll(
							versionedPattern,
							""
						);

				if (classes.containsKey(otherName)) {
					stack.push(Map.entry(entryName, entry.getValue()));
					stack.push(Map.entry(otherName, classes.get(otherName)));
				} else {
					IO.println(entryName + " attempted to load " + otherName + " but failed.");
					IO.println("Loader's real name: " + entry.getKey());
					IO.println("Other's real name: " + error.getMessage()
							.replace("/", "."));
					IO.println("Are there missing dependencies?");
					IO.println();
					throw error;
				}
			}
		}
	}

	/// Adds a [Map] of interdependent classes to the [SprocketClassLoader].
	///
	/// @param classes a map of binary class names to bytecode.
	/// @see #defineClass(String, byte[], int, int)
	public void addClasses(Map<String, byte[]> classes) {
		for (Map.Entry<String, byte[]> entry : classes.entrySet()) {
			if (!this.classesToLoad.contains(entry.getKey())) {
				this.defineClasses(entry.getKey(), entry.getValue(), classes);
			}
		}
	}

	@Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
		final Class<?> cls = this.loadedClasses.get(name);

		if (cls != null) {
			return cls;
		}

		return super.findClass(name);
	}

	@Override
	protected Class<?> findClass(String moduleName, String name) {
		try {
			return this.findClass(name);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
}
