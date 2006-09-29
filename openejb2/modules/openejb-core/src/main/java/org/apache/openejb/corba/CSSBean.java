/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
