package org.mozilla.javascript.nat.type.impl.factory;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Factory, that uses a WeakHashMap, so that the garbage collection can collect unused cache keys.
 *
 * @author ZZZank
 */
public final class WeakReferenceFactory extends AbstractCacheFactory {

    @Override
    <K, V> Map<K, V> createMap() {
        return Collections.synchronizedMap(new WeakHashMap<>());
    }
}
