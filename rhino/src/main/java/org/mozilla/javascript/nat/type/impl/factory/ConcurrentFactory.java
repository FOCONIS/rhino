package org.mozilla.javascript.nat.type.impl.factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory, that uses a ConcurrentHashMap. This should be attached to the scope only and not to a
 * static value.
 *
 * @author ZZZank
 */
public class ConcurrentFactory extends AbstractCacheFactory {
    @Override
    <K, V> Map<K, V> createMap() {
        return new ConcurrentHashMap<K, V>();
    }
}
