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
 * $Id: PseudoTransactionService.java 444908 2004-09-09 16:05:24Z jboynes $
 */
package org.apache.openejb.server.ejbd;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.UserTransaction;
import javax.transaction.xa.XAResource;

import org.apache.openejb.spi.TransactionService;

public class PseudoTransactionService implements TransactionService {
    TransactionManager txm = new MyTransactionManager();
    Hashtable map = new Hashtable();
    
    public void init(java.util.Properties props ) {
        props = props;
    }
    
    public TransactionManager getTransactionManager( ){
        return txm;
    }
    public class MyTransactionManager implements TransactionManager{
        public UserTransaction getUserTransaction(Object txID){
            return new UserTransaction( ){
                public void begin() {MyTransactionManager.this.begin();}
                public void commit()  throws RollbackException {MyTransactionManager.this.commit();}
                public int getStatus()throws javax.transaction.SystemException{
                    return MyTransactionManager.this.getStatus();
                }
                public void rollback() {MyTransactionManager.this.rollback();}
                public void setRollbackOnly() {MyTransactionManager.this.setRollbackOnly();}
                public void setTransactionTimeout(int seconds) {MyTransactionManager.this.setTransactionTimeout(seconds);}
            };
        }
        public void begin( ){
            Transaction tx = new MyTransaction( );
            map.put(Thread.currentThread(), tx);
        }
        public void commit() throws RollbackException {
            MyTransaction tx = (MyTransaction)map.remove(Thread.currentThread());
            if(tx!=null)
                tx.commit();
            else
                throw new IllegalStateException();
        }
        public int getStatus()throws javax.transaction.SystemException{
            Transaction tx = (Transaction)map.get(Thread.currentThread());
            if(tx==null)return Status.STATUS_NO_TRANSACTION;
            return tx.getStatus();
        }
        public Transaction getTransaction( ){
            return (Transaction)map.get(Thread.currentThread());
        }
        public void resume(Transaction tx)
        throws javax.transaction.SystemException, javax.transaction.InvalidTransactionException{
            Transaction ctx = (Transaction)map.get(Thread.currentThread());
            int status = tx.getStatus();
            // allow to resume a tx that has been marked for rollback.
            if(ctx!= null || tx == null || (status != Status.STATUS_ACTIVE && status != Status.STATUS_MARKED_ROLLBACK))
                throw new javax.transaction.InvalidTransactionException();
            map.put(Thread.currentThread(),tx);
        }
        public Transaction suspend( ){
            return (Transaction)map.remove(Thread.currentThread());
        }
        public void rollback(){
            MyTransaction tx = (MyTransaction)map.remove(Thread.currentThread());
            if(tx==null) throw new IllegalStateException();
            tx.rollback();
        }
        public void setRollbackOnly( ){
            MyTransaction tx = (MyTransaction)map.get(Thread.currentThread());
            if(tx==null) throw new IllegalStateException();
            tx.setRollbackOnly();
        }
        public void setTransactionTimeout(int x){}
    
    }
    public class MyTransaction implements Transaction {
        Vector registeredSynchronizations = new Vector();
        Vector xaResources = new Vector();
        int status = Status.STATUS_ACTIVE;
        public void commit() throws RollbackException {
	    if ( status == Status.STATUS_MARKED_ROLLBACK ) {
		rollback();
		throw new RollbackException();
	    }
            doBeforeCompletion();
            doXAResources(Status.STATUS_COMMITTED);
            status = Status.STATUS_COMMITTED;
            doAfterCompletion(Status.STATUS_COMMITTED);
            registeredSynchronizations = new Vector();
            map.remove(Thread.currentThread());
        }
        public boolean delistResource(XAResource xaRes, int flag) {
            xaResources.remove(xaRes); return true;
        }
        public boolean enlistResource(XAResource xaRes){
            xaResources.addElement(xaRes);return true;
        }
        public int getStatus() {return status;}
        
        public void registerSynchronization(Synchronization sync){
            registeredSynchronizations.addElement(sync);
        }
        public void rollback() {
            doXAResources(Status.STATUS_ROLLEDBACK);
            doAfterCompletion(Status.STATUS_ROLLEDBACK);
            status = Status.STATUS_ROLLEDBACK;
            registeredSynchronizations = new Vector();
            map.remove(Thread.currentThread());
        }
        public void setRollbackOnly() {status = Status.STATUS_MARKED_ROLLBACK;}
        
        // the transaciton must be NOT be rolleback for this method to execute.
        private void doBeforeCompletion(){
            Enumeration e = registeredSynchronizations.elements();
            while(e.hasMoreElements()){
                try{
                Synchronization sync = (Synchronization)e.nextElement();
                sync.beforeCompletion();
                }catch(RuntimeException re){
                    re.printStackTrace();
                }
            }
        }
        private void doAfterCompletion(int status){
            Enumeration e = registeredSynchronizations.elements();
            while(e.hasMoreElements()){
                try{
                Synchronization sync = (Synchronization)e.nextElement();
                sync.afterCompletion(status);
                }catch(RuntimeException re){
                    re.printStackTrace();
                }
            }
        }
        
        private void doXAResources(int status){
            Object [] resources = xaResources.toArray();
            for(int i = 0; i < resources.length; i++){
                XAResource xaRes = (XAResource)resources[i];
                if(status == Status.STATUS_COMMITTED){
                    try{
                    xaRes.commit(null,true);
                    }catch(javax.transaction.xa.XAException xae){
                        // do nothing.
                    }
                    try{
                    xaRes.end(null,XAResource.TMSUCCESS);
                    }catch(javax.transaction.xa.XAException xae){
                        // do nothing.
                    }
                }else{
                    try{
                    xaRes.rollback(null);
                    }catch(javax.transaction.xa.XAException xae){
                        // do nothing.
                    }
                    try{
                    xaRes.end(null,XAResource.TMFAIL);
                    }catch(javax.transaction.xa.XAException xae){
                        // do nothing.
                    }
                }
            }
            xaResources = new Vector();
            
        }
        
    }
    
}
