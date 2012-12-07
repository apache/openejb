/*
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
package org.apache.openejb.core.webservices;

import javax.xml.ws.EndpointReference;

import org.w3c.dom.Element;

/**
 * This interface defines the WS-Addressing functions of {@link javax.xml.ws.WebServiceContext WebServiceContext} 
 * that must be implemented by each JAX-WS provider. This interface is used within 
 * {@link org.apache.openejb.core.stateless.EjbWsContext EjbWsContext} and its implementation can be passed to 
 * the stateless or singleton container on Web Service invocations. 
 */
public interface AddressingSupport {
    
    public EndpointReference getEndpointReference(org.w3c.dom.Element... referenceParameters);

    public <T extends EndpointReference> T getEndpointReference(Class<T> clazz, Element... referenceParameters);
    
}
