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
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
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
