/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
*/

package gay.sylv.polycog.api.core;

import gay.sylv.polycog.impl.core.HashMapRegistry;

/// A central object containing maps of [Identifier] to values of type [T].
public interface Registry<T> {
	static <T> Registry<T> of() {
		return new HashMapRegistry<>();
	}

	T register(Identifier id, T value);
}
