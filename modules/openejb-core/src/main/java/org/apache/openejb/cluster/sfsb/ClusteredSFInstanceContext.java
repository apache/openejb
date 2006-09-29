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

import javax.ejb.SessionBean;

import org.apache.openejb.StatefulEjbContainer;
import org.apache.openejb.StatefulEjbDeployment;
import org.apache.openejb.client.ServerMetaData;
import org.apache.openejb.cluster.server.ClusteredEJBInstanceContext;
import org.apache.openejb.cluster.server.ServerMetaDataArrayHolder;
import org.apache.openejb.proxy.EJBProxyFactory;
import org.apache.openejb.sfsb.StatefulInstanceContext;

/**
 * @version $Revision$ $Date$
 */
public class ClusteredSFInstanceContext extends StatefulInstanceContext implements ClusteredEJBInstanceContext {
    private ServerMetaDataArrayHolder serversHolder;

    public ClusteredSFInstanceContext(StatefulEjbDeployment statefulEjbDeployment,
            StatefulEjbContainer statefulEjbContainer,
            SessionBean instance,
            Object id,
            EJBProxyFactory proxyFactory,
            ServerMetaDataArrayHolder serversHolder) {
        super(statefulEjbDeployment,
                statefulEjbContainer,
                instance,
                id,
                proxyFactory
        );
        this.serversHolder = serversHolder;
    }

    public ServerMetaData[] getServers() {
        return serversHolder.getServers();
    }
}
