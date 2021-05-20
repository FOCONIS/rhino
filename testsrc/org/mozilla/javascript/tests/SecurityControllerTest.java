/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.javascript.tests;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.AccessControlException;
import java.security.CodeSource;
import java.security.Permission;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.URIParameter;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import junit.framework.TestCase;
import org.junit.Test;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.EcmaError;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.SecurityController;
import org.mozilla.javascript.tools.shell.Global;
import org.mozilla.javascript.tools.shell.JavaPolicySecurity;

/** Perform some tests when we have a securityController in place. */
public class SecurityControllerTest extends TestCase {

    private static ProtectionDomain UNTRUSTED_JAVASCRIPT;
    private static ProtectionDomain ALLOW_SECURITY;
    private static ProtectionDomain ALLOW_FILE_IO_JAVASCRIPT;
    private static ProtectionDomain ALLOW_LOGGER_JAVASCRIPT;
    protected final Global global = new Global();

    /** Sets up the security manager and loads the "grant-all-java.policy". */
    static void setupSecurityManager() {
        URL url = SecurityControllerTest.class.getResource("grant-all-java.policy");
        if (url != null) {
            System.out.println("Initializing security manager with grant-all-java.policy");
            System.setProperty("java.security.policy", url.toString());
            Policy.getPolicy().refresh();
            System.setSecurityManager(new SecurityManager());
        }
        SecurityController.initGlobal(new JavaPolicySecurity());
    }

    /** Creates a new protectionDomain with the given Code-Source Suffix. */
    static ProtectionDomain createProtectionDomain(Policy policy, String csSuffix)
            throws MalformedURLException {
        URL url = new URL(SecurityController.class.getResource("/"), csSuffix);
        CodeSource cs = new CodeSource(url, (java.security.cert.Certificate[]) null);
        Permissions perms = new Permissions();
        Enumeration<Permission> elems = policy.getPermissions(cs).elements();
        while (elems.hasMoreElements()) {
            perms.add(elems.nextElement());
        }
        perms.setReadOnly();
        return new ProtectionDomain(cs, perms, null, null);
    }

    /** Setup the security. */
    static {
        setupSecurityManager();
        try {
            URL url = SecurityControllerTest.class.getResource("javascript.policy");
            Policy policy = Policy.getInstance("JavaPolicy", new URIParameter(url.toURI()));
            UNTRUSTED_JAVASCRIPT = createProtectionDomain(policy, "UNTRUSTED_JAVASCRIPT");
            ALLOW_FILE_IO_JAVASCRIPT = createProtectionDomain(policy, "ALLOW_FILE_IO_JAVASCRIPT");
            ALLOW_LOGGER_JAVASCRIPT = createProtectionDomain(policy, "ALLOW_LOGGER_JAVASCRIPT");
            ALLOW_SECURITY = createProtectionDomain(policy, "ALLOW_SECURITY");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testFileAccessTrusted() {
        runScript("new java.io.File('tmp').exists()", null);
    }

    public void testFileAccessUntrusted() {
        try {
            runScript("new java.io.File('tmp').exists()", UNTRUSTED_JAVASCRIPT);
            fail("AccessControlException expected");
        } catch (AccessControlException ace) {
        }
    }

    public void testFileAccessAllowFileIo() {
        runScript("new java.io.File('tmp').exists()", ALLOW_FILE_IO_JAVASCRIPT);
    }

    @Test(expected = AccessControlException.class)
    public void testFileAccessAllowLogger() {
        try {
            runScript("new java.io.File('tmp').exists()", ALLOW_LOGGER_JAVASCRIPT);
            fail("AccessControlException expected");
        } catch (AccessControlException ace) {
        }
    }

    public void testLoggerAccessTrusted() {
        runScript("java.util.logging.Logger.getLogger('foo').toString()", null);
    }

    public void testLoggerAccessUntrusted() {
        try {
            runScript("java.util.logging.Logger.getLogger('foo').toString()", UNTRUSTED_JAVASCRIPT);
            fail("AccessControlException expected");
        } catch (AccessControlException ace) {
        }
    }

    public void testLoggerAccessAllowFileIo() {
        try {
            runScript(
                    "java.util.logging.Logger.getLogger('foo').toString()",
                    ALLOW_FILE_IO_JAVASCRIPT);
            fail("AccessControlException expected");
        } catch (AccessControlException ace) {
        }
    }

    public void testLoggerAccessAllowLogger() {
        runScript("java.util.logging.Logger.getLogger('foo').toString()", ALLOW_LOGGER_JAVASCRIPT);
    }

    public void testSecure2() {
        //        try {
        //            runScript("com.example.securitytest.SomeFactory.TEST", UNTRUSTED_JAVASCRIPT);
        //            fail("AccessControlException expected");
        //        } catch (AccessControlException ace) { }
        //        try {
        //            runScript("f = new com.example.securitytest.SomeFactory()",
        // UNTRUSTED_JAVASCRIPT);
        //            fail("AccessControlException expected");
        //        } catch (AccessControlException ace) { }
        //        runScript("f = new com.example.securitytest.SomeFactory(); var i = f.create();
        // i.size(), i.foo(); i.bar();", null);
        try {
            runScript(
                    "f = new com.example.securitytest.SomeFactory(); var i = f.create(); i.size(); i.foo(); i.bar();",
                    ALLOW_SECURITY);
            fail("EcmaError expected");
        } catch (EcmaError ee) {
            assertEquals("TypeError: Cannot find function bar in object []. (#1)", ee.getMessage());
        }
    }

    public void testSecure() {
        try {
            runScript("com.example.securitytest.SomeFactory.TEST", UNTRUSTED_JAVASCRIPT);
            fail("AccessControlException expected");
        } catch (AccessControlException ace) {
        }
        try {
            runScript(
                    "f = new com.example.securitytest.SomeFactory(); var i = f.create(); i.clear();",
                    ALLOW_SECURITY);
            fail("EcmaError expected");
        } catch (EcmaError ee) {
            assertEquals(
                    "TypeError: Cannot find function clear in object []. (#1)", ee.getMessage());
        }
    }

    public void testCalendarAccessTrusted() {
        runScript("var c = java.util.Calendar.getInstance(); c.toZonedDateTime()", null);
    }

    public void testAccessJavaSecurity() {
        runScript("var c = java.security.MessageDigest.getInstance('MD5');", ALLOW_SECURITY);
        try {
            runScript(
                    "var c = java.security.MessageDigest.getInstance('MD5');",
                    ALLOW_FILE_IO_JAVASCRIPT);
            fail("AccessControlException expected");
        } catch (AccessControlException ace) {
        }
    }
    /**
     * This classShutter checks the "visibelToScripts.{pkg}" runtime property, which can be defined
     * in a policy file. Note: Every other code in your stack-chain will need this permission also.
     */
    private static class PolicyClassShutter implements ClassShutter {

        @Override
        public boolean visibleToScripts(String fullClassName) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                int idx = fullClassName.lastIndexOf('.');
                if (idx != -1) {
                    String pkg = fullClassName.substring(0, idx);
                    sm.checkPermission(new RuntimePermission("rhino.visible." + pkg));
                }
            }
            return true;
        }

        @Override
        public void checkAccessible(Class<?> clazz) {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null) {
                sm.checkPermission(
                        new RuntimePermission("rhino.accessible." + clazz.getPackage().getName()));
            }
        }

        public boolean isUsable(Class<?> clazz, Collection<Method> methods) {
            for (Method m : methods) {
                if (!m.getDeclaringClass().getPackage().getName().equals("java.lang")) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean visibleToScripts(Class<?> clazz, Method method) {
            if (List.class.isAssignableFrom(clazz) && method.getName().equals("clear")) {
                return false;
            } else {
                return true;
            }
        }
    }

    /** Compiles and runs the script with the given protection domain. */
    private void runScript(String scriptSourceText, ProtectionDomain pd) {
        Utils.runWithAllOptimizationLevels(
                context -> {
                    context.setClassShutter(new PolicyClassShutter());
                    Scriptable scope = context.initStandardObjects(global);

                    return context.evaluateString(scope, scriptSourceText, "", 1, pd);
                });
    }
}
