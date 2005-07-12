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

import java.net.URLClassLoader;
import java.net.URL;
import java.io.File;

/**
 * @version $Revision$ $Date$
 */
/*-------------------------------------------------------*/
/* System ClassLoader Support */
/*-------------------------------------------------------*/
public class SystemClassPath extends BasicURLClassPath {

    private URLClassLoader sysLoader;

    public void addJarsToPath(File dir) throws Exception {
        this.addJarsToPath(dir, getSystemLoader());
        this.rebuildJavaClassPathVariable();
    }

    public void addJarToPath(URL jar) throws Exception {
        //System.out.println("[|] SYSTEM "+jar.toExternalForm());
        this.addJarToPath(jar, getSystemLoader());
        this.rebuildJavaClassPathVariable();
    }

    public ClassLoader getClassLoader() {
        try {
            return getSystemLoader();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private URLClassLoader getSystemLoader() throws Exception {
        if (sysLoader == null) {
            sysLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        }
        return sysLoader;
    }

    private void rebuildJavaClassPathVariable() throws Exception {
        sun.misc.URLClassPath cp = getURLClassPath(getSystemLoader());
        URL[] urls = cp.getURLs();
        //for (int i=0; i < urls.length; i++){
        //    System.out.println(urls[i].toExternalForm());
        //}
        if (urls.length < 1)
            return;

        StringBuffer path = new StringBuffer(urls.length * 32);

        File s = new File(urls[0].getFile());
        path.append(s.getPath());
        //System.out.println(s.getPath());

        for (int i = 1; i < urls.length; i++) {
            path.append(File.pathSeparator);

            s = new File(urls[i].getFile());
            //System.out.println(s.getPath());
            path.append(s.getPath());
        }
        try {
            System.setProperty("java.class.path", path.toString());
        } catch (Exception e) {
        }
    }
}
