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

import javax.jms.ObjectMessage;

import org.activecluster.ClusterEvent;
import org.activecluster.ClusterListener;
import org.codehaus.wadi.gridstate.Dispatcher;
import org.apache.openejb.client.ServerMetaData;

/**
 * 
 * @version $Revision$ $Date$
 */
public class DefaultEJBContainerMonitor implements ClusterListener {
    private final EJBContainerCallback callback;
    private final Dispatcher dispatcher;
    
    public DefaultEJBContainerMonitor(Dispatcher dispatcher, EJBContainerCallback callback) {
        this.dispatcher = dispatcher;
        this.callback = callback;
    }
    
    public void start() {
        dispatcher.register(this, "onEJBContainerEvent", ClusteredEJBContainerEvent.class);
        dispatcher.setClusterListener(this);
    }

    public void stop() {
        // TODO refactor WADI to get ride of this timeout.
        dispatcher.deregister("onEJBContainerEvent", ClusteredEJBContainerEvent.class, 1000);

        // TODO Enhance WADI to support unregistration of ClusterListener.
    }

    public void onEJBContainerEvent(ObjectMessage message, ClusteredEJBContainerEvent event) {
        ServerMetaData server = event.getServer();
        if (event.getType() == ClusteredEJBContainerEvent.JOIN) {
            callback.fireEJBContainerJoin(server, event.getContainerID());
        } else if (event.getType() == ClusteredEJBContainerEvent.LEAVE) {
            callback.fireEJBContainerLeave(server, event.getContainerID());
        }
    }

    public void onNodeAdd(ClusterEvent event) {
    }

    public void onNodeUpdate(ClusterEvent event) {
    }

    public void onNodeRemoved(ClusterEvent event) {
        callback.fireNodeLeave(event.getNode().getName());
    }

    public void onNodeFailed(ClusterEvent event) {
        callback.fireNodeLeave(event.getNode().getName());
    }

    public void onCoordinatorChanged(ClusterEvent event) {
    }
}