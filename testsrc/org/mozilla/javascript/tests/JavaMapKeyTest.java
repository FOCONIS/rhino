/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.javascript.tests;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptRuntime;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/*
 * This testcase tests the basic access to Java classess implementing Iterable
 * (eg. ArrayList)
 */
@RunWith(Parameterized.class)
public class JavaMapKeyTest {

    @Parameters
    public static Collection<Map<?, ?>> data() {
        return Arrays.asList(
                new Map[] {
                    mapWithEnumKey(), mapWithStringKey(), mapWithIntKey(),
                    mapWithLongKey(), mapWithDoubleKey(), mapWithUUIDKey()
                });
    }

    private static Map<String, Integer> mapWithStringKey() {
        Map<String, Integer> map = new LinkedHashMap<>();
        map.put("foo", 7);
        map.put("bar", 2);
        map.put("baz", 5);
        return map;
    }

    public enum MyEnum {
        foo,
        bar,
        baz
    }

    private static Map<MyEnum, Integer> mapWithEnumKey() {
        Map<MyEnum, Integer> map = new EnumMap<>(MyEnum.class);
        map.put(MyEnum.foo, 7);
        map.put(MyEnum.bar, 2);
        map.put(MyEnum.baz, 5);
        return map;
    }

    private static Map<Integer, String> mapWithIntKey() {
        Map<Integer, String> map = new LinkedHashMap<>();
        map.put(1, "one");
        map.put(2, "two");
        map.put(6, "six");
        return map;
    }

    private static Map<Long, String> mapWithLongKey() {
        Map<Long, String> map = new LinkedHashMap<>();
        map.put(1L, "one");
        map.put(2L, "two");
        map.put(Long.MAX_VALUE, "max");
        return map;
    }

    private static Map<Double, String> mapWithDoubleKey() {
        Map<Double, String> map = new LinkedHashMap<>();
        map.put(1D, "one");
        map.put(2D, "two");
        map.put(Math.PI, "pi");
        map.put(Double.NaN, "nan");
        return map;
    }

    private static Map<UUID, String> mapWithUUIDKey() {
        Map<UUID, String> map = new LinkedHashMap<>();
        map.put(UUID.randomUUID(), "one");
        map.put(UUID.randomUUID(), "two");
        return map;
    }

    private Map<?, ?> map;

    public JavaMapKeyTest(Map<?, ?> map) {
        this.map = map;
    }

    // iterate over all values with 'for each'
    @Test
    public void testForEachValue() {
        String js = "var ret = []; for each(var value in map) ret.push(value); ret";
        testJsMap(js, new ArrayList<>(map.values()));
        testJavaMap(js, new ArrayList<>(map.values()));
    }

    @Test
    public void testForKey() {
        String js = "var ret = []; for (var key in map) ret.push(map[key]); ret";
        testJsMap(js, new ArrayList<>(map.values()));
        testJavaMap(js, new ArrayList<>(map.values()));
    }

    @Test
    public void testObjectKeys() {
        String js = "Object.keys(map)";
        // keys are always from type "String"
        testJsMap(
                js,
                map.keySet().stream().map(ScriptRuntime::toString).collect(Collectors.toList()));
        testJavaMap(
                js,
                map.keySet().stream().map(ScriptRuntime::toString).collect(Collectors.toList()));
    }

    @Test
    public void testObjectValues() {
        String js = "Object.values(map)";
        testJsMap(js, new ArrayList<>(map.values()));
        testJavaMap(js, new ArrayList<>(map.values()));
    }

    @Test
    public void testObjectEntries() {
        String js = "Object.entries(map)";
        testJsMap(
                js, map.entrySet().stream().map(this::toEntryNumber).collect(Collectors.toList()));
        testJavaMap(
                js, map.entrySet().stream().map(this::toEntryNumber).collect(Collectors.toList()));
    }

    private Object toEntryNumber(Map.Entry e) {
        return Arrays.asList(
                isInteger(e.getKey()) ? ((Number) e.getKey()).intValue() : e.getKey().toString(),
                e.getValue());
    }

    private Object toEntryInt(Map.Entry e) {
        return Arrays.asList(
                e.getKey() instanceof Number ? e.getKey() : e.getKey().toString(), e.getValue());
    }

    private void testJavaMap(String script, Object expected) {
        Utils.runWithAllOptimizationLevels(
                new ContextFactory() {
                    @Override
                    protected boolean hasFeature(Context cx, int featureIndex) {
                        return super.hasFeature(cx, featureIndex)
                                || featureIndex == Context.FEATURE_ENABLE_JAVA_MAP_ACCESS;
                    }
                },
                cx -> {
                    cx.setLanguageVersion(Context.VERSION_ES6);
                    final ScriptableObject scope = cx.initStandardObjects();
                    scope.put("map", scope, map);
                    Object o = cx.evaluateString(scope, script, "testJavaMap.js", 1, null);
                    assertEquals(expected, o);

                    return null;
                });
    }

    private void testJsMap(String script, Object expected) {
        Utils.runWithAllOptimizationLevels(
                cx -> {
                    cx.setLanguageVersion(Context.VERSION_ES6);
                    final ScriptableObject scope = cx.initStandardObjects();
                    Scriptable obj = cx.newObject(scope);
                    map.forEach(
                            (key, value) -> {
                                if (isInteger(key)) {
                                    obj.put(((Number) key).intValue(), obj, value);
                                } else {
                                    obj.put(String.valueOf(key), obj, value);
                                }
                            });
                    scope.put("map", scope, obj);
                    Object o = cx.evaluateString(scope, script, "testJsMap.js", 1, null);
                    assertEquals(expected, o);

                    return null;
                });
    }

    private boolean isInteger(Object key) {
        return (key instanceof Number)
                && (long) ((Number) key).intValue() == ((Number) key).doubleValue();
    }
}
