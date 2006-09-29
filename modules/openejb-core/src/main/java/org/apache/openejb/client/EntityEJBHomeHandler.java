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
package org.apache.openejb.client;

import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;

import org.apache.geronimo.security.ContextManager;

/**
 * 
 * @since 11/25/2001
 */
public class EntityEJBHomeHandler extends EJBHomeHandler {
    
    public EntityEJBHomeHandler(){
    }
    
    public EntityEJBHomeHandler(EJBMetaDataImpl ejb, ServerMetaData[] servers){
        super(ejb, servers);
    }
   
    /**
     * <P>
     * EJB 1.1 --
     * 9.1.8 Finder method return type
     * 
     * 9.1.8.1 Single-object finder
     * 
     * Some finder methods (such as ejbFindByPrimaryKey) are designed to return 
     * at most one entity object. For these single-object finders, the result type
     * of the find<METHOD>(...)method defined in the entity bean’s home interface 
     * is the entity bean’s remote interface. The result type of the corresponding
     * ejbFind<METHOD>(...) method defined in the entity’s implementation class is
     * the entity bean’s primary key type.
     * 
     * 9.1.8.2 Multi-object finders
     * 
     * Some finder methods are designed to return multiple entity objects. For
     * these multi-object finders, the result type of the find<METHOD>(...)method
     * defined in the entity bean’s home interface is a col-lection of objects
     * implementing the entity bean’s remote interface. The result type of the
     * corresponding ejbFind<METHOD>(...) implementation method defined in the
     * entity bean’s implementation class is a collection of objects of the entity
     * bean’s primary key type.
     * 
     * The Bean Provider can choose two types to define a collection type for a finder:
     * • the JDK™ 1.1 java.util.Enumeration interface
     * • the Java™ 2 java.util.Collection interface
     * 
     * A Bean Provider that wants to ensure that the entity bean is compatible
     * with containers and clients based on JDK TM 1.1 software must use the
     * java.util.Enumeration interface for the finder’s result type.
     * </P>
     * <P>
     * Locates and returns a new EJBObject or a collection
     * of EJBObjects.  The EJBObject(s) is a new proxy with
     * a new handler. This implementation should not be
     * sent outside the virtual machine.
     * </P>
     * <P>
     * This method propogates to the container
     * system.
     * </P>
     * <P>
     * The find method is required to be defined
     * by the bean's home interface of Entity beans.
     * </P>
     * 
     * @param method
     * @param args
     * @param proxy
     * @return Returns an new EJBObject proxy and handler
     * @exception Throwable
     */
    protected Object findX(Method method, Object[] args, Object proxy) throws Throwable {
        EJBRequest req = new EJBRequest( EJB_HOME_FIND ); 
        
        req.setMethodParameters( args );
        req.setMethodInstance( method );
        req.setClientIdentity( ContextManager.getThreadPrincipal() );
        req.setContainerCode( ejb.deploymentCode );
        req.setContainerID( ejb.deploymentID );
        req.setPrimaryKey( primaryKey );
        
        EJBResponse res = request( req );
        
        Object primKey = null;
        EJBObjectHandler handler = null;
        Object[] primaryKeys = null;

        switch (res.getResponseCode()) {
        case EJB_ERROR:
            throw (Throwable)res.getResult();
        case EJB_SYS_EXCEPTION:
            throw (Throwable)res.getResult();
        case EJB_APP_EXCEPTION:
            throw (Throwable)res.getResult();
        
        case EJB_OK_FOUND:
            primKey = res.getResult();
            if (null == primKey) {
                return null;
            }
            handler = EJBObjectHandler.createEJBObjectHandler(ejb,servers,primKey);
            handler.setEJBHomeProxy((EJBHomeProxy)proxy);
            registerHandler(ejb.deploymentID+":"+primKey, handler);
            return handler.createEJBObjectProxy();
        
        case EJB_OK_FOUND_COLLECTION:
            // The result is an array of primary keys
            // We are going to convert those primary keys into
            // EJBObject instance and reuse the same array to 
            // create the collection.
            primaryKeys = (Object[])res.getResult();

            for (int i=0; i < primaryKeys.length; i++){
                primKey = primaryKeys[i];
                if (null == primKey) {
                    continue;
                }
                handler = EJBObjectHandler.createEJBObjectHandler(ejb,servers,primKey);
                handler.setEJBHomeProxy((EJBHomeProxy)proxy);
                registerHandler(ejb.deploymentID+":"+primKey, handler);
                primaryKeys[i] = handler.createEJBObjectProxy();
            }
            return java.util.Arrays.asList( primaryKeys );
        case EJB_OK_FOUND_ENUMERATION:
            // TODO: it's almost duplication of what's in EJB_OK_FOUND_COLLECTION
            primaryKeys = (Object[])res.getResult();

            for (int i=0; i < primaryKeys.length; i++){
                primKey = primaryKeys[i];
                if (null == primKey) {
                    continue;
                }
                handler = EJBObjectHandler.createEJBObjectHandler(ejb,servers,primKey);
                handler.setEJBHomeProxy((EJBHomeProxy)proxy);
                registerHandler(ejb.deploymentID+":"+primKey, handler);
                primaryKeys[i] = handler.createEJBObjectProxy();
            }
            return new org.apache.openejb.util.ArrayEnumeration( java.util.Arrays.asList( primaryKeys ) );
        default:
            throw new RemoteException("Received invalid response code from server: "+res.getResponseCode());
        }
    }
    /**
     * <P>
     * Attempts to remove an EJBObject from the
     * container system.  The EJBObject to be removed
     * is represented by the primaryKey passed
     * into the remove method of the EJBHome.
     * </P>
     * <P>
     * This method propogates to the container system.
     * </P>
     * <P>
     * remove(Object primary) is a method of javax.ejb.EJBHome
     * </P>
     * <P>
     * Checks if the caller is authorized to invoke the
     * javax.ejb.EJBHome.remove on the EJBHome of the
     * deployment.
     * </P>
     *
     * @param method
     * @param args
     * @return Returns null
     * @exception Throwable
     * @see javax.ejb.EJBHome
     * @see javax.ejb.EJBHome#remove
     */
    protected Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable{
        
        Object primKey = args[0];

        // TODO:1: Check that this is exactly spec compliant
        if (primKey == null) throw new RemoteException("The primary key is null");

        EJBRequest req = new EJBRequest(EJB_HOME_REMOVE_BY_PKEY);
        req.setClientIdentity(ContextManager.getThreadPrincipal());
        req.setContainerCode(ejb.deploymentCode);
        req.setContainerID(ejb.deploymentID);
        req.setMethodInstance(method);
        req.setMethodParameters(args);
        req.setPrimaryKey(primKey);

        EJBResponse res = request(req);

        if (res.getResponseCode() == ResponseCodes.EJB_ERROR ||
                res.getResponseCode() == ResponseCodes.EJB_SYS_EXCEPTION ||
                res.getResponseCode() == ResponseCodes.EJB_APP_EXCEPTION) {
            throw (Throwable) res.getResult();
        }

        /*
         * This operation takes care of invalidating all the EjbObjectProxyHanders 
         * associated with the same RegistryId. See this.createProxy().
         */
        invalidateAllHandlers(ejb.deploymentID+":"+primKey);
        return null;
    }
    /**
     * <P>
     * Attempts to remove an EJBObject from the
     * container system.  The EJBObject to be removed
     * is represented by the javax.ejb.Handle object passed
     * into the remove method in the EJBHome.
     * </P>
     * <P>
     * This method propogates to the container system.
     * </P>
     * <P>
     * remove(Handle handle) is a method of javax.ejb.EJBHome
     * </P>
     * <P>
     * Checks if the caller is authorized to invoke the
     * javax.ejb.EJBHome.remove on the EJBHome of the
     * deployment.
     * </P>
     * 
     * @param method
     * @param args
     * @return Returns null
     * @exception Throwable
     * @see javax.ejb.EJBHome
     * @see javax.ejb.EJBHome#remove
     */
    protected Object removeWithHandle(Method method, Object[] args, Object proxy) throws Throwable {
        // Extract the primary key from the handle
        EJBObjectHandle handle = (EJBObjectHandle) args[0];

        // TODO:1: Check that this is exactly spec compliant
        if (handle == null) throw new RemoteException("The handle is null");

        EJBObjectHandler handler = handle.handler;
        Object primKey = handler.primaryKey;

        if (handler.isInvalidReference) {
            throw new NoSuchObjectException("Handle has been invalidated due to removal or system exception");
        }

        // TODO:1: Check that this is exactly spec compliant
        if (!handler.ejb.deploymentID.equals(this.ejb.deploymentID)) {
            throw new RemoteException("The handle is not from the same deployment");
        }

        EJBRequest req = new EJBRequest(EJB_HOME_REMOVE_BY_HANDLE);
        req.setClientIdentity(ContextManager.getThreadPrincipal());
        req.setContainerCode(handler.ejb.deploymentCode);
        req.setContainerID(handler.ejb.deploymentID);
        req.setMethodInstance(method);
        req.setMethodParameters(args);
        req.setPrimaryKey(primKey);

        EJBResponse res = request(req);

        if (res.getResponseCode() == ResponseCodes.EJB_ERROR ||
                res.getResponseCode() == ResponseCodes.EJB_SYS_EXCEPTION ||
                res.getResponseCode() == ResponseCodes.EJB_APP_EXCEPTION) {
            throw (Throwable) res.getResult();
        }

        invalidateAllHandlers(handler.getRegistryId());
        handler.invalidateReference();
        return null;
    }

}
