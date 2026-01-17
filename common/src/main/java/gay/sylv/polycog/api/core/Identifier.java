/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.api.core;

import gay.sylv.polycog.impl.core.IdentifierImpl;

public interface Identifier {
	static Identifier of(String namespace, String path) {
		return new IdentifierImpl(namespace, path);
	}

	static Identifier ofDefault(String path) {
		return of("polycog", path);
	}

	String namespace();

	String path();
}
