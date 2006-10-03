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
package org.apache.openejb.corba;

import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.system.serverinfo.ServerInfo;

import org.apache.openejb.corba.security.config.ConfigAdapter;

import java.net.InetSocketAddress;

/**
 * @version $Revision$ $Date$
 */
public final class NameServiceGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(NameServiceGBean.class, "CORBA Naming Service", NameService.class, NameFactory.CORBA_NAME_SERVICE);

        infoFactory.addReference("ServerInfo", ServerInfo.class, NameFactory.GERONIMO_SERVICE);
        infoFactory.addReference("configAdapter", ConfigAdapter.class, NameFactory.ORB_CONFIG);
        infoFactory.addAttribute("host", String.class, true);
        infoFactory.addAttribute("port", int.class, true);
        infoFactory.addAttribute("address", InetSocketAddress.class, false);
        infoFactory.addAttribute("local", boolean.class, true);
        infoFactory.setConstructor(new String[]{"ServerInfo", "configAdapter", "host", "port"});

        GBEAN_INFO = infoFactory.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
