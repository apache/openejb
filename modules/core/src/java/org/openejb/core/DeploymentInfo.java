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
package org.openejb.core;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;

import javax.ejb.EJBContext;
import javax.ejb.EJBHome;
import javax.ejb.SessionSynchronization;
import javax.naming.Context;

import org.openejb.Container;
import org.openejb.RpcContainer;
import org.openejb.alt.containers.castor_cmp11.CastorCMP11_EntityContainer;
import org.openejb.alt.containers.castor_cmp11.CastorCmpEntityTxPolicy;
import org.openejb.alt.containers.castor_cmp11.KeyGenerator;
import org.openejb.core.entity.EntityEjbHomeHandler;
import org.openejb.core.ivm.BaseEjbProxyHandler;
import org.openejb.core.ivm.EjbHomeProxyHandler;
import org.openejb.core.ivm.SpecialProxyInfo;
import org.openejb.core.stateful.SessionSynchronizationTxPolicy;
import org.openejb.core.stateful.StatefulBeanManagedTxPolicy;
import org.openejb.core.stateful.StatefulContainerManagedTxPolicy;
import org.openejb.core.stateful.StatefulEjbHomeHandler;
import org.openejb.core.stateless.StatelessBeanManagedTxPolicy;
import org.openejb.core.stateless.StatelessEjbHomeHandler;
import org.openejb.core.transaction.TransactionContainer;
import org.openejb.core.transaction.TransactionPolicy;
import org.openejb.core.transaction.TxManditory;
import org.openejb.core.transaction.TxNever;
import org.openejb.core.transaction.TxNotSupported;
import org.openejb.core.transaction.TxRequired;
import org.openejb.core.transaction.TxRequiresNew;
import org.openejb.core.transaction.TxSupports;
import org.openejb.util.proxy.ProxyManager;

/**
 * Contains all the information needed by the container for a particular 
 * deployment.  Some of this information is generic, but this class is 
 * largely becoming a dumping ground for information specific to individual
 * containers.  This class should be abstracted and subclassed in the individual
 * container packages.  The container should be required to provide its own DeploymentInfo
 * implementation, possibly returning it to the assembler and OpenEJB in general via a
 * new accessor method.
 * 
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @version $Revision$ $Date$
 */
public class DeploymentInfo implements org.openejb.DeploymentInfo{

    private Object    deploymentId;
    private Class     homeInterface;
    private Class     remoteInterface;
    private Class     beanClass;
    private Class     pkClass;
        
    private boolean   isBeanManagedTransaction;
    private boolean   isReentrant;
    private Container container;
    
    private EJBHome   ejbHomeRef;
    
    private Context   jndiContextRoot;
    
    
    /**
     * Stateless session beans only have one create method. The getCreateMethod is
     * used by instance manager of the core.stateless.StatelessContainer as a 
     * convenience method for obtaining the ejbCreate method.
     */
    private Method createMethod = null;
    
    /**
     * Every Entity ejbCreate method has a matching ejbPostCreate method. This 
     * maps that relationship.
     */
    private HashMap postCreateMethodMap          = new HashMap();
    /**
     * Can be one of the following DeploymentInfo values: STATEFUL, STATELESS, 
     * BMP_ENTITY, CMP_ENTITY.
     */
    private byte componentType;

    private HashMap methodPermissions            = new HashMap();
    private HashMap methodTransactionAttributes  = new HashMap();
    private HashMap methodTransactionPolicies    = new HashMap();
    private HashMap methodMap                    = new HashMap();
    /**
     */
    private HashMap securityRoleReferenceMap     = new HashMap();
    private HashSet methodsWithRemoteReturnTypes = null;
    
    /**
     * Creates an empty DeploymentInfo instance.
     */
    public DeploymentInfo( ){}

    /**
     * Constructs a DeploymentInfo object to represent the specified bean's 
     * deployment information.
     * 
     * @param did    the id of this bean deployment
     * @param homeClassName
     *               the fully qualified class name of the bean's home interface definition
     * @param remoteClassName
     *               the fully qualified class name of the bean's remote interface definition
     * @param beanClassName
     *               the fully qualified class name of the bean's class definition
     * @param pkClassName
     *               the fully qualified class name of the bean's primary key class
     * @param componentType
     *               one of the component type constants defined in org.openejb.DeploymentInfo
     * @exception ClassNotFoundException
     *                   if the home, remote, bean class, or primary key definitions could not be found and loaded
     * @exception org.openejb.SystemException
     * @see org.openejb.ContainerSystem
     * @see org.openejb.Container#getContainerID
     * @see org.openejb.ContainerManager#getContainerManagerID
     * @see org.openejb.DeploymentInfo#STATEFUL
     * @see org.openejb.DeploymentInfo#STATELESS
     * @see org.openejb.DeploymentInfo#BMP_ENTITY
     * @see org.openejb.DeploymentInfo#CMP_ENTITY
     */
    public DeploymentInfo(Object did, String homeClassName, String remoteClassName, String beanClassName, String pkClassName, byte componentType)
    throws org.openejb.SystemException{

        try{
            set(did, Class.forName(homeClassName), Class.forName(remoteClassName), Class.forName(beanClassName), null, componentType);
            pkClass = (pkClassName == null)? null : Class.forName(pkClassName);
        }catch(java.lang.ClassNotFoundException cnfe){
            throw new org.openejb.SystemException("Could not find class " + cnfe);
        }
    }

    /**
     * Constructs a DeploymentInfo object to represent the specified bean's 
     * deployment information.
     * 
     * @param did       the id of this bean deployment
     * @param homeClass the bean's home interface definition
     * @param remoteClass
     *                  the bean's remote interface definition
     * @param beanClass the bean's class definition
     * @param pkClass   the bean's primary key class
     * @param componentType
     *                  one of the component type constants defined in org.openejb.DeploymentInfo
     * @exception org.openejb.SystemException
     * @see org.openejb.ContainerSystem
     * @see org.openejb.Container#getContainerID
     * @see org.openejb.ContainerManager#getContainerManagerID
     * @see org.openejb.DeploymentInfo#STATEFUL
     * @see org.openejb.DeploymentInfo#STATELESS
     * @see org.openejb.DeploymentInfo#BMP_ENTITY
     * @see org.openejb.DeploymentInfo#CMP_ENTITY
     */
    public DeploymentInfo(Object did, Class homeClass, Class remoteClass, Class beanClass, Class pkClass, byte componentType)
    throws org.openejb.SystemException{
        set(did, homeClass, remoteClass, beanClass, null, componentType);
        this.pkClass = pkClass;
    }

    /**
     * Constructs a DeploymentInfo object to represent the specified bean's 
     * deployment information.
     * 
     * @param did    the id of this bean deployment
     * @param homeClassName
     *               the fully qualified class name of the bean's home interface definition
     * @param remoteClassName
     *               the fully qualified class name of the bean's remote interface definition
     * @param beanClassName
     *               the fully qualified class name of the bean's class definition
     * @param pkClassName
     *               the fully qualified class name of the bean's primary key class
     * @param componentType
     *               one of the component type constants defined in org.openejb.DeploymentInfo
     * @param loader A ClassLoader to use to load the classes named here
     * @exception ClassNotFoundException
     *                   if the home, remote, bean class, or primary key definitions could not be found and loaded
     * @exception org.openejb.SystemException
     * @see org.openejb.ContainerSystem
     * @see org.openejb.Container#getContainerID
     * @see org.openejb.ContainerManager#getContainerManagerID
     * @see org.openejb.DeploymentInfo#STATEFUL
     * @see org.openejb.DeploymentInfo#STATELESS
     * @see org.openejb.DeploymentInfo#BMP_ENTITY
     * @see org.openejb.DeploymentInfo#CMP_ENTITY
     */
    public DeploymentInfo(Object did, String homeClassName, String remoteClassName, String beanClassName, String pkClassName, byte componentType, ClassLoader loader)
    throws org.openejb.SystemException{

        try{
            set(did, loader.loadClass(homeClassName), loader.loadClass(remoteClassName), loader.loadClass(beanClassName), null, componentType);
            pkClass = (pkClassName == null)? null : loader.loadClass(pkClassName);
        }catch(java.lang.ClassNotFoundException cnfe){
            throw new org.openejb.SystemException(cnfe);
        }
    }

    /**
     * Constructs a DeploymentInfo object to represent the specified bean's 
     * deployment information.
     * 
     * @param did    the id of this bean deployment
     * @param home   the class of the bean's home interface definition
     * @param remote the class of the bean's remote interface definition
     * @param bean   the class of the bean's class definition
     * @param pk     the class of the bean's primary key class
     * @param componentType
     *               one of the component type constants defined in org.openejb.DeploymentInfo
     * @exception org.openejb.SystemException
     * @see org.openejb.ContainerSystem
     * @see org.openejb.Container#getContainerID
     * @see org.openejb.ContainerManager#getContainerManagerID
     * @see org.openejb.DeploymentInfo#STATEFUL
     * @see org.openejb.DeploymentInfo#STATELESS
     * @see org.openejb.DeploymentInfo#BMP_ENTITY
     * @see org.openejb.DeploymentInfo#CMP_ENTITY
     */
    public void set(Object did,Class home, Class remote, Class bean, Class pk, byte componentType)
    throws org.openejb.SystemException{

        deploymentId = did;

        homeInterface = home;
        remoteInterface = remote;
        beanClass = bean;
        pkClass = pk;
        this.componentType = componentType;
        
        createMethodMap();

    }
    

    /**
     * Container must have its Container set explicitly by the Assembler to avoid
     * a chicken and egg problem: Which is created first the container or the 
     * DeploymentInfo This assembler will invoke this method when needed. The 
     * Container must be set before the bean is ready to process requests.
     * 
     * @param cont
     */
    public void setContainer(Container cont){
        container = cont;
    }



    //====================================
    // begin DeploymentInfo Implementation
    //

    /**
     * Gets the type of this bean component.
     * Will return a 
     * <code>STATEFUL</code>, 
     * <code>STATELESS</code>, 
     * <code>BMP_ENTITY</code> or 
     * <code>CMP_ENTITY</code>.
     * 
     * @return Returns <code>STATEFUL</code>, <code>STATELESS</code>, <code>BMP_ENTITY</code> or <code>CMP_ENTITY</code>.
     * @see #STATEFUL
     * @see #STATELESS
     * @see #BMP_ENTITY
     * @see #CMP_ENTITY
     * @see #MESSAGE_DRIVEN
     */
    public byte getComponentType( ){
        return componentType;
    }

    /**
     * Gets the transaction attribute that must be applied to this method when 
     * executing.
     * 
     * The type can be anyone of 
     * <code>TX_NEVER</code>, 
     * <code>TX_NOT_SUPPORTED</code>, 
     * <code>TX_SUPPORTS</code>, 
     * <code>TX_MANDITORY</code>, 
     * <code>TX_REQUIRED</code>, 
     * <code>TX_REQUIRES_NEW</code>,
     * 
     * @param method the bean's method for which transaction attribute information is needed
     * @return the transaction constant that states the method's transaction attribute
     * @see #TX_NEVER
     * @see #TX_NOT_SUPPORTED
     * @see #TX_SUPPORTS
     * @see #TX_MANDITORY
     * @see #TX_REQUIRED
     * @see #TX_REQUIRES_NEW
     */
    public byte getTransactionAttribute(Method method){

        Byte byteWrapper = (Byte)methodTransactionAttributes.get(method);
        if(byteWrapper==null)
            return TX_NOT_SUPPORTED;// non remote or home interface method
        else
            return byteWrapper.byteValue();
    }

    public TransactionPolicy getTransactionPolicy(Method method){

        TransactionPolicy policy = (TransactionPolicy)methodTransactionPolicies.get(method);
        if(policy==null && !isBeanManagedTransaction) {
            org.apache.log4j.Logger.getLogger("OpenEJB").warn("The following method doesn't have a transaction policy assigned: "+method);
        }
        if ( policy == null && container instanceof TransactionContainer) {
            if ( isBeanManagedTransaction ) {
                if ( componentType == STATEFUL ) {
                    policy = new StatefulBeanManagedTxPolicy((TransactionContainer) container );
                } else if (componentType == STATELESS) {
                    policy = new StatelessBeanManagedTxPolicy((TransactionContainer) container );
                }
            } else if ( componentType == STATEFUL ){
                policy = new TxNotSupported((TransactionContainer) container );
                policy = new StatefulContainerManagedTxPolicy( policy );
            } else if ( componentType == CMP_ENTITY && container instanceof CastorCMP11_EntityContainer){
                policy = new TxNotSupported((TransactionContainer) container );
                policy = new CastorCmpEntityTxPolicy( policy );
            } else {
                policy = new TxNotSupported((TransactionContainer) container );
            }
            methodTransactionPolicies.put( method, policy );
        }
        return policy;
    }

    /**
     * Gets the roles that are authorised to execute this method.
     *
     * Used primarily by the container to check the caller's rights to
     * access and execute the specifed method.
     *
     * @param method the bean's method for which security information is needed
     * @return a String array of the roles that are authorised to execute this method
     * @see org.openejb.spi.SecurityService#isCallerAuthorized
     */
    public String [] getAuthorizedRoles(Method method){
        HashSet roleSet = (HashSet)methodPermissions.get(method);
        if(roleSet == null) return null;
        String [] roles = new String[roleSet.size()];
        return (String [])roleSet.toArray(roles);
    }

    public String [] getAuthorizedRoles(String action){
        return null;
    }

    /**
     * Gets the the container that this deployed bean is in.
     *
     * @return the the deployment's container.
     * @see Container#getContainerID() Container.getContainerID()
     */
    public Container getContainer( ){
        return container;
    }

    /**
     * Gets the id of this bean deployment.
     *
     * @return the id of of this bean deployment
     * @see ContainerManager#getContainerManagerID() ContainerManager.getContainerManagerID()
     * @see Container#getContainerManagerID() Container.getContainerManagerID()
     */
    public Object getDeploymentID( ){
        return deploymentId;
    }

    /**
     * Returns true if this bean deployment has chosen  bean-managed transaction 
     * demarcation. Returns false if the continer will be managing the bean's 
     * transactions.
     * 
     * @return Returns true if this bean deployment is managing its own transactions.
     */
    public boolean isBeanManagedTransaction(){
        return isBeanManagedTransaction;
    }

    /**
     * Gets the home interface for the bean deployment.
     * 
     * Used primarily by Servers integrating OpenEJB into their platform.  Aids in
     * implementing the bean's home interface.
     * 
     * @return a Class object of the bean's home interface
     * @see javax.ejb.EJBHome
     */
    public Class getHomeInterface( ){
        return homeInterface;
    }

    /**
     * Gets the remote interface for the bean deployment.
     * 
     * Used primarily by Servers integrating OpenEJB into their platform.  Aids in
     * implementing the bean's remote interface.
     * 
     * @return a Class object of the bean's remote interface
     * @see javax.ejb.EJBObject
     */
    public Class getRemoteInterface( ){
        return remoteInterface;
    }

    /**
     * Gets the bean's class definition.
     *
     * Used primarily by containers to instantiate new instances of a bean.
     *
     * @return a Class object of the bean's class definition
     * @see javax.ejb.EnterpriseBean
     */
    public Class getBeanClass( ){
        return beanClass;
    }

    /**
     * Gets the Class type of the primary key for this bean deployment.
     * Returns null if the bean is a type that does not need a primary key.
     *
     * @return the Class type of the bean's primary key or null if the bean doesn't need a primary key
     */
    public Class getPrimaryKeyClass( ){
        return pkClass;
    }

    //
    // end DeploymentInfo Implementation
    //==================================


    //===================================================
    // begin accessors & mutators for this implementation
    //

    /**
     * Gets the <code>EJBHome</code> object of this bean deployment.
     *
     * @return the javax.ejb.EJBHome object
     * @see javax.ejb.EJBHome
     */
    public EJBHome getEJBHome() {
        if(ejbHomeRef == null)
            createEJBHomeRef();
        return ejbHomeRef;
    }

    /**
     * Sets this bean deployment to container-managed or bean-managed transaction
     * demarcation.
     * 
     * @param value  true if this bean is managing transaction, false if the container should manage them
     */
    public void setBeanManagedTransaction(boolean value){
        isBeanManagedTransaction = value;
    }

    /**
     * Sets the JNDI namespace for the bean's environment.  This will be the ony
     * namespace that the bean will be able to access using the java: URL in the JNDI.
     *
     * @param the Context of the bean's JNDI environment
     * @see javax.naming.Context
     */
    public void setJndiEnc(javax.naming.Context cntx){
        jndiContextRoot = cntx;
    }

    /**
     * Gets the JNDI namespace for the bean's environment.  This will be the ony
     * namespace that the bean will be able to access using the java: URL in the JNDI.
     *
     * Used primarily by the IntraVM naming server to get a bean's environemtn naming context
     * when the bean performs a lookup using the "java:" URL.
     * @see javax.naming.Context
     */
    public javax.naming.Context getJndiEnc( ){
        return jndiContextRoot;
    }

    /**
     * Returns true if the bean deployment allows reenterace.
     * 
     * @return 
     */
    public boolean isReentrant(){
        return isReentrant;
    }

    public void setIsReentrant(boolean reentrant){
        isReentrant = reentrant;
    }
    
    /**
     * Business methods that return EJBHome or EJBObject references to local beans
     * (beans in the same container system) must have the return value converted 
     * to a ProxyInfo object, so that the server can provide the client with a 
     * proper remote reference.  Local remote references are implemented using the
     * org.openejb.core.ivm.BaseEjbProxyHandler types, which should not be 
     * returned to the client.  Non-local remote references are assumed to be 
     * serializable and valid return types for the clients.
     * 
     * If the reference is a local remote reference, then a subtype of ProxyInfo 
     * is returned. The subtype is a org.openejb.core.ivm.SpecialProxyInfo. This 
     * class type is useful when the calling server is the IntraVM server.  
     * Instead of creating a new remote ref from the proxy the IntraVM takes a 
     * short cut and reuses the original local remote reference -- they are thread
     * safe with no synchronization.
     * 
     * This method is based on a method map (methodWithRemoteReturnTypes) which is
     * constructed in the createMethodMap( ) method of this class.
     * 
     * See Section 2.2.1.2.5 Remote References of the OpenEJB 1.1 specification.
     * 
     * @param businessMethod
     * @param returnValue
     * @return 
     */
    public Object convertIfLocalReference(Method businessMethod, Object returnValue){
        if(returnValue == null || methodsWithRemoteReturnTypes == null)
           return returnValue;

        // if its a local reference; local refs are always Proxy types whose handler is the BaseEjbProxyHandler
        // the assumption is that the Set is faster then the instanceof operation,
        // so its used to reduce the overhead of this method.
        try{
            if(   methodsWithRemoteReturnTypes.contains(businessMethod)
               && ProxyManager.isProxyClass( returnValue.getClass() )
               && ProxyManager.getInvocationHandler( returnValue ) instanceof BaseEjbProxyHandler){
    
               return new SpecialProxyInfo( returnValue );
            }
        } catch (ClassCastException e) {
            // Do nothing.  This happens when the return value is in fact a 
            // proxy, a java.lang.reflect.Proxy for example, but the handler
            // is not an OpenEJB implementation. This just means, it's not
            // a local reference.
        }
        return returnValue;

    }

    /**
     * Returns a method in the bean class that matches the method passed in.
     * 
     * Used to map the methods from the home or remote interface to the methods in
     * the bean class.  This is needed because the java.lang.reflect.Method object
     * passed into the container by the server is declared by a remote or home 
     * interface.  
     * 
     * Since the bean class is not required to implement either of these 
     * interfaces, the matching method on the bean class may be a different
     * java.lang.reflect.Method object.  Invoking a the remote or home interface 
     * Method object on the bean class will not work if the bean doesn't implement
     * the Method's interface. This method provide the container with the proper 
     * Method object.  
     * 
     * The mapping is performed at assembly time by the createMethodMap( ) 
     * declared in this class.
     * 
     * @param interfaceMethod
     *               the Method of the home or remote interface
     * @return the Method in the bean class that maps to the method specified
     * @see org.openejb.core.DeploymentInfo.createMethodMap()
     * @see java.lang.reflect.Method
     */
    public Method getMatchingBeanMethod(Method interfaceMethod){
        Method mthd = (Method)methodMap.get(interfaceMethod);
        return (mthd == null)? interfaceMethod : mthd;
    }

    /**
     * Appends a Method and a list of authorized roles to the internal list of mehtod permissions.
     *
     * @param m the Method the roles map to
     * @param roleNames the roles that are authorized to execute the specified method
     * @see java.lang.reflect.Method
     */
    public void appendMethodPermissions(Method m, String [] roleNames){
        HashSet hs = (HashSet)methodPermissions.get(m);
        if(hs == null){
            hs = new HashSet( );// FIXME: Set appropriate load and intial capacity
            methodPermissions.put(m,hs);
        }
        for(int i = 0; i < roleNames.length; i++){
            hs.add(roleNames[i]);
        }
    }
    /**
     * Beans can test to see if the current principal is a member of a specified 
     * role using the method EJBContext.isPrincipalInRole(String roleName).  The 
     * role name used is a logical role name specified by the bean provider. When 
     * the bean is packaged this role name must be declared in the 
     * security-role-ref and linked to a security-role element in the deployment 
     * descriptor.
     * 
     * In OpenEJB the security-role element is also assigned a physical role, 
     * ased on the target environment.  Its this physical role that must be mapped
     * to the security-role-reference used by the bean provider.
     * <p>
     * The org.openejb.core.CoreContext uses this method to obtain the physical 
     * role mapped to the logical role used by the bean.
     * <p>
     * 
     * @param securityRoleReference
     *               the role used by the bean code; the security-role-ref
     * @return the physical role in the target environment that is mapped to the security-
     * @see #addSecurityRoleReference
     */
    public String [] getPhysicalRole(String securityRoleReference){
        return (String[])securityRoleReferenceMap.get(securityRoleReference);
    }
    /**
     * Adds a security-role-ref to physical role mapping. The security-role-ref is
     * used to test the caller's membership in a particular role at runtime.
     * <p>
     * 
     * @param securityRoleReference
     *               the role used by the bean code; the security-role-ref
     * @param physicalRoles
     * @see #getPhysicalrole
     */
    public void addSecurityRoleReference(String securityRoleReference, String [] physicalRoles){
        securityRoleReferenceMap.put(securityRoleReference, physicalRoles);
    }
    /**
     * Gets the <code>EJBContext</code> this container manager will expose to the
     * <code>Container</code>s it manages.
     * 
     * @return an EJBContext that containers can give to beans.
     * @see "javax.ejb.EJBContext"
     */
    public EJBContext getEJBContext( ){
        if(componentType == this.STATEFUL)
            return new org.openejb.core.stateful.StatefulContext();
        else if(componentType == this.STATELESS)
            return new org.openejb.core.stateless.StatelessContext();
        else if(componentType == this.BMP_ENTITY || componentType == this.CMP_ENTITY )
            return new org.openejb.core.entity.EntityContext();
        else
            return null;

    }

    /**
     * Sets the transaction attribute of the method in the bean's class.
     * There are six valid transaction attributes: "Supports", "RequiresNew", 
     * "Manditory", "NotSupported", "Required", and "Never".
     * 
     * @param method the Method the specified transaction attribute applies to.
     * @param transAttribute
     *               one of "Supports", "RequiresNew", "Manditory", "NotSupported", "Required", or "Never".
     * @see java.lang.reflect.Method
     */
    public void setMethodTransactionAttribute(Method method, String transAttribute){
        Byte byteValue = null;
        TransactionPolicy policy = null;

        if(transAttribute.equals("Supports")){
            if (container instanceof TransactionContainer) {
                policy = new TxSupports((TransactionContainer)container);
            }
            byteValue = new Byte(TX_SUPPORTS);

        } else if(transAttribute.equals("RequiresNew")) {
            if (container instanceof TransactionContainer) {
                policy = new TxRequiresNew((TransactionContainer)container);
            }
            byteValue = new Byte(TX_REQUIRES_NEW);

        } else if(transAttribute.equals("Mandatory")) {
            if (container instanceof TransactionContainer) {
                policy = new TxManditory((TransactionContainer)container);
            }
            byteValue = new Byte(TX_MANDITORY);

        } else if(transAttribute.equals("NotSupported")) {
            if (container instanceof TransactionContainer) {
                policy = new TxNotSupported((TransactionContainer)container);
            }
            byteValue = new Byte(TX_NOT_SUPPORTED);

        } else if(transAttribute.equals("Required")) {
            if (container instanceof TransactionContainer) {
                policy = new TxRequired((TransactionContainer)container);
            }
            byteValue = new Byte(TX_REQUIRED);

        } else if(transAttribute.equals("Never")) {
            if (container instanceof TransactionContainer) {
                policy = new TxNever((TransactionContainer)container);
            }
            byteValue = new Byte(TX_NEVER);
        } else{
            throw new IllegalArgumentException("Invalid transaction attribute \""+transAttribute+"\" declared for method "+method.getName()+". Please check your configuration.");
        }
        
        /* EJB 1.1 page 55
         Only a stateful Session bean with container-managed transaction demarcation may implement the
         SessionSynchronization interface. A stateless Session bean must not implement the SessionSynchronization
         interface.
         */
        // If this is a Stateful SessionBean that implements the 
        // SessionSynchronization interface and the method transaction
        // attribute is not Never or notSupported, then wrap the TransactionPolicy
        // with a SessionSynchronizationTxPolicy
        if ( componentType == STATEFUL && !isBeanManagedTransaction  && container instanceof TransactionContainer){

            // we have a stateful session bean with container-managed transactions
            if ( SessionSynchronization.class.isAssignableFrom(beanClass) ) {
                if ( !transAttribute.equals("Never") && !transAttribute.equals("NotSupported") ){
                    // wrap the policy unless attribute is "never" or "NotSupported"
                    policy = new SessionSynchronizationTxPolicy( policy );
                }
            } else {
                // CMT stateful session bean but does not implement SessionSynchronization
                policy = new StatefulContainerManagedTxPolicy( policy );
            }

        } else if ( componentType == CMP_ENTITY && container instanceof CastorCMP11_EntityContainer){
            policy = new CastorCmpEntityTxPolicy( policy );
        }
        methodTransactionAttributes.put( method, byteValue );
        methodTransactionPolicies.put( method, policy );
    }

    //
    // end accessors & mutators for this implementation
    //=================================================

    //====================================
    // Begin DeploymentInfo Initialization
    //

    /**
     * Attempts to lookup and instantiate the EJBHome proxy for the bean deployment.
     * 
     * @exception RuntimeException
     *                   if there is a problem locating or instantiating the EJBHome proxy
     */
    private void createEJBHomeRef(){

        EjbHomeProxyHandler handler = null;
        
        switch ( getComponentType() ) {
        case STATEFUL:
            handler = new StatefulEjbHomeHandler((RpcContainer)container, null, getDeploymentID());
            break;
        
        case STATELESS:
            handler = new StatelessEjbHomeHandler((RpcContainer)container, null, getDeploymentID());
            break;
        case CMP_ENTITY:
        case BMP_ENTITY:
            handler = new EntityEjbHomeHandler((RpcContainer)container, null, getDeploymentID());
            break;
        }

        Object proxy = null;
        try{
            Class[] interfaces = new Class[]{ this.getHomeInterface(), org.openejb.core.ivm.IntraVmProxy.class };
            proxy = ProxyManager.newProxyInstance( interfaces , handler );
        }catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException("Can't create EJBHome stub" + e.getMessage());
        }

        ejbHomeRef = (javax.ejb.EJBHome)proxy;

    }

    /**
     * Creates a HashMap that maps the methods from the home and remote interfaces 
     * to the methods in the bean class.
     * 
     * @exception RuntimeException
     *                   if the mothod in the remote or home interface cannot be mapped to a method in the bean class
     * @exception org.openejb.SystemException
     */
    private void createMethodMap() throws org.openejb.SystemException{
        Method [] remoteMethods = remoteInterface.getMethods();
        for(int i = 0; i < remoteMethods.length; i++){
            if(remoteMethods[i].getDeclaringClass() != javax.ejb.EJBObject.class){
                try{
                    Method beanMethod =
                    beanClass.getMethod(remoteMethods[i].getName(),remoteMethods[i].getParameterTypes());
                    methodMap.put(remoteMethods[i],beanMethod);
                }catch(NoSuchMethodException nsme){
                    throw new RuntimeException("Invalid method ["+ remoteMethods[i] +"]. Not declared by "+beanClass.getName()+" class");
                }
                /*
                   check for return type of java.rmi.Remote. If one of the business method returns a
                   java.rmi.Remote type, it may be a org.openejb.ivm.BaseEjbProxyHandler type at runtime,
                   in which case it will need to be converted by the container into a ProxyInfo object.
                   The container will use the convertIfLocalReference() to check.
                   This block of code sets up that method.
                 */
                if(java.rmi.Remote.class.isAssignableFrom(remoteMethods[i].getReturnType())) {
                    if(methodsWithRemoteReturnTypes==null)methodsWithRemoteReturnTypes = new HashSet();
                    methodsWithRemoteReturnTypes.add(remoteMethods[i]);
                }

            }
        }
        Method [] homeMethods = homeInterface.getMethods();
        for(int i = 0; i < homeMethods.length; i++){
            Method method = homeMethods[i];
            if(method.getDeclaringClass() != javax.ejb.EJBHome.class ){
            try{

                Method beanMethod = null;
                if(method.getName().equals("create")){
                    beanMethod = beanClass.getMethod("ejbCreate",method.getParameterTypes());
                    createMethod = beanMethod;
                    /*
                    Entity beans have a ejbCreate and ejbPostCreate methods with matching 
                    parameters. This code maps that relationship.
                    */
                    if(this.componentType==this.BMP_ENTITY || this.componentType==this.CMP_ENTITY){
                        Method postCreateMethod = beanClass.getMethod("ejbPostCreate",method.getParameterTypes());
                        postCreateMethodMap.put(createMethod,postCreateMethod);
                    }
                    /*
                     * Stateless session beans only have one create method. The getCreateMethod is
                     * used by instance manager of the core.stateless.StatelessContainer as a convenience
                     * method for obtaining the ejbCreate method.
                    */
                }else if(method.getName().startsWith("find")){
                    if(this.componentType == this.BMP_ENTITY ){
                        // CMP 1.1 beans do not define a find method in the bean class
                        String beanMethodName = "ejbF"+method.getName().substring(1);
                        beanMethod = beanClass.getMethod(beanMethodName,method.getParameterTypes());
                    }
                }else {
                    String beanMethodName = "ejbHome"+method.getName().substring(0,1).toUpperCase()+method.getName().substring(1);
                    beanMethod = beanClass.getMethod(beanMethodName,method.getParameterTypes());
                }
                if(beanMethod!=null){
                    methodMap.put(homeMethods[i],beanMethod);
                }
            }catch(NoSuchMethodException nsme){
                throw new RuntimeException("Invalid method ["+method+"] Not declared by "+beanClass.getName()+" class");
            }
            }
        }
        try{
        if(componentType == STATEFUL || componentType == STATELESS){
            Method beanMethod = javax.ejb.SessionBean.class.getDeclaredMethod("ejbRemove", new Class []{});
            Method clientMethod = EJBHome.class.getDeclaredMethod("remove", new Class [] {javax.ejb.Handle.class});
            methodMap.put(clientMethod, beanMethod);
            clientMethod = EJBHome.class.getDeclaredMethod("remove", new Class [] {java.lang.Object.class});
            methodMap.put(clientMethod, beanMethod);
            clientMethod = javax.ejb.EJBObject.class.getDeclaredMethod("remove", null);
            methodMap.put(clientMethod, beanMethod);
        }else if(componentType == BMP_ENTITY || componentType == CMP_ENTITY){
            Method beanMethod = javax.ejb.EntityBean.class.getDeclaredMethod("ejbRemove", new Class []{});
            Method clientMethod = EJBHome.class.getDeclaredMethod("remove", new Class [] {javax.ejb.Handle.class});
            methodMap.put(clientMethod, beanMethod);
            clientMethod = EJBHome.class.getDeclaredMethod("remove", new Class [] {java.lang.Object.class});
            methodMap.put(clientMethod, beanMethod);
            clientMethod = javax.ejb.EJBObject.class.getDeclaredMethod("remove", null);
            methodMap.put(clientMethod, beanMethod);
        }
        }catch(java.lang.NoSuchMethodException nsme){
            throw new org.openejb.SystemException(nsme);
        }
        int i = 1 +2;// beak point

    }

    protected String  extractHomeBeanMethodName(String methodName){
        if(methodName.equals("create"))
            return "ejbCreate";
        else if(methodName.startsWith("find"))
            return "ejbF"+methodName.substring(1);
        else
            return "ejbH"+methodName.substring(1);
    }
    /**
     * Used for stateless session beans only
     * 
     * @return 
     */
    public Method getCreateMethod( ){
        return createMethod;
    }
    /**
     * Used for entity beans only.
     * 
     * @param createMethod
     * @return 
     */
    public Method getMatchingPostCreateMethod(Method createMethod){
        return (Method)this.postCreateMethodMap.get(createMethod);
    }
    
    //
    // End DeploymentInfo Initialization
    //==================================


    //==================================
    // Castor CMP Container Specific
    //
    private KeyGenerator keyGenerator;
    private Field     primKeyField;
    private String[]  cmrFields;
    
    
    /**
     * Each query method, ejbFind or ejbSelect(EJB 2.0), can be mapped to
     * query string which describes the behavior of the method. For example,
     * with the Castor JDO CMP container for EJB 1.1, every ejbFind method
     * for each Deployment maps to a specific OQL statement which Castor JDO
     * uses to access the object cache.
     */
    private HashMap queryMethodMap               = new HashMap();

    
    /**
     * Gets the Field of the CMP entity bean class which corresponds to the simple
     * primary key.  Entity beans that have complex primary keys (keys with several
     * fields) will not have a primkey-field.
     * <P>
     * Useful for Container-Managed Persistence (CMP) Entity beans with Simple
     * Primary Keys.
     * </P>
     * 
     * @return the EntityBean field that corresponds to the simple primary key.
     *         return null if the bean is not a CMP Entity bean with a simple Primary key
     */
    public Field getPrimaryKeyField( ){
        return primKeyField;
    }
    
    public void setPrimKeyField(String fieldName)
    throws java.lang.NoSuchFieldException{
        if(componentType == this.CMP_ENTITY){
            
            primKeyField = beanClass.getField(fieldName);            
        }
    }
    
    /**
     * Returns the names of the bean's container-managed fields. Used for
     * container-managed persistence only.
     * 
     * @return 
     */
    public String [] getCmrFields( ){
        return cmrFields;
    }
    
    public void setCmrFields(String [] cmrFields){
        this.cmrFields = cmrFields;   
    }
    
    public KeyGenerator getKeyGenerator(){
        return keyGenerator;
    }
    
    public void setKeyGenerator(KeyGenerator keyGenerator){
        this.keyGenerator = keyGenerator;
    }

    /**
     * This method maps a query method (ejbFind) to a query string.
     * <P>
     * Each query method, ejbFind or ejbSelect(EJB 2.0), can be mapped to
     * query string which describes the behavior of the method. For example,
     * with the Castor JDO CMP container for EJB 1.1, every ejbFind method
     * for each Deployment maps to a specific OQL statement which Castor JDO
     * uses to access the object cache.
     * </P>
     * 
     * @param queryMethod
     * @param queryString
     */
    public void addQuery(Method queryMethod, String queryString){
        queryMethodMap.put(queryMethod, queryString);
    }
    /**
     * This method retrieves the query string associated with the query method.
     * <P>
     * Each query method, ejbFind or ejbSelect(EJB 2.0), can be mapped to
     * query string which describes the behavior of the method. For example,
     * with the Castor JDO CMP container for EJB 1.1, every ejbFind method
     * for each Deployment maps to a specific OQL statement which Castor JDO
     * uses to access the object cache.
     * </P>
     * 
     * @param queryMethod
     * @return 
     */
    public String getQuery(Method queryMethod){
        return (String)queryMethodMap.get(queryMethod);
    }
    //
    // Castor CMP Container Specific
    //==================================
}
