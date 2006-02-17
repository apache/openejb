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
package org.openejb;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.management.J2EEServer;
import org.apache.geronimo.management.J2EEApplication;
import org.apache.geronimo.management.EJBModule;
import org.apache.geronimo.kernel.Kernel;

/**
 * @version $Revision$ $Date$
 */
public final class EJBModuleImplGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(EJBModuleImplGBean.class, EJBModuleImpl.class, NameFactory.EJB_MODULE);
        infoBuilder.addReference("J2EEServer", J2EEServer.class);
        infoBuilder.addReference("J2EEApplication", J2EEApplication.class);

        infoBuilder.addAttribute("deploymentDescriptor", String.class, true);

        infoBuilder.addAttribute("kernel", Kernel.class, false);
        infoBuilder.addAttribute("objectName", String.class, false);
        infoBuilder.addAttribute("server", String.class, false);
        infoBuilder.addAttribute("application", String.class, false);
        infoBuilder.addAttribute("javaVMs", String[].class, false);
        infoBuilder.addAttribute("ejbs", String[].class, false);

        infoBuilder.addInterface(EJBModule.class);

        infoBuilder.setConstructor(new String[]{
                "kernel",
                "objectName",
                "J2EEServer",
                "J2EEApplication",
                "deploymentDescriptor",
        });

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
