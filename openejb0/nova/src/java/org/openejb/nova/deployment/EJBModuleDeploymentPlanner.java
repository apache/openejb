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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URL;
import java.util.Set;
import java.util.HashMap;
import java.util.Collection;
import java.util.ArrayList;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.transaction.UserTransaction;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.model.ejb.CmpField;
import org.apache.geronimo.deployment.model.ejb.Ejb;
import org.apache.geronimo.deployment.model.ejb.RpcBean;
import org.apache.geronimo.deployment.model.ejb.CmrField;
import org.apache.geronimo.deployment.model.geronimo.ejb.EjbRelation;
import org.apache.geronimo.deployment.model.geronimo.ejb.Relationships;
import org.apache.geronimo.deployment.model.geronimo.ejb.EjbRelationshipRole;
import org.apache.geronimo.deployment.model.geronimo.ejb.EjbJar;
import org.apache.geronimo.deployment.model.geronimo.ejb.EnterpriseBeans;
import org.apache.geronimo.deployment.model.geronimo.ejb.Entity;
import org.apache.geronimo.deployment.model.geronimo.ejb.GeronimoEjbJarDocument;
import org.apache.geronimo.deployment.model.geronimo.ejb.MessageDriven;
import org.apache.geronimo.deployment.model.geronimo.ejb.Query;
import org.apache.geronimo.deployment.model.geronimo.ejb.Session;
import org.apache.geronimo.deployment.model.geronimo.j2ee.JNDIEnvironmentRefs;
import org.apache.geronimo.ejb.metadata.TransactionDemarcation;
import org.apache.geronimo.kernel.deployment.AbstractDeploymentPlanner;
import org.apache.geronimo.kernel.deployment.DeploymentException;
import org.apache.geronimo.kernel.deployment.DeploymentHelper;
import org.apache.geronimo.kernel.deployment.DeploymentInfo;
import org.apache.geronimo.kernel.deployment.DeploymentPlan;
import org.apache.geronimo.kernel.deployment.goal.DeployURL;
import org.apache.geronimo.kernel.deployment.goal.RedeployURL;
import org.apache.geronimo.kernel.deployment.goal.UndeployURL;
import org.apache.geronimo.kernel.deployment.service.ClassSpaceMetadata;
import org.apache.geronimo.kernel.deployment.service.MBeanMetadata;
import org.apache.geronimo.kernel.deployment.task.CreateClassSpace;
import org.apache.geronimo.kernel.deployment.task.DeployGeronimoMBean;
import org.apache.geronimo.kernel.deployment.task.RegisterMBeanInstance;
import org.apache.geronimo.kernel.deployment.task.StartMBeanInstance;
import org.apache.geronimo.kernel.jmx.JMXKernel;
import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.naming.java.ComponentContextBuilder;
import org.apache.geronimo.naming.java.ReadOnlyContext;
import org.apache.geronimo.naming.java.ReferenceFactory;
import org.apache.geronimo.naming.jmx.JMXReferenceFactory;
import org.apache.geronimo.xml.deployment.GeronimoEjbJarLoader;
import org.apache.geronimo.xml.deployment.LoaderUtil;
import org.openejb.nova.EJBContainerConfiguration;
import org.openejb.nova.entity.EntityContainerConfiguration;
import org.openejb.nova.entity.cmp.CMRelation;
import org.openejb.nova.sfsb.StatefulContainer;
import org.openejb.nova.slsb.StatelessContainer;
import org.openejb.nova.transaction.EJBUserTransaction;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

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
                new DeploymentHelper(url, goal.getType(), "EJBModule", "ejb-jar.xml", "geronimo-ejb-jar.xml");
        //URL j2eeURL = dHelper.locateJ2eeDD();
        URL geronimoURL = dHelper.locateGeronimoDD();
        // Is the specific URL deployable?
        if (null == geronimoURL) {
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
        String moduleName = ejbJar.getModuleName();
        String datasourceName = ejbJar.getDatasourceName();
        EnterpriseBeans enterpriseBeans = ejbJar.getGeronimoEnterpriseBeans();
        //All ejbs deployed in one plan
        DeploymentPlan plan = new DeploymentPlan();
        for (int i = 0; i < enterpriseBeans.getGeronimoSession().length; i++) {
            Session session = enterpriseBeans.getGeronimoSession(i);
            planSession(plan, session, deploymentUnitName, classSpaceMetaData, baseURI);
        }

        //Message driven
        for (int i = 0; i < enterpriseBeans.getGeronimoMessageDriven().length; i++) {
            MessageDriven messageDriven = enterpriseBeans.getGeronimoMessageDriven()[i];
            planMessageDriven(plan, messageDriven, deploymentUnitName, classSpaceMetaData, baseURI);
        }

        //Entity
        //Create the schema if datasource is specified
        DeploySchemaMBean schemaTask = null;
        if (datasourceName != null) {
            MBeanMetadata schemaMetadata = getMBeanMetadata(classSpaceMetaData.getName(), deploymentUnitName, baseURI);
            ObjectName datasourceObjectName;
            try {
                schemaMetadata.setName(ObjectName.getInstance("geronimo.j2ee:J2eeType=AbstractSchema,name=" + moduleName));
                datasourceObjectName = ObjectName.getInstance("geronimo.management:j2eeType=JCAManagedConnectionFactory,name=" + datasourceName);
            } catch (MalformedObjectNameException e) {
                throw new DeploymentException("Bad object name", e);
            }

            schemaTask = new DeploySchemaMBean(getServer(), datasourceObjectName, schemaMetadata);
            plan.addTask(schemaTask);
            plan.addTask(new StartMBeanInstance(getServer(), schemaMetadata));
        }

        //Construct an ejb-name to abstract-schema-name map.
        HashMap ejbNameToAbstractSchemaNameMap = new HashMap();
        for (int i = 0; i < enterpriseBeans.getGeronimoEntity().length; i++) {
            Entity entity = enterpriseBeans.getGeronimoEntity()[i];
            if (entity.getPersistenceType().equals("Container")) {
                String ejbName = entity.getEJBName();
                String abstractSchemaName = entity.getAbstractSchemaName();
                ejbNameToAbstractSchemaNameMap.put(ejbName, abstractSchemaName);
            }
        }

        //As we process the relationships, we store them in a collection.
        //These collections are kept in a map indexed by ejb-name

        HashMap ejbNameToRelationshipRoleCollectionMap = new HashMap();
        Relationships relationships = ejbJar.getGeronimoRelationships();
        if (relationships != null) {
            EjbRelation[] ejbRelations = relationships.getGeronimoEjbRelation();
            for (int i = 0; i < ejbRelations.length; i++) {
                EjbRelation ejbRelation = ejbRelations[i];
                assert ejbRelation.getEjbRelationshipRole().length == 2;
                String leftEjbName = ejbRelation.getEjbRelationshipRole(0).getRelationshipRoleSource().getEjbName();
                String leftAbstractSchemaName = (String)ejbNameToAbstractSchemaNameMap.get(leftEjbName);
                String rightEjbName = ejbRelation.getEjbRelationshipRole(1).getRelationshipRoleSource().getEjbName();
                String rightAbstractSchemaName = (String)ejbNameToAbstractSchemaNameMap.get(rightEjbName);
                CMRelation leftCMRelation = getCMRelation(ejbRelation.getGeronimoEjbRelationshipRole(0), rightAbstractSchemaName);
                CMRelation rightCMRelation = getCMRelation(ejbRelation.getGeronimoEjbRelationshipRole(1), leftAbstractSchemaName);
                mapCMRelation(leftEjbName, leftCMRelation, ejbNameToRelationshipRoleCollectionMap);
                mapCMRelation(rightEjbName, rightCMRelation, ejbNameToRelationshipRoleCollectionMap);
            }
        }

        //Now set up the entities.
        for (int i = 0; i < enterpriseBeans.getGeronimoEntity().length; i++) {
            Entity entity = enterpriseBeans.getGeronimoEntity()[i];
            if (entity.getPersistenceType().equals("Container")) {
                assert datasourceName != null;
                Collection rels = (Collection)ejbNameToRelationshipRoleCollectionMap.get(entity.getEJBName());
                CMRelation[] cmRelation;
                if (rels == null) {
                    cmRelation = new CMRelation[0];
                } else {
                    cmRelation = (CMRelation[])rels.toArray(new CMRelation[rels.size()]);
                }
                planCMPEntity(plan, entity, cmRelation, schemaTask, deploymentUnitName, classSpaceMetaData, baseURI);
            } else {
                planBMPEntity(plan, entity, deploymentUnitName, classSpaceMetaData, baseURI);
            }
        }
        plans.add(plan);
        return true;
    }

    private void mapCMRelation(String ejbName, CMRelation cmRelation, HashMap ejbNameToRelationshipRoleCollectionMap) {
        Collection roles = (Collection)ejbNameToRelationshipRoleCollectionMap.get(ejbName);
        if (roles == null) {
            roles = new ArrayList();
            ejbNameToRelationshipRoleCollectionMap.put(ejbName, roles);
        }
        roles.add(cmRelation);
    }

    private CMRelation getCMRelation(EjbRelationshipRole ejbRelationshipRole, String abstractSchemaName) {
        String name = ejbRelationshipRole.getCmrField().getCmrFieldName();
        boolean cascadeDelete = ejbRelationshipRole.isCascadeDelete();
        return new CMRelation(name, abstractSchemaName, cascadeDelete);
    }

    private void planBMPEntity(DeploymentPlan plan, Entity entity, ObjectName deploymentUnitName, ClassSpaceMetadata classSpaceMetaData, URI baseURI) throws DeploymentException {
        MBeanMetadata ejbMetadata = getMBeanMetadata(classSpaceMetaData.getName(), deploymentUnitName, baseURI);
        ejbMetadata.setName(getContainerName(entity));
        ejbMetadata.setGeronimoMBeanInfo(EJBInfo.getBMPEntityGeronimoMBeanInfo());
        EJBContainerConfiguration config = getEntityConfig(entity);

        ejbMetadata.setConstructorArgs(new Object[] {config},
                new String[] {EntityContainerConfiguration.class.getName()});
        addTasks(plan, ejbMetadata);
    }

    private void planCMPEntity(DeploymentPlan plan,
                               Entity entity,
                               CMRelation[] cmRelations,
                               DeploySchemaMBean schemaTask,
                               ObjectName deploymentUnitName,
                               ClassSpaceMetadata classSpaceMetaData,
                               URI baseURI) throws DeploymentException {
        MBeanMetadata ejbMetadata = getMBeanMetadata(classSpaceMetaData.getName(), deploymentUnitName, baseURI);
        ejbMetadata.setName(getContainerName(entity));
        ejbMetadata.setGeronimoMBeanInfo(EJBInfo.getCMPEntityGeronimoMBeanInfo());
        EntityContainerConfiguration config = getEntityConfig(entity);

        Query[] queries = entity.getGeronimoQuery();
        Query[] updates = entity.getUpdate();
        Query[] call = entity.getCall();
        CmpField[] fields = entity.getCmpField();
        String[] cmpFieldNames = new String[fields.length];
        for (int i = 0; i < fields.length; i++) {
            CmpField field = fields[i];
            cmpFieldNames[i] = field.getFieldName();
        }

        plan.addTask(new DeployCMPEntityContainer(getServer(),
                ejbMetadata,
                schemaTask,
                config,
                queries,
                updates,
                call,
                cmpFieldNames,
                cmRelations));
        plan.addTask(new StartMBeanInstance(getServer(), ejbMetadata));

    }

    void planSession(DeploymentPlan plan, Session session, ObjectName deploymentUnitName, ClassSpaceMetadata classSpaceMetaData, URI baseURI) throws DeploymentException {
        MBeanMetadata ejbMetadata = getMBeanMetadata(classSpaceMetaData.getName(), deploymentUnitName, baseURI);
        ejbMetadata.setName(getContainerName(session));
        ejbMetadata.setGeronimoMBeanInfo(EJBInfo.getSessionGeronimoMBeanInfo(session.getSessionType().equals("Stateless")?
                StatelessContainer.class.getName():StatefulContainer.class.getName()));
        EJBContainerConfiguration config = getSessionConfig(session);

        ejbMetadata.setConstructorArgs(new Object[] {config},
                new String[] {EJBContainerConfiguration.class.getName()});
        addTasks(plan, ejbMetadata);
    }

    void planMessageDriven(DeploymentPlan plan, MessageDriven messageDriven, ObjectName deploymentUnitName, ClassSpaceMetadata classSpaceMetaData, URI baseURI) throws DeploymentException {
        MBeanMetadata ejbMetadata = getMBeanMetadata(classSpaceMetaData.getName(), deploymentUnitName, baseURI);
        ejbMetadata.setName(getContainerName(messageDriven));
        ejbMetadata.setGeronimoMBeanInfo(EJBInfo.getMessageDrivenGeronimoMBeanInfo());
//        MessageDrivenContainer.class.getName()));
        EJBContainerConfiguration config = getMessageDrivenConfig(messageDriven);

        ejbMetadata.setConstructorArgs(new Object[] {config},
                new String[] {EJBContainerConfiguration.class.getName()});
        addTasks(plan, ejbMetadata);
    }

    private EJBContainerConfiguration getMessageDrivenConfig(MessageDriven messageDriven) {
        return null;//TODO

    }


    EJBContainerConfiguration getSessionConfig(Session session) throws DeploymentException {
        EJBContainerConfiguration config = new EJBContainerConfiguration();
        //configure config

        genericConfig(session, config);
        config.txnDemarcation = TransactionDemarcation.valueOf(session.getTransactionType());
        config.userTransaction = config.txnDemarcation.isContainer()? null: new EJBUserTransaction();

        //config.txnManager = txManager;   // needs to be endpoint
        return config;
    }

    private EntityContainerConfiguration getEntityConfig(Entity entity) throws DeploymentException {
        EntityContainerConfiguration config = new EntityContainerConfiguration();
        genericConfig(entity, config);
        config.pkClassName = entity.getPrimKeyClass();
        return config;
    }

    private void genericConfig(RpcBean rpcBean, EJBContainerConfiguration config) throws DeploymentException {

        config.uri = null;//???
        config.beanClassName = rpcBean.getEJBClass();
        config.homeInterfaceName = rpcBean.getHome();
        config.remoteInterfaceName = rpcBean.getRemote();
        config.localHomeInterfaceName = rpcBean.getLocalHome();
        config.localInterfaceName = rpcBean.getLocal();
        config.componentContext = getComponentContext((JNDIEnvironmentRefs)rpcBean, config.userTransaction);
    }

    private  ReadOnlyContext getComponentContext(JNDIEnvironmentRefs refs, UserTransaction userTransaction) throws DeploymentException {
        ReferenceFactory referenceFactory = new JMXReferenceFactory(getMBeanServerId());
        ComponentContextBuilder builder = new ComponentContextBuilder(referenceFactory, userTransaction);
        ReadOnlyContext context = builder.buildContext(refs);
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


    void addTasks(DeploymentPlan plan, MBeanMetadata ejbMetadata) {
        plan.addTask(new DeployGeronimoMBean(getServer(), ejbMetadata));
        plan.addTask(new StartMBeanInstance(getServer(), ejbMetadata));
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
