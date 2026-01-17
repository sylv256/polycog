/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
*/

package gay.sylv.polycog.impl.core;

import java.util.Optional;

import org.jspecify.annotations.Nullable;

import gay.sylv.polycog.api.core.Identifier;
import gay.sylv.polycog.api.core.RegistryEntry;

public final class RegistryEntryImpl<T> implements RegistryEntry<T> {
	private @Nullable T value;
	private boolean isBound;
	private Identifier id;
	private Identifier registryId;

	@Override
	public Optional<T> value() {
		return Optional.ofNullable(this.value);
	}

	@Override
	public boolean isBound() {
		return this.isBound;
	}

	@Override
	public Identifier id() {
		return this.id;
	}

	@Override
	public Identifier registryId() {
		return this.registryId;
	}
}
