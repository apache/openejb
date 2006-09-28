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
 * Copyright 2004-2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id: CSSBean.java 446446 2006-09-11 20:19:03Z kevan $
 */
package org.apache.openejb.corba;

import edu.emory.mathcs.backport.java.util.concurrent.Executor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UserException;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContextExt;
import org.omg.CosNaming.NamingContextExtHelper;
import org.apache.openejb.corba.security.config.ConfigAdapter;
import org.apache.openejb.corba.security.config.css.CSSConfig;
import org.apache.openejb.corba.transaction.ClientTransactionPolicyConfig;
import org.apache.openejb.corba.transaction.nodistributedtransactions.NoDTxClientTransactionPolicyConfig;

import java.net.URI;
import java.util.ArrayList;
import java.util.Properties;
import javax.transaction.TransactionManager;


/**
 * @version $Revision$ $Date$
 */
public class CSSBean implements GBeanLifecycle {

    private final static Log log = LogFactory.getLog(CSSBean.class);

    private final ClassLoader classLoader;
    private final Executor threadPool;
    private final ConfigAdapter configAdapter;
    private final TransactionManager transactionManager;
    private String description;
    private CSSConfig nssConfig;
    private CSSConfig cssConfig;
    private ORB nssORB;
    private ORB cssORB;
    private ArrayList nssArgs;
    private ArrayList cssArgs;
    private Properties nssProps;
    private Properties cssProps;
    private ClientContext context;

    public CSSBean() {
        this.classLoader = null;
        this.threadPool = null;
        this.configAdapter = null;
        this.transactionManager = null;
    }

    public CSSBean(String configAdapter, Executor threadPool, TransactionManager transactionManager, ClassLoader classLoader) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        this.classLoader = classLoader;
        this.threadPool = threadPool;
        this.transactionManager = transactionManager;
        this.configAdapter = (ConfigAdapter) classLoader.loadClass(configAdapter).newInstance();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CSSConfig getNssConfig() {
        return nssConfig;
    }

    public void setNssConfig(CSSConfig nssConfig) {
        this.nssConfig = nssConfig;
    }

    public CSSConfig getCssConfig() {
        return cssConfig;
    }

    public void setCssConfig(CSSConfig config) {
        if (config == null) config = new CSSConfig();
        this.cssConfig = config;
    }

    public ORB getORB() {
        return cssORB;
    }

    public ArrayList getNssArgs() {
        return nssArgs;
    }

    public void setNssArgs(ArrayList nssArgs) {
        this.nssArgs = nssArgs;
    }

    public ArrayList getCssArgs() {
        return cssArgs;
    }

    public void setCssArgs(ArrayList cssArgs) {
        if (cssArgs == null) cssArgs = new ArrayList();
        this.cssArgs = cssArgs;
    }

    public Properties getNssProps() {
        return nssProps;
    }

    public void setNssProps(Properties nssProps) {
        this.nssProps = nssProps;
    }

    public Properties getCssProps() {
        return cssProps;
    }

    public void setCssProps(Properties cssProps) {
        if (cssProps == null) cssProps = new Properties();
        this.cssProps = cssProps;
    }

    public org.omg.CORBA.Object getHome(URI nsURI, String name) {

        if (log.isDebugEnabled())
            log.debug(description + " - Looking up home from " + nsURI.toString() + " at " + name);

        try {
            org.omg.CORBA.Object ref = nssORB.string_to_object(nsURI.toString());
            NamingContextExt ic = NamingContextExtHelper.narrow(ref);

            NameComponent[] nameComponent = ic.to_name(name);
            org.omg.CORBA.Object bean = ic.resolve(nameComponent);
            String beanIOR = nssORB.object_to_string(bean);

            ClientContext oldClientContext = ClientContextManager.getClientContext();
            try {
                ClientContextManager.setClientContext(context);
                bean = cssORB.string_to_object(beanIOR);
            } finally {
                ClientContextManager.setClientContext(oldClientContext);
            }

            return bean;
        } catch (UserException ue) {
            log.error(description + " - Looking up home", ue);
            throw new RuntimeException(ue);
        }
    }

    public void doStart() throws Exception {

        if (cssConfig == null) {
            cssConfig = new CSSConfig();
        }
        if (cssArgs == null) {
            cssArgs = new ArrayList();
        }
        if (cssProps == null) {
            cssProps = new Properties();
        }

        if (nssConfig == null) {
            if (log.isDebugEnabled()) log.debug("Defaulting NSS config to be CSS config");
            nssConfig = cssConfig;
        }
        if (nssArgs == null) {
            if (log.isDebugEnabled()) log.debug("Defaulting NSS args to be CSS args");
            nssArgs = cssArgs;
        }
        if (nssProps == null) {
            if (log.isDebugEnabled()) log.debug("Defaulting NSS props to be CSS props");
            nssProps = cssProps;
        }

        ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);

            if (log.isDebugEnabled()) log.debug("Starting NameService ORB");

            nssORB = ORB.init(configAdapter.translateToArgs(nssConfig, nssArgs), configAdapter.translateToProps(nssConfig, nssProps));
            configAdapter.postProcess(nssConfig, nssORB);

            if (log.isDebugEnabled()) log.debug("Starting CSS ORB");

            cssORB = ORB.init(configAdapter.translateToArgs(cssConfig, cssArgs), configAdapter.translateToProps(cssConfig, cssProps));
            configAdapter.postProcess(cssConfig, cssORB);

            context = new ClientContext();
            context.setSecurityConfig(cssConfig);
            context.setTransactionConfig(buildClientTransactionPolicyConfig());

        } finally {
            Thread.currentThread().setContextClassLoader(savedLoader);
        }

        log.debug("Started CORBA Client Security Server - " + description);
    }

    private ClientTransactionPolicyConfig buildClientTransactionPolicyConfig() {
        return new NoDTxClientTransactionPolicyConfig(transactionManager);
    }

    public void doStop() throws Exception {
        nssORB.destroy();
        cssORB.destroy();
        log.debug("Stopped CORBA Client Security Server - " + description);
    }

    public void doFail() {
        log.debug("Failed CORBA Client Security Server " + description);
    }

}
