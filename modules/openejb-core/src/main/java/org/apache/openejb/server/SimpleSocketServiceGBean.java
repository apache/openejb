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
package org.apache.openejb.server;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.openejb.DeploymentIndex;
import org.activeio.xnet.SocketService;

import java.net.InetAddress;

/**
 * @version $Revision$ $Date$
 */
public final class SimpleSocketServiceGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(SimpleSocketServiceGBean.class, SimpleSocketService.class);

        infoFactory.addAttribute("serviceClassName", String.class, true);
        infoFactory.addAttribute("onlyFrom", InetAddress[].class, true);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);
        infoFactory.addAttribute("name", String.class, false);

        infoFactory.addReference("ContainerIndex", DeploymentIndex.class, NameFactory.GERONIMO_SERVICE);

        infoFactory.addInterface(SocketService.class);

        infoFactory.setConstructor(new String[]{"serviceClassName", "onlyFrom", "ContainerIndex", "classLoader"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
