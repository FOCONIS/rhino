/* -*- Mode: java; tab-width: 8; indent-tabs-mode: nil; c-basic-offset: 4 -*-
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package org.mozilla.javascript.tests;

import java.io.FilePermission;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessControlException;
import java.security.CodeSource;
import java.security.NoSuchAlgorithmException;
import java.security.Permission;
import java.security.Permissions;
import java.security.Policy;
import java.security.ProtectionDomain;
import java.security.URIParameter;
import java.util.Enumeration;
import java.util.PropertyPermission;
import java.util.function.Function;
import java.util.logging.Logger;

import org.junit.Test;
import org.mozilla.javascript.ClassShutter;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.SecurityController;
import org.mozilla.javascript.tools.shell.Global;
import org.mozilla.javascript.tools.shell.JavaPolicySecurity;

import junit.framework.TestCase;

/**
 * Perform some tests when we have a securityController in place. 
 */
public class SecurityControllerTest extends TestCase {

    private static ProtectionDomain UNTRUSTED_JAVASCRIPT;
    private static ProtectionDomain ALLOW_FILE_IO_JAVASCRIPT;
    private static ProtectionDomain ALLOW_LOGGER_JAVASCRIPT;
    protected final Global global = new Global();

    /**
     * Sets up the security manager and loads the "grant-all-java.policy".
     */
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

    /**
     * Creates a new protectionDomain with the given Code-Source Suffix.
     */
    static ProtectionDomain createProtectionDomain(Policy policy, String csSuffix) throws MalformedURLException {
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
    
    /**
     * Setup the security.
     */
    static {
        setupSecurityManager();
        try {
            URL url = SecurityControllerTest.class.getResource("javascript.policy");
            Policy policy = Policy.getInstance("JavaPolicy", new URIParameter(url.toURI()));
            UNTRUSTED_JAVASCRIPT = createProtectionDomain(policy, "UNTRUSTED_JAVASCRIPT");
            ALLOW_FILE_IO_JAVASCRIPT = createProtectionDomain(policy, "ALLOW_FILE_IO_JAVASCRIPT");
            ALLOW_LOGGER_JAVASCRIPT = createProtectionDomain(policy, "ALLOW_LOGGER_JAVASCRIPT");

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
        } catch (AccessControlException ace) { }
    }
    
    public void testFileAccessAllowFileIo() {
        runScript("new java.io.File('tmp').exists()", ALLOW_FILE_IO_JAVASCRIPT);
    }
    
    @Test(expected = AccessControlException.class)
    public void testFileAccessAllowLogger() {
        try {
            runScript("new java.io.File('tmp').exists()", ALLOW_LOGGER_JAVASCRIPT);
            fail("AccessControlException expected");
        } catch (AccessControlException ace) { }
    }
    
    public void testLoggerAccessTrusted() {
        runScript("java.util.logging.Logger.getLogger('foo').toString()", null);
    }
    
    public void testLoggerAccessUntrusted() {
        try {
            runScript("java.util.logging.Logger.getLogger('foo').toString()", UNTRUSTED_JAVASCRIPT);
            fail("AccessControlException expected");
        } catch (AccessControlException ace) { }
    }
    
    public void testLoggerAccessAllowFileIo() {
        try {
            runScript("java.util.logging.Logger.getLogger('foo').toString()", ALLOW_FILE_IO_JAVASCRIPT);
            fail("AccessControlException expected");
        } catch (AccessControlException ace) { }
    }
    
    public void testLoggerAccessAllowLogger() {
        runScript("java.util.logging.Logger.getLogger('foo').toString()", ALLOW_LOGGER_JAVASCRIPT);
    }

    /**
     * This classShutter checks the "visibelToScripts.{pkg}" runtime property, which can be defined in a policy file.
     * Note: Every other code in your stack-chain will need this permission also. 
     */
    private static class PolicyClassShutter implements ClassShutter {

        @Override
        public boolean visibleToScripts(String fullClassName) {
            int idx = fullClassName.lastIndexOf('.');
            if (idx != -1) {
                SecurityManager sm = System.getSecurityManager();
                if (sm != null) {
                    sm.checkPermission(new RuntimePermission(
                            "visibleToScripts." + fullClassName.substring(0, idx)));
                }
            }
            // thows AccessControlException, if package is not visible.
            return true;
        }
    }
    
    /**
     * Compiles and runs the script with the given protection domain.
     */
    private void runScript(String scriptSourceText, ProtectionDomain pd) {
        Utils.runWithAllOptimizationLevels(
        context -> {
            context.setClassShutter(new PolicyClassShutter());
            Scriptable scope = context.initStandardObjects(global);
            
            context.evaluateString(scope, scriptSourceText, "", 1, pd);
            return null;
        });
    }
}
