/**
*
* Licensed to the Apache Software Foundation (ASF) under one or more
*  contributor license agreements.  See the NOTICE file distributed with
*  this work for additional information regarding copyright ownership.
*  The ASF licenses this file to You under the Apache License, Version 2.0
*  (the "License"); you may not use this file except in compliance with
*  the License.  You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software
*  distributed under the License is distributed on an "AS IS" BASIS,
*  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*  See the License for the specific language governing permissions and
*  limitations under the License.
*/ 

package org.apache.openejb.yoko;

import javax.ejb.EJBHome;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.yoko.rmi.impl.MethodDescriptor;
import org.apache.yoko.rmi.impl.RMIStub; 

import org.omg.CORBA.ORB;
import org.apache.openejb.corba.CorbaApplicationServer;
import org.apache.openejb.corba.CORBAEJBMemento;
import org.apache.openejb.corba.ClientContext;
import org.apache.openejb.corba.ClientContextHolder;
import org.apache.openejb.corba.ClientContextManager;
import org.apache.openejb.server.ServerFederation;
import org.apache.openejb.spi.ApplicationServer;
import org.apache.openejb.proxy.SerializationHandler;
import org.apache.openejb.proxy.ReplacementStrategy;

/**
 * This class is the InvocationHandler for instances of POAStub. When a client
 * calls a remote method, this is translated to a call to the invoke() method in
 * this class.
 */
public class RMIStubHandler extends org.apache.yoko.rmi.impl.RMIStubHandler {
    // the application server singleton
    private static CorbaApplicationServer corbaApplicationServer = new CorbaApplicationServer();
    // the client context this stub was created under 
    private final ClientContext clientContext;
    
    public RMIStubHandler() {
        clientContext = ClientContextManager.getClientContext();
    }

    public Object stubWriteReplace(RMIStub stub) {
        String ior = getOrb().object_to_string(stub);
        return new CORBAEJBMemento(ior, stub instanceof EJBHome);
    }
    
    public Object invoke(RMIStub stub, MethodDescriptor method, Object[] args) throws Throwable {
        ClientContext oldContext = ClientContextManager.getClientContext();
        // object types must bbe written in the context of the corba application server
        // which properly write replaces our objects for corba
        ApplicationServer oldApplicationServer = ServerFederation.getApplicationServer();

        ServerFederation.setApplicationServer(corbaApplicationServer);
        SerializationHandler.setStrategy(ReplacementStrategy.REPLACE);

        try {
            ClientContextManager.setClientContext(clientContext);
            // let the super class handle everything.  We just need to wrap the context 
            return super.invoke(stub, method, args); 
            
        } finally {
            ServerFederation.setApplicationServer(oldApplicationServer);
            SerializationHandler.setStrategy(null);
            ClientContextManager.setClientContext(oldContext);
        }
    }
    
    private static ORB getOrb() {
        try {
            Context context = new InitialContext();
            ORB orb = (ORB) context.lookup("java:comp/ORB");
            return orb;
        } catch (Throwable e) {
            throw new org.omg.CORBA.MARSHAL("Could not find ORB in jndi at java:comp/ORB", 0, org.omg.CORBA.CompletionStatus.COMPLETED_YES);
        }
    }
}

