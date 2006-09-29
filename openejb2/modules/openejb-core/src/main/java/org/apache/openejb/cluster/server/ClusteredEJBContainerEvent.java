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

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.apache.openejb.client.ServerMetaData;

/**
 * 
 * @version $Revision$ $Date$
 */
class ClusteredEJBContainerEvent implements Serializable {
    public static final EventType JOIN = new EventType("JOIN");
    public static final EventType LEAVE = new EventType("LEAVE");
    
    public static class EventType implements Serializable {
        private final String name;
        private EventType(String name) {
            this.name = name;
        }
        private Object readResolve() throws ObjectStreamException {
            if (name.equals(JOIN.name)) {
                return JOIN;
            } else if (name.equals(LEAVE.name)) {
                return LEAVE;
            } else {
                throw new AssertionError();
            }
        }
    }
    
    private final EventType type;
    private final Object containerID;
    private final ServerMetaData server;
    
    public ClusteredEJBContainerEvent(EventType type, Object containerId, ServerMetaData server) {
        this.type = type;
        this.containerID = containerId;
        this.server = server;
    }

    public Object getContainerID() {
        return containerID;
    }

    public ServerMetaData getServer() {
        return server;
    }

    public EventType getType() {
        return type;
    }
}