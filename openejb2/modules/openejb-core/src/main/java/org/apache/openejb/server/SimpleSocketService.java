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
package org.apache.openejb.server;

import org.activeio.xnet.ServerService;
import org.activeio.xnet.ServiceException;
import org.activeio.xnet.ServiceLogger;
import org.activeio.xnet.ServicePool;
import org.activeio.xnet.SocketService;
import org.activeio.xnet.hba.IPAddressPermission;
import org.activeio.xnet.hba.ServiceAccessController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.kernel.ClassLoading;
import org.apache.openejb.DeploymentIndex;
import org.apache.openejb.OpenEJB;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.Socket;

/**
 * @version $Revision$ $Date$
 */
public class SimpleSocketService implements SocketService, GBeanLifecycle {
    private static final Log log = LogFactory.getLog(SimpleSocketService.class);
    private final ServerService server;

    public SimpleSocketService(String serviceClassName, IPAddressPermission[] onlyFrom, DeploymentIndex deploymentIndex, ClassLoader cl) throws Exception {
        ServerService service;

        Class serviceClass = ClassLoading.loadClass(serviceClassName, cl);
        if (!serviceClass.isAssignableFrom(serviceClass)) {
            throw new ServiceException("Server service class does not implement " + ServerService.class.getName() + ": " + serviceClassName);
        }
        try {
            Constructor constructor = serviceClass.getConstructor(new Class[]{DeploymentIndex.class});
            service = (ServerService) constructor.newInstance(new Object[]{deploymentIndex});
        } catch (Exception e) {
            throw new ServiceException("Error constructing server service class", e);
        }

        String name = "ejb";
        int threads = 20;
        int priority = Thread.NORM_PRIORITY;
        String[] logOnSuccess = new String[]{"HOST", "NAME", "THREADID", "USERID"};
        String[] logOnFailure = new String[]{"HOST", "NAME"};

        service = new ServicePool(service, name, threads, priority);
        service = new ServiceAccessController(name, service, onlyFrom);
        service = new ServiceLogger(name, service, logOnSuccess, logOnFailure);
        server = service;

        // TODO Horrid hack, the concept needs to survive somewhere
        if (OpenEJB.getApplicationServer() == null) {
            OpenEJB.setApplicationServer(new ServerFederation());
        }
    }

    public synchronized void doStart() throws ServiceException {
        server.start();
    }

    public synchronized void doStop() throws ServiceException {
        server.stop();
    }

    public void doFail() {
        try {
            server.stop();
        } catch (ServiceException e) {
            log.error("Could not clean up simple socket service");
        }
    }

    public void service(Socket socket) throws ServiceException, IOException {
        server.service(socket);
    }

    public String getName() {
        return server.getName();
    }

}
