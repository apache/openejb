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

package org.openejb.alt.config;

import org.openejb.OpenEJBException;
import org.openejb.util.SafeToolkit;
import org.openejb.util.FileUtils;

import java.util.HashMap;
import java.net.URL;
import java.io.File;

/**
 * @version $Revision$ $Date$
 */
public class TempCodebase {

    protected static final HashMap tempCodebases = new HashMap();

    private final String codebase;
    private final ClassLoader classLoader;

    public TempCodebase(String codebase) throws OpenEJBException {
        this.codebase = codebase;
        ClassLoader cl = null;
        try {
            URL[] urlCodebase = new URL[1];
            urlCodebase[0] = createTempCopy(codebase).toURL();
            cl = new java.net.URLClassLoader(urlCodebase, TempCodebase.class.getClassLoader());
        } catch (java.net.MalformedURLException mue) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0001", codebase, mue.getMessage()));
        } catch (SecurityException se) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0002", codebase, se.getMessage()));
        }
        this.classLoader = cl;
    }

    public String getCodebase() {
        return codebase;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public static TempCodebase getTempCodebase(String codebase) throws OpenEJBException {
        if (codebase == null) {
            codebase = "CLASSPATH";
        }
        TempCodebase tempCodebase = (TempCodebase)tempCodebases.get(codebase);
        if (tempCodebase == null){
            tempCodebase = new TempCodebase(codebase);
            tempCodebases.put(codebase, tempCodebase);
        }
        return tempCodebase;
    }

    public Class loadClass(String className) throws OpenEJBException {
        ClassLoader cl = getClassLoader();
        Class clazz = null;
        try {
            clazz = cl.loadClass(className);
        } catch (ClassNotFoundException cnfe) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0007", className, codebase));
        }
        return clazz;
    }

    public static void unloadTempCodebase(String codebase) {
        //TODO Delete temp jar
        tempCodebases.remove(codebase);
    }

    /**
     * Ensures that a class loader for each code base used in the
     * system is created at most one time.  The default bootsrap
     * classloader is used if codebase is null.
     *
     * @param codebase
     * @return ClassLoader
     * @throws org.openejb.OpenEJBException
     */
    protected static ClassLoader getCodebaseTempClassLoader(String codebase) throws OpenEJBException {
        if (codebase == null) codebase = "CLASSPATH";

        ClassLoader cl = (ClassLoader) tempCodebases.get(codebase);
        if (cl == null) {
            synchronized (SafeToolkit.codebases) {
                cl = (ClassLoader) SafeToolkit.codebases.get(codebase);
                if (cl == null) {
                    try {
                        URL[] urlCodebase = new URL[1];
                        urlCodebase[0] = createTempCopy(codebase).toURL();

// make sure everything works if we were not loaded by the system class loader
                        cl = new java.net.URLClassLoader(urlCodebase, SafeToolkit.class.getClassLoader());

                        tempCodebases.put(codebase, cl);
                    } catch (java.net.MalformedURLException mue) {
                        throw new OpenEJBException(SafeToolkit.messages.format("cl0001", codebase, mue.getMessage()));
                    } catch (SecurityException se) {
                        throw new OpenEJBException(SafeToolkit.messages.format("cl0002", codebase, se.getMessage()));
                    }
                }
            }
        }
        return cl;
    }

    /**
     * Ensures that a class loader for each code base used in the
     * system is created at most one time.  The default bootsrap
     * classloader is used if codebase is null.
     *
     * @param codebase
     * @return ClassLoader
     * @throws org.openejb.OpenEJBException
     */
    protected static ClassLoader getTempClassLoader(String codebase) throws OpenEJBException {
        ClassLoader cl = null;
        try {
            URL[] urlCodebase = new URL[1];
            urlCodebase[0] = createTempCopy(codebase).toURL();

            // make sure everything works if we were not loaded by the system class loader
            cl = new java.net.URLClassLoader(urlCodebase, SafeToolkit.class.getClassLoader());
        } catch (java.net.MalformedURLException mue) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0001", codebase, mue.getMessage()));
        } catch (SecurityException se) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0002", codebase, se.getMessage()));
        }
        return cl;
    }

    protected static File createTempCopy(String codebase) throws OpenEJBException {
        File file = null;

        try {
            File codebaseFile = new File(codebase);
//            if (codebaseFile.isDirectory()) return codebaseFile;

            file = File.createTempFile("openejb_validate", ".jar", null);
            file.deleteOnExit();

            FileUtils.copyFile(file, codebaseFile);
        } catch (Exception e) {
            throw new OpenEJBException(SafeToolkit.messages.format("cl0002", codebase, e.getMessage()));
        }
        return file;
    }
}
