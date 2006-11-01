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
package org.apache.openejb.mejb;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.system.jmx.MBeanServerReference;

/**
 * @version $Revision$ $Date$
 */
public final class MEJBGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        // NOTE: do not use "StatelessSessionBean" for the j2ee type of this bean
        // JSR-77 requires that all mbean mounted into the server with that j2ee type
        // to have an EJBModule key in the object name, and GBeans will not have that
        // key which violated the specification.
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(MEJBGBean.class, MEJB.class, org.apache.geronimo.j2ee.mejb.MEJB.GBEAN_INFO, "MEJB");
        infoBuilder.addReference("MBeanServerReference", MBeanServerReference.class);

        infoBuilder.setConstructor(new String[]{"objectName", "MBeanServerReference"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
