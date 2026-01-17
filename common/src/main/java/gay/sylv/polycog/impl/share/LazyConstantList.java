package gay.sylv.polycog.impl.share;

import java.util.List;
import java.util.function.Consumer;

public final class LazyConstantList<T> extends LazyConstant<List<T>> {
	public void forEach(Consumer<T> action) {
		if (this.has()) {
			for (T t : this.get()) {
				action.accept(t);
			}
		}
	}
}
