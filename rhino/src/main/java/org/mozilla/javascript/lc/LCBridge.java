/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

// API class

package org.mozilla.javascript.lc;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.ServiceLoader;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

public abstract class LCBridge {

    public static final LCBridge instance = makeInstance();

    private static LCBridge makeInstance() {
        Iterator<LCBridge> iterator = ServiceLoader.load(LCBridge.class).iterator();
        if (iterator.hasNext()) {
            return iterator.next();
        }
        return new LCBridge_jdk18();
    }

    /**
     * In many JVMSs, public methods in private classes are not accessible by default (Sun Bug
     * #4071593). VMBridge instance should try to workaround that via, for example, calling
     * method.setAccessible(true) when it is available. The implementation is responsible to catch
     * all possible exceptions like SecurityException if the workaround is not available.
     *
     * @return true if it was possible to make method accessible or false otherwise.
     */
    public abstract boolean tryToMakeAccessible(AccessibleObject accessible);

    /**
     * Create helper object to create later proxies implementing the specified interfaces later.
     * Under JDK 1.3 the implementation can look like:
     *
     * <pre>
     * return java.lang.reflect.Proxy.getProxyClass(..., interfaces).
     *     getConstructor(new Class[] {
     *         java.lang.reflect.InvocationHandler.class });
     * </pre>
     *
     * @param interfaces Array with one or more interface class objects.
     */
    public abstract Object getInterfaceProxyHelper(ContextFactory cf, Class<?>[] interfaces);

    /**
     * Create proxy object for {@link InterfaceAdapter}. The proxy should call {@link
     * InterfaceAdapter#invoke(ContextFactory, Object, Scriptable, Object, Method, Object[])} as
     * implementation of interface methods associated with <code>proxyHelper</code>. {@link Method}
     *
     * @param proxyHelper The result of the previous call to {@link
     *     #getInterfaceProxyHelper(ContextFactory, Class[])}.
     */
    public abstract Object newInterfaceProxy(
            Object proxyHelper,
            ContextFactory cf,
            InterfaceAdapter adapter,
            Object target,
            Scriptable topScope);

    public abstract String[] getTopPackageNames();
}
