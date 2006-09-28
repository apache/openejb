/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
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
