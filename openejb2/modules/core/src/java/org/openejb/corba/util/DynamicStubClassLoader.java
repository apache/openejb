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

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import net.sf.cglib.core.NamingPolicy;
import net.sf.cglib.core.Predicate;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.CallbackFilter;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.FixedValue;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.NoOp;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;


/**
 * @version $Revision$ $Date$
 */
public class DynamicStubClassLoader extends ClassLoader implements GBeanLifecycle {
    private final static Log log = LogFactory.getLog(DynamicStubClassLoader.class);
    private final static String PACKAGE_PREFIX = "org.omg.stub.";

    private boolean stopped = true;

    public synchronized Class loadClass(final String name) throws ClassNotFoundException {
        if (stopped) {
            throw new ClassNotFoundException("DynamicStubClassLoader is stopped");
        }

        if (log.isDebugEnabled()) {
            log.debug("Load class " + name);
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        // check if the stub already exists first
        try {
            return classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            if (log.isDebugEnabled()) {
                log.debug("Unable to load class from the context class loader");
            }
        }

        // if this is not a class from the org.omb.stub name space don't attempt to generate
        if (!name.startsWith(PACKAGE_PREFIX)) {
            if (log.isDebugEnabled()) {
                log.debug("Could not load class: " + name);
            }
            throw new ClassNotFoundException("Could not load class: " + name);
        }

        // load the interfaces class we are attempting to create a stub for
        Class iface = loadStubInterfaceClass(name, classLoader);

        // create the stub builder
        try {
            Enhancer enhancer = new Enhancer();
            enhancer.setSuperclass(ClientContextHolderStub.class);
            enhancer.setInterfaces(new Class[]{iface});
            enhancer.setCallbackFilter(FILTER);
            enhancer.setCallbackTypes(new Class[]{NoOp.class, MethodInterceptor.class, FixedValue.class});
            enhancer.setUseFactory(false);
            enhancer.setClassLoader(classLoader);
            enhancer.setNamingPolicy(new NamingPolicy() {
                public String getClassName(String s, String s1, Object o, Predicate predicate) {
                    return name;
                }
            });

            // generate the class
            Class result = enhancer.createClass();
            assert result != null;

            StubMethodInterceptor interceptor = new StubMethodInterceptor(iface);
            Ids ids = new Ids(iface);
            Enhancer.registerStaticCallbacks(result, new Callback[]{NoOp.INSTANCE, interceptor, ids});

            if (log.isDebugEnabled()) {
                log.debug("result: " + result.getName());
            }
            return result;
        } catch (RuntimeException e) {
            log.error("Unable to generate stub: " + name, e);
            throw e;
        } catch (Error e) {
            log.error("Unable to generate stub: " + name, e);
            throw e;
        }
    }

    private Class loadStubInterfaceClass(String name, ClassLoader classLoader) throws ClassNotFoundException {
        try {
            int begin = name.lastIndexOf('.') + 1;
            String iPackage = name.substring(13, begin);
            String iName = iPackage + name.substring(begin + 1, name.length() - 5);

            return classLoader.loadClass(iName);
        } catch (ClassNotFoundException e) {
            // don't log exceptions from CosNaming because it attempts to load every
            // class bound into the name server
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
    }

    private static final CallbackFilter FILTER = new CallbackFilter() {
        public int accept(Method method) {
            // we don't intercept non-public methods like finalize
            if (!Modifier.isPublic(method.getModifiers())) {
                return 0;
            }

            if (method.getReturnType().equals(String[].class) && method.getParameterTypes().length ==0 && method.getName().equals("_ids")){
                return 2;
            }

            if (Modifier.isAbstract(method.getModifiers())) {
                return 1;
            }

            return 0;
        }
    };

    private static final class Ids implements FixedValue {
        private final String[] typeIds;

        public Ids(Class type) {
            typeIds = Util.createCorbaIds(type);
        }

        public Object loadObject() throws Exception {
            return typeIds;
        }
    }

    public synchronized void doStart() throws Exception {
        UtilDelegateImpl.setClassLoader(this);
        stopped = false;
    }

    public synchronized void doStop() throws Exception {
        stopped = true;
        log.debug("Stopped");
    }

    public synchronized void doFail() {
        stopped = true;
        log.warn("Failed");
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(DynamicStubClassLoader.class, NameFactory.CORBA_SERVICE);
        infoFactory.addOperation("loadClass", new Class[]{String.class});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
