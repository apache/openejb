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
package org.apache.openejb.corba;

import java.util.HashMap;
import java.util.Map;

import org.omg.CORBA.ORB;
import org.omg.CORBA.Policy;
import org.omg.CosNaming.NamingContextExt;
import org.omg.PortableServer.POA;
import org.apache.openejb.EJBComponentType;
import org.apache.openejb.EjbDeployment;
import org.apache.openejb.RpcEjbDeployment;

/**
 * @version $Revision$ $Date$
 */
public final class AdapterWrapper {
    private final static Map adapters = new HashMap();
    private final TSSLink tssLink;
    private Adapter generator;

    public AdapterWrapper(TSSLink tssLink) {
        this.tssLink = tssLink;

    }

    public void start(ORB orb, POA poa, NamingContextExt initialContext, Policy securityPolicy) throws CORBAException {
        switch (tssLink.getProxyInfo().getComponentType()) {
            case EJBComponentType.STATELESS:
                generator = new AdapterStateless(tssLink, orb, poa, securityPolicy);
                break;
            case EJBComponentType.STATEFUL:
                generator = new AdapterStateful(tssLink, orb, poa, securityPolicy);
                break;
            case EJBComponentType.BMP_ENTITY:
            case EJBComponentType.CMP_ENTITY:
                generator = new AdapterEntity(tssLink, orb, poa, securityPolicy);
                break;
            default:
                throw new CORBAException("CORBA Adapter does not handle MDB containers");
        }
        adapters.put(tssLink.getContainerId(), generator);
    }

    public void stop() throws CORBAException {
        generator.stop();
        adapters.remove(tssLink.getContainerId());
    }

    public static RefGenerator getRefGenerator(String containerId) {
        return (RefGenerator) adapters.get(containerId);
    }
}
