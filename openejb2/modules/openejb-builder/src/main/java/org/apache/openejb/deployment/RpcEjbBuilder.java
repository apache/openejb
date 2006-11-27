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
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentBuilder;
import org.apache.geronimo.security.deploy.DefaultPrincipal;

/**
 * @version $Revision$ $Date$
 */
public abstract class RpcEjbBuilder implements ResourceEnvironmentBuilder, SecureBuilder {
    protected String containerId;
    private String ejbName;
    protected String homeInterfaceName;
    protected String remoteInterfaceName;
    protected String localHomeInterfaceName;
    protected String localInterfaceName;
    private String beanClassName;
    private String ejbContainerName;
    private String[] jndiNames;
    private String[] localJndiNames;
    private String policyContextId;
    private boolean securityEnabled;
    private DefaultPrincipal defaultPrincipal;
    private Subject runAs;
    private SortedMap transactionPolicies;
    private Map componentContext;
    private AbstractNameQuery tssBeanQuery;
    private Set unshareableResources;
    private Set applicationManagedSecurityResources;

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

    public void setEjbName(String ejbName) {
        this.ejbName = ejbName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public String getHomeInterfaceName() {
        return homeInterfaceName;
    }

    public void setHomeInterfaceName(String homeInterfaceName) {
        this.homeInterfaceName = homeInterfaceName;
    }

    public String getRemoteInterfaceName() {
        return remoteInterfaceName;
    }

    public void setRemoteInterfaceName(String remoteInterfaceName) {
        this.remoteInterfaceName = remoteInterfaceName;
    }

    public String getLocalHomeInterfaceName() {
        return localHomeInterfaceName;
    }

    public void setLocalHomeInterfaceName(String localHomeInterfaceName) {
        this.localHomeInterfaceName = localHomeInterfaceName;
    }

    public String getLocalInterfaceName() {
        return localInterfaceName;
    }

    public void setLocalInterfaceName(String localInterfaceName) {
        this.localInterfaceName = localInterfaceName;
    }

    public void setEjbContainerName(String ejbContainerName) {
        this.ejbContainerName = ejbContainerName;
    }

    public void setJndiNames(String[] jndiNames) {
        this.jndiNames = jndiNames;
    }

    public void setLocalJndiNames(String[] localJndiNames) {
        this.localJndiNames = localJndiNames;
    }

    public String getPolicyContextId() {
        return policyContextId;
    }

    public void setPolicyContextId(String policyContextId) {
        this.policyContextId = policyContextId;
    }

    public boolean isSecurityEnabled() {
        return securityEnabled;
    }

    public void setSecurityEnabled(boolean securityEnabled) {
        this.securityEnabled = securityEnabled;
    }

    public DefaultPrincipal getDefaultPrincipal() {
        return defaultPrincipal;
    }

    public void setDefaultPrincipal(DefaultPrincipal defaultPrincipal) {
        this.defaultPrincipal = defaultPrincipal;
    }

    public Subject getRunAs() {
        return runAs;
    }

    public void setRunAs(Subject runAs) {
        this.runAs = runAs;
    }

    public void setTssBeanQuery(AbstractNameQuery tssBeanQuery) {
        this.tssBeanQuery = tssBeanQuery;
    }

    public void setComponentContext(Map componentContext) {
        this.componentContext = componentContext;
    }

    public void setTransactionPolicies(SortedMap transactionPolicies) {
        this.transactionPolicies = transactionPolicies;
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

    public GBeanData createConfiguration() throws Exception {
        URI uri = new URI(containerId);
        AbstractName abstractName = new AbstractName(uri);
        GBeanData gbean = new GBeanData(abstractName, getTargetGBeanInfo());

        gbean.setAttribute("ejbName", ejbName);

        gbean.setAttribute("homeInterfaceName", homeInterfaceName);
        gbean.setAttribute("remoteInterfaceName", remoteInterfaceName);
        gbean.setAttribute("localHomeInterfaceName", localHomeInterfaceName);
        gbean.setAttribute("localInterfaceName", localInterfaceName);
        gbean.setAttribute("beanClassName", beanClassName);

        gbean.setAttribute("jndiNames", jndiNames);
        gbean.setAttribute("localJndiNames", localJndiNames);

        AbstractNameQuery ejbContainerQuery = new AbstractNameQuery(null, Collections.singletonMap("name", ejbContainerName), getEjbContainerType().getName());
        gbean.setReferencePattern("ejbContainer", ejbContainerQuery);

        gbean.setAttribute("policyContextId", policyContextId);
        gbean.setAttribute("securityEnabled", new Boolean(securityEnabled));
        gbean.setAttribute("defaultPrincipal", defaultPrincipal);
        gbean.setAttribute("runAs", runAs);

        gbean.setAttribute("transactionPolicies", transactionPolicies);

        gbean.setAttribute("componentContextMap", componentContext);

        if (tssBeanQuery != null) {
//            gbean.setReferencePattern("TSSBean", tssBeanQuery);
        }

        gbean.setAttribute("unshareableResources", unshareableResources);
        gbean.setAttribute("applicationManagedSecurityResources", applicationManagedSecurityResources);

        return gbean;
    }

    protected abstract GBeanInfo getTargetGBeanInfo();

    protected abstract Class getEjbContainerType();
}
