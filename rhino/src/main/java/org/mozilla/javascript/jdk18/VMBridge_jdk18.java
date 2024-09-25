/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.javascript.jdk18;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.VMBridge;

public class VMBridge_jdk18 extends VMBridge {
    private static final ThreadLocal<Object[]> contextLocal = new ThreadLocal<>();

    @Override
    protected Object getThreadContextHelper() {
        // To make subsequent batch calls to getContext/setContext faster
        // associate permanently one element array with contextLocal
        // so getContext/setContext would need just to read/write the first
        // array element.
        // Note that it is necessary to use Object[], not Context[] to allow
        // garbage collection of Rhino classes. For details see comments
        // by Attila Szegedi in
        // https://bugzilla.mozilla.org/show_bug.cgi?id=281067#c5

        Object[] storage = contextLocal.get();
        if (storage == null) {
            storage = new Object[1];
            contextLocal.set(storage);
        }
        return storage;
    }

    @Override
    protected Context getContext(Object contextHelper) {
        Object[] storage = (Object[]) contextHelper;
        return (Context) storage[0];
    }

    @Override
    protected void setContext(Object contextHelper, Context cx) {
        Object[] storage = (Object[]) contextHelper;
        storage[0] = cx;
    }
}
