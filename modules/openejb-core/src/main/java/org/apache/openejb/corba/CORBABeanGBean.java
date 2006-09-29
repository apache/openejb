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
import org.apache.geronimo.security.SecurityService;
import org.apache.openejb.corba.security.config.tss.TSSConfig;
import org.omg.CORBA.ORB;
import org.omg.PortableServer.POA;

import javax.ejb.spi.HandleDelegate;
import java.util.ArrayList;
import java.util.Properties;
import java.net.InetSocketAddress;

import edu.emory.mathcs.backport.java.util.concurrent.Executor;

/**
 * @version $Revision$ $Date$
 */
public final class CORBABeanGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(CORBABeanGBean.class, "OpenEJB ORB Adapter", CORBABean.class, NameFactory.CORBA_SERVICE);

        infoBuilder.addAttribute("configAdapter", String.class, true);
        infoBuilder.addAttribute("tssConfig", TSSConfig.class, true);
        infoBuilder.addAttribute("args", ArrayList.class, true);
        infoBuilder.addAttribute("props", Properties.class, true);

        infoBuilder.addAttribute("listenAddress", InetSocketAddress.class, false);
        infoBuilder.addAttribute("ORB", ORB.class, false);
        infoBuilder.addAttribute("rootPOA", POA.class, false);

        infoBuilder.addAttribute("handleDelegate", HandleDelegate.class, false);

        infoBuilder.addAttribute("classLoader", ClassLoader.class, false);
        infoBuilder.addReference("ThreadPool", Executor.class, NameFactory.GERONIMO_SERVICE);
        infoBuilder.addReference("SecurityService", SecurityService.class, NameFactory.GERONIMO_SERVICE);
        infoBuilder.addReference("NameService", SunNameService.class, NameFactory.CORBA_SERVICE);

        infoBuilder.setConstructor(new String[]{"configAdapter", "classLoader", "ThreadPool", "SecurityService", "NameService"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
