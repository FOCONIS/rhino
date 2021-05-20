/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.javascript.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.testing.TestErrorReporter;

/*
 * This testcase tests the basic access to Java classess implementing Iterable
 * (eg. ArrayList)
 */
public class JavaMapSpecialKeyTest {

    private Map<Object, Object> map = new LinkedHashMap<>();

    // iterate over all values with 'for each'
    private ContextFactory factory =
            new ContextFactory() {
                @Override
                protected boolean hasFeature(Context cx, int featureIndex) {
                    return super.hasFeature(cx, featureIndex)
                            || featureIndex == Context.FEATURE_ENABLE_JAVA_MAP_ACCESS;
                }
            };

    @Test
    public void testAmbiguousNullKey() {
        map.put("null", "empty");
        map.put(null, "really empty");
        assertEquals(2, map.size());

        try (Context cx = factory.enterContext()) {
            TestErrorReporter e =
                    new TestErrorReporter(
                            null,
                            new String[] {
                                "Key 'null' (Type:java.lang.String) and 'null' (Type: null) are ambiguous in NativeJavaMap"
                            });
            cx.setErrorReporter(e);
            Object o = evaluate(cx, "Object.keys(map)");
            assertEquals(Arrays.asList("null", "null"), o);
            assertTrue(e.hasEncounteredAllWarnings());
        }
    }

    @Test
    public void testAmbiguousNumbers() {
        map.put(1L, "one");
        map.put(1.0D, "one.zero");
        assertEquals(2, map.size());

        try (Context cx = factory.enterContext()) {
            TestErrorReporter e =
                    new TestErrorReporter(
                            null,
                            new String[] {
                                "Key '1' (Type:java.lang.Long) and '1.0' (Type: java.lang.Double) are ambiguous in NativeJavaMap"
                            });
            cx.setErrorReporter(e);
            Object o = evaluate(cx, "Object.keys(map)");
            assertEquals(Arrays.asList("1", "1"), o);
            assertTrue(e.hasEncounteredAllWarnings());
        }
    }

    @Test
    public void testOrderingJs() {
        map.put(3, "three");
        map.put(2, "two");
        map.put(1, "one");

        try (Context cx = factory.enterContext()) {
            Object o = evaluate(cx, "Object.keys(map)");
            assertEquals(Arrays.asList("1", "2", "3"), o);
        }
    }

    private Object evaluate(Context cx, String script) {
        cx.setLanguageVersion(Context.VERSION_ES6);
        final ScriptableObject scope = cx.initStandardObjects();
        scope.put("map", scope, map);
        return cx.evaluateString(scope, script, "testJsMap.js", 1, null);
    }
}
