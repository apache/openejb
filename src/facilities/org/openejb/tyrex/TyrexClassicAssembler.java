/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */

package org.openejb.tyrex;

import org.openejb.alt.assembler.classic.*;
/*import org.openejb.alt.assembler.classic.EnterpriseBeanInfo;
import org.openejb.alt.assembler.classic.EntityBeanInfo;
import org.openejb.alt.assembler.classic.StatefulBeanInfo;
import org.openejb.alt.assembler.classic.StatelessBeanInfo;
import org.openejb.alt.assembler.classic.ResourceReferenceInfo;*/

import org.openejb.core.*;
//import org.openejb.core.DeploymentInfo;
import org.openejb.core.entity.EntityContainer;
import org.openejb.core.stateful.StatefulContainer;
import org.openejb.core.stateful.StatefulInstanceManager;
import org.openejb.core.stateless.StatelessContainer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import javax.transaction.TransactionManager;
import java.lang.reflect.Method;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import javax.naming.InitialContext;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Vector;


import org.openejb.core.ivm.naming.IvmContext;
import org.openejb.core.ivm.naming.NameNode;
import org.openejb.core.ivm.naming.ParsedName;
import org.openejb.core.ivm.naming.ENCReference;
import tyrex.tm.TransactionDomain;
import tyrex.resource.Resources;
import tyrex.resource.Resource;
import java.util.Properties;
import org.openejb.core.EnvProps;
import org.openejb.OpenEJBException;
import org.openejb.util.SafeProperties;
import org.openejb.util.SafeToolkit;
import java.util.Vector;
import java.lang.reflect.Method;



public class TyrexClassicAssembler extends org.openejb.alt.assembler.classic.Assembler {
    
    protected static SafeToolkit toolkit = SafeToolkit.getToolkit("TyrexClassicAssembler");


    public void init(Properties props) throws OpenEJBException{
        
        // the EnvProps.THREAD_CONTEXT_IMPL is accessed from the System properties
        // rather then the props because the ThreadContext class doen't have access to the
        // the Assembler's props.
        if(!System.getProperties().contains(EnvProps.THREAD_CONTEXT_IMPL)){
            if(!props.contains(EnvProps.THREAD_CONTEXT_IMPL)){
                System.setProperty(EnvProps.THREAD_CONTEXT_IMPL, "org.openejb.tyrex.TyrexThreadContext");
            }else{
                System.setProperty(EnvProps.THREAD_CONTEXT_IMPL, props.getProperty(EnvProps.THREAD_CONTEXT_IMPL));
            }
        }
        // the TyrexEnvProps.TX_DOMAIN property is accessed from the System properties from the
        // bindJndiResourceRefs() rather then the props because its a static method.
        
        // need to create the domain if it does not already exist.
        if(TransactionDomain.getDomain("default") ==null ){
            String domainPath = props.getProperty(TyrexEnvProps.TX_DOMAIN);
            if(domainPath==null){
                domainPath  = System.getProperty(TyrexEnvProps.TX_DOMAIN);
            }
            if(domainPath!=null){
                try{
                TransactionDomain.createDomain(domainPath);
                }catch(tyrex.tm.DomainConfigurationException dce){
                    throw new OpenEJBException("Although the TyrexEnvProp.TX_DOMAIN property was set the domain could not be created", dce);
                }
            }
        }
            
        
        super.init(props);
        
        
    }
    
    /**
     * When given a complete ContainerSystemInfo object, this method,
     * will construct all the containers (entity, stateful, stateless)
     * and add those containers to the ContainerSystem.  The containers 
     * are constructed using the assembleContainer() method. Once constructed
     * the container and its deployments are added to the container system.
     *
     * Assembles and returns a {@link ContainerManager} for the {@link ContainerSystem} using the
     * information from the {@link ContainerManagerInfo} object passed in.
     * 
     * @param containerSystem the system to which the container should be added.
     * @param containerSystemInfo defines the contain system,its containers, and deployments.
     * @return 
     * @exception throws    Exception if there was a problem constructing the ContainerManager.
     * @exception Exception
     * @see org.openejb.core.ContainerManager
     * @see org.openejb.core.ContainerSystem
     * @see ContainerManagerInfo
     */
    public static void assembleContainers (ContainerSystem containerSystem, ContainerSystemInfo containerSystemInfo) throws Exception{
        
        ArrayList list = new ArrayList();
        if(containerSystemInfo.entityContainers!=null)list.addAll(Arrays.asList(containerSystemInfo.entityContainers));
        if(containerSystemInfo.statefulContainers!=null)list.addAll(Arrays.asList(containerSystemInfo.statefulContainers));
        if(containerSystemInfo.statelessContainers!=null)list.addAll(Arrays.asList(containerSystemInfo.statelessContainers));
        Iterator iterator = list.iterator();
        while(iterator.hasNext()){
            ContainerInfo containerInfo = (ContainerInfo)iterator.next();
            org.openejb.Container container = assembleContainer(containerInfo);
            containerSystem.addContainer(container.getContainerID(),container);
        }

        // ADD deployments to container system and to Global JNDI name space
        org.openejb.Container [] containers = containerSystem.containers();
        for(int i = 0; i < containers.length; i++){
            org.openejb.DeploymentInfo deployments [] = containers[i].deployments();
            for(int x = 0; x < deployments.length; x++){
                containerSystem.addDeployment((org.openejb.core.DeploymentInfo)deployments[x]);
            }
        }
        

    }

    
    /**
     * This method can construct a Container of any kind based on information in the 
     * ContainerInfo object: StatefulContainer, StatelessContainer, or EntityContainer
     * In addition to constructing the containers, this method also constructs all the 
     * deployments declared in the containerInfo object and adds them to the containers 
     * It constructs the deployment Info object using the assembleDeploymentInfo 
     * method.
     * @param containerInfo describes a Container and its deployments.
     * @return the Container that was constructed (StatefulContainer, StatelessContainer, EntityContainer)
     * @see org.openejb.alt.assembler.classic.ContainerInfo
     * @see org.openejb.alt.assembler.classic.Assembler.assembleDeploymentInfo();
    */
    public static org.openejb.Container assembleContainer(ContainerInfo containerInfo)
    throws org.openejb.OpenEJBException{
        HashMap deployments = new HashMap();
        for(int z = 0; z < containerInfo.ejbeans.length; z++){
            DeploymentInfo deployment = assembleDeploymentInfo(containerInfo.ejbeans[z]);
            deployments.put(containerInfo.ejbeans[z].ejbDeploymentId, deployment);
        }
        org.openejb.Container container = null;
        
        if(containerInfo.className != null){
           // create the custom container
           try{
                //container = (org.openejb.Container)Class.forName(containerInfo.codebase).newInstance();
                // Support for an actual codebase.
                container = (org.openejb.Container)toolkit.loadClass(containerInfo.className, containerInfo.codebase).newInstance();
           }catch(OpenEJBException oee){
               Object[] details = {containerInfo, oee.getMessage()};
               throw new OpenEJBException("as0002", details);
           }catch(InstantiationException ie){
               Object[] details = {containerInfo, ie.getMessage()};
               throw new OpenEJBException("as0003", details);
           }catch(IllegalAccessException iae){
               Object[] details = {containerInfo, iae.getMessage()};
               throw new OpenEJBException("as0003", details);
           }
        }else{
            // create a standard container
            switch(containerInfo.containerType){
                case ContainerInfo.STATEFUL_SESSION_CONTAINER:
                    container = new StatefulContainer();
                    break;
                case ContainerInfo.ENTITY_CONTAINER:
                    container = new EntityContainer();
                    break;
                case ContainerInfo.STATELESS_SESSION_CONTAINER:
                    container = new StatelessContainer();
                    
            }
        }
        try{
            container.init(containerInfo.containerName, deployments, containerInfo.properties);                    
        } catch (OpenEJBException e){
            Object[] details = {containerInfo.containerName, e.getMessage()};
            throw new OpenEJBException("as0004",details);
        }
        
        return container;
    }
       
    public static DeploymentInfo assembleDeploymentInfo(EnterpriseBeanInfo beanInfo)
    throws org.openejb.SystemException, org.openejb.OpenEJBException {
  
        boolean isEntity = false;
        EntityBeanInfo ebi = null;
        
        /*[1] Check the bean's type */
        byte componentType;
        if(beanInfo instanceof EntityBeanInfo){
            isEntity = true;
            ebi = (EntityBeanInfo)beanInfo;
            if(ebi.persistenceType.equals("Container")){
                componentType = org.openejb.core.DeploymentInfo.CMP_ENTITY;
            }else{
                componentType = org.openejb.core.DeploymentInfo.BMP_ENTITY;
            }
        }else if(beanInfo instanceof StatefulBeanInfo)
            componentType = org.openejb.core.DeploymentInfo.STATEFUL;
        else
            componentType = org.openejb.core.DeploymentInfo.STATELESS;
                
        /*[2] Load the bean's classes */
        Class ejbClass = null;
        Class home     = null;
        Class remote   = null;
        Class ejbPk    = null;

        /*[2.1] Load the bean class */
        try {
            ejbClass = toolkit.loadClass(beanInfo.ejbClass, beanInfo.codebase);
        } catch (OpenEJBException e){
            Object[] details = {beanInfo.ejbClass, beanInfo.ejbDeploymentId, e.getMessage() };
            throw new OpenEJBException("cl0005", details);
        }
        /*[2.2] Load the remote interface */
        try {
            home = toolkit.loadClass(beanInfo.home, beanInfo.codebase);
        } catch (OpenEJBException e){
            Object[] details = {beanInfo.home, beanInfo.ejbDeploymentId, e.getMessage() };
            throw new OpenEJBException("cl0004", details);
        }

        /*[2.3] Load the home interface */
        try {
            remote = toolkit.loadClass(beanInfo.remote, beanInfo.codebase);
        } catch (OpenEJBException e){
            Object[] details = {beanInfo.remote, beanInfo.ejbDeploymentId, e.getMessage() };
            throw new OpenEJBException("cl0003", details);
        }

        /*[2.4] Load the primary-key class */
        if (isEntity || ebi.primKeyClass != null) {
            try {
                ejbPk = toolkit.loadClass(ebi.primKeyClass, beanInfo.codebase);
            } catch (OpenEJBException e){
                Object[] details = {ebi.primKeyClass, beanInfo.ejbDeploymentId, e.getMessage() };
                throw new OpenEJBException("cl0006", details);
            }
        }
        /*[3] Populate a new DeploymentInfo object  */
        
        IvmContext root = new IvmContext(new NameNode(null, new ParsedName("comp"),null));
       /*javax.naming.Context tyrexRoot = null;
       try {
         
         tyrex.naming.MemoryContextFactory factory = new tyrex.naming.MemoryContextFactory();
         java.util.Hashtable tyrexEnv = new java.util.Hashtable();

         tyrexRoot = factory.getInitialContext( tyrexEnv );
      } catch(javax.naming.NamingException ex) {ex.printStackTrace();}*/                 
                
        TyrexDeploymentInfo deployment = new TyrexDeploymentInfo(root,beanInfo.ejbDeploymentId, home, remote, ejbClass, ejbPk ,componentType);
        
        /*[3.1] Add Entity bean specific values */
        if ( isEntity ) {
            /*[3.1.1] Set reenterant property */
            deployment.setIsReentrant( ebi.reentrant.equalsIgnoreCase("true") );
            
            /*[3.1.2] Set persistenceType property */
            if(ebi.persistenceType.equals("Container")){
                deployment.setCmrFields(ebi.cmpFieldNames);
                try{
                /*[3.1.2.1] Set primKeyField property */
                if(ebi.primKeyField != null)
                    deployment.setPrimKeyField(ebi.primKeyField);
                }catch(java.lang.NoSuchFieldException ne){
                    throw new org.openejb.SystemException("Can not set prim-key-field on deployment "+deployment.getDeploymentID(), ne);
                }
                
                /*[3.1.2.2] map the finder methods to the query statements. */
                if(ebi.queries != null){
                    Vector finderMethods = new Vector();
                    for(int i = 0; i < ebi.queries.length; i++){
                        resolveMethods(finderMethods, deployment.getHomeInterface(), ebi.queries[i].method);
                        for(int j =0; j<finderMethods.size(); j++){
                            deployment.addQuery((Method)finderMethods.elementAt(j), ebi.queries[i].queryStatement);       
                        }
                        finderMethods.clear();
                    }
                }
                
            }
        }
        
        
        /*[3.2] Set transactionType property */
        if( beanInfo.transactionType != null || beanInfo.transactionType.equals("Bean") )
            deployment.setBeanManagedTransaction(true);
        else
            deployment.setBeanManagedTransaction(false);
       
        /*[4.2] Add BeanRefs to namespace */
        bindJndiBeanRefs(beanInfo, root);
        
        /*[4.3] Add EnvEntries to namespace */
        bindJndiEnvEntries(beanInfo, root);
        
        /*[4.4] Add ResourceRefs to namespace */
        bindJndiResourceRefs(beanInfo, root);
        
        return deployment;
        
    }
    
    protected static void bindJndiResourceRefs(EnterpriseBeanInfo bean,IvmContext root ) 
    throws org.openejb.OpenEJBException {
        if(bean.jndiEnc == null || bean.jndiEnc.ejbReferences == null)
            return;
        ResourceReferenceInfo reference = null;
        
        for (int i=0; i< bean.jndiEnc.resourceRefs.length; i++){
            reference = bean.jndiEnc.resourceRefs[i];
            
            TransactionDomain td = TransactionDomain.getDomain("default");
            if(td == null){
                //FIXME: log no domain path
                throw new RuntimeException("The Tyrex \"default\" was not set. The "+bean.ejbDeploymentId+" bean deployment utilizes resources and needs to access the domain.xml");
            }
            
            
            Resources resources = td.getResources();
            try{
            Resource resource = resources.getResource(reference.resourceID);
            if(resource==null) {
              throw new org.openejb.OpenEJBException("The reference with resource id "+reference.resourceID+" defined in the OpenEJB deployment file is not present in the Tyrex configuration file.");
            }
            ENCReference ref = null;
            if(EntityBeanInfo.class.isAssignableFrom(bean.getClass()))
                ref = new org.openejb.core.entity.EncReference(resource);
            else if(StatefulBeanInfo.class.isAssignableFrom(bean.getClass()))
                ref = new org.openejb.core.stateful.EncReference(resource);
            else if(StatelessBeanInfo.class.isAssignableFrom(bean.getClass()))
                ref = new org.openejb.core.stateless.EncReference(resource);
            TyrexReference tyrexRef = new TyrexReference(ref);    
                     root.bind(prefixForBinding(reference.referenceName), tyrexRef);
            
            }catch(javax.naming.NamingException ne){
                // FIXME: Log this exception
                ne.printStackTrace();
                // this is not a critical error since it only impacts one deployment
                continue;
                
            }catch(tyrex.resource.ResourceException re){
                // FIXME: Log this exception
                re.printStackTrace();
                // this is not a critical error since it only impacts one deployment
                continue;
            }
            
            
        }   
    }
    
    
}
