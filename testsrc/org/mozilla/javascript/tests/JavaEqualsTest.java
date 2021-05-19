/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.javascript.tests;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.Test;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/*
 * This testcase tests if equals and hashCode for NativeArray (implements List)
 * and NativeObject (implements Map) works the same way as in other Lists/Maps
 * (e.g. ArrayList/LinkedList)
 */
public class JavaEqualsTest {

    @Test
    public void testNativeArrayEquals() {
        testJs("var a = []; a[1]='bar'; a", Arrays.asList(null, "bar"));
        testJs("['foo','bar']", Arrays.asList("foo", "bar"));
    }

    @Test
    public void testNativeObjectEquals() {
        // Note: parser will int
        testJs("({})", Collections.emptyMap());
        Map<String, String> map = new LinkedHashMap<>();
        map.put("a", "b");
        map.put("x", "y");
        testJs("({'a' : 'b', 'x': 'y'})", map);
    }

    private void testJs(String script, Object expected) {
        Utils.runWithAllOptimizationLevels(
                cx -> {
                    cx.setLanguageVersion(Context.VERSION_ES6);
                    final ScriptableObject scope = cx.initStandardObjects();
                    Scriptable obj = cx.newObject(scope);
                    Object o = cx.evaluateString(scope, script, "JavaEqualsTest.js", 1, null);
                    assertEquals(expected, o);
                    assertEquals(o, expected);
                    assertEquals(o.hashCode(), expected.hashCode());
                    return null;
                });
    }
}
