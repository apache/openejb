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

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.resource.spi.ActivationSpec;

import org.apache.geronimo.kernel.deployment.task.DeployGeronimoMBean;
import org.apache.geronimo.kernel.deployment.service.MBeanMetadata;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.management.State;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.openejb.nova.EJBContainerConfiguration;
import org.openejb.nova.mdb.MDBContainer;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class DeployMDBContainer  extends DeployGeronimoMBean {
    private final EJBContainerConfiguration config;
    private final ObjectName resourceAdapterName;

    public DeployMDBContainer(MBeanServer server,
                              MBeanMetadata metadata,
                              EJBContainerConfiguration config,
                              ObjectName resourceAdapterName) {
        super(server, metadata);
        this.config = config;
        this.resourceAdapterName = resourceAdapterName;
    }

    public  boolean canRun() throws DeploymentException {
        if (!super.canRun()) {
            return false;
        }
        try {
            return new Integer(State.RUNNING_INDEX).equals(server.getAttribute(resourceAdapterName, "state"));
        } catch (Exception e) {
            return false;
        }
    }

    public void perform() throws DeploymentException {
        GeronimoMBeanInfo mbeanInfo = metadata.getGeronimoMBeanInfo();
        //Create activation spec.
        ActivationSpec activationSpec;


        try {
            activationSpec = (ActivationSpec)server.invoke(resourceAdapterName, "createActivationSpec", new Object[] {mbeanInfo.getTargetClass(EJBInfo.ACTIVATION_SPEC)}, new String[] {String.class.getName()});
        } catch (InstanceNotFoundException e) {
            throw new DeploymentException("Did not find resourceAdapter at " + resourceAdapterName, e);
        } catch (MBeanException e) {
            throw new DeploymentException("Problem accessing resource adapter at " + resourceAdapterName, e);
        } catch (ReflectionException e) {
            throw new DeploymentException("Problem accessing resource adapter at " + resourceAdapterName, e);
        }
        mbeanInfo.setTarget(EJBInfo.ACTIVATION_SPEC, activationSpec);
        MDBContainer container = new MDBContainer(config, activationSpec);
        mbeanInfo.setTarget(container);
        super.perform();//registers mbean, sets attribute values on activationSpec.
        //TODO see if the spec indicates that the attribute values should be set before the
        //ActivationSpec is registered with the resource adapter.
        try {
            server.invoke(resourceAdapterName, "registerActivationSpec", new Object[] {activationSpec}, new String[] {ActivationSpec.class.getName()});
        } catch (InstanceNotFoundException e) {
            throw new DeploymentException("Did not find resourceAdapter at " + resourceAdapterName, e);
        } catch (MBeanException e) {
            throw new DeploymentException("Problem accessing resource adapter at " + resourceAdapterName, e);
        } catch (ReflectionException e) {
            throw new DeploymentException("Problem accessing resource adapter at " + resourceAdapterName, e);
        }
    }
}
