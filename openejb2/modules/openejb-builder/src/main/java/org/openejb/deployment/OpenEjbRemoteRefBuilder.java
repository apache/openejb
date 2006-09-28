/**
 *
 * Copyright 2006 The Apache Software Foundation
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

package org.openejb.deployment;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
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
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefDocument;
import org.apache.geronimo.xbeans.geronimo.naming.GerEjbRefType;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.geronimo.xbeans.j2ee.EjbRefType;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;
import org.openejb.RpcEjbDeployment;
import org.openejb.corba.proxy.CORBAProxyReference;
import org.openejb.proxy.EJBProxyReference;

/**
 * @version $Revision$ $Date$
 */
public class OpenEjbRemoteRefBuilder extends OpenEjbAbstractRefBuilder {

    private static final QName GER_EJB_REF_QNAME = GerEjbRefDocument.type.getDocumentElementName();
    private static final QNameSet GER_EJB_REF_QNAME_SET = QNameSet.singleton(GER_EJB_REF_QNAME);

    private final QNameSet ejbRefQNameSet;

    public OpenEjbRemoteRefBuilder(Environment defaultEnvironment, String[] eeNamespaces) {
        super(defaultEnvironment);
        ejbRefQNameSet = buildQNameSet(eeNamespaces, "ejb-ref");
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) {
         return getEjbRefs(specDD).length > 0;
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module, Map componentContext) throws DeploymentException {
        XmlObject[] ejbRefsUntyped = getEjbRefs(specDD);
        XmlObject[] gerEjbRefsUntyped = plan == null? NO_REFS: plan.selectChildren(GER_EJB_REF_QNAME_SET);
        Map ejbRefMap = mapEjbRefs(gerEjbRefsUntyped);
        ClassLoader cl = localConfiguration.getConfigurationClassLoader();

        for (int i = 0; i < ejbRefsUntyped.length; i++) {
            EjbRefType ejbRef = (EjbRefType) ejbRefsUntyped[i];

            String ejbRefName = getStringValue(ejbRef.getEjbRefName());
            GerEjbRefType remoteRef = (GerEjbRefType) ejbRefMap.get(ejbRefName);

            Reference ejbReference = addEJBRef(localConfiguration, remoteConfiguration, module.getModuleURI(), ejbRef, remoteRef, cl);
            componentContext.put(ENV + ejbRefName, ejbReference);
        }
    }

    private XmlObject[] getEjbRefs(XmlObject specDD) {
        return convert(specDD.selectChildren(ejbRefQNameSet), J2EE_CONVERTER, EjbRefType.type);
    }

    private Reference addEJBRef(Configuration earContext, Configuration ejbContext, URI moduleURI, EjbRefType ejbRef, GerEjbRefType remoteRef, ClassLoader cl) throws DeploymentException {
        String remote = getStringValue(ejbRef.getRemote());
        String refName = getStringValue(ejbRef.getEjbRefName());
        try {
            assureEJBObjectInterface(remote, cl);
        } catch (DeploymentException e) {
            throw new DeploymentException("Error processing 'remote' element for EJB Reference '" + refName + "' for module '" + moduleURI + "': " + e.getMessage());
        }

        String home = getStringValue(ejbRef.getHome());
        try {
            assureEJBHomeInterface(home, cl);
        } catch (DeploymentException e) {
            throw new DeploymentException("Error processing 'home' element for EJB Reference '" + refName + "' for module '" + moduleURI + "': " + e.getMessage());
        }

        Reference ejbReference;
        boolean isSession = "Session".equals(getStringValue(ejbRef.getEjbRefType()));

        if (isSession && remote.equals("javax.management.j2ee.Management") && home.equals("javax.management.j2ee.ManagementHome")) {
            AbstractNameQuery query = new AbstractNameQuery(null, Collections.singletonMap("name", "ejb/mgmt/MEJB"));
            ejbReference = createEJBRemoteRef(null, ejbContext, null, null, null, null, query, isSession, home, remote);
        } else {

            String ejbLink = null;
            if (remoteRef != null && remoteRef.isSetNsCorbaloc()) {
                try {
                    AbstractNameQuery cssBean;
                    if (remoteRef.isSetCssLink()) {
                        String cssLink = remoteRef.getCssLink().trim();
                        cssBean = buildAbstractNameQuery(null, null, cssLink, NameFactory.CORBA_CSS, NameFactory.EJB_MODULE);
                    } else {
                        GerPatternType css = remoteRef.getCss();
                        cssBean = buildAbstractNameQuery(css, NameFactory.CORBA_CSS, NameFactory.EJB_MODULE, null);
                    }
                    ejbReference = createCORBAReference(earContext,
                            cssBean,
                            new URI(remoteRef.getNsCorbaloc().trim()),
                            remoteRef.getName().trim(),
                            home);
                } catch (URISyntaxException e) {
                    throw new DeploymentException("Could not construct CORBA NameServer URI: " + remoteRef.getNsCorbaloc(), e);
                }
            } else {
                Artifact targetConfigId = null;
                String optionalModule = moduleURI == null ? null : moduleURI.toString();
                String requiredModule = null;
                AbstractNameQuery containerQuery = null;
                if (remoteRef != null && remoteRef.isSetEjbLink()) {
                    ejbLink = remoteRef.getEjbLink();
                } else if (ejbRef.isSetEjbLink()) {
                    ejbLink = getStringValue(ejbRef.getEjbLink());
                    targetConfigId = ejbContext.getId();
                }
                if (ejbLink != null) {
                    String[] bits = ejbLink.split("#");
                    if (bits.length == 2) {
                        //look only in specified module.
                        requiredModule = bits[0];
                        if (moduleURI != null) {
                            requiredModule = moduleURI.resolve(requiredModule).getPath();
                        }
                        ejbLink = bits[1];
                    }
                } else if (remoteRef != null) {
                    GerPatternType patternType = remoteRef.getPattern();
                    containerQuery = buildAbstractNameQuery(patternType, null, NameFactory.EJB_MODULE, null);
                }
                ejbReference = createEJBRemoteRef(refName, ejbContext, ejbLink, requiredModule, optionalModule, targetConfigId, containerQuery, isSession, home, remote);
            }
        }
        return ejbReference;
    }

    public QNameSet getSpecQNameSet() {
        return ejbRefQNameSet;
    }

    public QNameSet getPlanQNameSet() {
        return GER_EJB_REF_QNAME_SET;
    }

    private static Map mapEjbRefs(XmlObject[] refs) {
        Map refMap = new HashMap();
        if (refs != null) {
            for (int i = 0; i < refs.length; i++) {
                GerEjbRefType ref = (GerEjbRefType) refs[i].copy().changeType(GerEjbRefType.type);
                refMap.put(ref.getRefName().trim(), ref);
            }
        }
        return refMap;
    }

    public static Class assureEJBObjectInterface(String remote, ClassLoader cl) throws DeploymentException {
        return assureInterface(remote, "javax.ejb.EJBObject", "Remote", cl);
    }

    public static Class assureEJBHomeInterface(String home, ClassLoader cl) throws DeploymentException {
        return assureInterface(home, "javax.ejb.EJBHome", "Home", cl);
    }

    private void checkRemoteProxyInfo(AbstractNameQuery query, String home, String remote, Configuration configuration) throws DeploymentException {
        if (remote.equals("javax.management.j2ee.Management") && home.equals("javax.management.j2ee.ManagementHome")) {
            // Don't verify the MEJB because it doesn't have a proxyInfo attribute
            return;
        }
        GBeanData data;
        try {
            data = configuration.findGBeanData(query);
        } catch (GBeanNotFoundException e) {
            return;
            //we can't check anything, hope for the best.
//            throw new DeploymentException("Could not locate ejb matching " + query + " in configuration " + configuration.getId());
        }
        if (!home.equals(getHomeInterface(data)) || !remote.equals(OpenEjbRemoteRefBuilder.getRemoteInterface(data))) {
            throw new DeploymentException("Reference interfaces do not match bean interfaces:\n" +
                    "reference home: " + home + "\n" +
                    "ejb home: " + OpenEjbRemoteRefBuilder.getHomeInterface(data) + "\n" +
                    "reference remote: " + remote + "\n" +
                    "ejb remote: " + OpenEjbRemoteRefBuilder.getRemoteInterface(data));
        }
    }

    public Reference createCORBAReference(Configuration configuration, AbstractNameQuery containerNameQuery, URI nsCorbaloc, String objectName, String home) throws DeploymentException {
        try {
            configuration.findGBean(containerNameQuery);
        } catch (GBeanNotFoundException e) {
            throw new DeploymentException("Could not find css bean matching " + containerNameQuery + " from configuration " + configuration.getId());
        }
        return new CORBAProxyReference(configuration.getId(), containerNameQuery, nsCorbaloc, objectName, home);
    }

    public Reference createEJBRemoteRef(String refName, Configuration configuration, String name, String requiredModule, String optionalModule, Artifact targetConfigId, AbstractNameQuery query, boolean isSession, String home, String remote) throws DeploymentException {
        AbstractNameQuery match;
        if (query != null) {
            checkRemoteProxyInfo(query, home, remote, configuration);
            match = new AbstractNameQuery(query.getArtifact(), query.getName(), RpcEjbDeployment.class.getName());
        } else if (name != null) {
            match = getMatch(refName, configuration, name, requiredModule, true, isSession, home, remote);
        } else {
            match = getImplicitMatch(refName, configuration, optionalModule, true, isSession, home, remote);
        }
        return buildRemoteReference(configuration.getId(), match, isSession, home, remote);
    }


    protected Reference buildRemoteReference(Artifact configurationId, AbstractNameQuery abstractNameQuery, boolean session, String home, String remote) {
        return EJBProxyReference.createRemote(configurationId, abstractNameQuery, session, home, remote);
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(OpenEjbRemoteRefBuilder.class, NameFactory.MODULE_BUILDER); //TODO decide what type this should be
        infoBuilder.addAttribute("eeNamespaces", String[].class, true, true);
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);

        infoBuilder.setConstructor(new String[] {"defaultEnvironment", "eeNamespaces"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
