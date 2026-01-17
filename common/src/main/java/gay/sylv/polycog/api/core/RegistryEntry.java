/*
 * Polycog
 * Copyright (c) 2026 Sylv
 *
 * All Rights Reserved
 */

package gay.sylv.polycog.api.core;

import java.util.Optional;

/// An entry in a [Registry].
///
/// @param <T> the type of the value this entry refers to.
public interface RegistryEntry<T> {
	/// @return a value if bound, otherwise [Optional#empty()].
	Optional<T> value();

	/// @return whether the value of this entry is bound to a value in a
	/// [Registry].
	boolean isBound();

	/// @return the [Identifier] of this entry in a [Registry].
	Identifier id();

	/// @return the [Identifier] of the [Registry] this entry is bound to.
	Identifier registryId();
}
