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
package org.apache.openejb.server.ejbd;


import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.Properties;

import org.activeio.xnet.ServerService;
import org.activeio.xnet.ServiceException;
import org.apache.openejb.DeploymentIndex;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.client.EJBObjectProxyHandle;
import org.apache.openejb.server.ServerFederation;

/**
 * @since 11/25/2001
 */
public class EjbServer implements ServerService {
    private EjbDaemon ejbDaemon;

    static {
        // TODO Horrid hack, the concept needs to survive somewhere
        if (OpenEJB.getApplicationServer() == null) {
            OpenEJB.setApplicationServer(new ServerFederation());
            EJBObjectProxyHandle.client = false;
        }
    }

    public EjbServer() throws Exception {
        ejbDaemon = EjbDaemon.getEjbDaemon();
    }

    public EjbServer(DeploymentIndex deploymentIndex, Collection orbRefs) throws Exception {
        ejbDaemon = new EjbDaemon(deploymentIndex, orbRefs);
    }

    public void init(Properties props) throws Exception {
    }

    public void service(Socket socket) throws ServiceException, IOException {
        ServerFederation.setApplicationServer(ejbDaemon);
        ejbDaemon.service(socket);
    }

    public void start() throws ServiceException {
    }

    public void stop() throws ServiceException {
    }

    public String getName() {
        return "ejbd";
    }

    public int getPort() {
        return 0;
    }

    public String getIP() {
        return "";
    }

}
