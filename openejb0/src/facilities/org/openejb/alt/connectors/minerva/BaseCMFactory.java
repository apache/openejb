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
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.alt.connectors.minerva;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Properties;

import javax.resource.spi.ManagedConnectionFactory;
import javax.security.auth.Subject;
import javax.transaction.TransactionManager;

import org.openejb.OpenEJB;
import org.openejb.OpenEJBException;
import org.openejb.spi.ConnectionManagerConfig;
import org.openejb.spi.ConnectionManagerFactory;
import org.openejb.spi.OpenEJBConnectionManager;
import org.openejb.util.proxy.ProxyManager;
import org.opentools.minerva.connector.BaseConnectionManager;
import org.opentools.minerva.connector.ServerSecurityManager;
import org.opentools.minerva.connector.ServerTransactionManager;
import org.opentools.minerva.pool.PoolParameters;

/**
 * Abstract base class for Minerva connection manager factories.  The logic is
 * the same for all the factories; the only difference is which implementation
 * of ConnectionManager they return.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public abstract class BaseCMFactory implements ConnectionManagerFactory {
    private PrintWriter logger;
    private BaseConnectionManager mgr;
    private HashSet factories;

    public BaseCMFactory() {
        factories = new HashSet();
        mgr = createConnectionManager();
        mgr.setTransactionManager(new ServerTransactionManager() {
            public TransactionManager getTransactionManager(ManagedConnectionFactory p0, String p1) {
                return OpenEJB.getTransactionManager();
            }
        });
        mgr.setSecurityManager(new ServerSecurityManager() {
            public Subject getSubject(ManagedConnectionFactory p0, String p1) {
                Object securityIdentity = OpenEJB.getSecurityService().getSecurityIdentity();
                return (Subject)OpenEJB.getSecurityService().translateTo(securityIdentity, Subject.class);
            }
        });
    }

    /**
     * Creates a Minerva ConnectionManager of the appropriate type for this
     * factory.
     */
    protected abstract BaseConnectionManager createConnectionManager();

    /**
     * Sets the logger to use for this factory and its ConnectionManager.
     */
    public void setLogWriter(PrintWriter logger) {
        this.logger = logger;
    }

    /**
     * Configures this factory.  There is not currently any configuration
     * necessary, so this method does nothing.
     */
    public void setProperties(Properties props) {
        if(props.size() > 0) {
            System.out.println(getClass().getName()+" can't handle "+props.size()+" properties.");
        }
    }

    /**
     * Configures this factory's ConnectionManager instance for the specified
     * factory, and returns it.  This method always returns a proxy wrapping
     * the same ConnectionManager implementation.  The proxy is there to manage
     * the setting for container managed vs. bean managed sign on to the
     * resource adapter.
     */
    public OpenEJBConnectionManager createConnectionManager(
               String name, ConnectionManagerConfig config,
               ManagedConnectionFactory factory) throws OpenEJBException {

        // Note that this can be called twice per factory, once for a
        // container-managed sign-in configuration, and once for a bean-managed
        // sign-in factory.  Only add the pool once - we assume all other
        // parameters are the same.
        if(!factories.contains(factory)) {
            factories.add(factory);
            Properties props = config.properties;
            PoolParameters params = new PoolParameters();
            String s = props.getProperty(PoolParameters.BLOCKING_KEY);
            if(s != null) try {params.blocking = new Boolean(s).booleanValue();} catch(Exception e) {}
            s = props.getProperty(PoolParameters.GC_ENABLED_KEY);
            if(s != null) try {params.gcEnabled = new Boolean(s).booleanValue();} catch(Exception e) {}
            s = props.getProperty(PoolParameters.GC_INTERVAL_MS_KEY);
            if(s != null) try {params.gcIntervalMillis = Long.parseLong(s);} catch(Exception e) {}
            s = props.getProperty(PoolParameters.GC_MIN_IDLE_MS_KEY);
            if(s != null) try {params.gcMinIdleMillis = Long.parseLong(s);} catch(Exception e) {}
            s = props.getProperty(PoolParameters.IDLE_TIMEOUT_ENABLED_KEY);
            if(s != null) try {params.idleTimeoutEnabled = new Boolean(s).booleanValue();} catch(Exception e) {}
            s = props.getProperty(PoolParameters.IDLE_TIMEOUT_MS_KEY);
            if(s != null) try {params.idleTimeoutMillis = Long.parseLong(s);} catch(Exception e) {}
            s = props.getProperty(PoolParameters.INVALIDATE_ON_ERROR_KEY);
            if(s != null) try {params.invalidateOnError = new Boolean(s).booleanValue();} catch(Exception e) {}
            s = props.getProperty(PoolParameters.MAX_IDLE_TIMEOUT_PERCENT_KEY);
            if(s != null) try {params.maxIdleTimeoutPercent = new Float(s).floatValue();} catch(Exception e) {}
            s = props.getProperty(PoolParameters.MAX_SIZE_KEY);
            if(s != null) try {params.maxSize = Integer.parseInt(s);} catch(Exception e) {}
            s = props.getProperty(PoolParameters.MIN_SIZE_KEY);
            if(s != null) try {params.minSize = Integer.parseInt(s);} catch(Exception e) {}
            s = props.getProperty(PoolParameters.LOGGER_ENABLED);
            if(s == null || s.equalsIgnoreCase("true")) {
                params.logger = logger;
            }

            // Check pool configuration
            String configuration = props.getProperty(MinervaSharedLocalCM.POOL_CONFIGURATION_KEY);
            boolean perFactory = BaseConnectionManager.DEFAULT_POOL_PER_FACTORY;
            if(configuration != null) {
                if(configuration.equals(BaseConnectionManager.POOL_CONFIG_VALUE_PER_FACTORY)) {
                    perFactory = true;
                } else if(configuration.equals(BaseConnectionManager.POOL_CONFIG_VALUE_PER_USER)) {
                    perFactory = false;
                }
            }
            if(perFactory) {
                mgr.createPerFactoryPool(factory, params, name);
            } else {
                mgr.createPerUserPool(factory, params, name);
            }
        }

        try {
            return (OpenEJBConnectionManager)ProxyManager.newProxyInstance(OpenEJBConnectionManager.class,
                      new MinervaAuthenticationInvocationHandler((OpenEJBConnectionManager)mgr, config.containerManagedSignOn));
        } catch(IllegalAccessException e) {
            throw new OpenEJBException("Unable to create ConnectionManager proxy: "+e);
        }
    }
}