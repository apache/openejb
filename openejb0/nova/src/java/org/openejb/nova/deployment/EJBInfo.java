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

package org.openejb.nova.deployment;

import javax.transaction.TransactionManager;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanEndpoint;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.connector.deployment.ResourceAdapterHelper;
import org.apache.geronimo.deployment.model.geronimo.ejb.ActivationConfig;
import org.apache.geronimo.deployment.model.ejb.ActivationConfigProperty;
import org.openejb.nova.entity.bmp.BMPEntityContainer;
import org.openejb.nova.entity.cmp.CMPEntityContainer;
import org.openejb.nova.EJBContainerConfiguration;
import org.openejb.nova.mdb.MDBContainer;

/**
 * EJBInfo has static methods to construct GeronimoMBeanInfo objects for each type of
 * Container, to avoid contaminating Nova classes with Geronimo kernel structue.
 *
 * @version $Revision$ $Date$
 *
 * */
public class EJBInfo {
    static final String ACTIVATION_SPEC = "activation_spec";

    private EJBInfo() {}

    public static GeronimoMBeanInfo getSessionGeronimoMBeanInfo(String containerClassName, EJBContainerConfiguration config) {
        GeronimoMBeanInfo mbeanInfo= getGeronimoMBeanInfo(containerClassName, config);
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Demarcation", true, false, "Transaction demarcation"));
        return mbeanInfo;
    }

    public static GeronimoMBeanInfo getEntityGeronimoMBeanInfo(String containerClassName, EJBContainerConfiguration config) {
        GeronimoMBeanInfo mbeanInfo= getGeronimoMBeanInfo(containerClassName, config);
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("PrimaryKeyClassName", true, false, "Primary Key class name"));
        return mbeanInfo;
    }

    public static GeronimoMBeanInfo getMessageDrivenGeronimoMBeanInfo(EJBContainerConfiguration config, ActivationConfig activationConfig) throws DeploymentException {
        GeronimoMBeanInfo mbeanInfo = getSessionGeronimoMBeanInfo(MDBContainer.class.getName(), config);

        //set up ActivationSpec target.
        mbeanInfo.setTargetClass(ACTIVATION_SPEC, activationConfig.getActivationSpecClass());
        for (int i = 0; i < activationConfig.getActivationConfigProperty().length; i++) {
            ActivationConfigProperty activationConfigProperty = activationConfig.getActivationConfigProperty()[i];
            mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo(activationConfigProperty.getActivationConfigPropertyName(),
                    true,
                    true,
                    "no description yet",
                    ACTIVATION_SPEC,
                    activationConfigProperty.getActivationConfigPropertyValue()));

        }

        //set up resource adapter endpoint.
        ObjectName resourceAdapterName;
        try {
            resourceAdapterName = ObjectName.getInstance("geronimo.j2ee:J2eeType=ResourceAdapter,name=" + activationConfig.getResourceAdapterName());
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Bad resource adapter name", e);
        }

        mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint("ResourceAdapter",
                ResourceAdapterHelper.class.getName(),
                resourceAdapterName,
                true));
        return mbeanInfo;
    }

    public static GeronimoMBeanInfo getGeronimoMBeanInfo(String className, EJBContainerConfiguration config) {
        GeronimoMBeanInfo mbeanInfo= new GeronimoMBeanInfo();
        mbeanInfo.setTargetClass(className);
        //mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Uri", true, false, "Original deployment package URI?"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("BeanClassName", true, false, "Bean implementation class name"));
        if (config.homeInterfaceName != null) {
            mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("HomeClassName", true, false, "Home interface class name"));
            mbeanInfo.addOperationInfo(new GeronimoOperationInfo("getEJBHome"));
        }
        if (config.remoteInterfaceName != null) {
            mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("RemoteClassName", true, false, "Remote interface class name"));
        }
        if (config.localHomeInterfaceName != null) {
            mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("LocalHomeClassName", true, false, "Local home interface class name"));
        }
        if (config.localInterfaceName != null) {
            mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("LocalClassName", true, false, "Local interface class name"));
        }
        if (config.messageEndpointInterfaceName != null) {
            mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("MessageEndpointClassName", true, false, "Local interface class name"));
        } else {
            mbeanInfo.addOperationInfo(new GeronimoOperationInfo("getEJBHome"));
            mbeanInfo.addOperationInfo(new GeronimoOperationInfo("getEJBLocalHome"));
        }
        try {
            mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint("TransactionManager",
                    TransactionManager.class.getName(),
                    ObjectName.getInstance("geronimo.transaction:role=TransactionManager"),
                    true));
        } catch (MalformedObjectNameException e) {
            throw new AssertionError();//our o.n. is not malformed.
        }
        return mbeanInfo;
    }

}
