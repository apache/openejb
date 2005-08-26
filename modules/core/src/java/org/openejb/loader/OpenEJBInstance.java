/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */

package org.openejb.loader;

import org.openejb.loader.ClassPath;
import org.openejb.loader.SystemInstance;
import org.openejb.util.FileUtils;

import javax.servlet.ServletException;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.io.File;

/**
 * This class can exist in several child classloaders
 * Specifically in the tomcat webapp style, this class
 * is loaded several times.
 */
public class OpenEJBInstance {
    private final Class openejb;
    private final Method init;
    private final Method isInitialized;

    public OpenEJBInstance() throws Exception {
        this.openejb = loadOpenEJBClass();
        this.init = openejb.getMethod("init", new Class[]{Properties.class});
        this.isInitialized = openejb.getMethod("isInitialized", new Class[]{});
    }

    public void init(Properties props) throws Exception {
        try {
            init.invoke(null, new Object[]{props});
        } catch (InvocationTargetException e) {
            throw (Exception) e.getCause();
        } catch (Exception e) {
            throw new RuntimeException("OpenEJB.init: ", e);
        }
    }

    public boolean isInitialized() {
        try {
            Boolean b = (Boolean) isInitialized.invoke(null, new Object[]{});
            return b.booleanValue();
        } catch (InvocationTargetException e) {
            throw new RuntimeException("OpenEJB.isInitialized: ", e.getCause());
        } catch (Exception e) {
            throw new RuntimeException("OpenEJB.isInitialized: ", e);
        }
    }

    private Class loadOpenEJBClass() throws Exception {
        ClassPath classPath = SystemInstance.get().getClassPath();
        ClassLoader classLoader = classPath.getClassLoader();
        try {
            return classLoader.loadClass("org.openejb.OpenEJB");
        } catch (Exception e) {
            try {
                checkOpenEjbHome(SystemInstance.get().getHome().getDirectory());
                FileUtils home = SystemInstance.get().getHome();
                classPath.addJarsToPath(home.getDirectory("lib"));
            } catch (Exception e2) {
                throw new Exception("Could not load OpenEJB libraries. Exception: " + e2.getClass().getName() + " " + e2.getMessage());
            }
            try {
                return classLoader.loadClass("org.openejb.OpenEJB");
            } catch (Exception e2) {
                throw new Exception("Could not load OpenEJB class after embedding libraries. Exception: " + e2.getClass().getName() + " " + e2.getMessage());
            }
        }
    }
    String NO_HOME = "The openejb.home is not set.";

    String BAD_HOME = "Invalid openejb.home: ";

    String NOT_THERE = "The path specified does not exist.";

    String NOT_DIRECTORY = "The path specified is not a directory.";

    String NO_DIST = "The path specified is not correct, it does not contain a 'dist' directory.";

    String NO_LIBS = "The path specified is not correct, it does not contain any OpenEJB libraries.";

    // TODO: move this part back into the LoaderServlet
    String INSTRUCTIONS = "Please edit the web.xml of the openejb_loader webapp and set the openejb.home init-param to the full path where OpenEJB is installed.";

    private void checkOpenEjbHome(File openejbHome) throws Exception {
        try {

            String homePath = openejbHome.getAbsolutePath();
            
            // The openejb.home must exist
            if (!openejbHome.exists())
                handleError(BAD_HOME + homePath, NOT_THERE, INSTRUCTIONS);

            // The openejb.home must be a directory
            if (!openejbHome.isDirectory())
                handleError(BAD_HOME + homePath, NOT_DIRECTORY, INSTRUCTIONS);

            // The openejb.home must contain a 'lib' directory
            File openejbHomeLibs = new File(openejbHome, "lib");
            if (!openejbHomeLibs.exists())
                handleError(BAD_HOME + homePath, NO_DIST, INSTRUCTIONS);

            // The openejb.home there must be openejb*.jar files in the 'dist'
            // directory
            String[] libs = openejbHomeLibs.list();
            boolean found = false;
            for (int i = 0; i < libs.length && !found; i++) {
                found = (libs[i].startsWith("openejb-") && libs[i].endsWith(".jar"));
            }
            if (!found)
                handleError(BAD_HOME + homePath, NO_LIBS, INSTRUCTIONS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleError(String m1, String m2, String m3) throws Exception {
        System.err.println("--[PLEASE FIX]-------------------------------------");
        System.err.println(m1);
        System.err.println(m2);
        System.err.println(m3);
        System.err.println("---------------------------------------------------");
        throw new Exception(m1 + " " + m2 + " " + m3);
    }

    private void handleError(String m1, String m2) throws Exception {
        System.err.println("--[PLEASE FIX]-------------------------------------");
        System.err.println(m1);
        System.err.println(m2);
        System.err.println("---------------------------------------------------");
        throw new Exception(m1 + " " + m2);
    }

}
