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
package org.openejb.mdb;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.management.ObjectName;

import junit.framework.TestCase;
import org.apache.geronimo.connector.outbound.connectiontracking.ConnectionTrackingCoordinator;
import org.apache.geronimo.gbean.jmx.GBeanMBean;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.jmx.JMXUtil;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.transaction.GeronimoTransactionManager;
import org.openejb.deployment.TransactionPolicySource;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.mdb.mockra.DeploymentHelper;
import org.openejb.transaction.ContainerPolicy;
import org.openejb.transaction.TransactionPolicy;

/**
 * @version $Revision$ $Date$
 */
public class BasicMDBContainerTest extends TestCase {
    private static final ObjectName TCA_NAME = JMXUtil.getObjectName("geronimo.test:role=TrackedConnectionAssociator");
    private Kernel kernel;
    private GBeanMBean container;

    protected void setUp() throws Exception {
        super.setUp();
        kernel = new Kernel("MDBTest");
        kernel.boot();
        GBeanMBean tmGBean = new GBeanMBean(GeronimoTransactionManager.GBEAN_INFO);
        Set rmpatterns = new HashSet();
        rmpatterns.add(ObjectName.getInstance("geronimo.server:j2eeType=JCAManagedConnectionFactory,*"));
        tmGBean.setReferencePatterns("ResourceManagers", rmpatterns);
        DeploymentHelper.start(kernel, DeploymentHelper.TRANSACTIONMANAGER_NAME, tmGBean);
        GBeanMBean trackedConnectionAssociator = new GBeanMBean(ConnectionTrackingCoordinator.GBEAN_INFO);
        DeploymentHelper.start(kernel, TCA_NAME, trackedConnectionAssociator);


        DeploymentHelper.setUpResourceAdapter(kernel);

        MDBContainerBuilder builder = new MDBContainerBuilder();
        builder.setClassLoader(this.getClass().getClassLoader());
        builder.setContainerId(DeploymentHelper.CONTAINER_NAME.getCanonicalName());
        builder.setEJBName("MockEJB");
        builder.setBeanClassName(MockEJB.class.getName());
        builder.setEndpointInterfaceName("javax.jms.MessageListener");
        builder.setActivationSpecName(DeploymentHelper.ACTIVATIONSPEC_NAME);
        builder.setUnshareableResources(new HashSet());
        builder.setTransactionPolicySource(new TransactionPolicySource() {
            public TransactionPolicy getTransactionPolicy(String methodIntf, InterfaceMethodSignature signature) {
                return ContainerPolicy.Required;
            }
        });
        builder.setComponentContext(new ReadOnlyContext());
        container = builder.createConfiguration();

       //start the ejb container
        container.setReferencePatterns("transactionManager", Collections.singleton(DeploymentHelper.TRANSACTIONMANAGER_NAME));
        container.setReferencePatterns("trackedConnectionAssociator", Collections.singleton(TCA_NAME));
        DeploymentHelper.start(kernel, DeploymentHelper.CONTAINER_NAME, container);
    }

    protected void tearDown() throws Exception {
        DeploymentHelper.stop(kernel, DeploymentHelper.CONTAINER_NAME);
        DeploymentHelper.stop(kernel, DeploymentHelper.TRANSACTIONMANAGER_NAME);
        DeploymentHelper.stop(kernel, TCA_NAME);
        DeploymentHelper.tearDownAdapter(kernel);
        kernel.shutdown();
    }


    public void testMessage() throws Exception {
        // @todo put a wait limit in here... otherwise this can lock a build
        // Wait for 3 messages to arrive..
        System.out.println("Waiting for message 1");
        MockEJB.messageCounter.acquire();
        System.out.println("Waiting for message 2");
        MockEJB.messageCounter.acquire();
        System.out.println("Waiting for message 3");
        MockEJB.messageCounter.acquire();

        System.out.println("Done.");
    }
}
