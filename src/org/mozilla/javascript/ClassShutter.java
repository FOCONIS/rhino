/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */

// API class

package org.mozilla.javascript;

import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Embeddings that wish to filter Java classes that are visible to scripts through the LiveConnect,
 * should implement this interface.
 *
 * <p><b>Note:</b><br>
 * It his highly recommended not enable <code>FEATURE_ENHANCED_JAVA_ACCESS</code>, as it allows you
 * to access all protected and private methods and this completely undermines the security.
 *
 * @see Context#setClassShutter(ClassShutter)
 * @since 1.5 Release 4
 * @author Norris Boyd
 */
public interface ClassShutter {

    /**
     * Return true iff the Java class with the given name should be exposed to scripts.
     *
     * <p>An embedding may filter which Java classes are exposed through LiveConnect to JavaScript
     * scripts.
     *
     * <p>Due to the fact that there is no package reflection in Java, this method will also be
     * called with package names. There is no way for Rhino to tell if "Packages.a.b" is a package
     * name or a class that doesn't exist. What Rhino does is attempt to load each segment of
     * "Packages.a.b.c": It first attempts to load class "a", then attempts to load class "a.b",
     * then finally attempts to load class "a.b.c". On a Rhino installation without any ClassShutter
     * set, and without any of the above classes, the expression "Packages.a.b.c" will result in a
     * [JavaPackage a.b.c] and not an error.
     *
     * <p>With ClassShutter supplied, Rhino will first call visibleToScripts before attempting to
     * look up the class name. If visibleToScripts returns false, the class name lookup is not
     * performed and subsequent Rhino execution assumes the class is not present. So for
     * "java.lang.System.out.println" the lookup of "java.lang.System" is skipped and thus Rhino
     * assumes that "java.lang.System" doesn't exist. So then for "java.lang.System.out", Rhino
     * attempts to load the class "java.lang.System.out" because it assumes that "java.lang.System"
     * is a package name.
     *
     * <p>
     *
     * @param fullClassName the full name of the class (including the package name, with '.' as a
     *     delimiter). For example the standard string class is "java.lang.String"
     * @return whether or not to reveal this class to scripts
     */
    public boolean visibleToScripts(String fullClassName);

    /**
     * This method is similar to {@link #visibleToScripts(String)} to check if a concrete Class is
     * accessible from script. If this method throws a {@link SecurityException}, the lookup will
     * continue recursively on the interfaces and superclasses and will collect all methods, of all
     * accessible classes in the hierarchy tree. After that, <code>isUsable</code> is called, which
     * can decide if the found methods are enough. (If the algorithm finds only methods definde by
     * the hierarchy root( <code>java.lang.Object</code>) the class is not usable and the {@link
     * SecurityException} will be rethrown.
     *
     * <p>CheckAccessible is designed to call a method from the SecurityManager. e.g.
     *
     * <pre>
     * String pkg = clazz.getPackage().getName();
     * securityManager.checkPermission(
     *         new RuntimePermission("accessibleToScripts." + pkg));
     * </pre>
     *
     * <b>Note:</b> a class that should be accessible must also be visible to scripts. but there may
     * be more classes that are visible to scripts than accessible as shown in the next example.
     *
     * <p><b>Example 1:</b><br>
     * checkAccessible is implmemented in a way that it allows only classes in the <code>java.lang
     * </code> and <code>org.slf4j.*</code> package. For all other classes it will throw a
     * SecurityException.<br>
     * If we take look at this class
     *
     * <pre>
     * public class ch.qos.logback.classic.Logger implements
     *   org.slf4j.spi.LocationAwareLogger,
     *   ch.qos.logback.core.spi.AppenderAttachable,
     *   java.io.Serializable
     * </pre>
     *
     * a call to <code>checkAccessible(ch.qos.logback.classic.Logger.class)</code> will throw a
     * SecurityException and the warning "Could not discover accessible methods of class $CLASS due
     * to lack of privileges, attemping superclasses/interfaces." will be logged in the context.
     * (Note: as mentioned above, the class <code>ch.qos.logback.classic.Logger</code> must be
     * visible to scripts, otherwise it will not wrapped at all.)
     *
     * <p>The discovery will then continue with interfaces, in this case <code>
     * org.slf4j.spi.LocationAwareLogger</code> and will also call <code>checkAccessible</code> on
     * all inherited classes.
     *
     * <p>As result, you may access only methods in scripts provided by the interfaces (<code>
     * org.slf4j.spi.LocationAwareLogger</code> and <code>org.slf4j.Logger</code>) and the
     * super-classes (in this case only <code>java.lang.Object</code>)
     *
     * <p>Notably, the method <code>setLevel</code> which is defined in <code>
     * ch.qos.logback.classic.Logger</code> but not in any interfaces is not accessible in scripts
     *
     * <p><b>Example 2:</b><br>
     * Assume that <code>checkAccessible</code> throws a {@link SecurityException} when a script
     * tries to access <code>java.io.File</code>. The algorithm will continue the search for methods
     * on the <code>Serializable</code> and <code>Compareable</code> interface and then on <code>
     * java.lang.Object</code>
     *
     * <p>As <code>java.io.Serialize</code> is only a marker interface, it would not define any
     * methods, we will end up with the <code>java.lang.Compareable::compareTo</code> and the
     * default methods in <code>java.lang.Object</code>
     *
     * @param clazz the class to check
     * @throws SecurityException if the class is not accessible.
     */
    default void checkAccessible(Class<?> clazz) throws SecurityException {}

    /**
     * Returns true iff the method is visible to scripts. This allows you to implement
     * black/whitelist of certain security policies. E.g. you can filter for method calls like
     * <code>getClass()</code> or <code>getClassLoader()</code>
     */
    default boolean visibleToScripts(Class<?> clazz, Method method) {
        return true;
    }

    /**
     * If a class is not accessible by ClassShutter (or even by SecurityManager) search is continued
     * on the interfaces/superclasses. After that is done, this method is called, which can decide,
     * if the security-exception is rethrown or if the class is usable.
     */
    default boolean isUsable(Class<?> clazz, Collection<Method> methods) {
        return true;
    }
}
