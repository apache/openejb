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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.tomee.catalina;

import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;

import java.util.ArrayList;
import java.util.List;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.tomee.catalina.event.AfterApplicationCreated;

/**
* @version $Rev$ $Date$
*/
public class WebDeploymentListeners {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, WebDeploymentListeners.class);

    public boolean add(WebDeploymentListener webDeploymentListener) { // compatibility
        LOGGER.warning("WebDeploymentListener API is replaced by 'void afterApplicationCreated(@Observes final AfterApplicationCreated event)' API");
        SystemInstance.get().addObserver(new WebDeploymentListenerObserver(webDeploymentListener));
        return true;
    }

    private static class WebDeploymentListenerObserver {
        private final WebDeploymentListener delegate;

        public WebDeploymentListenerObserver(final WebDeploymentListener webDeploymentListener) {
            delegate = webDeploymentListener;
        }

        public void afterApplicationCreated(@Observes final AfterApplicationCreated event) {
            delegate.afterApplicationCreated(event.getApp(), event.getWeb());
        }
    }
}
