/*
 * Copyright  2002, Apple Computer, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1.  Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in
 * the documentation and/or other materials provided with the
 * distribution.
 * 3.  Neither the name of Apple Computer, Inc. ("Apple")
 * nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written
 * permission.
 *
 * THIS SOFTWARE IS PROVIDED BY APPLE AND ITS CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL APPLE OR ITS
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH
 * DAMAGE.
 */

package org.openejb.corba.core;

/**
 * This class is a factory class for {@link org.openejb.corba.core.Containerdapter}
 *
 * @author Stefan Reich sreich@apple.com
 */
class AdapterFactory {

    /**
     * creates an instance of the matching subclass of ContainerAdapter for the container type
     * and passes the parameters to it.
     */
    static ContainerAdapter createAdapter(org.omg.CORBA.ORB orb,
                                          org.omg.PortableServer.POA homePOA,
                                          org.omg.CosNaming.NamingContextExt nameServiceRootContext,
                                          org.openejb.RpcContainer cntr,
                                          ContainerSystem system)
    throws org.openejb.OpenEJBException {
        switch(cntr.getContainerType()) {
            case org.openejb.Container.STATELESS:
                return new StatelessContainerAdapter(orb, homePOA, nameServiceRootContext, cntr, system);
            case org.openejb.Container.STATEFUL:
                return new StatefulContainerAdapter(orb, homePOA, nameServiceRootContext, cntr, system);
            case org.openejb.Container.ENTITY:
                return new EntityContainerAdapter(orb, homePOA, nameServiceRootContext, cntr, system);
            default:
                throw new org.openejb.OpenEJBException("Unknown container type");
        }
    }
}

