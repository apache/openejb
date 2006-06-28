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
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
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
