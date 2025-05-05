package org.mozilla.javascript;

import java.lang.reflect.Array;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * A representation of Java type, aiming at preserving more type information than what a {@link
 * Class} can provide
 *
 * @see TypeInfoExt : more predefined TypeInfo
 * @see #asClass() : how to convert TypeInfo back to Class
 * @see #is(Class) : how to determine whether a TypeInfo represents a specific class
 */
public interface TypeInfo {

    TypeInfoLoader loader =
            Objects.requireNonNull(
                    ScriptRuntime.loadOneServiceImplementation(TypeInfoLoader.class),
                    "TypeInfoLoader not found");
    TypeInfo NONE = loader.of((Class<?>) null);

    TypeInfo[] EMPTY_ARRAY = new TypeInfo[0];

    /**
     * use {@link TypeInfo#isObjectExact()} to determine whether a type represents a {@link Object}
     * class, using `typeInfo == TypeInfo.OBJECT` might cause problem with VariableTypeInfo
     */
    TypeInfo OBJECT = loader.of(Object.class);

    TypeInfo OBJECT_ARRAY = OBJECT.asArray();

    TypeInfo PRIMITIVE_VOID = loader.of(Void.TYPE);
    TypeInfo PRIMITIVE_BOOLEAN = loader.of(Boolean.TYPE);
    TypeInfo PRIMITIVE_BYTE = loader.of(Byte.TYPE);
    TypeInfo PRIMITIVE_SHORT = loader.of(Short.TYPE);
    TypeInfo PRIMITIVE_INT = loader.of(Integer.TYPE);
    TypeInfo PRIMITIVE_LONG = loader.of(Long.TYPE);
    TypeInfo PRIMITIVE_FLOAT = loader.of(Float.TYPE);
    TypeInfo PRIMITIVE_DOUBLE = loader.of(Double.TYPE);
    TypeInfo PRIMITIVE_CHARACTER = loader.of(Character.TYPE);

    TypeInfo VOID = loader.of(Void.class);
    TypeInfo BOOLEAN = loader.of(Boolean.class);
    TypeInfo BYTE = loader.of(Byte.class);
    TypeInfo SHORT = loader.of(Short.class);
    TypeInfo INT = loader.of(Integer.class);
    TypeInfo LONG = loader.of(Long.class);
    TypeInfo FLOAT = loader.of(Float.class);
    TypeInfo DOUBLE = loader.of(Double.class);
    TypeInfo CHARACTER = loader.of(Character.class);

    TypeInfo NUMBER = loader.of(Number.class);
    TypeInfo STRING = loader.of(String.class);
    TypeInfo STRING_ARRAY = STRING.asArray();
    TypeInfo RAW_CLASS = loader.of(Class.class);
    TypeInfo DATE = loader.of(Date.class);

    TypeInfo RUNNABLE = loader.interfaceTypeInfo(Runnable.class, ByteAsBool.TRUE);
    TypeInfo RAW_CONSUMER = loader.interfaceTypeInfo(Consumer.class, ByteAsBool.TRUE);
    TypeInfo RAW_SUPPLIER = loader.interfaceTypeInfo(Supplier.class, ByteAsBool.TRUE);
    TypeInfo RAW_FUNCTION = loader.interfaceTypeInfo(Function.class, ByteAsBool.TRUE);
    TypeInfo RAW_PREDICATE = loader.interfaceTypeInfo(Predicate.class, ByteAsBool.TRUE);

    TypeInfo RAW_LIST = loader.interfaceTypeInfo(List.class, ByteAsBool.FALSE);
    TypeInfo RAW_SET = loader.interfaceTypeInfo(Set.class, ByteAsBool.FALSE);
    TypeInfo RAW_MAP = loader.interfaceTypeInfo(Map.class, ByteAsBool.FALSE);
    TypeInfo RAW_OPTIONAL = loader.of(Optional.class);
    TypeInfo RAW_ENUM_SET = loader.of(EnumSet.class);

    Class<?> asClass();

    default TypeInfo param(int index) {
        return NONE;
    }

    /**
     * @return true if this TypeInfo represents the same class as the {@link Class} parameter, false
     *     otherwise
     * @see #isNot(Class)
     */
    default boolean is(Class<?> c) {
        return asClass() == c;
    }

    /**
     * @return false if this TypeInfo does not represent the same class as the {@link Class}
     *     parameter, true otherwise
     * @see #is(Class)
     */
    default boolean isNot(Class<?> c) {
        return !is(c);
    }

    default boolean isPrimitive() {
        return false;
    }

    default boolean shouldConvert() {
        return true;
    }

    static TypeInfo of(Class<?> c) {
        return loader.of(c);
    }

    static TypeInfo of(TypeVariable<?> variable) {
        return loader.of(variable);
    }

    static TypeInfo of(Type type) {
        if (type instanceof Class<?>) {
            Class<?> clz = (Class<?>) type;
            return of(clz);
        } else if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            return of(paramType.getRawType())
                    .withParams(ofArray(paramType.getActualTypeArguments()));
        } else if (type instanceof GenericArrayType) {
            GenericArrayType arrType = (GenericArrayType) type;
            return of(arrType.getGenericComponentType()).asArray();
        } else if (type instanceof TypeVariable<?>) {
            TypeVariable<?> variable = (TypeVariable<?>) type;
            return of(variable);
        } else if (type instanceof WildcardType) {
            WildcardType wildcard = (WildcardType) type;
            var upper = wildcard.getUpperBounds();
            if (upper.length != 0 && upper[0] != Object.class) {
                return of(upper[0]);
            }

            var lower = wildcard.getLowerBounds();
            if (lower.length != 0) {
                return of(lower[0]);
            }
        }
        return NONE;
    }

    static TypeInfo[] ofArray(Type[] array) {
        if (array.length == 0) {
            return EMPTY_ARRAY;
        }
        var len = array.length;
        var arr = new TypeInfo[len];
        for (int i = 0; i < len; i++) {
            arr[i] = of(array[i]);
        }
        return arr;
    }

    default String signature() {
        return toString();
    }

    /**
     * @see #append(TypeFormatContext, StringBuilder)
     */
    @Override
    String toString();

    void append(TypeFormatContext ctx, StringBuilder builder);

    /**
     * @see Class#getComponentType()
     */
    default TypeInfo getComponentType() {
        return NONE;
    }

    /** get an array whose element type is the caller TypeInfo */
    default Object newArray(int length) {
        return Array.newInstance(asClass(), length);
    }

    /**
     * get an array TypeInfo whose component type is the caller TypeInfo
     *
     * @see #getComponentType()
     */
    default TypeInfo asArray() {
        return loader.arrayTypeInfo(this);
    }

    /**
     * get a parameterized TypeInfo whose raw type is the caller TypeInfo, with parameters provided
     * by the `params` arg
     *
     * @see #param(int)
     */
    default TypeInfo withParams(TypeInfo... params) {
        if (params.length == 0) {
            return this;
        }

        return loader.parameterizedTypeInfo(this, List.of(params));
    }

    /**
     * @see FunctionalInterface
     */
    default boolean isFunctionalInterface() {
        return false;
    }

    /**
     * @see Class#getEnumConstants()
     */
    default List<Object> enumConstants() {
        return List.of();
    }

    default Object createDefaultValue() {
        return null;
    }

    /**
     * @return true if this TypeInfo represents {@link Void} class or {@code void} class
     */
    default boolean isVoid() {
        return false;
    }

    /**
     * @return true if this TypeInfo represents {@link Boolean} class or {@code boolean} class
     */
    default boolean isBoolean() {
        return false;
    }

    /**
     * @return true if this TypeInfo represents a type assignable to {@link Number} class
     * @see #isAssignableFrom(TypeInfo)
     */
    default boolean isNumber() {
        // the implementation here does not look like other `isXXX()` method because Number is not a
        // final class, so we cannot match type directly
        return Number.class.isAssignableFrom(asClass());
    }

    /**
     * @return true if this TypeInfo represents {@link Byte} class or {@code byte} class
     */
    default boolean isByte() {
        return false;
    }

    /**
     * @return true if this TypeInfo represents {@link Short} class or {@code short} class
     */
    default boolean isShort() {
        return false;
    }

    /**
     * @return true if this TypeInfo represents {@link Integer} class or {@code int} class
     */
    default boolean isInt() {
        return false;
    }

    /**
     * @return true if this TypeInfo represents {@link Long} class or {@code long} class
     */
    default boolean isLong() {
        return false;
    }

    /**
     * @return true if this TypeInfo represents {@link Float} class or {@code float} class
     */
    default boolean isFloat() {
        return false;
    }

    /**
     * @return true if this TypeInfo represents {@link Double} class or {@code double} class
     */
    default boolean isDouble() {
        return false;
    }

    /**
     * @return true if this TypeInfo represents {@link Character} class or {@code char} class
     */
    default boolean isCharacter() {
        return false;
    }

    /**
     * @return true if this TypeInfo represents {@link Object} class
     */
    default boolean isObjectExact() {
        return false;
    }

    default void collectComponentClass(Consumer<Class<?>> collector) {
        collector.accept(asClass());
    }

    /**
     * @see Class#isInterface()
     */
    default boolean isInterface() {
        return false;
    }

    /**
     * @see Class#isArray()
     */
    default boolean isArray() {
        return false;
    }

    /**
     * @return {@code true} if the caller TypeInfo represents the super class of the class
     *     represented by {@code another} TypeInfo
     * @see Class#isAssignableFrom(Class)
     */
    default boolean isAssignableFrom(TypeInfo another) {
        return asClass().isAssignableFrom(another.asClass());
    }

    /**
     * @return true if {@code o} is an instance of this class represented by the caller TypeInfo
     * @see Class#isInstance(Object)
     */
    default boolean isInstance(Object o) {
        return asClass().isInstance(o);
    }

    /**
     * @see FunctionObject#getTypeTag(Class)
     */
    default int getTypeTag() {
        if (this == TypeInfo.STRING) {
            return FunctionObject.JAVA_STRING_TYPE;
        } else if (isInt()) {
            return FunctionObject.JAVA_INT_TYPE;
        } else if (isBoolean()) {
            return FunctionObject.JAVA_BOOLEAN_TYPE;
        } else if (isDouble()) {
            return FunctionObject.JAVA_DOUBLE_TYPE;
        } else if (Scriptable.class.isAssignableFrom(asClass())) {
            return FunctionObject.JAVA_SCRIPTABLE_TYPE;
        } else if (isObjectExact()) {
            return FunctionObject.JAVA_OBJECT_TYPE;
        }

        // Note that the long type is not supported; see the javadoc for
        // the constructor for this class

        return FunctionObject.JAVA_UNSUPPORTED_TYPE;
    }

    /**
     * consolidate a type with provided mapping, that is, try to replace VariableTypeInfo in this
     * type with corresponding type in the same mapping entry
     *
     * @see TypeConsolidator#getMapping(Class)
     */
    default TypeInfo consolidate(Map<TypeInfo, TypeInfo> mapping) {
        return this;
    }

    default TypeInfo rawType() {
        return this;
    }

    default List<TypeInfo> params() {
        return List.of();
    }
}
