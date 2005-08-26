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

import org.openejb.util.FileUtils;

import java.io.File;
import java.util.Enumeration;
import java.util.Properties;
import java.lang.reflect.Method;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins </a>
 */
public class LoaderServlet extends HttpServlet {
    private OpenEJBInstance openejb;

    public void init(ServletConfig config) throws ServletException {
        Properties p = new Properties();
        p.setProperty("openejb.loader","tomcat");

        Enumeration enum = config.getInitParameterNames();
        System.out.println("OpenEJB init-params:");
        while (enum.hasMoreElements()) {
            String name = (String) enum.nextElement();
            String value = config.getInitParameter(name);
            p.put(name, value);
            System.out.println("\tparam-name: " + name + ", param-value: " + value);
        }

        String loader = p.getProperty("openejb.loader"); // Default loader set above
        if (loader.endsWith("tomcat-webapp")) {
            ServletContext ctx = config.getServletContext();
            File webInf = new File(ctx.getRealPath("WEB-INF"));
            File webapp = webInf.getParentFile();
            String webappPath = webapp.getAbsolutePath();

            setPropertyIfNUll(p, "openejb.base", webappPath);
            setPropertyIfNUll(p, "openejb.configuration", "META-INF/openejb.xml");
            setPropertyIfNUll(p, "openejb.container.decorators", "org.openejb.core.TomcatJndiSupport");
            setPropertyIfNUll(p, "log4j.configuration", "META-INF/log4j.properties");
        }

        try {
            init(p);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void init(Properties properties) throws Exception {
        if (openejb != null) return;
        SystemInstance.init(properties);
        openejb = new OpenEJBInstance();
        if (openejb.isInitialized()) return;
        openejb.init(properties);
    }

    private Object setPropertyIfNUll(Properties properties, String key, String value){
        String currentValue = properties.getProperty(key);
        if (currentValue == null){
            properties.setProperty(key, value);
        }
        return currentValue;
    }
}
