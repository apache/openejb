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
package org.apache.openejb.assembler;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Properties;

import org.apache.openejb.EJBComponentType;
import org.apache.openejb.OpenEJBException;

public class ContainerBuilder implements RpcContainer {

    private Object containerId = null;
    private HashMap deployments = new HashMap();

    public void init(Object containerId, HashMap deploymentsMap, Properties properties)
            throws OpenEJBException {

        setupJndi();


        this.containerId = containerId;

        Object[] deploys = deploymentsMap.values().toArray();

        for (int i = 0; i < deploys.length; i++) {
            CoreDeploymentInfo info = (CoreDeploymentInfo) deploys[i];
            deploy(info.getDeploymentID(), info);
        }
    }

    private void setupJndi() {
        /* Add Geronimo JNDI service ///////////////////// */
        String str = System.getProperty(javax.naming.Context.URL_PKG_PREFIXES);
        if (str == null)
            str = ":org.apache.geronimo.naming";
        else
            str = str + ":org.apache.geronimo.naming";
        System.setProperty(javax.naming.Context.URL_PKG_PREFIXES, str);
    }

    public Object invoke(
            Object deployID,
            Method callMethod,
            Object[] args,
            Object primKey,
            Object securityIdentity)
            throws OpenEJBException {
        return null;
    }

    public int getContainerType() {
        return EJBComponentType.STATELESS;
    }

    public org.apache.openejb.assembler.DeploymentInfo getDeploymentInfo(Object deploymentID) {
        return (DeploymentInfoWrapper) deployments.get(deploymentID);
    }

    public org.apache.openejb.assembler.DeploymentInfo[] deployments() {
        return (CoreDeploymentInfo[]) deployments.values().toArray(new DeploymentInfoWrapper[0]);

    }

    public void deploy(Object deploymentID, org.apache.openejb.assembler.DeploymentInfo info)
            throws OpenEJBException {
        ((org.apache.openejb.assembler.CoreDeploymentInfo) info).setContainer(this);
        deployments.put(info.getDeploymentID(), new DeploymentInfoWrapper(info));
    }

    public Object getContainerID() {
        return containerId;
    }

    static class DeploymentInfoWrapper extends CoreDeploymentInfo {

        public DeploymentInfoWrapper(org.apache.openejb.assembler.DeploymentInfo deploymentInfo) {
            this((CoreDeploymentInfo) deploymentInfo);
        }

        public DeploymentInfoWrapper(CoreDeploymentInfo di) {
        }

    }

}
