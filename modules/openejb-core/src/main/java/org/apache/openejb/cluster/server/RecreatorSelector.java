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
package org.apache.openejb.cluster.server;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.wadi.PoolableInvocationWrapper;
import org.codehaus.wadi.PoolableInvocationWrapperPool;

/**
 * 
 * @version $Revision$ $Date$
 */
class RecreatorSelector implements PoolableInvocationWrapperPool {
    private final Map containerIdToRecreator;
    
    public RecreatorSelector() {
        containerIdToRecreator = new HashMap();
    }

    void addMapping(Object containerID, EJBInstanceContextRecreator recreator) {
        synchronized (containerIdToRecreator) {
            containerIdToRecreator.put(containerID, recreator);
        }
    }
    
    void removeMapping(Object containerID) {
        synchronized (containerIdToRecreator) {
            containerIdToRecreator.remove(containerID);
        }
    }
    
    public PoolableInvocationWrapper take() {
        return new EJBInvocationWrapper(this);
    }

    public void put(PoolableInvocationWrapper wrapper) {
        if (false == wrapper instanceof EJBInvocationWrapper) {
            throw new IllegalArgumentException(EJBInvocationWrapper.class +
                    " is expected.");
        }
        wrapper.destroy();
    }
   
    public EJBInstanceContextRecreator select(Object containerId) {
        EJBInstanceContextRecreator recreator;
        synchronized (containerIdToRecreator) {
            recreator = (EJBInstanceContextRecreator) containerIdToRecreator.get(containerId);
        }
        if (null == recreator) {
            throw new IllegalArgumentException("Container id " + containerId +
                    " is unknown.");
        }
        return recreator;
    }
}