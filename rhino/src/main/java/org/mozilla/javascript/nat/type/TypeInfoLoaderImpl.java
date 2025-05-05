package org.mozilla.javascript.nat.type;

import java.lang.reflect.TypeVariable;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import org.mozilla.javascript.TypeInfo;
import org.mozilla.javascript.TypeInfoLoader;
import org.mozilla.javascript.nat.ByteAsBool;

/**
 * @author Roland Praml, Foconis Analytics GmbH
 */
public class TypeInfoLoaderImpl implements TypeInfoLoader {

    @Override
    public TypeInfo interfaceTypeInfo(Class<?> type, byte functional) {
        return new InterfaceTypeInfo(type, functional);
    }

    public TypeInfo parameterizedTypeInfo(TypeInfo typeInfo, List<TypeInfo> params) {
        return new ParameterizedTypeInfo(typeInfo, params);
    }

    @Override
    public TypeInfo arrayTypeInfo(TypeInfo typeInfo) {
        return new ArrayTypeInfo(typeInfo);
    }

    TypeInfo NONE = NoTypeInfo.INSTANCE;

    TypeInfo[] EMPTY_ARRAY = new TypeInfo[0];

    /**
     * use {@link TypeInfo#isObjectExact()} to determine whether a type represents a {@link Object}
     * class, using `typeInfo == TypeInfo.OBJECT` might cause problem with VariableTypeInfo
     */
    TypeInfo OBJECT = new BasicClassTypeInfo(Object.class);

    TypeInfo OBJECT_ARRAY = OBJECT.asArray();

    TypeInfo PRIMITIVE_VOID = new PrimitiveClassTypeInfo(Void.TYPE, null);
    TypeInfo PRIMITIVE_BOOLEAN = new PrimitiveClassTypeInfo(Boolean.TYPE, false);
    TypeInfo PRIMITIVE_BYTE = new PrimitiveClassTypeInfo(Byte.TYPE, (byte) 0);
    TypeInfo PRIMITIVE_SHORT = new PrimitiveClassTypeInfo(Short.TYPE, (short) 0);
    TypeInfo PRIMITIVE_INT = new PrimitiveClassTypeInfo(Integer.TYPE, 0);
    TypeInfo PRIMITIVE_LONG = new PrimitiveClassTypeInfo(Long.TYPE, 0L);
    TypeInfo PRIMITIVE_FLOAT = new PrimitiveClassTypeInfo(Float.TYPE, 0F);
    TypeInfo PRIMITIVE_DOUBLE = new PrimitiveClassTypeInfo(Double.TYPE, 0D);
    TypeInfo PRIMITIVE_CHARACTER = new PrimitiveClassTypeInfo(Character.TYPE, (char) 0);

    TypeInfo VOID = new BasicClassTypeInfo(Void.class);
    TypeInfo BOOLEAN = new BasicClassTypeInfo(Boolean.class);
    TypeInfo BYTE = new BasicClassTypeInfo(Byte.class);
    TypeInfo SHORT = new BasicClassTypeInfo(Short.class);
    TypeInfo INT = new BasicClassTypeInfo(Integer.class);
    TypeInfo LONG = new BasicClassTypeInfo(Long.class);
    TypeInfo FLOAT = new BasicClassTypeInfo(Float.class);
    TypeInfo DOUBLE = new BasicClassTypeInfo(Double.class);
    TypeInfo CHARACTER = new BasicClassTypeInfo(Character.class);

    TypeInfo NUMBER = new BasicClassTypeInfo(Number.class);
    TypeInfo STRING = new BasicClassTypeInfo(String.class);
    TypeInfo STRING_ARRAY = STRING.asArray();
    TypeInfo RAW_CLASS = new BasicClassTypeInfo(Class.class);
    TypeInfo DATE = new BasicClassTypeInfo(Date.class);

    TypeInfo RUNNABLE = new InterfaceTypeInfo(Runnable.class, ByteAsBool.TRUE);
    TypeInfo RAW_CONSUMER = new InterfaceTypeInfo(Consumer.class, ByteAsBool.TRUE);
    TypeInfo RAW_SUPPLIER = new InterfaceTypeInfo(Supplier.class, ByteAsBool.TRUE);
    TypeInfo RAW_FUNCTION = new InterfaceTypeInfo(Function.class, ByteAsBool.TRUE);
    TypeInfo RAW_PREDICATE = new InterfaceTypeInfo(Predicate.class, ByteAsBool.TRUE);

    TypeInfo RAW_LIST = new InterfaceTypeInfo(List.class, ByteAsBool.FALSE);
    TypeInfo RAW_SET = new InterfaceTypeInfo(Set.class, ByteAsBool.FALSE);
    TypeInfo RAW_MAP = new InterfaceTypeInfo(Map.class, ByteAsBool.FALSE);
    TypeInfo RAW_OPTIONAL = new BasicClassTypeInfo(Optional.class);
    TypeInfo RAW_ENUM_SET = new BasicClassTypeInfo(EnumSet.class);

    @Override
    public TypeInfo of(Class<?> c) {
        if (c == null) {
            return NONE;
        } else if (c == Object.class) {
            return OBJECT;
        }
        if (c.isPrimitive()) {
            if (c == Void.TYPE) {
                return PRIMITIVE_VOID;
            } else if (c == Boolean.TYPE) {
                return PRIMITIVE_BOOLEAN;
            } else if (c == Byte.TYPE) {
                return PRIMITIVE_BYTE;
            } else if (c == Short.TYPE) {
                return PRIMITIVE_SHORT;
            } else if (c == Integer.TYPE) {
                return PRIMITIVE_INT;
            } else if (c == Long.TYPE) {
                return PRIMITIVE_LONG;
            } else if (c == Float.TYPE) {
                return PRIMITIVE_FLOAT;
            } else if (c == Double.TYPE) {
                return PRIMITIVE_DOUBLE;
            } else if (c == Character.TYPE) {
                return PRIMITIVE_CHARACTER;
            }
        }
        if (c == Void.class) {
            return VOID;
        } else if (c == Boolean.class) {
            return BOOLEAN;
        } else if (c == Byte.class) {
            return BYTE;
        } else if (c == Short.class) {
            return SHORT;
        } else if (c == Integer.class) {
            return INT;
        } else if (c == Long.class) {
            return LONG;
        } else if (c == Float.class) {
            return FLOAT;
        } else if (c == Double.class) {
            return DOUBLE;
        } else if (c == Character.class) {
            return CHARACTER;
        } else if (c == Number.class) {
            return NUMBER;
        } else if (c == String.class) {
            return STRING;
        } else if (c == Class.class) {
            return RAW_CLASS;
        } else if (c == Date.class) {
            return DATE;
        } else if (c == Optional.class) {
            return RAW_OPTIONAL;
        } else if (c == EnumSet.class) {
            return RAW_ENUM_SET;
        } else if (c == Runnable.class) {
            return RUNNABLE;
        } else if (c == Consumer.class) {
            return RAW_CONSUMER;
        } else if (c == Supplier.class) {
            return RAW_SUPPLIER;
        } else if (c == Function.class) {
            return RAW_FUNCTION;
        } else if (c == Predicate.class) {
            return RAW_PREDICATE;
        } else if (c == List.class) {
            return RAW_LIST;
        } else if (c == Set.class) {
            return RAW_SET;
        } else if (c == Map.class) {
            return RAW_MAP;
        } else if (c == Object[].class) {
            return OBJECT_ARRAY;
        } else if (c == String[].class) {
            return STRING_ARRAY;
        } else if (c.isArray()) {
            return of(c.getComponentType()).asArray();
        } else if (c.isEnum()) {
            synchronized (EnumTypeInfo.CACHE) {
                return EnumTypeInfo.CACHE.computeIfAbsent(c, EnumTypeInfo::new);
            }
        } else if (c.isInterface()) {
            synchronized (InterfaceTypeInfo.CACHE) {
                return InterfaceTypeInfo.CACHE.computeIfAbsent(c, InterfaceTypeInfo::new);
            }
        }
        synchronized (BasicClassTypeInfo.CACHE) {
            return BasicClassTypeInfo.CACHE.computeIfAbsent(c, BasicClassTypeInfo::new);
        }
    }

    @Override
    public VariableTypeInfo of(TypeVariable<?> variable) {
        synchronized (VariableTypeInfo.CACHE) {
            return VariableTypeInfo.CACHE.computeIfAbsent(variable, VariableTypeInfo::new);
        }
    }
}
