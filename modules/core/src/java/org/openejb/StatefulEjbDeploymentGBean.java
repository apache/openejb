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
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.security.deploy.DefaultPrincipal;
import org.openejb.cluster.server.EJBClusterManager;
import org.openejb.corba.TSSBean;

import javax.security.auth.Subject;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

/**
 * @version $Revision$ $Date$
 */
public final class StatefulEjbDeploymentGBean {

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoFactory = GBeanInfoBuilder.createStatic(StatefulEjbDeploymentGBean.class, StatefulEjbDeployment.class, NameFactory.STATEFUL_SESSION_BEAN);

        infoFactory.addAttribute("objectName", String.class, false);
        infoFactory.addAttribute("ejbName", String.class, true);

        infoFactory.addAttribute("homeInterfaceName", String.class, true);
        infoFactory.addAttribute("remoteInterfaceName", String.class, true);
        infoFactory.addAttribute("localHomeInterfaceName", String.class, true);
        infoFactory.addAttribute("localInterfaceName", String.class, true);
        infoFactory.addAttribute("beanClassName", String.class, true);
        infoFactory.addAttribute("classLoader", ClassLoader.class, false);

        infoFactory.addReference("ejbContainer", StatefulEjbContainer.class, "StatefulEjbContainer");

        infoFactory.addAttribute("jndiNames", String[].class, true);
        infoFactory.addAttribute("localJndiNames", String[].class, true);

        infoFactory.addAttribute("securityEnabled", boolean.class, true);
        infoFactory.addAttribute("policyContextId", String.class, true);
        infoFactory.addAttribute("defaultPrincipal", DefaultPrincipal.class, true);
        infoFactory.addAttribute("runAs", Subject.class, true);

        infoFactory.addAttribute("beanManagedTransactions", boolean.class, true);
        infoFactory.addAttribute("transactionPolicies", SortedMap.class, true);

        infoFactory.addAttribute("componentContextMap", Map.class, true);

        infoFactory.addAttribute("kernel", Kernel.class, false);

        infoFactory.addReference("TSSBean", TSSBean.class);

        infoFactory.addAttribute("unshareableResources", Set.class, true);
        infoFactory.addAttribute("applicationManagedSecurityResources", Set.class, true);

        infoFactory.addReference("EJBClusterManager", EJBClusterManager.class);

        infoFactory.setConstructor(new String[]{
                "objectName",
                "ejbName",

                "homeInterfaceName",
                "remoteInterfaceName",
                "localHomeInterfaceName",
                "localInterfaceName",
                "beanClassName",
                "classLoader",

                "ejbContainer",

                "jndiNames",
                "localJndiNames",

                "securityEnabled",
                "policyContextId",
                "defaultPrincipal",
                "runAs",

                "beanManagedTransactions",
                "transactionPolicies",

                "componentContextMap",

                "kernel",

                "TSSBean",

                "unshareableResources",
                "applicationManagedSecurityResources",

                "EJBClusterManager",
        });

        infoFactory.addInterface(StatefulEjbDeployment.class);

        GBEAN_INFO = infoFactory.getBeanInfo();
    }


    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }
}
