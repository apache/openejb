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

import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.gbean.ReferenceCollection;
import org.apache.geronimo.gbean.ReferenceCollectionEvent;
import org.apache.geronimo.gbean.ReferenceCollectionListener;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.kernel.Kernel;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.net.URI;


/**
 * This class is a bit crufty.  Need something like this, but not static
 * and more along the lines of a collection of deployments registered as gbeans
 */
public class DeploymentIndex implements ReferenceCollectionListener, GBeanLifecycle {
    private static final Log log = LogFactory.getLog(DeploymentIndex.class);

    // todo delete me
    private static DeploymentIndex deploymentIndex = new DeploymentIndex();

    public static DeploymentIndex getInstance() {
        return deploymentIndex;
    }

    /**
     * The kernel in which this index is registered.  This is only needed due to the inability to control the order of
     * notifications in the kernel.
     */
    private final Kernel kernel;

    /**
     * The deployment lookup table.
     */
    private RpcEjbDeployment[] deployments = new RpcEjbDeployment[1];

    /**
     * Index from the deployment id to the index (Integer) number in the deployments lookup table
     */
    private final HashMap deploymentIdToIndex = new HashMap();

    /**
     * Index from jndi name to the index (Integer) number in the deployments lookup table
     */
    private final HashMap jndiNameToIndex = new HashMap();

    /**
     * GBean reference collection that we watch for new deployments to register
     */
    private ReferenceCollection ejbDeployments;

    protected DeploymentIndex() {
        kernel = null;
    }

    public DeploymentIndex(Collection ejbDeployments, Kernel kernel) {
        this.kernel = kernel;
        DeploymentIndex.deploymentIndex = this;
        this.ejbDeployments = (ReferenceCollection) ejbDeployments;
        this.ejbDeployments.addReferenceCollectionListener(this);
    }

    public synchronized void doStart() throws Exception {
        deployments = new RpcEjbDeployment[ejbDeployments.size() + 1];
        Iterator iterator = ejbDeployments.iterator();
        for (int i = 1; i < deployments.length && iterator.hasNext(); i++) {
            RpcEjbDeployment deployment = (RpcEjbDeployment) iterator.next();
            deployment = (RpcEjbDeployment) deployment.getUnmanagedReference();
            deployments[i] = deployment;
            deploymentIdToIndex.put(deployment.getContainerId(), new Integer(i));
            addJNDINames(deployment, i);
        }
    }

    private void addJNDINames(RpcEjbDeployment deployment, int i) {
        String[] jnidNames = deployment.getJndiNames();
        if (jnidNames != null) {
            for (int j = 0; j < jnidNames.length; j++) {
                String jnidName = jnidNames[j];
                jndiNameToIndex.put(jnidName, new Integer(i));
            }
        }
    }

    public synchronized void doStop() throws Exception {
        deploymentIdToIndex.clear();
        Arrays.fill(deployments, null);
        jndiNameToIndex.clear();
    }

    public synchronized void doFail() {
        deploymentIdToIndex.clear();
        Arrays.fill(deployments, null);
        jndiNameToIndex.clear();
    }

    public synchronized Integer addDeployment(RpcEjbDeployment deployment) {
        deployment = (RpcEjbDeployment) deployment.getUnmanagedReference();
        Object containerId = deployment.getContainerId();
        Integer index;
        if ((index = (Integer) deploymentIdToIndex.get(containerId)) != null) {
            return index;
        }

        int i = deployments.length;

        RpcEjbDeployment[] newArray = new RpcEjbDeployment[i + 1];
        System.arraycopy(deployments, 0, newArray, 0, i);
        deployments = newArray;

        deployments[i] = deployment;
        index = new Integer(i);
        deploymentIdToIndex.put(containerId, index);
        addJNDINames(deployment, i);
        return index;
    }

    public synchronized void removeDeployment(RpcEjbDeployment deployment) {
        Integer index = (Integer) deploymentIdToIndex.remove(deployment.getContainerId());
        if (index != null) {
            deployments[index.intValue()] = null;
        }

        String[] jnidNames = deployment.getJndiNames();
        for (int i = 0; i < jnidNames.length; i++) {
            String jnidName = jnidNames[i];
            jndiNameToIndex.remove(jnidName);
        }
    }

    public void memberAdded(ReferenceCollectionEvent event) {
        addDeployment((RpcEjbDeployment) event.getMember());
    }

    public void memberRemoved(ReferenceCollectionEvent event) {
        removeDeployment((RpcEjbDeployment) event.getMember());
    }

    public synchronized int length() {
        return deployments.length;
    }

    public synchronized int getDeploymentIndex(Object containerId) {
        return getDeploymentIndex((String) containerId);
    }

    public synchronized int getDeploymentIndex(String containerId) {
        Integer index = (Integer) deploymentIdToIndex.get(containerId);

        // try to fault in the deployment using the kernel directly
        if ((index == null)) {
            URI uri = URI.create(containerId);
            AbstractNameQuery abstractNameQuery = new AbstractNameQuery(uri);
            Set results = kernel.listGBeans(abstractNameQuery);
            if (results.size() != 1) {
                log.error( "Name query " + abstractNameQuery + " not satisfied in kernel, matches: " + results);
                return -1;
            }
            AbstractName name = (AbstractName) results.iterator().next();
            RpcEjbDeployment ejbDeployment;
            try {
                ejbDeployment = (RpcEjbDeployment) kernel.getGBean(name);
            } catch (Exception e) {
                // couldn't find the deployment
                log.debug("Deployment not found: " + containerId, e);
                return -1;
            }
            index = addDeployment(ejbDeployment);
        }

        if (index == null) {
            return -1;
        } else {
            return index.intValue();
        }
    }

    public synchronized int getDeploymentIndexByJndiName(String jndiName) {
        Integer index = (Integer) jndiNameToIndex.get(jndiName);
        return (index == null) ? -1 : index.intValue();
    }

    public synchronized RpcEjbDeployment getDeployment(String containerId) throws DeploymentNotFoundException {
        int deploymentIndex = getDeploymentIndex(containerId);
        if (deploymentIndex < 0) {
            throw new DeploymentNotFoundException(containerId);
        }
        return getDeployment(deploymentIndex);
    }

    public synchronized EjbDeployment getDeployment(Integer index) {
        return (index == null) ? null : getDeployment(index.intValue());
    }

    public synchronized EjbDeployment getDeploymentByJndiName(String jndiName) {
        return getDeployment(getDeploymentIndexByJndiName(jndiName));
    }

    public synchronized RpcEjbDeployment getDeployment(int index) {
        return deployments[index];
    }


}



