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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.corba.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.system.serverinfo.ServerInfo;

import org.openejb.corba.compiler.CompilerException;
import org.openejb.corba.compiler.StubGenerator;
import org.openejb.util.FileUtils;


/**
 * @version $Revision$ $Date$
 */
public class OpenORBStubClassLoader extends ClassLoader implements GBeanLifecycle {

    private final static Log log = LogFactory.getLog(OpenORBStubClassLoader.class);
    private final static String PACKAGE_PREFIX = "org.omg.stub.";

    private final static int STOPPED = 0;
    private final static int STARTED = 1;
    private int state;
    private final StubGenerator stubGenerator;
    private final File cacheDir;
    private final Map parentToNameToLoaderMap = new HashMap();
    private final Map nameToClassMap = new HashMap();
    private static Random random = new Random(System.currentTimeMillis());

    public OpenORBStubClassLoader(ServerInfo serverInfo, StubGenerator stubGenerator, String cacheDir) {
        this.stubGenerator = stubGenerator;
        this.cacheDir = new File(serverInfo.resolvePath(cacheDir), "CORBA_STUB_CACHE");
        this.state = STOPPED;
    }

    public synchronized Class loadClass(String name) throws ClassNotFoundException {

        if (state == STOPPED) {
            throw new ClassNotFoundException("OpenORBStubClassLoader is stopped");
        }

        if (log.isDebugEnabled()) log.debug("Load class " + name);

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class result = null;
        try {
            result = classLoader.loadClass(name);
            return result;
        } catch (ClassNotFoundException e) {
            if (log.isDebugEnabled()) log.debug("Unable to load class from the context class loader");
        }

        if (name.startsWith(PACKAGE_PREFIX)) {

            Map nameToLoaderMap = (Map) parentToNameToLoaderMap.get(classLoader);
            ClassLoader loader = null;
            if (nameToLoaderMap == null) {
                nameToLoaderMap = new HashMap();
                parentToNameToLoaderMap.put(classLoader, nameToLoaderMap);
            } else {
                loader = (ClassLoader) nameToLoaderMap.get(name);
            }
            if (loader == null) {
                URL url = (URL) nameToClassMap.get(name);
                if (url == null) {
                    try {
                        File file = new File(cacheDir, "STUB_" + (random.nextLong()) + ".jar");
                        while (file.exists()) {
                            file = new File(cacheDir, "STUB_" + (random.nextLong()) + ".jar");
                        }

                        if (log.isDebugEnabled()) log.debug("Generating stubs in " + file.toString());

                        int begin = name.lastIndexOf('.') + 1;
                        String iPackage = name.substring(13, begin);
                        String iName = iPackage + name.substring(begin + 1, name.length() - 5);

                        stubGenerator.generateStubs(Collections.singleton(iName), file, classLoader);

                        url = file.toURL();
                        nameToClassMap.put(name, url);
                    } catch (IOException e) {
                        logAndThrow(e);
                    } catch (CompilerException e) {
                        logAndThrow(e);
                    }
                }
                loader = new URLClassLoader(new URL[]{url}, classLoader);
                nameToLoaderMap.put(name, loader);
            } else {
                if (log.isDebugEnabled()) log.debug("Found cached loader");
            }

            result = loader.loadClass(name);
            assert result != null;

            if (log.isDebugEnabled()) log.debug("result: " + result.getName());
            return result;

        }
        if (log.isDebugEnabled()) log.debug("Could not load class: " + name);
        throw new ClassNotFoundException("Could not load class: " + name);
    }

    private static void logAndThrow(Exception e) throws ClassNotFoundException {
        boolean shouldLog = true;
        StackTraceElement[] stackTrace = e.getStackTrace();
        for (int i = 0; i < stackTrace.length; i++) {
            StackTraceElement stackTraceElement = stackTrace[i];
            if (stackTraceElement.getClassName().equals("org.omg.CosNaming.NamingContextExtPOA") &&
                    stackTraceElement.getMethodName().equals("_invoke")) {
                shouldLog = false;
                break;
            }
        }
        if (shouldLog) {
            log.error("Unable to generate stub", e);
        }

        throw new ClassNotFoundException("Unable to generate stub", e);
    }

    public synchronized void doStart() throws Exception {
        FileUtils.recursiveDelete(cacheDir);
        cacheDir.mkdirs();

        UtilDelegateImpl.setClassLoader(this);

        this.state = STARTED;

        log.info("Started - caching in " + cacheDir);
    }

    public synchronized void doStop() throws Exception {
        this.state = STOPPED;

        parentToNameToLoaderMap.clear();
        nameToClassMap.clear();

        FileUtils.recursiveDelete(cacheDir);

        log.info("Stopped");
    }

    public synchronized void doFail() {
        this.state = STOPPED;

        FileUtils.recursiveDelete(cacheDir);

        log.info("Failed");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {

        GBeanInfoBuilder infoFactory = new GBeanInfoBuilder(OpenORBStubClassLoader.class, NameFactory.CORBA_SERVICE);
        infoFactory.addReference("ServerInfo", ServerInfo.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addReference("StubGenerator", StubGenerator.class, NameFactory.CORBA_SERVICE);
        infoFactory.addAttribute("cacheDir", String.class, true);
        infoFactory.addOperation("loadClass", new Class[]{String.class});
        infoFactory.setConstructor(new String[]{"ServerInfo", "StubGenerator", "cacheDir"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
