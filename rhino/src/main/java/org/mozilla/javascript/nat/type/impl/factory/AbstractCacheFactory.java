package org.mozilla.javascript.nat.type.impl.factory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.TypeVariable;
import java.util.Map;
import org.mozilla.javascript.nat.type.TypeInfo;
import org.mozilla.javascript.nat.type.impl.BasicClassTypeInfo;
import org.mozilla.javascript.nat.type.impl.EnumTypeInfo;
import org.mozilla.javascript.nat.type.impl.InterfaceTypeInfo;
import org.mozilla.javascript.nat.type.impl.VariableTypeInfoImpl;

/**
 * Factory, that provides basic cache functionality.
 *
 * <p>The cacheFactory itself is serializable, but does not serialize the cached values
 *
 * @author ZZZank
 */
abstract class AbstractCacheFactory implements FactoryBase, Serializable {
    private static final long serialVersionUID = 1L;
    private transient Map<TypeVariable<?>, VariableTypeInfoImpl> variableCache = createMap();
    private transient Map<Class<?>, BasicClassTypeInfo> basicClassCache = createMap();
    private transient Map<Class<?>, InterfaceTypeInfo> interfaceCache = createMap();
    private transient Map<Class<?>, EnumTypeInfo> enumCache = createMap();

    abstract <K, V> Map<K, V> createMap();

    @Override
    public TypeInfo create(Class<?> clazz) {
        final var predefined = matchPredefined(clazz);
        if (predefined != null) {
            return predefined;
        } else if (clazz.isArray()) {
            return toArray(create(clazz.getComponentType()));
        } else if (clazz.isEnum()) {
            return enumCache.computeIfAbsent(clazz, EnumTypeInfo::new);
        } else if (clazz.isInterface()) {
            return interfaceCache.computeIfAbsent(clazz, InterfaceTypeInfo::new);
        }
        return basicClassCache.computeIfAbsent(clazz, BasicClassTypeInfo::new);
    }

    @Override
    public TypeInfo create(TypeVariable<?> typeVariable) {
        return variableCache.computeIfAbsent(
                typeVariable, raw -> new VariableTypeInfoImpl(raw, this));
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        variableCache = createMap();
        basicClassCache = createMap();
        interfaceCache = createMap();
        enumCache = createMap();
    }
}
