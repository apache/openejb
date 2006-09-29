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
package org.apache.openejb.config;

import java.lang.reflect.Method;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.ejb11.*;
import org.apache.openejb.config.sys.Connector;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.config.sys.Openejb;
import org.apache.openejb.util.SafeToolkit;

/**
 * This class represents a command line tool for deploying beans.
 * <p/>
 * At the moment it contains multiple println statements
 * and statements that read input from the user.
 * <p/>
 * These statements are really in chunks in specific times throughout
 * the class.  These chunks could be refactored into methods. Then
 * the implementation of those methods could actually be delegated
 * to another class that implements a specific interface we create.
 * <p/>
 * The command line statements could be moved into an implementation
 * of this new interface. We could then create another implementation
 * that gathers information from a GUI.
 * <p/>
 * This would give us a Deploy API rather than just a command line
 * tool.  Then beans could be deployed programmatically by another
 * application, by a GUI screen, or by command line.
 * <p/>
 * Note: The command line version should be finished first!!!  We
 * don't want to start on a crusade of abstracting code that doesn't
 * yet exist.  Functionality first, neat flexible stuff later.
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class AutoDeployer {

    private Openejb config;
    private String configFile;
    private Container[] containers;
    private Connector[] resources;

    public AutoDeployer() throws OpenEJBException {
        this(ConfigUtils.readConfig());
    }

    public AutoDeployer(Openejb config) {
        this.config = config;
        
        /* Load container list */
        this.containers = config.getContainer();
        
        /* Load resource list */
        this.resources = config.getConnector();
        System.out.println("resources " + resources.length);
    }

    public void init() throws OpenEJBException {
    }

    public OpenejbJar deploy(String jarLocation) throws OpenEJBException {
        EjbJar jar = EjbJarUtils.readEjbJar(jarLocation);

        return deploy(jar, jarLocation);
    }

    public OpenejbJar deploy(EjbJar jar, String jarLocation) throws OpenEJBException {
        OpenejbJar openejbJar = new OpenejbJar();

        Bean[] beans = getBeans(jar);

        for (int i = 0; i < beans.length; i++) {
            openejbJar.addEjbDeployment(deployBean(beans[i], jarLocation));
        }
        return openejbJar;
    }

    private EjbDeployment deployBean(Bean bean, String jarLocation) throws OpenEJBException {
        EjbDeployment deployment = new EjbDeployment();

        deployment.setEjbName(bean.getEjbName());

        deployment.setDeploymentId(autoAssignDeploymentId(bean));

        deployment.setContainerId(autoAssignContainerId(bean));

        ResourceRef[] refs = bean.getResourceRef();

        if (refs.length > 1) {
            throw new OpenEJBException("Beans with more that one resource-ref cannot be autodeployed;  there is no accurate way to determine how the references should be mapped.");
        }

        for (int i = 0; i < refs.length; i++) {
            deployment.addResourceLink(autoAssingResourceRef(refs[i]));
        }

        if (bean.getType().equals("CMP_ENTITY")) {
            if (bean.getHome() != null) {
                Class tempBean = SafeToolkit.loadTempClass(bean.getHome(), jarLocation);
                if (hasFinderMethods(tempBean)) {
                    throw new OpenEJBException("CMP 1.1 Beans with finder methods cannot be autodeployed; finder methods require OQL Select statements which cannot be generated accurately.");
                }
            }
            if (bean.getLocalHome() != null) {
                Class tempBean = SafeToolkit.loadTempClass(bean.getHome(), jarLocation);
                if (hasFinderMethods(tempBean)) {
                    throw new OpenEJBException("CMP 1.1 Beans with finder methods cannot be autodeployed; finder methods require OQL Select statements which cannot be generated accurately.");
                }
            }
        }

        return deployment;
    }

    private boolean hasFinderMethods(Class bean)
            throws OpenEJBException {

        Method[] methods = bean.getMethods();

        for (int i = 0; i < methods.length; i++) {
            if (methods[i].getName().startsWith("find")
                    && !methods[i].getName().equals("findByPrimaryKey")) {
                return true;
            }
        }
        return false;
    }

    private String autoAssignDeploymentId(Bean bean) throws OpenEJBException {
        return bean.getEjbName();
    }

    private String autoAssignContainerId(Bean bean) throws OpenEJBException {
        String answer = null;
        boolean replied = false;

        Container[] cs = getUsableContainers(bean);

        if (cs.length == 0) {
            throw new OpenEJBException("A container of type " + bean.getType() + " must be declared in the configuration file.");
        }
        return cs[0].getId();
    }

    private ResourceLink autoAssingResourceRef(ResourceRef ref) throws OpenEJBException {
        if (resources.length == 0) {
            throw new OpenEJBException("A Connector must be declared in the configuration file to satisfy the resource-ref " + ref.getResRefName());
        }

        ResourceLink link = new ResourceLink();
        link.setResRefName(ref.getResRefName());
        link.setResId(resources[0].getId());
        return link;
    }

    /*------------------------------------------------------*/
    /*    Refactored Methods                                */
    /*------------------------------------------------------*/
    private Bean[] getBeans(EjbJar jar) {
        return EjbJarUtils.getBeans(jar);
    }

    private Container[] getUsableContainers(Bean bean) {
        return EjbJarUtils.getUsableContainers(containers, bean);
    }
}
