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
package org.apache.openejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import javax.management.ObjectName;

import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.management.EJB;
import org.apache.geronimo.management.EJBModule;
import org.apache.geronimo.management.J2EEApplication;
import org.apache.geronimo.management.J2EEServer;

/**
 * @version $Revision$ $Date$
 */
public class EJBModuleImpl implements EJBModule {
    private final J2EEServer server;
    private final J2EEApplication application;
    private final String deploymentDescriptor;
    private final String objectName;

    private final Collection ejbs;

    public EJBModuleImpl(String objectName, J2EEServer server, J2EEApplication application, String deploymentDescriptor, Collection ejbs) {
        this.objectName = objectName;
        ObjectName myObjectName = ObjectNameUtil.getObjectName(objectName);
        verifyObjectName(myObjectName);

        this.server = server;
        this.application = application;
        this.deploymentDescriptor = deploymentDescriptor;
        this.ejbs = ejbs;
    }

    public String getObjectName() {
        return objectName;
    }

    public boolean isStateManageable() {
        return true;
    }

    public boolean isStatisticsProvider() {
        return false;
    }

    public boolean isEventProvider() {
        return true;
    }

    public String getDeploymentDescriptor() {
        return deploymentDescriptor;
    }

    public String getServer() {
        return server.getObjectName();
    }

    public String getApplication() {
        if (application == null) {
            return null;
        }
        return application.getObjectName();
    }

    public String[] getJavaVMs() {
        return server.getJavaVMs();
    }

    public String[] getEjbs() {
        if (ejbs == null) return new String[0];

        ArrayList copy;
        synchronized (ejbs) {
            copy = new ArrayList(ejbs);
        }
        String[] result = new String[copy.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = ((EJB) copy.get(i)).getObjectName();
        }
        return result;
    }

    /**
     * ObjectName must match this pattern:
     * <p/>
     * domain:j2eeType=EJBModule,name=MyName,J2EEServer=MyServer,J2EEApplication=MyApplication
     */
    private void verifyObjectName(ObjectName objectName) {
        if (objectName.isPattern()) {
            throw new InvalidObjectNameException("ObjectName can not be a pattern", objectName);
        }
        Hashtable keyPropertyList = objectName.getKeyPropertyList();
        if (!"EJBModule".equals(keyPropertyList.get("j2eeType"))) {
            throw new InvalidObjectNameException("EJBModule object name j2eeType property must be 'EJBModule'", objectName);
        }
        if (!keyPropertyList.containsKey("name")) {
            throw new InvalidObjectNameException("EJBModule object must contain a name property", objectName);
        }
        if (!keyPropertyList.containsKey("J2EEServer")) {
            throw new InvalidObjectNameException("EJBModule object name must contain a J2EEServer property", objectName);
        }
        if (!keyPropertyList.containsKey("J2EEApplication")) {
            throw new InvalidObjectNameException("EJBModule object name must contain a J2EEApplication property", objectName);
        }
        if (keyPropertyList.size() != 4) {
            throw new InvalidObjectNameException("EJBModule object name can only have j2eeType, name, J2EEApplication, and J2EEServer properties", objectName);
        }
    }
}
