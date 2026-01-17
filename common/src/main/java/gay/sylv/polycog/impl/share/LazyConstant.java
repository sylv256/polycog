package gay.sylv.polycog.impl.share;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;

/// Me: Can we get LazyConstant?
///
/// Mom: We have LazyConstant at home.
///
/// The LazyConstant at home:
public sealed class LazyConstant<T> permits LazyConstantList, LazyConstantMap {
	private @Nullable T value;

	protected LazyConstant() {
	}

	public static <T> LazyConstant<T> of() {
		return new LazyConstant<>();
	}

	public static <T> LazyConstantList<T> ofList() {
		return new LazyConstantList<>();
	}

	public static <K, V> LazyConstantMap<K, V> ofMap() {
		return new LazyConstantMap<>();
	}

	public boolean has() {
		return this.value != null;
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

	public void run(Consumer<T> action) {
		if (this.has()) {
			action.accept(this.get());
		}
	}
}
