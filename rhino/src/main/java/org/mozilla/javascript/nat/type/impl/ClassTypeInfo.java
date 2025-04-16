package org.mozilla.javascript.nat.type.impl;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import org.mozilla.javascript.nat.type.TypeFormatContext;
import org.mozilla.javascript.nat.type.TypeInfo;
import org.mozilla.javascript.nat.type.TypeInfoFactory;
import org.mozilla.javascript.nat.type.VariableTypeInfo;

public abstract class ClassTypeInfo extends TypeInfoBase {
    private final Class<?> type;

    ClassTypeInfo(Class<?> type) {
        this.type = type;
    }

    @Override
    public final Class<?> asClass() {
        return type;
    }

    @Override
    public boolean is(Class<?> c) {
        return type == c;
    }

    @Override
    public boolean shouldConvert() {
        return type != Object.class;
    }

    @Override
    public int hashCode() {
        return type.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return (o == this) || ((o instanceof ClassTypeInfo) && (type == ((ClassTypeInfo) o).type));
    }

    @Override
    public void append(TypeFormatContext ctx, StringBuilder builder) {
        builder.append(ctx.getClassName(this.type));
    }

    TypeInfo resolve(Class<?> iFace) {
        assert iFace.isInterface();
        Class cls = type;

        while (cls != Object.class) {
            for (Type iType : cls.getGenericInterfaces()) {
                if (iType instanceof ParameterizedType) {
                    ParameterizedType pType = (ParameterizedType) iType;
                    if (iFace.isAssignableFrom((Class) pType.getRawType())) {
                        return TypeInfoFactory.NO_CACHE.create(pType);
                    }
                }
            }
            Type iType = cls.getGenericSuperclass();
            if (iType instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) iType;
                if (iFace.isAssignableFrom((Class) pType.getRawType())) {
                    return TypeInfoFactory.NO_CACHE.create(pType);
                }
            }
            cls = cls.getSuperclass();
        }
        return NONE;
    }

    @Override
    public TypeInfo resolveBound(Class<?> iFace, int index) {
        TypeInfo param = resolve(iFace).param(index);
        if (param instanceof VariableTypeInfo) {
            return ((VariableTypeInfo) param).mainBound();
        }
        return param;
    }
}
