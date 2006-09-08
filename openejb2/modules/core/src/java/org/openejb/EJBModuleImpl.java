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
package org.openejb;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;

import javax.management.ObjectName;

import org.apache.geronimo.connector.outbound.ConnectionFactorySource;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.j2ee.management.impl.InvalidObjectNameException;
import org.apache.geronimo.kernel.ObjectNameUtil;
import org.apache.geronimo.management.EJB;
import org.apache.geronimo.management.EJBModule;
import org.apache.geronimo.management.J2EEApplication;
import org.apache.geronimo.management.J2EEServer;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.tranql.ejb.TransactionManagerDelegate;
import org.tranql.query.ConnectionFactoryDelegate;

/**
 * @version $Revision$ $Date$
 */
public class EJBModuleImpl implements GBeanLifecycle, EJBModule {
    private final J2EEServer server;
    private final J2EEApplication application;
    private final String deploymentDescriptor;
    private final ConnectionFactoryDelegate delegate;
    private final ConnectionFactorySource connectionFactory;
    private final TransactionManagerDelegate tmDelegate;
    private final TransactionContextManager transactionContextManager;
    private final String objectName;

    private final Collection ejbs;

    public EJBModuleImpl(String objectName, J2EEServer server, J2EEApplication application, String deploymentDescriptor, ConnectionFactoryDelegate delegate, ConnectionFactorySource connectionFactory, TransactionManagerDelegate tmDelegate, TransactionContextManager transactionContextManager, Collection ejbs) {
        this.objectName = objectName;
        ObjectName myObjectName = ObjectNameUtil.getObjectName(objectName);
        verifyObjectName(myObjectName);

        this.server = server;
        this.application = application;
        this.deploymentDescriptor = deploymentDescriptor;
        this.delegate = delegate;
        this.connectionFactory = connectionFactory;
        this.tmDelegate = tmDelegate;
        this.transactionContextManager = transactionContextManager;
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

    public void doStart() throws Exception {
        if (delegate != null) {
            delegate.setConnectionFactory(connectionFactory.$getResource());
        }
        if (null != tmDelegate) {
            tmDelegate.setTransactionManager(transactionContextManager.getTransactionManager());
        }
    }

    public void doStop() throws Exception {
        if (delegate != null) {
            delegate.setConnectionFactory(null);
        }
        if (null != tmDelegate) {
            tmDelegate.setTransactionManager(null);
        }
    }

    public void doFail() {
        if (delegate != null) {
            delegate.setConnectionFactory(null);
        }
        if (null != tmDelegate) {
            tmDelegate.setTransactionManager(null);
        }
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(EJBModuleImpl.class, NameFactory.EJB_MODULE);
        infoBuilder.addReference("J2EEServer", J2EEServer.class);
        infoBuilder.addReference("J2EEApplication", J2EEApplication.class);

        infoBuilder.addAttribute("deploymentDescriptor", String.class, true);
        infoBuilder.addReference("ConnectionFactory", ConnectionFactorySource.class, NameFactory.JCA_CONNECTION_FACTORY);
        infoBuilder.addAttribute("Delegate", ConnectionFactoryDelegate.class, true);
        infoBuilder.addReference("TransactionContextManager", TransactionContextManager.class, NameFactory.TRANSACTION_CONTEXT_MANAGER);
        infoBuilder.addAttribute("TMDelegate", TransactionManagerDelegate.class, true);

        infoBuilder.addReference("EJBCollection", EJB.class);

        infoBuilder.addInterface(EJBModule.class);

        infoBuilder.setConstructor(new String[]{
                "objectName",
                "J2EEServer",
                "J2EEApplication",
                "deploymentDescriptor",
                "Delegate",
                "ConnectionFactory",
                "TMDelegate",
                "TransactionContextManager",
                "EJBCollection"
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
