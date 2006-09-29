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
package org.apache.openejb.cluster.sfsb;

import java.io.Serializable;
import javax.ejb.EnterpriseBean;
import javax.ejb.SessionBean;

import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.StatefulEjbContainer;
import org.apache.openejb.StatefulEjbDeployment;
import org.apache.openejb.cluster.server.ClusteredInstanceContextFactory;
import org.apache.openejb.cluster.server.EJBClusterManager;
import org.apache.openejb.cluster.server.EJBInstanceContextRecreator;
import org.apache.openejb.cluster.server.ServerMetaDataArrayHolder;
import org.apache.openejb.proxy.EJBProxyFactory;
import org.apache.openejb.sfsb.StatefulInstanceContextFactory;

/**
 * @version $Revision$ $Date$
 */
public class ClusteredSFInstanceContextFactory extends StatefulInstanceContextFactory
        implements ClusteredInstanceContextFactory {
    private transient EJBClusterManager clusterManager;
    private final SFInstanceContextRecreator recreator;
    private transient ServerMetaDataArrayHolder serversHolder;

    public ClusteredSFInstanceContextFactory(StatefulEjbDeployment statefulEjbDeployment,
            StatefulEjbContainer statefulEjbContainer,
            EJBProxyFactory proxyFactory) {
        super(statefulEjbDeployment, statefulEjbContainer, proxyFactory);
        recreator = new SFInstanceContextRecreator();
    }

    public void setClusterManager(EJBClusterManager clusterManager) {
        this.clusterManager = clusterManager;
    }

    public void setServersHolder(ServerMetaDataArrayHolder serversHolder) {
        this.serversHolder = serversHolder;
    }

    public EJBInstanceContextRecreator getInstanceContextRecreator() {
        return recreator;
    }

    public EJBInstanceContext newInstance() throws Exception {
        SessionBean bean = createInstance();
        String id = clusterManager.addInstance(bean, statefulEjbDeployment.getContainerId());

        return newInstanceContext(id, bean);
    }

    private EJBInstanceContext newInstanceContext(String id, SessionBean bean) {
        return new ClusteredSFInstanceContext(
                statefulEjbDeployment,
                statefulEjbContainer,
                bean,
                id,
                proxyFactory,
                serversHolder);
    }

    private class SFInstanceContextRecreator implements EJBInstanceContextRecreator, Serializable {
        private static final long serialVersionUID = -3075688417789981035L;

        public EJBInstanceContext recreate(Object id, EnterpriseBean bean) {
            if (!(id instanceof String)) {
                throw new IllegalArgumentException("id must be a String");
            }
            if (!(bean instanceof SessionBean)) {
                throw new IllegalArgumentException("bean must be a SessionBean");
            }
            return newInstanceContext((String) id, (SessionBean) bean);
        }
    }
}