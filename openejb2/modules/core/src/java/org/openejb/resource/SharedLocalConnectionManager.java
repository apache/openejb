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
 package org.openejb.resource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.transaction.Transaction;

import org.openejb.OpenEJB;
/**
 * This ConnectionManager provides a simple algorithm for managing <i>shared-local</i> transactions for multiple
 * connectors.  It ensures that components participating in the same logical thread of execution share physical connections
 * to resources like JDBC connections or JMS connections.  For example, Component A calls Component B within one transaction. 
 * If both components access the same resource (e.g. some database), they will transparently share the same physical connection.
 * This makes it possible to group the resource work of both components with the same local transaction (all or nothing tx semantics),
 * as well as conserving resources.
 * <p>
 * This ConnectionManager relies on the the container's transaction manager to delineate the scope of the transaction using the 
 * TransactionManager's synchronization facilities.  The shared-local resources are not enlisted in 2PC transactions; they execute 
 * only in local transactions.
 * <p>
 * Multiple connectors can be managed by one instance of the this class. The instance will ensure that components share 
 * access to the correct connectors within the context of a same thread of execution.
 *
 * @version $ $
 */
public class SharedLocalConnectionManager implements javax.resource.spi.ConnectionManager, 
javax.resource.spi.ConnectionEventListener,
java.io.Serializable {
    
    private Set connSet;
    private SpecialHashThreadLocal threadLocal = new SpecialHashThreadLocal();
    private HashMap factoryMap = new HashMap();
    
    public void init(java.util.Properties props){
        // just for test purposes
        //props = props;
    }
    
    public SharedLocalConnectionManager() throws javax.resource.spi.ApplicationServerInternalException{
        connSet = java.util.Collections.synchronizedSet(new HashSet());
    }
    public java.lang.Object allocateConnection(ManagedConnectionFactory factory,
                                               ConnectionRequestInfo cxRequestInfo)
    throws javax.resource.ResourceException {
        
        //Object securityIdentity = OpenEJB.getSecurityService().getSecurityIdentity();
        //Subject subject = (Subject)OpenEJB.getSecurityService().translateTo(securityIdentity, Subject.class);
        
        ManagedConnection conn = (ManagedConnection)threadLocal.get(factory);
        if(conn == null){
            conn = factory.matchManagedConnections(connSet, null, cxRequestInfo); 
            if (conn != null) 
                connSet.remove(conn);
            else { //if(conn == null)
                conn = factory.createManagedConnection(null, cxRequestInfo);
                conn.addConnectionEventListener(this);
            }
            conn.getLocalTransaction().begin();
            
            try{
                /*
                * The transaction manager has a  wrapper that ensures that any Synchronization 
                * objects are handled after the EntityBean.ejbStore and SessionSynchronization methods of beans.  
                * In the StatefulContainer and EntityContainer enterprise beans are wrapped 
                * Synchronization wrappers, which must be handled
                * before the LocalTransaction objects in this connection manager.
                */
                Transaction tx = OpenEJB.getTransactionManager().getTransaction();
                if(tx!=null)
                tx.registerSynchronization(new Synchronizer(conn.getLocalTransaction()));
            }catch(javax.transaction.SystemException se){
                throw new javax.resource.spi.ApplicationServerInternalException("Can not obtain a Transaction object from TransactionManager. "+se.getMessage());
            }catch(javax.transaction.RollbackException re){
                throw new javax.resource.spi.ApplicationServerInternalException("Can not register org.openejb.resource.LocalTransacton with transaciton manager. Transaction has already been rolled back"+re.getMessage());
            }

            threadLocal.put(factory,conn);
        }
        // FIXME: Where do I get the javax.security.auth.Subject for the first parameter
        Object handle = conn.getConnection(null, cxRequestInfo);
        return handle;
    }
    
    public void connectionClosed(ConnectionEvent event){
        try{
        if(OpenEJB.getTransactionManager().getTransaction()==null){
            ManagedConnection conn = (ManagedConnection)event.getSource();
            conn.getLocalTransaction().commit();
            this.cleanup(conn);
        }
        }catch(javax.transaction.SystemException se){
            // this is a connection event notification so the exception can not be propagated.
            // log the exception but no action required.
        }catch(javax.resource.ResourceException re){
            // this exception is thrown bby the event.getSource() no processing required.
        }
    }
    
    public void connectionErrorOccurred(ConnectionEvent event){
        ManagedConnection conn = (ManagedConnection)event.getSource();
        // fetching the factory before doing clean up is important: The equals() value of the 
        // ManagedConnection may change after it has been cleaned up, which impacts hash lookups.
        ManagedConnectionFactory mcf = (ManagedConnectionFactory)threadLocal.getKey(conn);
        try{
        conn.destroy();
            if ( threadLocal.get(mcf)==conn ) threadLocal.put(mcf,null);
        }catch(javax.resource.ResourceException re){
            // do nothing. Allow conneciton to be garbage collected
        }
    }
    
    public void localTransactionCommitted(ConnectionEvent event){
        cleanup((ManagedConnection)event.getSource());
    } 
    
    public void localTransactionRolledback(ConnectionEvent event){
        cleanup((ManagedConnection)event.getSource());
    }
    
    private void cleanup(ManagedConnection conn){
        if(conn!=null){
            // fetching the factory before doing clean up is important: The equals() value of the 
            // ManagedConnection may change after it has been cleaned up, which impacts hash lookups.
            ManagedConnectionFactory mcf = (ManagedConnectionFactory)threadLocal.getKey(conn);
            try{
                conn.cleanup();
                connSet.add(conn);
                
            }catch(javax.resource.ResourceException re){
                try{
                // FIXME: log connection loss
                conn.destroy();   
                }catch(javax.resource.ResourceException re2){
                    // do nothing
                }
            }
            threadLocal.put(mcf,null);
        }
    }
    public void localTransactionStarted(ConnectionEvent event){
        // do nothing. This object started the transaction so its already aware of this event
    }    
    
    
    class Synchronizer implements javax.transaction.Synchronization{
        LocalTransaction localTx;
        
        public Synchronizer(LocalTransaction lt){
            localTx = lt;
        }
        public void beforeCompletion(){
        }

        public void afterCompletion(int status){
            if ( status == javax.transaction.Status.STATUS_COMMITTED ){
                try{
                localTx.commit();
                } catch ( javax.resource.ResourceException re ) {
                    throw new RuntimeException("JDBC driver failed to commit transaction. "+ re.getMessage());
                }
            } else {
                try{
                localTx.rollback();
                } catch ( javax.resource.ResourceException re ) {
                    throw new RuntimeException("JDBC driver failed to rollback transaction. "+ re.getMessage());
                }
            }
        }
    }
    
    /*
    * This class allows the ConnectionManager to determine the key used for 
    * any object stored in this type of HashThreadLocal.  Its needed when handling
    * ConnectionListner events because the key (ManagedConnectionFactory) used to 
    * store values (ManagedConnecitons) is not available.
    */
    class SpecialHashThreadLocal extends org.openejb.util.HashThreadLocal{
        HashMap keyMap = new HashMap();
        public synchronized void put(Object key, Object value){
            if(!keyMap.containsKey(key))keyMap.put(value,key);
            super.put(key,value);
        }
        public synchronized Object getKey(Object value){
            return keyMap.get(value);
        }
    }
    
}
