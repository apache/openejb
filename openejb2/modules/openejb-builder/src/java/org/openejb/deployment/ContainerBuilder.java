/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb@openejb.org.
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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.deployment;

import java.util.Map;
import javax.management.ObjectName;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.naming.deployment.ResourceEnvironmentBuilder;
import org.apache.geronimo.transaction.TrackedConnectionAssociator;
import org.apache.geronimo.transaction.context.TransactionContextManager;
import org.apache.geronimo.transaction.context.UserTransactionImpl;
import org.openejb.EJBContainer;
import org.openejb.deployment.corba.TransactionImportPolicyBuilder;
import org.openejb.transaction.TransactionPolicySource;

/**
 * @version $Revision$ $Date$
 */
public interface ContainerBuilder extends ResourceEnvironmentBuilder, SecureBuilder {
    ClassLoader getClassLoader();

    void setClassLoader(ClassLoader classLoader);

    String getEJBName();

    void setEJBName(String ejbName);

    String getBeanClassName();

    void setBeanClassName(String beanClassName);

    String getHomeInterfaceName();

    void setHomeInterfaceName(String homeInterfaceName);

    String getRemoteInterfaceName();

    void setRemoteInterfaceName(String remoteInterfaceName);

    String getLocalHomeInterfaceName();

    void setLocalHomeInterfaceName(String localHomeInterfaceName);

    String getLocalInterfaceName();

    void setLocalInterfaceName(String localInterfaceName);

    String getServiceEndpointName();

    void setServiceEndpointName(String localInterfaceName);

    String getPrimaryKeyClassName();

    void setPrimaryKeyClassName(String primaryKeyClassName);

    Map getComponentContext();

    void setComponentContext(Map componentContext);

    UserTransactionImpl getUserTransaction();

    void setUserTransaction(UserTransactionImpl userTransaction);

    TransactionPolicySource getTransactionPolicySource();

    void setTransactionPolicySource(TransactionPolicySource transactionPolicySource);

    TransactionContextManager getTransactionContextManager();

    void setTransactionContextManager(TransactionContextManager transactionContextManager);

    TrackedConnectionAssociator getTrackedConnectionAssociator();

    void setTrackedConnectionAssociator(TrackedConnectionAssociator trackedConnectionAssociator);

    String[] getJndiNames();

    void setJndiNames(String[] jndiNames);

    String[] getLocalJndiNames();

    void setLocalJndiNames(String[] localJndiNames);

    EJBContainer createContainer() throws Exception;

    GBeanData createConfiguration(AbstractNameQuery transactionContextManagerObjectName, AbstractNameQuery trackedConnectionAssociatorObjectName, AbstractNameQuery tssBeanObjectName, GBeanData gbeanData) throws Exception;

    AbstractNameQuery getTransactedTimerName();

    void setTransactedTimerName(AbstractNameQuery transactedTimerName);

    AbstractNameQuery getNonTransactedTimerName();

    void setNonTransactedTimerName(AbstractNameQuery nonTransactedTimerName);

    TransactionImportPolicyBuilder getTransactionImportPolicyBuilder();

    void setTransactionImportPolicyBuilder(TransactionImportPolicyBuilder transactionImportPolicyBuilder);
}
