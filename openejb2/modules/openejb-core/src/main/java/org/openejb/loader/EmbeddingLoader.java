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
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
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
 * Copyright 2002 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.loader;

import java.util.Hashtable;

import org.openejb.util.ClasspathUtils;

/**
 *
 */
public class EmbeddingLoader implements Loader {

    static boolean loaded = false;

    /**
     * Checks to see if OpenEJB is available through classpath.
     * If it isn't, then the required libraries are
     * added and OpenEJB is pulled in and instantiated.
     *
     * @param env
     * @exception Exception
     */
    public void load(Hashtable env) throws Exception {
        if (loaded) return;

        ClassLoader cl = ClasspathUtils.getContextClassLoader();
        try {
            cl.loadClass("org.openejb.OpenEJB");
        } catch (Exception e) {
            importOpenEJBLibraries(env);
        }
        try {
            Loader embedded = null;

            Class loaderClass = cl.loadClass("org.openejb.loader.EmbeddedLoader");

            embedded = (Loader) loaderClass.newInstance();
            embedded.load(env);

        } catch (Exception e) {
            throw new Exception("Cannot embed OpenEJB. Exception: " + e.getClass().getName(), e);
        }
        loaded = true;
    }

    // Sets the openejb.home system variable
    private void importOpenEJBLibraries(Hashtable env) throws Exception {
        // Sets the openejb.home system variable
        try {
            if (env.get("openejb.home") != null) {
                System.setProperty("openejb.home", (String) env.get("openejb.home"));
            }
        } catch (Exception e) {
        }

        try {
            // Loads all the libraries in the openejb.home/lib directory
            org.openejb.util.ClasspathUtils.addJarsToPath("lib");

            // Loads all the libraries in the openejb.home/dist directory
            org.openejb.util.ClasspathUtils.addJarsToPath("dist");

        } catch (Exception e) {
            throw new Exception("Could not load OpenEJB libraries. Exception: " +
                    e.getClass().getName() + " " + e.getMessage());
        }
    }
}
