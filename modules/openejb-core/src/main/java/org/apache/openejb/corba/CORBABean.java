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
import org.apache.geronimo.security.SecurityService;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.apache.openejb.corba.security.config.ConfigAdapter;
import org.apache.openejb.corba.security.config.ConfigException;
import org.apache.openejb.corba.security.config.tss.TSSConfig;
import org.apache.openejb.corba.util.Util;

import javax.ejb.spi.HandleDelegate;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Properties;


/**
 * @version $Revision$ $Date$
 */
public class CORBABean implements GBeanLifecycle, ORBRef {

    private final Log log = LogFactory.getLog(CORBABean.class);

    private final ClassLoader classLoader;
    private final Executor threadPool;
    private final ConfigAdapter configAdapter;
    private TSSConfig tssConfig;
    private ORB orb;
    private POA rootPOA;
    private ArrayList args = new ArrayList();
    private Properties props = new Properties();

    public CORBABean() {
        this.classLoader = null;
        this.threadPool = null;
        this.configAdapter = null;
    }

    public CORBABean(String configAdapter, ClassLoader classLoader, Executor threadPool, SecurityService securityService, SunNameService nameService) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        this.classLoader = classLoader;
        this.threadPool = threadPool;
        if (configAdapter != null) {
            this.configAdapter = (ConfigAdapter) classLoader.loadClass(configAdapter).newInstance();
        } else {
            this.configAdapter = null;
        }
        //security service included to force start order.
        //name service included to force start order.
    }

    public TSSConfig getTssConfig() {
        return tssConfig;
    }

    public void setTssConfig(TSSConfig config) {
        if (config == null) config = new TSSConfig();
        this.tssConfig = config;
    }

    public ORB getORB() {
        return orb;
    }

    public HandleDelegate getHandleDelegate() {
        return new CORBAHandleDelegate();
    }

    public POA getRootPOA() {
        return rootPOA;
    }

    public ArrayList getArgs() {
        return args;
    }

    public void setArgs(ArrayList args) {
        if (args == null) args = new ArrayList();
        this.args = args;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        if (props == null) props = new Properties();
        this.props = props;
    }


    public InetSocketAddress getListenAddress() {
        try {
            if (configAdapter != null) {
                return configAdapter.getDefaultListenAddress(tssConfig, orb);
            } else {
                log.debug("Don't know what default listen address is for an ORB without a configAdapter");
            }
        } catch (ConfigException e) {
            log.debug("Unable to calculate default listen address", e);
        }
        return null;
    }

    public void doStart() throws Exception {

        ClassLoader savedLoader = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(classLoader);

            if (configAdapter != null) {
                orb = ORB.init(configAdapter.translateToArgs(tssConfig, args), configAdapter.translateToProps(tssConfig, props));
                configAdapter.postProcess(tssConfig, orb);
            } else {
                orb = ORB.init((String[]) args.toArray(new String[args.size()]), props);
            }

            Util.setORB(orb);

            org.omg.CORBA.Object obj = orb.resolve_initial_references("RootPOA");

            rootPOA = POAHelper.narrow(obj);

        } finally {
            Thread.currentThread().setContextClassLoader(savedLoader);
        }

        log.debug("Started CORBABean");
    }

    public void doStop() throws Exception {
        orb.destroy();
        log.debug("Stopped CORBABean");
    }

    public void doFail() {
        log.warn("Failed CORBABean");
    }

}
