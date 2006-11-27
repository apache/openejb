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
package org.apache.openejb.deployment;

import java.util.Set;
import java.util.SortedMap;
import java.util.Map;
import java.util.Collections;
import java.net.URI;
import javax.security.auth.Subject;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentBuilder;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.apache.openejb.mdb.MdbDeploymentGBean;
import org.apache.openejb.MdbContainer;


/**
 * @version $Revision$ $Date$
 */
public class MdbBuilder implements ResourceEnvironmentBuilder, SecureBuilder {
    private String containerId;
    private String ejbName;

    private String endpointInterfaceName;
    private String beanClassName;

    private AbstractName activationSpecName;

    private String ejbContainerName;

    private String policyContextId;
    private Subject runAs;

    private boolean beanManagedTransactions = false;
    private SortedMap transactionPolicies;

    private Set unshareableResources;
    private Set applicationManagedSecurityResources;

    private Map componentContext;

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public void setEjbName(String ejbName) {
        this.ejbName = ejbName;
    }

    public void setEndpointInterfaceName(String endpointInterfaceName) {
        this.endpointInterfaceName = endpointInterfaceName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public void setActivationSpecName(AbstractName activationSpecName) {
        this.activationSpecName = activationSpecName;
    }

    public void setEjbContainerName(String ejbContainerName) {
        this.ejbContainerName = ejbContainerName;
    }

    public void setPolicyContextId(String policyContextId) {
        this.policyContextId = policyContextId;
    }

    public DefaultPrincipal getDefaultPrincipal() {
        return null;  // RETURN NOTHING
    }

    public void setDefaultPrincipal(DefaultPrincipal defaultPrincipal) {
        // DO NOTHING
    }

    public Subject getRunAs() {
        return runAs;
    }

    public void setRunAs(Subject runAs) {
        this.runAs = runAs;
    }

    public boolean isBeanManagedTransactions() {
        return beanManagedTransactions;
    }

    public void setBeanManagedTransactions(boolean beanManagedTransactions) {
        this.beanManagedTransactions = beanManagedTransactions;
    }

    public boolean isSecurityEnabled() {
        throw new UnsupportedOperationException();
    }

    public void setSecurityEnabled(boolean securityEnabled) {
    }

    public String getPolicyContextId() {
        return policyContextId;
    }

    public Map getComponentContext() {
        return componentContext;
    }

    public void setComponentContext(Map componentContext) {
        this.componentContext = componentContext;
    }

    public Set getUnshareableResources() {
        return unshareableResources;
    }

    public void setUnshareableResources(Set unshareableResources) {
        this.unshareableResources = unshareableResources;
    }

    public Set getApplicationManagedSecurityResources() {
        return applicationManagedSecurityResources;
    }

    public void setApplicationManagedSecurityResources(Set applicationManagedSecurityResources) {
        this.applicationManagedSecurityResources = applicationManagedSecurityResources;
    }

    public SortedMap getTransactionPolicies() {
        return transactionPolicies;
    }

    public void setTransactionPolicies(SortedMap transactionPolicies) {
        this.transactionPolicies = transactionPolicies;
    }

    public GBeanData createConfiguration() throws Exception {
        URI uri = new URI(containerId);
        AbstractName abstractName = new AbstractName(uri);
        GBeanData gbean = new GBeanData(abstractName, MdbDeploymentGBean.GBEAN_INFO);
        gbean.setAttribute("ejbName", ejbName);

        gbean.setAttribute("endpointInterfaceName", endpointInterfaceName);
        gbean.setAttribute("beanClassName", beanClassName);

        gbean.setReferencePattern("ActivationSpecWrapper", activationSpecName);

        AbstractNameQuery ejbContainerQuery = new AbstractNameQuery(null, Collections.singletonMap("name", ejbContainerName), MdbContainer.class.getName());
        gbean.setReferencePattern("ejbContainer", ejbContainerQuery);

        gbean.setAttribute("policyContextId", policyContextId);
        gbean.setAttribute("runAs", runAs);

        gbean.setAttribute("beanManagedTransactions", new Boolean(beanManagedTransactions));
        gbean.setAttribute("transactionPolicies", transactionPolicies);

        gbean.setAttribute("unshareableResources", unshareableResources);
        gbean.setAttribute("applicationManagedSecurityResources", applicationManagedSecurityResources);

        gbean.setAttribute("componentContextMap", componentContext);

        return gbean;
    }
}
