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
package org.apache.openejb.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.ejb.EJBHome;

import org.apache.openejb.EJBComponentType;


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
 * @since 11/25/2001
 */
public class EJBMetaDataImpl implements javax.ejb.EJBMetaData, java.io.Externalizable {
    
    protected transient int type;

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

    public EJBMetaDataImpl(Class homeInterface, Class remoteInterface, int typeOfBean) {
        this.type        = typeOfBean;
        this.homeClass   = homeInterface;
        this.remoteClass = remoteInterface;
    }

    public EJBMetaDataImpl(Class homeInterface, Class remoteInterface, Class primaryKeyClass, int typeOfBean) {
        this(homeInterface, remoteInterface, typeOfBean);
        if ( type == EJBComponentType.CMP_ENTITY || type == EJBComponentType.BMP_ENTITY ) {
            this.keyClass = primaryKeyClass;
        }
    }
    
    public EJBMetaDataImpl(Class homeInterface, Class remoteInterface, Class primaryKeyClass, int typeOfBean, String deploymentID) {
        this(homeInterface, remoteInterface, primaryKeyClass, typeOfBean);
        this.deploymentID   = deploymentID;
    }
    
    public EJBMetaDataImpl(Class homeInterface, Class remoteInterface, Class primaryKeyClass, int typeOfBean, String deploymentID, int deploymentCode) {
        this(homeInterface, remoteInterface, primaryKeyClass, typeOfBean, deploymentID);
        this.deploymentCode = deploymentCode;
    }
    /**
     * Obtain the Class object for the enterprise Bean's primary key class.
     */
    public Class getPrimaryKeyClass() {
        if ( type != EJBComponentType.BMP_ENTITY && type != EJBComponentType.CMP_ENTITY ){
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
        return type == EJBComponentType.STATELESS;
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
        return ( type == EJBComponentType.STATEFUL || type == EJBComponentType.STATELESS );
    }
    
    public void setEJBHomeProxy(EJBHomeProxy home) {
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