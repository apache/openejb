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
package org.openejb.client;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;

import javax.management.ObjectName;
import javax.naming.Context;

/**
 * @version $Revision$ $Date$
 */
public final class AppClientJNDIContextGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(AppClientJNDIContextGBean.class, AppClientJNDIContext.class);

        infoFactory.addOperation("startClient", new Class[]{ObjectName.class});
        infoFactory.addOperation("stopClient", new Class[]{ObjectName.class});
        infoFactory.addAttribute("host", String.class, true);
        infoFactory.addAttribute("port", int.class, true);
        infoFactory.addAttribute("context", Context.class, false);
        infoFactory.setConstructor(new String[]{"host", "port"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
