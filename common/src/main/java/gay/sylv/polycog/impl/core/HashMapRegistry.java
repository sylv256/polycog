/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.impl.core;

import java.util.HashMap;
import java.util.Map;

import gay.sylv.polycog.api.core.Identifier;
import gay.sylv.polycog.api.core.Registry;

public final class HashMapRegistry<T> implements Registry<T> {
	private final Map<Identifier, T> values = new HashMap<>();

	@Override
	public T register(Identifier id, T value) {
		if (this.values.containsKey(id)) {
			throw new IllegalStateException("Registry Entry with ID " + id);
		}

		return this.values.put(id, value);
	}
}
