package gay.sylv.polycog.impl.share;

import java.util.Map;
import java.util.function.BiConsumer;

public final class LazyConstantMap<K, V> extends LazyConstant<Map<K, V>> {
	public void forEach(BiConsumer<K, V> action) {
		if (this.has()) {
			for (Map.Entry<K, V> entry : this.get().entrySet()) {
				action.accept(entry.getKey(), entry.getValue());
			}
		}
	}
}
