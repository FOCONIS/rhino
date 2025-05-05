package org.mozilla.javascript;

import java.lang.reflect.TypeVariable;
import java.util.List;

/**
 * @author Roland Praml, Foconis Analytics GmbH
 */
public interface TypeInfoLoader {

    TypeInfo interfaceTypeInfo(Class<?> type, byte functional);

    TypeInfo parameterizedTypeInfo(TypeInfo typeInfo, List<TypeInfo> params);

    TypeInfo of(Class<?> c);

    TypeInfo of(TypeVariable<?> variable);

    TypeInfo arrayTypeInfo(TypeInfo typeInfo);
}
