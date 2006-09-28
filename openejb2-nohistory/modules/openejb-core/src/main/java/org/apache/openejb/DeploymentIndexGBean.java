/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

/**
 * @version $Revision$ $Date$
 */
public final class DeploymentIndexGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(DeploymentIndexGBean.class, DeploymentIndex.class); //name apparently hardcoded

        infoFactory.setConstructor(new String[]{"EjbDeployments", "kernel"});

        infoFactory.addOperation("getDeploymentIndex", new Class[]{Object.class});
        infoFactory.addOperation("getDeploymentIndex", new Class[]{String.class});
        infoFactory.addOperation("getDeploymentIndexByJndiName", new Class[]{String.class});
        infoFactory.addOperation("getDeployment", new Class[]{String.class});
        infoFactory.addOperation("getDeployment", new Class[]{Integer.class});
        infoFactory.addOperation("getDeployment", new Class[]{Integer.TYPE});
        infoFactory.addOperation("getDeploymentByJndiName", new Class[]{String.class});

        infoFactory.addReference("EjbDeployments", RpcEjbDeployment.class);//many types, specify type in patterns

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
