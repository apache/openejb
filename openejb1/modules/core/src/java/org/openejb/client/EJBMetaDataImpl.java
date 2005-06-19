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
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
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
package org.openejb.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.ejb.EJBHome;


/**
 * -------------------------------------------------
 * EJB 1.1
 * 
 * 9.3.6 Meta-data class
 * 
 * The deployment tools are responsible for implementing the class that 
 * provides meta-data information to the client view contract. The class must 
 * be a valid RMI-IIOP Value Type, and must implement the 
 * javax.ejb.EJBMetaData interface.
 * 
 * Because the meta-data class is not entity bean specific, the container may, but is not required to, use a
 * single class for all deployed enterprise beans.
 * -------------------------------------------------
 * 
 * The OpenEJB implementation of the javax.ejb.EJBMetaData interface.
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @since 11/25/2001
 */
public class EJBMetaDataImpl implements javax.ejb.EJBMetaData, java.io.Externalizable {
    
    /**
     * The constant that will be returned from the <code>getComponentType</code>
     * method if this bean is a stateful session bean.
     */
    public static final byte STATEFUL = (byte)6;

    /**
     * The constant that will be returned from the <code>getComponentType</code> 
     * method if this bean is a stateless session bean.
     */
    public static final byte STATELESS = (byte)7;

    /**
     * The constant that will be returned from the <code>getComponentType</code> 
     * method if this bean is an enitity bean with bean-managed persistence.
     */
    public static final byte BMP_ENTITY = (byte)8;

    /**
     * The constant that will be returned from the <code>getComponentType</code> 
     * method if this bean is a enitity bean with container-managed persistence.
     */
    public static final byte CMP_ENTITY = (byte)9;
    

    protected transient byte type;

    protected transient String deploymentID;
    protected transient int    deploymentCode;
                                                        
    /**
     * The home interface of the enterprise Bean.
     */
    protected transient Class homeClass;
    
    /**
     * The Class object for the enterprise Bean's remote interface.
     */
    protected transient Class remoteClass;
    
    /**
     * The Class object for the enterprise Bean's primary key class.
     */
    protected transient Class keyClass;
    
    protected transient EJBHomeProxy ejbHomeProxy;

    /** Public no-arg constructor required by Externalizable API */
    public EJBMetaDataImpl() {
    
    }

    public EJBMetaDataImpl(Class homeInterface, Class remoteInterface, byte typeOfBean) {
        this.type        = typeOfBean;
        this.homeClass   = homeInterface;
        this.remoteClass = remoteInterface;
    }

    public EJBMetaDataImpl(Class homeInterface, Class remoteInterface, Class primaryKeyClass, byte typeOfBean) {
        this(homeInterface, remoteInterface, typeOfBean);
        if ( type == CMP_ENTITY || type == BMP_ENTITY ) {
            this.keyClass = primaryKeyClass;
        }
    }
    
    public EJBMetaDataImpl(Class homeInterface, Class remoteInterface, Class primaryKeyClass, byte typeOfBean, String deploymentID) {
        this(homeInterface, remoteInterface, primaryKeyClass, typeOfBean);
        this.deploymentID   = deploymentID;
    }
    
    public EJBMetaDataImpl(Class homeInterface, Class remoteInterface, Class primaryKeyClass, byte typeOfBean, String deploymentID, int deploymentCode) {
        this(homeInterface, remoteInterface, primaryKeyClass, typeOfBean, deploymentID);
        this.deploymentCode = deploymentCode;
    }
    /**
     * Obtain the Class object for the enterprise Bean's primary key class.
     */
    public Class getPrimaryKeyClass() {
        if ( type != BMP_ENTITY && type != CMP_ENTITY ){
            // TODO:  Return a message
            throw new java.lang.UnsupportedOperationException();
        }
        return keyClass;
    }
    
    /**
     * Obtain the home interface of the enterprise Bean.
     */
    public EJBHome getEJBHome() {
        return ejbHomeProxy;
    }
    
    /**
     * Obtain the Class object for the enterprise Bean's home interface.
     */
    public Class getHomeInterfaceClass() {
        return homeClass;
    }
    
    /**
     * Test if the enterprise Bean's type is "stateless session".
     *
     * @return True if the type of the enterprise Bean is stateless
     *     session.
     */
    public boolean isStatelessSession() {
        return type == STATELESS;
    }
    
    /**
     * Obtain the Class object for the enterprise Bean's remote interface.
     */
    public Class getRemoteInterfaceClass() {
        return remoteClass;
    }
    
    /**
     * Test if the enterprise Bean's type is "session".
     *
     * @return True if the type of the enterprise Bean is session bean.
     */
    public boolean isSession() {
        return ( type == STATEFUL || type == STATELESS );
    }
    
    protected void setEJBHomeProxy(EJBHomeProxy home) {
        ejbHomeProxy = home;
    }

    //========================================
    // Externalizable object implementation
    //
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject( homeClass );
        out.writeObject( remoteClass );
        out.writeObject( keyClass );
        out.writeObject( ejbHomeProxy );
        out.writeByte(   type );
        out.writeUTF(    deploymentID );
        out.writeShort( (short)deploymentCode );
    }

    public void readExternal(ObjectInput in) throws IOException,ClassNotFoundException {
        homeClass      = (Class) in.readObject();
        remoteClass    = (Class) in.readObject();
        keyClass       = (Class) in.readObject();
        ejbHomeProxy   = (EJBHomeProxy) in.readObject();
        type           = in.readByte();
        deploymentID   = in.readUTF();
        deploymentCode = in.readShort();
    }

}