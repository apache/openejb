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
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.loader;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Properties;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins </a>
 */
public class LoaderServlet extends HttpServlet {

    private Loader loader;

    public void init(ServletConfig config) throws ServletException {
        if (loader != null) {
            return;
        }

        // Do just enough to get the Tomcat Loader into the classpath
        // let it do the rest.
        Properties p = initParamsToProperties(config);

        String embeddingStyle = p.getProperty("openejb.loader");

        // Set the mandatory values for a webapp-only setup
        if (embeddingStyle.endsWith("tomcat-webapp")) {
            setPropertyIfNUll(p, "openejb.base", getWebappPath(config));
//            setPropertyIfNUll(p, "openejb.configuration", "META-INF/openejb.xml");
//            setPropertyIfNUll(p, "openejb.container.decorators", "org.openejb.tomcat.TomcatJndiSupport");
//            setPropertyIfNUll(p, "log4j.configuration", "META-INF/log4j.properties");
        }

        try {
            SystemInstance.init(p);
            Embedder embedder = new Embedder("org.openejb.tomcat.TomcatLoader");
            Class loaderClass = embedder.load();
            Object instance = loaderClass.newInstance();
            try {
                loader = (Loader) instance;
            } catch (ClassCastException e) {
                loader = new LoaderWrapper(instance);
            }

            loader.init(config);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        loader.service(request, response);
    }

    private String getWebappPath(ServletConfig config) {
        ServletContext ctx = config.getServletContext();
        File webInf = new File(ctx.getRealPath("WEB-INF"));
        File webapp = webInf.getParentFile();
        String webappPath = webapp.getAbsolutePath();
        return webappPath;
    }

    private Properties initParamsToProperties(ServletConfig config) {
        Properties p = new Properties();

        // Set some defaults
        p.setProperty("openejb.loader","tomcat");

        // Load in each init-param as a property
        Enumeration enumeration = config.getInitParameterNames();
        System.out.println("OpenEJB init-params:");
        while (enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            String value = config.getInitParameter(name);
            p.put(name, value);
            System.out.println("\tparam-name: " + name + ", param-value: " + value);
        }

        return p;
    }

    private Object setPropertyIfNUll(Properties properties, String key, String value){
        String currentValue = properties.getProperty(key);
        if (currentValue == null){
            properties.setProperty(key, value);
        }
        return currentValue;
    }

    /**
     * Ain't classloaders fun?
     * This class exists to reconcile that loader implementations
     * may exist in the parent classloader while the loader interface
     * is also in this classloader.  Use this class in the event that
     * this is the case.
     * Think of this as an adapter for adapting the parent's idea of a
     * Loader to our idea of a Loader.
     */
    public static class LoaderWrapper implements Loader {
        private final Object loader;
        private final Method init;
        private final Method service;

        public LoaderWrapper(Object loader)  {
            this.loader = loader;
            try {
                Class loaderClass = loader.getClass();
                this.init = loaderClass.getMethod("init", new Class[]{ServletConfig.class});
                this.service = loaderClass.getMethod("service", new Class[]{HttpServletRequest.class, HttpServletResponse.class});
            } catch (NoSuchMethodException e) {
                throw (IllegalStateException) new IllegalStateException("Signatures for Loader are no longer correct.").initCause(e);
            }
        }

        public void init(ServletConfig servletConfig) throws ServletException {
            try {
                init.invoke(loader, new Object[]{servletConfig});
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else if (cause instanceof Error) {
                    throw (Error) cause;
                } else {
                    throw (ServletException) cause;
                }
            } catch (Exception e) {
                throw new RuntimeException("Loader.init: "+e.getMessage()+e.getClass().getName()+": "+e.getMessage(), e);
            }
        }

        public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            try {
                service.invoke(loader, new Object[]{request, response});
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                } else if (cause instanceof Error) {
                    throw (Error) cause;
                } else if (cause instanceof IOException) {
                    throw (IOException) cause;
                } else {
                    throw (ServletException) cause;
                }
            } catch (Exception e) {
                throw new RuntimeException("Loader.service: "+e.getMessage()+e.getClass().getName()+": "+e.getMessage(), e);
            }
        }
    }
}
