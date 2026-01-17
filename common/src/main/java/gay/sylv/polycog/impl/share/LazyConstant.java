package gay.sylv.polycog.impl.share;

import java.util.Objects;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/// Me: Can we get LazyConstant?
///
/// Mom: We have LazyConstant at home.
///
/// The LazyConstant at home:
public final class LazyConstant<T> {
	private @Nullable T value;

	private LazyConstant() {
	}

	public static <T> LazyConstant<T> of() {
		return new LazyConstant<>();
	}

	public T get() {
		return Objects.requireNonNull(
				this.value,
				"LazyConstant is uninitialized"
		);
	}

	public void set(T value) {
		if (this.value != null) {
			throw new IllegalStateException("LazyConstant cannot be initialized twice");
		}

		this.value = Objects.requireNonNull(value);
	}

	public T getOrSet(Supplier<T> supplier) {
		if (this.value == null) {
			this.value = supplier.get();
		}

		return this.value;
	}
}
