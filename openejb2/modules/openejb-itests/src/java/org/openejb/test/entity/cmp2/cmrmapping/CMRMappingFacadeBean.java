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
package org.openejb.test.entity.cmp2.cmrmapping;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Set;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionContext;
import javax.ejb.TransactionRolledbackLocalException;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.openejb.test.TestFailureException;

/**
 * @version $Revision$ $Date$
 */
public class CMRMappingFacadeBean implements javax.ejb.SessionBean {
    private InitialContext jndiContext;
    private SessionContext ctx;
    private CompoundPK compoundPK_20_10;
    private CompoundPK compoundPK_20_20;
    
    public void testOneToOneSetCMROnOwningSide() throws TestFailureException {
        UserTransaction userTransaction = ctx.getUserTransaction();
        try {
            userTransaction.begin();
            OneInverseSideLocal inverseLocal = createOneInverseSide(compoundPK_20_10.field1);
            OneOwningSideLocal owningLocal = createOneOwningSide(compoundPK_20_10);
            owningLocal.setOneInverseSide(inverseLocal);
            userTransaction.commit();
            
            validateOneToOneRelationship(userTransaction);
            
            removeOneToOne(userTransaction);
        } catch (Throwable e) {
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        }
    }

    public void testOneToOneSetCMROnOwningSideResetPK() throws TestFailureException {
        UserTransaction userTransaction = ctx.getUserTransaction();
        try {
            userTransaction.begin();
            OneInverseSideLocal inverseLocal = createOneInverseSide(compoundPK_20_10.field1);
            OneOwningSideLocal owningLocal = createOneOwningSide(compoundPK_20_20);
            owningLocal.setOneInverseSide(inverseLocal);
            Assert.fail();
        } catch (TransactionRolledbackLocalException e) {
            if (false == e.getCause() instanceof IllegalStateException) {
                throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
            }
            try {
                userTransaction.rollback();
            } catch (Exception e1) {
                throw new TestFailureException(new AssertionFailedError("Should not happen"));
            }
        } catch (Throwable e) {
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        }
    }

    public void testOneToOneSetCMROnInverseSide() throws TestFailureException {
        UserTransaction userTransaction = ctx.getUserTransaction();
        try {
            userTransaction.begin();
            OneInverseSideLocal inverseLocal = createOneInverseSide(compoundPK_20_10.field1);
            OneOwningSideLocal owningLocal = createOneOwningSide(compoundPK_20_10);
            inverseLocal.setOneOwningSide(owningLocal);
            userTransaction.commit();
            
            validateOneToOneRelationship(userTransaction);
            
            removeOneToOne(userTransaction);
        } catch (Throwable e) {
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        }
    }

    public void testOneToOneSetCMROnInverseSideResetPK() throws TestFailureException {
        UserTransaction userTransaction = ctx.getUserTransaction();
        try {
            userTransaction.begin();
            OneInverseSideLocal inverseLocal = createOneInverseSide(compoundPK_20_10.field1);
            OneOwningSideLocal owningLocal = createOneOwningSide(compoundPK_20_20);
            inverseLocal.setOneOwningSide(owningLocal);
            Assert.fail();
        } catch (TransactionRolledbackLocalException e) {
            if (false == e.getCause() instanceof IllegalStateException) {
                throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
            }
            try {
                userTransaction.rollback();
            } catch (Exception e1) {
                throw new TestFailureException(new AssertionFailedError("Should not happen"));
            }
        } catch (Throwable e) {
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        }
    }

    public void testOneToOneDoNotSetCMR() throws TestFailureException {
        UserTransaction userTransaction = ctx.getUserTransaction();
        try {
            userTransaction.begin();
            createOneOwningSide(compoundPK_20_10);
            userTransaction.commit();
            Assert.fail();
        } catch (IllegalStateException e) {
        } catch (Throwable e) {
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        }
    }

    public void testOneToManySetCMROnOwningSide() throws TestFailureException {
        UserTransaction userTransaction = ctx.getUserTransaction();
        try {
            userTransaction.begin();
            OneInverseSideLocal inverseLocal = createOneInverseSide(compoundPK_20_10.field1);
            ManyOwningSideLocal owningLocal = createManyOwningSide(compoundPK_20_10);
            owningLocal.setOneInverseSide(inverseLocal);
            userTransaction.commit();
            
            validateOneToManyRelationship(userTransaction);
            
            removeOneToMany(userTransaction);
        } catch (Throwable e) {
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        }
    }

    public void testEjbSelectWithCMR() throws TestFailureException {
        UserTransaction userTransaction = ctx.getUserTransaction();
        try {
            userTransaction.begin();
            OneInverseSideLocal inverseLocal = createOneInverseSide(compoundPK_20_10.field1);
            ManyOwningSideLocal owningLocal = createManyOwningSide(compoundPK_20_10);
            owningLocal.setOneInverseSide(inverseLocal);
            userTransaction.commit();

            owningLocal.testEJBSelect();

            removeOneToMany(userTransaction);
        } catch (Throwable e) {
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        }
    }

    public void testOneToManySetCMROnOwningSideResetPK() throws TestFailureException {
        UserTransaction userTransaction = ctx.getUserTransaction();
        try {
            userTransaction.begin();
            OneInverseSideLocal inverseLocal = createOneInverseSide(compoundPK_20_10.field1);
            ManyOwningSideLocal owningLocal = createManyOwningSide(compoundPK_20_20);
            owningLocal.setOneInverseSide(inverseLocal);
            Assert.fail();
        } catch (TransactionRolledbackLocalException e) {
            if (false == e.getCause() instanceof IllegalStateException) {
                throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
            }
            try {
                userTransaction.rollback();
            } catch (Exception e1) {
                throw new TestFailureException(new AssertionFailedError("Should not happen"));
            }
        } catch (Throwable e) {
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        }
    }

    public void testOneToManySetCMROnInverseSide() throws TestFailureException {
        UserTransaction userTransaction = ctx.getUserTransaction();
        try {
            userTransaction.begin();
            OneInverseSideLocal inverseLocal = createOneInverseSide(compoundPK_20_10.field1);
            ManyOwningSideLocal owningLocal = createManyOwningSide(compoundPK_20_10);
            inverseLocal.setManyOwningSide(Collections.singleton(owningLocal));
            userTransaction.commit();
            
            validateOneToManyRelationship(userTransaction);

            removeOneToMany(userTransaction);
        } catch (Throwable e) {
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        }
    }

    public void testOneToManySetCMROnInverseSideResetPK() throws TestFailureException {
        UserTransaction userTransaction = ctx.getUserTransaction();
        try {
            userTransaction.begin();
            OneInverseSideLocal inverseLocal = createOneInverseSide(compoundPK_20_10.field1);
            ManyOwningSideLocal owningLocal = createManyOwningSide(compoundPK_20_20);
            inverseLocal.setManyOwningSide(Collections.singleton(owningLocal));
            Assert.fail();
        } catch (TransactionRolledbackLocalException e) {
            if (false == e.getCause() instanceof IllegalStateException) {
                throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
            }
            try {
                userTransaction.rollback();
            } catch (Exception e1) {
                throw new TestFailureException(new AssertionFailedError("Should not happen"));
            }
        } catch (Throwable e) {
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        }
    }

    public void testOneToManyDoNotSetCMR() throws TestFailureException {
        UserTransaction userTransaction = ctx.getUserTransaction();
        try {
            userTransaction.begin();
            createManyOwningSide(compoundPK_20_10);
            userTransaction.commit();
            Assert.fail();
        } catch (IllegalStateException e) {
        } catch (Throwable e) {
            throw new TestFailureException(new AssertionFailedError("Received Exception " + e.getClass() + " : " + e.getMessage()));
        }
    }
    
    public void ejbCreate() throws javax.ejb.CreateException{
        try {
            jndiContext = new InitialContext();
            compoundPK_20_10 = new CompoundPK(new Integer(20), new Integer(10));
            compoundPK_20_20 = new CompoundPK(new Integer(20), new Integer(20));
        } catch (Exception e){
            throw new CreateException("Can not get the initial context: "+e.getMessage());
        }
    }

    public void setSessionContext(SessionContext ctx) throws EJBException,RemoteException {
        this.ctx = ctx;
    }

    public void ejbRemove() throws EJBException,RemoteException {
    }

    public void ejbActivate() throws EJBException,RemoteException {
    }

    public void ejbPassivate() throws EJBException,RemoteException {
    }

    private OneInverseSideLocal createOneInverseSide(Integer id) throws Exception {
        OneInverseSideLocalHome home = (OneInverseSideLocalHome) PortableRemoteObject.narrow(jndiContext.lookup("java:comp/env/ejb/OneInverseSideLocalHome"), OneInverseSideLocalHome.class);
        return home.create(id);
    }

    private OneInverseSideLocal findOneInverseSide(Integer id) throws Exception {
        OneInverseSideLocalHome home = (OneInverseSideLocalHome) PortableRemoteObject.narrow(jndiContext.lookup("java:comp/env/ejb/OneInverseSideLocalHome"), OneInverseSideLocalHome.class);
        return home.findByPrimaryKey(id);
    }

    private void removeOneInverseSide(Integer id) throws Exception {
        findOneInverseSide(id).remove();
    }

    private void validateOneToOneRelationship(UserTransaction userTransaction) throws Exception {
        OneInverseSideLocal inverseLocal = findOneInverseSide(compoundPK_20_10.field1);
        userTransaction.begin();
        Assert.assertEquals(compoundPK_20_10, inverseLocal.getOneOwningSide().getPrimaryKey());
        userTransaction.commit();
    }

    private void removeOneToOne(UserTransaction userTransaction) throws NotSupportedException, SystemException, Exception, HeuristicMixedException, HeuristicRollbackException, RollbackException {
        userTransaction.begin();
        removeOneOwningSide(compoundPK_20_10);
        removeOneInverseSide(compoundPK_20_10.field1);
        userTransaction.commit();
    }

    private OneOwningSideLocal createOneOwningSide(CompoundPK compoundPK) throws Exception {
        OneOwningSideLocalHome home = (OneOwningSideLocalHome) PortableRemoteObject.narrow(jndiContext.lookup("java:comp/env/ejb/OneOwningSideLocalHome"), OneOwningSideLocalHome.class);
        return home.create(compoundPK.id, compoundPK.field1);
    }

    private OneOwningSideLocal findOneOwningSide(CompoundPK compoundPK) throws Exception {
        OneOwningSideLocalHome home = (OneOwningSideLocalHome) PortableRemoteObject.narrow(jndiContext.lookup("java:comp/env/ejb/OneOwningSideLocalHome"), OneOwningSideLocalHome.class);
        return home.findByPrimaryKey(compoundPK);
    }

    private void removeOneOwningSide(CompoundPK compoundPK) throws Exception {
        findOneOwningSide(compoundPK).remove();
    }
    
    private ManyOwningSideLocal createManyOwningSide(CompoundPK compoundPK) throws Exception {
        ManyOwningSideLocalHome home = (ManyOwningSideLocalHome) PortableRemoteObject.narrow(jndiContext.lookup("java:comp/env/ejb/ManyOwningSideLocalHome"), ManyOwningSideLocalHome.class);
        return home.create(compoundPK.id, compoundPK.field1);
    }

    private ManyOwningSideLocal findManyOwningSide(CompoundPK compoundPK) throws Exception {
        ManyOwningSideLocalHome home = (ManyOwningSideLocalHome) PortableRemoteObject.narrow(jndiContext.lookup("java:comp/env/ejb/ManyOwningSideLocalHome"), ManyOwningSideLocalHome.class);
        return home.findByPrimaryKey(compoundPK);
    }

    private void removeManyOwningSide(CompoundPK compoundPK) throws Exception {
        findManyOwningSide(compoundPK).remove();
    }

    private void validateOneToManyRelationship(UserTransaction userTransaction) throws NotSupportedException, SystemException, Exception, HeuristicMixedException, HeuristicRollbackException, RollbackException {
        OneInverseSideLocal inverseLocal = findOneInverseSide(compoundPK_20_10.field1);
        userTransaction.begin();
        Set set = inverseLocal.getManyOwningSide();
        Assert.assertEquals(1, set.size());
        ManyOwningSideLocal owningLocal = (ManyOwningSideLocal) set.iterator().next();
        Assert.assertEquals(compoundPK_20_10, owningLocal.getPrimaryKey());
        userTransaction.commit();
    }
    
    private void removeOneToMany(UserTransaction userTransaction) throws NotSupportedException, SystemException, Exception, HeuristicMixedException, HeuristicRollbackException, RollbackException {
        userTransaction.begin();
        removeManyOwningSide(compoundPK_20_10);
        removeOneInverseSide(compoundPK_20_10.field1);
        userTransaction.commit();
    }
}
