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
package org.openejb.core.ivm;

import java.io.ObjectStreamException;

import javax.ejb.EJBHome;

import org.openejb.DeploymentInfo;
import org.openejb.util.proxy.ProxyManager;

/**
 * IntraVM server implementation of the javax.ejb.EJBMetaData interface.
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 */
public class IntraVmMetaData implements javax.ejb.EJBMetaData, java.io.Serializable {
    
    /**
     * Constant held by the {@link #type} member variable to
     * specify that this MetaData implementation represents 
     * an EntityBean.
     * 
     * @see #type
     */
    final public static byte ENTITY = DeploymentInfo.BMP_ENTITY;
    
    /**
     * Constant held by the {@link #type} member variable to
     * specify that this MetaData implementation represents 
     * a stateful SessionBean.
     * 
     * @see #type
     */
    final public static byte STATEFUL = DeploymentInfo.STATEFUL;
    
    /**
     * Constant held by the {@link #type} member variable to
     * specify that this MetaData implementation represents
     * a stateless SessionBean.
     * 
     * @see #type
     */
    final public static byte STATELESS = DeploymentInfo.STATELESS;

    /**
     * The Class of the bean's home interface.
     */
    protected Class homeClass;
    
    /**
     * The Class of the bean's remote interface.
     */
    protected Class remoteClass;
    
    /**
     * The Class of the bean's primary key or null if the 
     * bean is of a type that does not require a primary key.
     */
    protected Class keyClass;
    
    /**
     * The EJBHome stub/proxy for this bean deployment.
     */
    protected EJBHome homeStub;

    /**
     * The type of bean that this MetaData implementation represents.
     * 
     * @see #ENTITY
     * @see #STATEFUL
     * @see #STATELESS
     */
    protected byte type;

    /**
     * Constructs a IntraVmMetaData object to represent the 
     * MetaData of a bean deployment of the specified type 
     * with the specified home and remote interfaces.
     * 
     * @param homeInterface
     *                   The Class of the bean's home interface.
     * @param remoteInterface
     *                   The Class of the bean's remote interface.
     * @param typeOfBean One of the {@link #ENTITY}, {@link #STATEFUL} or {@link #STATELESS} constants that specify the type of bean this MetaData will represent.
     */
    public IntraVmMetaData(Class homeInterface, Class remoteInterface, byte typeOfBean) {
        this(homeInterface,remoteInterface, null, typeOfBean);
    }
    
    /**
     * Constructs a IntraVmMetaData object to represent the 
     * MetaData of a bean deployment of the specified type, 
     * with the specified home and remote interfaces and 
     * primary key class.
     * 
     * @param homeInterface
     *                   The Class of the bean's home interface.
     * @param remoteInterface
     *                   The Class of the bean's remote interface.
     * @param primaryKeyClass
     *                   The primary key class of the bean that this MetaData will represent.
     * @param typeOfBean One of the {@link #ENTITY}, {@link #STATEFUL} or {@link #STATELESS} constants that specify the type of bean this MetaData will represent.
     */
    public IntraVmMetaData(Class homeInterface, Class remoteInterface, Class primaryKeyClass, byte typeOfBean) {
        if(typeOfBean!=ENTITY && typeOfBean!=STATEFUL && typeOfBean!=STATELESS) {
            if(typeOfBean==DeploymentInfo.CMP_ENTITY) {
                typeOfBean=ENTITY;
            }else {
                throw new IllegalArgumentException("typeOfBean parameter not in range: "+typeOfBean);
            }
        }
        if(homeInterface==null || remoteInterface==null) {
            throw new IllegalArgumentException();
        }
        if(typeOfBean==ENTITY && primaryKeyClass==null) {
            throw new IllegalArgumentException();
        }
        type = typeOfBean;
        homeClass = homeInterface;
        remoteClass = remoteInterface;
            keyClass = primaryKeyClass;
        }
    
    /**
     * Returns the Class of the bean's home interface.
     * 
     * @return the Class of the bean's home interface
     */
    public Class getHomeInterfaceClass( ) {
        return homeClass;
    }
    
    /**
     * Returns the Class of the bean's remote interface.
     * 
     * @return the Class of the bean's remote interface
     */
    public Class getRemoteInterfaceClass() {
        return remoteClass;
    }
    
    /**
     * Returns the Class of the bean's primary key or null
     * if the bean is of a type that does not require a primary key.
     * 
     * EJB 1.1, section 5.5:
     * If the EJBMetaData.getPrimaryKeyClass() method is invoked on a
     * EJBMetaData object for a Session bean, the method throws the
     * java.lang.RuntimeException.  
     * UnsupportedOperationException is a java.lang.RuntimeException
     * 
     * @return Class
     */
    public Class getPrimaryKeyClass( ) {
        if ( type == ENTITY )
            return keyClass;
        else 
            throw new UnsupportedOperationException("Session objects are private resources and do not have primary keys");
    }
    
    /**
     * Returns true if this MetaData represents a bean 
     * deployment of type SessionBean.
     * 
     * @return boolean
     */
    public boolean isSession( ) {
        return(type == STATEFUL || type ==STATELESS);
    }
    
    /**
     * Returns true if this MetaData represents a bean
     * deployment that is a stateless SessionBean.
     * 
     * @return boolean
     */
    public boolean isStatelessSession() {
        return type == STATELESS;
    }
    
    /**
     * Sets the EJBHome stub/proxy for this bean deployment.
     * 
     * @param home   The EJBHome stub/proxy for this bean deployment.
     */
    public void setEJBHome(EJBHome home) {
        homeStub = home;
    }
    
    /**
     * Gets the EJBHome stub/proxy for this bean deployment.
     * 
     * @return The EJBHome stub/proxy for this bean deployment.
     */
    public javax.ejb.EJBHome getEJBHome() {
        return homeStub;
    }

    /**
     * If the meta data is being  copied between bean instances in a RPC
     * call we use the IntraVmArtifact
     * <P>
     * If the meta data is referenced by a stateful bean that is being
     * passivated by the container, we allow this object to be serialized.
     * <P>
     * If the meta data is serialized outside the core container system,
     * we allow the application server to handle it.
     * 
     * @return Object 
     * @exception ObjectStreamException
     */
    protected Object writeReplace() throws ObjectStreamException{

        /*
         * If the meta data is being  copied between bean instances in a RPC 
         * call we use the IntraVmArtifact 
         */
        if(IntraVmCopyMonitor.isIntraVmCopyOperation()){
            return new IntraVmArtifact(this);
        /* 
         * If the meta data is referenced by a stateful bean that is being
         * passivated by the container, we allow this object to be serialized. 
         */
        }else if(IntraVmCopyMonitor.isStatefulPassivationOperation()){
            return this;
        /*  
         * If the meta data is serialized outside the core container system, 
         * we allow the application server to handle it. 
         */
        }else{
            BaseEjbProxyHandler handler = (BaseEjbProxyHandler)ProxyManager.getInvocationHandler(homeStub);
            return org.openejb.OpenEJB.getApplicationServer().getEJBMetaData(handler.getProxyInfo());
        }
    }
}