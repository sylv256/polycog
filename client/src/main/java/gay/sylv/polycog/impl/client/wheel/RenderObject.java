package gay.sylv.polycog.impl.client.wheel;

import org.jetbrains.annotations.ApiStatus;

/// An object with a particular graphical meaning in its implementation. That is,
/// inheritors are not API sugar and may have internal data required by
/// implementations.
@ApiStatus.Internal
@ApiStatus.NonExtendable
public interface RenderObject<T> {
	@ApiStatus.Internal
	default <I extends T> I wheel$impl() {
		//noinspection unchecked // Checked by caller
		return (I) this;
	}
}
