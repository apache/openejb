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

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;

/**
 * @version $Revision$ $Date$
 */
public final class DefaultEJBClusterManagerGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(DefaultEJBClusterManager.class, "OpenEJB Cluster Manager", DefaultEJBClusterManager.class, NameFactory.MODULE_BUILDER);
        infoBuilder.addAttribute("clusterName", String.class, true);
        infoBuilder.addAttribute("clusterUri", String.class, true);
        infoBuilder.addAttribute("nodeName", String.class, true);
        infoBuilder.addAttribute("host", String.class, true);
        infoBuilder.addAttribute("port", Integer.TYPE, true);
        infoBuilder.addAttribute("nbPartitions", Integer.TYPE, true);

        infoBuilder.addInterface(EJBClusterManager.class);

        infoBuilder.setConstructor(new String[]{
                "clusterName",
                "clusterUri",
                "nodeName",
                "host",
                "port",
                "nbPartitions"});
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
