/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.openejb.deployment;

import java.net.URI;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.j2ee.deployment.Module;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.GBeanNotFoundException;
import org.apache.geronimo.kernel.config.Configuration;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.naming.deployment.AbstractNamingBuilder;
import org.apache.geronimo.naming.deployment.ENCConfigBuilder;
import org.apache.geronimo.xbeans.geronimo.naming.GerPatternType;
import org.apache.openejb.corba.TSSLinkGBean;
import org.apache.openejb.xbeans.ejbjar.OpenejbOpenejbJarDocument;
import org.apache.xmlbeans.QNameSet;
import org.apache.xmlbeans.XmlObject;

/**
 * @version $Rev:$ $Date:$
 */
public class TSSLinkBuilder extends AbstractNamingBuilder {

    private static final String OPENEJB_NAMESPACE = OpenejbOpenejbJarDocument.type.getDocumentElementName().getNamespaceURI();
    private static final QName EJB_NAME_QNAME = new QName(OPENEJB_NAMESPACE, "ejb-name");
    private static final QName TSS_LINK_QNAME = new QName(OPENEJB_NAMESPACE, "tss-link");
    private static final QName TSS_QNAME = new QName(OPENEJB_NAMESPACE, "tss");
    private static final QName JNDI_NAME_QNAME = new QName(OPENEJB_NAMESPACE, "jndi-name");

    public TSSLinkBuilder() {
    }

    public TSSLinkBuilder(Environment defaultEnvironment) {
        super(defaultEnvironment);
    }

    protected boolean willMergeEnvironment(XmlObject specDD, XmlObject plan) {
        return plan.selectChildren(TSS_LINK_QNAME).length > 0 ||
                plan.selectChildren(TSS_QNAME).length > 0;
    }

    public void buildNaming(XmlObject specDD, XmlObject plan, Configuration localConfiguration, Configuration remoteConfiguration, Module module, Map componentContext) throws DeploymentException {
        AbstractName ejbName = getGBeanName(componentContext);

        String[] tssLinks = toStringArray(plan.selectChildren(TSS_LINK_QNAME));
        XmlObject[] tsss = plan.selectChildren(TSS_QNAME);
        String[] jndiNames = toStringArray(plan.selectChildren(JNDI_NAME_QNAME));
        for (int i = 0; i < tssLinks.length; i++) {
            String tssLink = tssLinks[i];
            URI moduleURI = module.getModuleURI();
            String moduleString = moduleURI == null ? null : moduleURI.toString();
            AbstractNameQuery tssBeanName = ENCConfigBuilder.buildAbstractNameQuery(null, moduleString, tssLink, NameFactory.EJB_MODULE, NameFactory.EJB_MODULE);
            try {
                localConfiguration.findGBean(tssBeanName);
            } catch (GBeanNotFoundException e) {
                tssBeanName = ENCConfigBuilder.buildAbstractNameQuery(null, null, tssLink, null, NameFactory.EJB_MODULE);
                try {
                    localConfiguration.findGBean(tssBeanName);
                } catch (GBeanNotFoundException e1) {
                    throw new DeploymentException("No tss bean found", e);
                }
            }
            AbstractName tssLinkName = module.getEarContext().getNaming().createChildName(ejbName, "tssLink" + i, "TSSLink");
            GBeanData tssLinkData = new GBeanData(tssLinkName, TSSLinkGBean.GBEAN_INFO);
            tssLinkData.setAttribute("jndiNames", jndiNames);
            tssLinkData.setReferencePattern("EJB", ejbName);
            tssLinkData.setReferencePattern("TSSBean", tssBeanName);
            try {
                localConfiguration.addGBean(tssLinkData);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("tss link gbean already present", e);
            }
        }
        for (int i = 0; i < tsss.length; i++) {
            GerPatternType tss = (GerPatternType) tsss[i];
            AbstractNameQuery tssBeanName = ENCConfigBuilder.buildAbstractNameQuery(tss, NameFactory.CORBA_TSS, NameFactory.EJB_MODULE, null);
            AbstractName tssLinkName = module.getEarContext().getNaming().createChildName(ejbName, "tssRef" + i, "TSSLink");
            GBeanData tssLinkData = new GBeanData(tssLinkName, TSSLinkGBean.GBEAN_INFO);
            tssLinkData.setAttribute("jndiNames", jndiNames);
            tssLinkData.setReferencePattern("EJB", ejbName);
            tssLinkData.setReferencePattern("TSSBean", tssBeanName);
            try {
                localConfiguration.addGBean(tssLinkData);
            } catch (GBeanAlreadyExistsException e) {
                throw new DeploymentException("tss link gbean already present", e);
            }
        }

    }

    public QNameSet getSpecQNameSet() {
        return QNameSet.EMPTY;
    }

    public QNameSet getPlanQNameSet() {
        return QNameSet.singleton(TSS_LINK_QNAME);
    }

    private String[] toStringArray(XmlObject[] xmlObjects) {
        String[] result = new String[xmlObjects.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = xmlObjects[i].toString();
        }
        return result;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(TSSLinkBuilder.class, NameFactory.MODULE_BUILDER); //TODO decide what type this should be
        infoBuilder.addAttribute("defaultEnvironment", Environment.class, true, true);

        infoBuilder.setConstructor(new String[] {"defaultEnvironment"});

        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}


