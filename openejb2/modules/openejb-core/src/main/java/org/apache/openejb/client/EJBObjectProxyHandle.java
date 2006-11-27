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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import org.apache.openejb.proxy.EJBProxyFactory;


public class EJBObjectProxyHandle implements Externalizable {

    EJBObjectHandler handler;

    public EJBObjectProxyHandle() {
    }

    public EJBObjectProxyHandle(EJBObjectHandler handler) {
        this.handler = handler;
    }

    /**
     * Writes the instanceHandle to the stream.
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        // Write the full proxy data
        EJBMetaDataImpl ejb = handler.ejb;
        out.writeObject(ejb.homeClass);
        out.writeObject(ejb.remoteClass);
        out.writeObject(ejb.keyClass);
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

        ejb.homeClass = (Class) in.readObject();
        ejb.remoteClass = (Class) in.readObject();
        ejb.keyClass = (Class) in.readObject();
        ejb.type = in.readByte();
        ejb.deploymentID = in.readUTF();
        ejb.deploymentCode = in.readShort();

        ServerMetaData[] servers = (ServerMetaData[]) in.readObject();

        Object primaryKey = in.readObject();

        handler = EJBObjectHandler.createEJBObjectHandler(ejb, servers, primaryKey);

    }

    public static boolean client = true;
    private Object readResolve() {
        if (client) {
            return handler.createEJBObjectProxy();
        } else {
            EJBMetaDataImpl ejb = handler.ejb;
            Class remoteInterface = ejb.remoteClass;
            Class homeInterface = ejb.homeClass;

            EJBProxyFactory proxyFactory = new EJBProxyFactory(ejb.deploymentID,
                    ejb.isSession(),
                    remoteInterface,
                    homeInterface,
                    null,
                    null);

            return proxyFactory.getEJBObject(handler.primaryKey);
        }
    }
}
