package gay.sylv.polycog.impl.client.wheel;

import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.Nullable;

/// An object with a particular graphical meaning in its implementation. That is,
/// inheritors are not API sugar and may have internal data required by
/// implementations.
@ApiStatus.Internal
@ApiStatus.NonExtendable
public interface RenderObject<T> {
	@ApiStatus.Internal
	default @Nullable T wheel$default() {
		return null;
	}

	@ApiStatus.Internal
	default <I extends T> I wheel$internal() {
		//noinspection unchecked // Checked by caller
		return (I) this;
	}

	@ApiStatus.Internal
	default <I extends T> I wheel$internalChecked() {
		try {
			//noinspection unchecked
			return (I) this;
		} catch (ClassCastException e) {
			T t = this.wheel$default();

			if (t == null) {
				throw e;
			} else {
				//noinspection unchecked
				return (I) t;
			}
		}
	}
}
