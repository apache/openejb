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
import javax.ejb.EJBHome;
import javax.naming.Context;

/**
 * Contains all the information needed by the container for a particular
 * deployment.  Some of this information is generic, but this class is
 * largely becoming a dumping ground for information specific to individual
 * containers.  This class should be abstracted and subclassed in the individual
 * container packages.  The container should be required to provide its own DeploymentInfo
 * implementation, possibly returning it to the assembler and OpenEJB in general via a
 * new accessor method.
 *
 * @version $Revision$ $Date$
 */
public class CoreDeploymentInfo implements org.apache.openejb.assembler.DeploymentInfo {

    private final String deploymentId;
    private final Class homeInterface;
    private final Class remoteInterface;
    private final Class beanClass;
    private final Class pkClass;
    private final byte componentType;

    private final HashMap methodTransactionAttributes = new HashMap();

    private boolean isBeanManagedTransaction;
    private boolean isReentrant;
    private Container container;
    private Context jndiContextRoot;


    public CoreDeploymentInfo() {
        this(null, null, null, null, null, (byte) 0);
    }

    public CoreDeploymentInfo(String did, Class homeClass, Class remoteClass, Class beanClass, Class pkClass, byte componentType) {
        this.deploymentId = did;
        this.homeInterface = homeClass;
        this.remoteInterface = remoteClass;
        this.beanClass = beanClass;
        this.componentType = componentType;
        this.pkClass = pkClass;
    }

    public void setContainer(Container cont) {
        container = cont;
    }

    public int getComponentType() {
        return componentType;
    }

    public byte getTransactionAttribute(Method method) {
        Byte byteWrapper = (Byte) methodTransactionAttributes.get(method);
        if (byteWrapper == null)
            return TX_NOT_SUPPORTED;// non remote or home interface method
        else
            return byteWrapper.byteValue();
    }

    public Container getContainer() {
        return container;
    }

    public String getDeploymentID() {
        return deploymentId;
    }

    public boolean isBeanManagedTransaction() {
        return isBeanManagedTransaction;
    }

    public Class getHomeInterface() {
        return homeInterface;
    }

    public Class getRemoteInterface() {
        return remoteInterface;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public Class getPrimaryKeyClass() {
        return pkClass;
    }

    //
    // end DeploymentInfo Implementation
    //==================================


    //===================================================
    // begin accessors & mutators for this implementation
    //

    public EJBHome getEJBHome() {
        throw new UnsupportedOperationException();
    }

    public void setBeanManagedTransaction(boolean value) {
        isBeanManagedTransaction = value;
    }

    public void setJndiEnc(javax.naming.Context cntx) {
        jndiContextRoot = cntx;
    }

    public javax.naming.Context getJndiEnc() {
        return jndiContextRoot;
    }

    public boolean isReentrant() {
        return isReentrant;
    }

    public void setIsReentrant(boolean reentrant) {
        isReentrant = reentrant;
    }

    public void appendMethodPermissions(Method m, String[] roleNames) {
    }

    public void addSecurityRoleReference(String securityRoleReference, String[] physicalRoles) {
    }

    public void setMethodTransactionAttribute(Method method, String transAttribute) {
        Byte byteValue = null;

        if (transAttribute.equals("Supports")) {
            byteValue = new Byte(TX_SUPPORTS);
        } else if (transAttribute.equals("RequiresNew")) {
            byteValue = new Byte(TX_REQUIRES_NEW);
        } else if (transAttribute.equals("Mandatory")) {
            byteValue = new Byte(TX_MANDITORY);
        } else if (transAttribute.equals("NotSupported")) {
            byteValue = new Byte(TX_NOT_SUPPORTED);
        } else if (transAttribute.equals("Required")) {
            byteValue = new Byte(TX_REQUIRED);
        } else if (transAttribute.equals("Never")) {
            byteValue = new Byte(TX_NEVER);
        } else {
            throw new IllegalArgumentException("Invalid transaction attribute \"" + transAttribute + "\" declared for method " + method.getName() + ". Please check your configuration.");
        }

        methodTransactionAttributes.put(method, byteValue);
    }
}
