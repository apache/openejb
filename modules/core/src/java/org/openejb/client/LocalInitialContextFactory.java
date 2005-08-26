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
 * Copyright 2002 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.client;

import org.openejb.loader.OpenEJBInstance;
import org.openejb.loader.SystemInstance;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import java.util.Hashtable;
import java.util.Properties;

/**
 * LocalInitialContextFactory
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins </a>
 * @since 10/5/2002
 */
public class LocalInitialContextFactory implements javax.naming.spi.InitialContextFactory {

    static Context intraVmContext;
    private static OpenEJBInstance openejb;

    public Context getInitialContext(Hashtable env) throws javax.naming.NamingException {
        if (intraVmContext == null) {
            try {
                Properties properties = new Properties();
                properties.putAll(env);
                init(properties);
            } catch (Exception e) {
                throw (NamingException) new NamingException("Attempted to load OpenEJB. " + e.getMessage()).initCause(e);
            }
            intraVmContext = getIntraVmContext(env);
        }
        return intraVmContext;
    }

    public void init(Properties properties) throws Exception {
        if (openejb != null) return;
        SystemInstance.init(properties);
        openejb = new OpenEJBInstance();
        if (openejb.isInitialized()) return;
        openejb.init(properties);
    }

    private Context getIntraVmContext(Hashtable env) throws javax.naming.NamingException {
        Context context = null;
        try {
            InitialContextFactory factory = null;
            ClassLoader cl = SystemInstance.get().getClassLoader();
            Class ivmFactoryClass = Class.forName("org.openejb.core.ivm.naming.InitContextFactory", true, cl);

            factory = (InitialContextFactory) ivmFactoryClass.newInstance();
            context = factory.getInitialContext(env);
        } catch (Exception e) {
            throw new javax.naming.NamingException("Cannot instantiate an IntraVM InitialContext. Exception: "
                    + e.getClass().getName() + " " + e.getMessage());
        }

        return context;
    }
}

