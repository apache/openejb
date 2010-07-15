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
package org.apache.openejb.cdi.simple;

import java.io.Serializable;

import javax.ejb.EJB;
import javax.enterprise.inject.Produces;


public class SimpleModel implements Serializable{

    private @EJB EchoLocal echoLocal;
    
    private @Produces @EchoEjbQualifier @EJB EchoLocal local2ViaProduce;
        
    public EchoLocal getLocal2ViaProduce() {
        return local2ViaProduce;
    }

    public void setLocal2ViaProduce(EchoLocal local2ViaProduce) {
        this.local2ViaProduce = local2ViaProduce;
    }

    public EchoLocal getEchoLocal() {
        return echoLocal;
    }

    public void setEchoLocal(EchoLocal echoLocal) {
        this.echoLocal = echoLocal;
    }    
    
    
    
}
