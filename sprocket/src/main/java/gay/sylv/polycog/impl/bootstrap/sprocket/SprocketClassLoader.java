/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.bootstrap.sprocket;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
	final Map<String, byte[]> classesToLoad = new HashMap<>();
	final Map<String, byte[]> resources = new HashMap<>();

	private SprocketClassLoader(String name, ClassLoader parent) {
		super(name, parent);
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		if (this.loadedClasses.containsKey(name)) {
			return this.loadedClasses.get(name);
		} else if (this.classesToLoad.containsKey(name)) {
			byte[] b = this.classesToLoad.get(name);
			this.loadedClasses.put(name, this.defineClass(name, b, 0, b.length));
			this.classesToLoad.remove(name);
			return this.loadedClasses.get(name);
		} else if (name.startsWith("gay.sylv.polycog.impl")
				|| name.startsWith("gay.sylv.polycog.api")) {
			// Ensure we load Polycog's own classes in Sprocket
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

	@Override
	protected Enumeration<URL> findResources(String name) throws IOException {
		if (this.resources.containsKey(name)) {
			return Collections.enumeration(List.of(this.findResource(name)));
		}

		return super.findResources(name);
	}

	@Override
	protected URL findResource(String name) {
		boolean isResource = this.resources.containsKey(name);

		if (!isResource) {
			return super.findResource(name);
		}

		try {
			final String finalName = name;

			if (!name.startsWith("/")) {
				name = "//" + name;
			}

			return URL.of(
					new URI("sprocket", null, name, null), new URLStreamHandler() {
						@Override
						protected URLConnection openConnection(URL u) {
							return new URLConnection(u) {
								@Override
								public void connect() {
								}

								@Override
								public InputStream getInputStream() {
									return new ByteArrayInputStream(resources.get(finalName));
								}
							};
						}
					}
			);
		} catch (MalformedURLException | URISyntaxException e) {
			throw new RuntimeException(e);
		}
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
			final String versioned = "^META-INF\\.versions\\.[0-9]+\\.";
			String entryName = entry.getKey().replaceAll(
					versioned,
					""
			);

			if (entry.getKey().endsWith("module-info")) {
				continue;
			}

			try {
				this.classesToLoad.put(entryName, classes.get(entry.getKey()));
			} catch (NullPointerException error) {
				String otherName = entry.getKey()
						.replace("/", ".")
						.replaceAll(
							versioned,
							""
						);

				if (classes.containsKey(otherName)) {
					stack.push(Map.entry(entryName, entry.getValue()));
					stack.push(Map.entry(otherName, classes.get(otherName)));
				} else {
					IO.println(entryName + " attempted to load " + otherName + " but failed.");
					IO.println("Loader's real name: " + entry.getKey());
					IO.println("Other's real name: " + error.getMessage().replaceAll("\\s*\\(wrong name: ", "")
							.replace(")", "")
							.replace("/", "."));
					IO.println("Are there missing dependencies?");
					IO.println();
					throw error;
				}
			}
		}
	}

	public void addResources(Map<String, byte[]> files) {
		this.resources.putAll(files);
	}

	/// Adds a [Map] of interdependent classes to the [SprocketClassLoader].
	///
	/// @param classes a map of binary class names to bytecode.
	/// @see #defineClass(String, byte[], int, int)
	public void addClasses(Map<String, byte[]> classes) {
		for (Map.Entry<String, byte[]> entry : classes.entrySet()) {
			if (!this.loadedClasses.containsKey(entry.getKey())) {
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
