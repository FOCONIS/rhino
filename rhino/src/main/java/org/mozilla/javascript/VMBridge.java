/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

// API class

package org.mozilla.javascript;

public abstract class VMBridge {

    static final VMBridge instance = makeInstance();

    private static VMBridge makeInstance() {
        String[] classNames = {
            "org.mozilla.javascript.VMBridge_custom", "org.mozilla.javascript.jdk18.VMBridge_jdk18",
        };
        for (int i = 0; i != classNames.length; ++i) {
            String className = classNames[i];
            Class<?> cl = Kit.classOrNull(className);
            if (cl != null) {
                VMBridge bridge = (VMBridge) Kit.newInstanceOrNull(cl);
                if (bridge != null) {
                    return bridge;
                }
            }
        }
        throw new IllegalStateException("Failed to create VMBridge instance");
    }

    /**
     * Return a helper object to optimize {@link Context} access.
     *
     * <p>The runtime will pass the resulting helper object to the subsequent calls to {@link
     * #getContext(Object contextHelper)} and {@link #setContext(Object contextHelper, Context cx)}
     * methods. In this way the implementation can use the helper to cache information about current
     * thread to make {@link Context} access faster.
     */
    protected abstract Object getThreadContextHelper();

    /**
     * Get {@link Context} instance associated with the current thread or null if none.
     *
     * @param contextHelper The result of {@link #getThreadContextHelper()} called from the current
     *     thread.
     */
    protected abstract Context getContext(Object contextHelper);

    /**
     * Associate {@link Context} instance with the current thread or remove the current association
     * if <code>cx</code> is null.
     *
     * @param contextHelper The result of {@link #getThreadContextHelper()} called from the current
     *     thread.
     */
    protected abstract void setContext(Object contextHelper, Context cx);
}
