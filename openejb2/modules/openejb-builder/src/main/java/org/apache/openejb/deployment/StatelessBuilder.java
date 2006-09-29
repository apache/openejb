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
package org.apache.openejb.deployment;

import java.util.List;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.openejb.StatelessEjbDeploymentGBean;
import org.apache.openejb.StatelessEjbContainer;

/**
 * @version $Revision$ $Date$
 */
public class StatelessBuilder extends SessionBuilder {
    private String serviceEndpointInterfaceName;
    private List handlerInfos;
    public void setServiceEndpointInterfaceName(String serviceEndpointInterfaceName) {
        this.serviceEndpointInterfaceName = serviceEndpointInterfaceName;
    }

    public void setHandlerInfos(List handlerInfos) {
        this.handlerInfos = handlerInfos;
    }

    protected GBeanInfo getTargetGBeanInfo() {
        return StatelessEjbDeploymentGBean.GBEAN_INFO;
    }

    protected Class getEjbContainerType() {
        return StatelessEjbContainer.class;
    }

    public GBeanData createConfiguration() throws Exception {
        GBeanData gbean = super.createConfiguration();
        gbean.setAttribute("handlerInfos", handlerInfos);
        gbean.setAttribute("serviceEndpointInterfaceName", serviceEndpointInterfaceName);
        return gbean;
    }
}
