/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.openejb.nova.deployment;

import java.util.Set;
import java.net.URL;
import java.net.URI;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;

import org.apache.geronimo.kernel.deployment.AbstractDeploymentPlanner;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.DeploymentPlan;
import org.apache.geronimo.kernel.deployment.DeploymentInfo;
import org.apache.geronimo.kernel.deployment.DeploymentHelper;
import org.apache.geronimo.kernel.deployment.service.MBeanMetadata;
import org.apache.geronimo.kernel.deployment.service.ClassSpaceMetadata;
import org.apache.geronimo.kernel.deployment.task.RegisterMBeanInstance;
import org.apache.geronimo.kernel.deployment.task.StartMBeanInstance;
import org.apache.geronimo.kernel.deployment.task.CreateClassSpace;
import org.apache.geronimo.kernel.deployment.task.DeployGeronimoMBean;
import org.apache.geronimo.kernel.deployment.goal.DeployURL;
import org.apache.geronimo.kernel.deployment.goal.RedeployURL;
import org.apache.geronimo.kernel.deployment.goal.UndeployURL;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanEndpoint;
import org.apache.geronimo.kernel.jmx.JMXKernel;
import org.apache.geronimo.xml.deployment.LoaderUtil;
import org.apache.geronimo.xml.deployment.GeronimoEjbJarLoader;
import org.apache.geronimo.deployment.model.geronimo.ejb.GeronimoEjbJarDocument;
import org.apache.geronimo.deployment.model.geronimo.ejb.EjbJar;
import org.apache.geronimo.deployment.model.geronimo.ejb.EnterpriseBeans;
import org.apache.geronimo.deployment.model.geronimo.ejb.Session;
import org.apache.geronimo.deployment.model.ejb.RpcBean;
import org.apache.geronimo.deployment.model.ejb.Ejb;
import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.java.ComponentContextBuilder;
import org.apache.geronimo.naming.java.ReferenceFactory;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.openejb.nova.slsb.StatelessContainer;
import org.openejb.nova.EJBContainerConfiguration;
import org.openejb.nova.sfsb.StatefulContainer;
import org.openejb.nova.transaction.EJBUserTransaction;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class EJBModuleDeploymentPlanner extends AbstractDeploymentPlanner{

    private static final Log log = LogFactory.getLog(EJBModuleDeploymentPlanner.class);

    private String serverId;

    public static GeronimoMBeanInfo getGeronimoMBeanInfo() {
        return AbstractDeploymentPlanner.getGeronimoMBeanInfo(EJBModuleDeploymentPlanner.class.getName());
    }

    protected boolean addURL(DeployURL goal, Set goals, Set plans) throws DeploymentException {
        URL url = goal.getUrl();
        DeploymentHelper dHelper =
                new DeploymentHelper(url, goal.getType(), "EJBModule", ".jar", "ejb-jar.xml", "geronimo-ejb-jar.xml");
        //URL j2eeURL = dHelper.locateJ2eeDD();
        URL geronimoURL = dHelper.locateGeronimoDD();
        // Is the specific URL deployable?
        if (null == geronimoURL) {
            log.info("Looking at and rejecting url " + url);
            return false;
        }
        URI baseURI = URI.create(url.toString()).normalize();

        log.trace("Planning the ejb module deployment " + url);

        // One can deploy the specified URL. One removes it from the current
        // goal set.
        goals.remove(goal);

        ObjectName deploymentUnitName = dHelper.buildDeploymentName();

        // Defines a deployment plan for the deployment unit.
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        DeploymentInfo deploymentInfo =
                new DeploymentInfo(deploymentUnitName, null, url);
        deploymentPlan.addTask(
                new RegisterMBeanInstance(getServer(), deploymentUnitName, deploymentInfo));
        MBeanMetadata deploymentUnitMetadata = new MBeanMetadata(deploymentUnitName);
        deploymentPlan.addTask(
                new StartMBeanInstance(getServer(), deploymentUnitMetadata));
        // Define the ClassSpace for the archives.
        ClassSpaceMetadata classSpaceMetaData = dHelper.buildClassSpace();
        deploymentPlan.addTask(new CreateClassSpace(getServer(), classSpaceMetaData));//parent???
        plans.add(deploymentPlan);

        //now another plan for the tasks that depend on the class space.
        deploymentPlan = new DeploymentPlan();
        // Load the deployment descriptor into our POJO
        URI gejbURI = URI.create(geronimoURL.toString()).normalize();
        log.trace("Loading deployment descriptor " + gejbURI);

        GeronimoEjbJarDocument gejbDoc = null;
        try {
            //currently everything is in the geronimo-ejb-jar.xml
            Document gejbDocument =
                    LoaderUtil.parseXML(new InputStreamReader(geronimoURL.openStream()));
            gejbDoc = GeronimoEjbJarLoader.load(gejbDocument);
        } catch (FileNotFoundException e1) {
            throw new DeploymentException("Deployment descriptor not found", e1);
        } catch (SAXException e1) {
            throw new DeploymentException("geronimo-ejb-jar.xml malformed", e1);
        } catch (IOException e1) {
            throw new DeploymentException("Deployment descriptor not readable", e1);
        }
        EjbJar ejbJar = gejbDoc.getEjbJar();
        EnterpriseBeans enterpriseBeans = ejbJar.getGeronimoEnterpriseBeans();
        for (int i = 0; i < enterpriseBeans.getGeronimoSession().length; i++) {
            Session session = enterpriseBeans.getGeronimoSession(i);
            plans.add(planSession(session, deploymentUnitName, classSpaceMetaData, baseURI));

        }

        return true;
    }

    DeploymentPlan planSession(Session session, ObjectName deploymentUnitName, ClassSpaceMetadata classSpaceMetaData, URI baseURI) throws DeploymentException {
        MBeanMetadata ejbMetadata = getMBeanMetadata(classSpaceMetaData.getName(), deploymentUnitName, baseURI);
        ejbMetadata.setName(getContainerName(session));
        ejbMetadata.setGeronimoMBeanInfo(getSessionGeronimoMBeanInfo(session.getSessionType().equals("Stateless")?
                StatelessContainer.class.getName():StatefulContainer.class.getName()));
        EJBContainerConfiguration config = getSessionConfig(session);

        ejbMetadata.setConstructorArgs(new Object[] {config},
                new String[] {EJBContainerConfiguration.class.getName()});
        return addTasks(ejbMetadata);
    }

    EJBContainerConfiguration getSessionConfig(Session session) throws DeploymentException {
        EJBContainerConfiguration config = new EJBContainerConfiguration();
        //configure config

        RpcBean bean = session;

        config.uri = null;//???
        config.beanClassName = bean.getEJBClass();
        config.homeInterfaceName = bean.getHome();
        config.remoteInterfaceName = bean.getRemote();
        config.localHomeInterfaceName = bean.getLocalHome();
        config.localInterfaceName = bean.getLocal();
        config.txnDemarcation = TransactionDemarcation.valueOf(session.getTransactionType());
        config.userTransaction = config.txnDemarcation.isContainer()? null: new EJBUserTransaction();
        config.componentContext = getComponentContext(session, config.userTransaction);
        //config.txnManager = txManager;   // needs to be endpoint
        return config;
    }

    private  ReadOnlyContext getComponentContext(Session session, UserTransaction userTransaction) throws DeploymentException {
        ReferenceFactory referenceFactory = new JMXReferenceFactory(getMBeanServerId());
        ComponentContextBuilder builder = new ComponentContextBuilder(referenceFactory, userTransaction);
        ReadOnlyContext context = builder.buildContext(session);
        return context;
    }

    private String getMBeanServerId() {
        if (serverId != null) {
            return serverId;
        }
        return JMXKernel.getMBeanServerId(getMBeanContext().getServer());
    }

    void setMBeanServerId(String serverId) {
        this.serverId = serverId;
    }

    private GeronimoMBeanInfo getSessionGeronimoMBeanInfo(String className) {
        GeronimoMBeanInfo mbeanInfo= new GeronimoMBeanInfo();
        mbeanInfo.setTargetClass(className);
        //mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Uri", true, false, "Original deployment package URI?"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("BeanClassName", true, false, "Bean implementation class name"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("HomeClassName", true, false, "Home interface class name"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("RemoteClassName", true, false, "Remote interface class name"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("LocalHomeClassName", true, false, "Local home interface class name"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("LocalClassName", true, false, "Local interface class name"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Demarcation", true, false, "Transaction demarcation"));

        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("getEJBHome"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("getEJBLocalHome"));
        try {
            mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint("TransactionManager",
                    TransactionManager.class.getName(),
                    ObjectName.getInstance("geronimo.transaction:role=TransactionManager"),
                    true));
        } catch (MalformedObjectNameException e) {
            throw new AssertionError();//our o.n. is not malformed.
        }
        return mbeanInfo;
    }

    DeploymentPlan addTasks(MBeanMetadata ejbMetadata) {
        DeploymentPlan deploymentPlan = new DeploymentPlan();
        deploymentPlan.addTask(new DeployGeronimoMBean(getServer(), ejbMetadata));
        deploymentPlan.addTask(new StartMBeanInstance(getServer(), ejbMetadata));
        return deploymentPlan;
    }

    private ObjectName getContainerName(Ejb ejb) throws DeploymentException {
        try {
            return ObjectName.getInstance("geronimo.j2ee:J2eeType=SessionBean,name=" + ejb.getEJBName());
        } catch (MalformedObjectNameException e) {
            throw new DeploymentException("Ejb name " + ejb.getEJBName() + "won't fit in an object name", e);
        }
    }

    protected boolean redeployURL(RedeployURL redeployURL, Set goals) throws DeploymentException {
        return false;
    }

    protected boolean removeURL(UndeployURL undeployURL, Set goals, Set plans) throws DeploymentException {
        return false;
    }
}
