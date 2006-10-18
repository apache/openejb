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

package org.apache.openejb.deployment;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Reference;
import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbLocalRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbLocalRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.xbeans.j2ee.EjbLocalRefType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
import org.apache.openejb.RpcEjbDeployment;
import org.apache.openejb.proxy.EJBProxyReference;

/**
 * @version $Revision$ $Date$
 */
public class OpenEjbLocalRefBuilder extends OpenEjbAbstractRefBuilder {

    private static final QName GER_EJB_LOCAL_REF_QNAME = GerEjbLocalRefDocument.type.getDocumentElementName();
    private static final QNameSet GER_EJB_LOCAL_REF_QNAME_SET = QNameSet.singleton(GER_EJB_LOCAL_REF_QNAME);

    private final QNameSet ejbLocalRefQNameSet;

    public OpenEjbLocalRefBuilder(Environment defaultEnvironment, String[] eeNamespaces) {
        super(defaultEnvironment);
        ejbLocalRefQNameSet = buildQNameSet(eeNamespaces, "ejb-local-ref");
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module, Map componentContext) throws DeploymentException {
        XmlObject[] ejbLocalRefsUntyped = convert(specDD.selectChildren(ejbLocalRefQNameSet), J2EE_CONVERTER, EjbLocalRefType.type);
        XmlObject[] gerEjbLocalRefsUntyped = plan == null? NO_REFS: plan.selectChildren(GER_EJB_LOCAL_REF_QNAME_SET);
        Map ejbLocalRefMap = mapEjbLocalRefs(gerEjbLocalRefsUntyped);
        ClassLoader cl = localConfiguration.getConfigurationClassLoader();

        for (int i = 0; i < ejbLocalRefsUntyped.length; i++) {
            EjbLocalRefType ejbLocalRef = (EjbLocalRefType) ejbLocalRefsUntyped[i];

            String ejbRefName = getStringValue(ejbLocalRef.getEjbRefName());
            GerEjbLocalRefType localRef = (GerEjbLocalRefType) ejbLocalRefMap.get(ejbRefName);

            Reference ejbReference = addEJBLocalRef(remoteConfiguration, module.getModuleURI(), ejbLocalRef, localRef, cl);
            getJndiContextMap(componentContext).put(ENV + ejbRefName, ejbReference);
        }
    }

    private Reference addEJBLocalRef(Configuration ejbContext, URI moduleURI, EjbLocalRefType ejbLocalRef, GerEjbLocalRefType localRef, ClassLoader cl) throws DeploymentException {
        String local = getStringValue(ejbLocalRef.getLocal());
        String refName = getStringValue(ejbLocalRef.getEjbRefName());
        try {
            assureEJBLocalObjectInterface(local, cl);
        } catch (DeploymentException e) {
            throw new DeploymentException("Error processing 'local' element for EJB Local Reference '" + refName + "' for module '" + moduleURI + "': " + e.getMessage());
        }

        String localHome = getStringValue(ejbLocalRef.getLocalHome());
        try {
            assureEJBLocalHomeInterface(localHome, cl);
        } catch (DeploymentException e) {
            throw new DeploymentException("Error processing 'local-home' element for EJB Local Reference '" + refName + "' for module '" + moduleURI + "': " + e.getMessage());
        }

        boolean isSession = "Session".equals(getStringValue(ejbLocalRef.getEjbRefType()));

        String ejbLink = null;
        if (localRef != null && localRef.isSetEjbLink()) {
            ejbLink = localRef.getEjbLink();
        } else if (ejbLocalRef.isSetEjbLink()) {
            ejbLink = getStringValue(ejbLocalRef.getEjbLink());
        }

        Artifact targetConfigId = null;
        String optionalModule = moduleURI == null ? null : moduleURI.toString();
        String requiredModule = null;
        AbstractNameQuery containerQuery = null;
        if (localRef != null && localRef.isSetEjbLink()) {
            ejbLink = localRef.getEjbLink();
        } else if (ejbLocalRef.isSetEjbLink()) {
            ejbLink = getStringValue(ejbLocalRef.getEjbLink());
            targetConfigId = ejbContext.getId();
        }
        if (ejbLink != null) {
            String[] bits = ejbLink.split("#");
            if (bits.length == 2) {
                //look only in specified module.
                requiredModule = bits[0];
                ejbLink = bits[1];
            }
        } else if (localRef != null) {
            GerPatternType patternType = localRef.getPattern();
            containerQuery = buildAbstractNameQuery(patternType, null, NameFactory.EJB_MODULE, null);
        }
        return createEJBLocalRef(refName, ejbContext, ejbLink, requiredModule, optionalModule, targetConfigId, containerQuery, isSession, localHome, local);
    }

    public QNameSet getSpecQNameSet() {
        return ejbLocalRefQNameSet;
    }

    public QNameSet getPlanQNameSet() {
        return GER_EJB_LOCAL_REF_QNAME_SET;
    }


    private static Map mapEjbLocalRefs(XmlObject[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerEjbLocalRefType ref = (GerEjbLocalRefType) refs[i].copy().changeType(GerEjbLocalRefType.type);
                refMap.put(ref.getRefName().trim(), ref);
            }
        }
        return refMap;
    }

    public static Class assureEJBLocalObjectInterface(String local, ClassLoader cl) throws DeploymentException {
        return assureInterface(local, "javax.ejb.EJBLocalObject", "Local", cl);
    }

    public static Class assureEJBLocalHomeInterface(String localHome, ClassLoader cl) throws DeploymentException {
        return assureInterface(localHome, "javax.ejb.EJBLocalHome", "LocalHome", cl);
    }


    private void checkLocalProxyInfo(AbstractNameQuery query, String localHome, String local, Configuration configuration) throws DeploymentException {
        GBeanData data;
        try {
            data = configuration.findGBeanData(query);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Could not locate ejb matching " + query + " in configuration " + configuration.getId());
        }
        if (!localHome.equals(OpenEjbLocalRefBuilder.getLocalHomeInterface(data)) || !local.equals(OpenEjbLocalRefBuilder.getLocalInterface(data))) {
            throw new DeploymentException("Reference interfaces do not match bean interfaces:\n" +
                    "reference localHome: " + localHome + "\n" +
                    "ejb localHome: " + OpenEjbLocalRefBuilder.getLocalHomeInterface(data) + "\n" +
                    "reference local: " + local + "\n" +
                    "ejb local: " + OpenEjbLocalRefBuilder.getLocalInterface(data));
        }
    }

    public Reference createEJBLocalRef(String refName, Configuration configuration, String name, String requiredModule, String optionalModule, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String localHome, String local) throws DeploymentException {
        AbstractNameQuery match;
        if (query != null) {
            checkLocalProxyInfo(query, localHome, local, configuration);
            match = new AbstractNameQuery(query.getArtifact(), query.getName(), RpcEjbDeployment.class.getName());
        } else if (name != null) {
            match = getMatch(refName, configuration, name, requiredModule, false, isSession, localHome, local);
        } else {
            match = getImplicitMatch(refName, configuration, optionalModule, false, isSession, localHome, local);
        }
        return buildLocalReference(configuration.getId(), match, isSession, localHome, local);
    }

    protected Reference buildLocalReference(Artifact configurationId, AbstractNameQuery abstractNameQuery, boolean session, String localHome, String local) {
        return EJBProxyReference.createLocal(configurationId, abstractNameQuery, session, localHome, local);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(OpenEjbLocalRefBuilder.class, NameFactory.MODULE_BUILDER); //TODO decide what type this should be
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);

        infoBuilder.setConstructor(new String[] {"defaultEnvironment", "eeNamespaces"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
