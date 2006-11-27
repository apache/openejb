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
import java.rmi.RemoteException;
import javax.ejb.EJBObject;

/**
 * -------------------------------------
 * EJB 1.1
 * <p/>
 * 9.3.4 Handle class
 * <p/>
 * The deployment tools are responsible for implementing the handle class for
 * the entity bean. The handle class must be serializable by the Java
 * programming language Serialization protocol.
 * <p/>
 * As the handle class is not entity bean specific, the container may, but is
 * not required to, use a single class for all deployed entity beans.
 * -------------------------------------
 * <p/>
 * The handle class for all deployed beans, not just entity beans.
 *
 * @since 11/25/2001
 */
public class EJBObjectHandle implements java.io.Externalizable, javax.ejb.Handle {

    protected transient EJBObjectProxy ejbObjectProxy;
    protected transient EJBObjectHandler handler;

    /**
     * Public no-arg constructor required by Externalizable API
     */
    public EJBObjectHandle() {
    }

    public EJBObjectHandle(EJBObjectProxy proxy) {
        this.ejbObjectProxy = proxy;
        this.handler = ejbObjectProxy.getEJBObjectHandler();
    }

    protected void setEJBObjectProxy(EJBObjectProxy ejbObjectProxy) {
        this.ejbObjectProxy = ejbObjectProxy;
        this.handler = ejbObjectProxy.getEJBObjectHandler();
    }

    /**
     * Obtain the EJB object reference represented by this handle.
     *
     * @throws RemoteException The EJB object could not be obtained
     * because of a system-level failure.
     */
    public EJBObject getEJBObject() throws RemoteException {
        return ejbObjectProxy;
    }

    //========================================
    // Externalizable object implementation
    //
    public void writeExternal(ObjectOutput out) throws IOException {

        // Write the full proxy data
        EJBMetaDataImpl ejb = handler.ejb;
        out.writeObject(getClassName(ejb.homeClass));
        out.writeObject(getClassName(ejb.remoteClass));
        out.writeObject(getClassName(ejb.keyClass));
        out.writeByte(ejb.type);
        out.writeUTF(ejb.deploymentID);
        out.writeShort(ejb.deploymentCode);
        
        out.writeObject(handler.servers);

        out.writeObject(handler.primaryKey);
    }

    /**
     * Reads the instanceHandle from the stream
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        EJBMetaDataImpl ejb = new EJBMetaDataImpl();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }

        String homeClassName = (String) in.readObject();
        ejb.homeClass = loadClass(classLoader, homeClassName);

        String remoteClassName = (String) in.readObject();
        ejb.remoteClass = loadClass(classLoader, remoteClassName);

        String keyClassName = (String) in.readObject();
        ejb.keyClass = loadClass(classLoader, keyClassName);

        ejb.type = in.readByte();
        ejb.deploymentID = in.readUTF();
        ejb.deploymentCode = in.readShort();

        ServerMetaData[] servers = (ServerMetaData[]) in.readObject();
        
        Object primaryKey = in.readObject();

        handler = EJBObjectHandler.createEJBObjectHandler(ejb, servers, primaryKey);
        ejbObjectProxy = handler.createEJBObjectProxy();
    }

    private static String getClassName(Class clazz) {
        if (clazz == null) {
            return null;
        }
        return clazz.getName();
    }

    private static Class loadClass(ClassLoader classLoader, String homeClassName) throws ClassNotFoundException {
        if (homeClassName == null) {
            return null;
        }
        return classLoader.loadClass(homeClassName);
    }

}