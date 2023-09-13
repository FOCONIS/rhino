package examples;/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

import examples.Counter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

/**
 * examples.RunScript4: Execute scripts in an environment that includes the example examples.Counter class.
 *
 * @author Norris Boyd
 */
public class RunScript4 {
    public static void main(String args[]) throws Exception {
        Context cx = Context.enter();
        try {
            Scriptable scope = cx.initStandardObjects();

            // Use the examples.Counter class to define a examples.Counter constructor
            // and prototype in JavaScript.
            ScriptableObject.defineClass(scope, Counter.class);

            // Create an instance of examples.Counter and assign it to
            // the top-level variable "myCounter". This is
            // equivalent to the JavaScript code
            //    myCounter = new examples.Counter(7);
            Object[] arg = {Integer.valueOf(7)};
            Scriptable myCounter = cx.newObject(scope, "examples.Counter", arg);
            scope.put("myCounter", scope, myCounter);

            String s = "";
            for (int i = 0; i < args.length; i++) {
                s += args[i];
            }
            Object result = cx.evaluateString(scope, s, "<cmd>", 1, null);
            System.err.println(Context.toString(result));
        } finally {
            Context.exit();
        }
    }
}
