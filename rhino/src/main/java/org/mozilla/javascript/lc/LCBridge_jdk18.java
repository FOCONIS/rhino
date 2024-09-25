/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

package org.mozilla.javascript.lc;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;

public class LCBridge_jdk18 extends LCBridge {

    @SuppressWarnings("deprecation")
    @Override
    public boolean tryToMakeAccessible(AccessibleObject accessible) {
        if (!accessible.isAccessible()) {
            accessible.setAccessible(true);
        }
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public Object getInterfaceProxyHelper(ContextFactory cf, Class<?>[] interfaces) {
        // XXX: How to handle interfaces array withclasses from different
        // class loaders? Using cf.getApplicationClassLoader() ?
        ClassLoader loader = interfaces[0].getClassLoader();
        Class<?> cl = Proxy.getProxyClass(loader, interfaces);
        Constructor<?> c;
        try {
            c = cl.getConstructor(InvocationHandler.class);
        } catch (NoSuchMethodException ex) {
            // Should not happen
            throw new IllegalStateException(ex);
        }
        return c;
    }

    @Override
    public Object newInterfaceProxy(
            Object proxyHelper,
            final ContextFactory cf,
            final InterfaceAdapter adapter,
            final Object target,
            final Scriptable topScope) {
        Constructor<?> c = (Constructor<?>) proxyHelper;

        InvocationHandler handler =
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) {
                        // In addition to methods declared in the interface, proxies
                        // also route some java.lang.Object methods through the
                        // invocation handler.
                        if (method.getDeclaringClass() == Object.class) {
                            String methodName = method.getName();
                            if (methodName.equals("equals")) {
                                Object other = args[0];
                                // Note: we could compare a proxy and its wrapped function
                                // as equal here but that would break symmetry of equal().
                                // The reason == suffices here is that proxies are cached
                                // in ScriptableObject (see NativeJavaObject.coerceType())
                                return Boolean.valueOf(proxy == other);
                            }
                            if (methodName.equals("hashCode")) {
                                return Integer.valueOf(target.hashCode());
                            }
                            if (methodName.equals("toString")) {
                                return "Proxy[" + target.toString() + "]";
                            }
                        }
                        return adapter.invoke(cf, target, topScope, proxy, method, args);
                    }
                };
        Object proxy;
        try {
            proxy = c.newInstance(handler);
        } catch (InvocationTargetException ex) {
            throw Context.throwAsScriptRuntimeEx(ex);
        } catch (IllegalAccessException ex) {
            // Should not happen
            throw new IllegalStateException(ex);
        } catch (InstantiationException ex) {
            // Should not happen
            throw new IllegalStateException(ex);
        }
        return proxy;
    }

    @Override
    public String[] getTopPackageNames() {
        // Include "android" top package if running on Android
        return "Dalvik".equals(System.getProperty("java.vm.name"))
                ? new String[] {"java", "javax", "org", "com", "edu", "net", "android"}
                : new String[] {"java", "javax", "org", "com", "edu", "net"};
    }
}
