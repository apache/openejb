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
package org.openejb.test.entity.cmp;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.DuplicateKeyException;
import javax.ejb.EJBException;
import javax.ejb.ObjectNotFoundException;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;

import org.openejb.test.TestFailureException;

/**
 * 
 */
public class SessionFacadeBean implements javax.ejb.SessionBean {
    private SessionContext ejbContext;
    private InitialContext jndiContext;
    
    //=============================
    // Home interface methods
    //    
    
    //    
    // Home interface methods
    //=============================
    

    //=============================
    // Remote interface methods
    //
    /**
     * execute a create-remove-create of the same CMP within a single TX.
     */
    public void invokeCreateRemoveCreateSameCMP() throws TestFailureException {
        try {
            try {
                BasicCmpHome home = (BasicCmpHome) javax.rmi.PortableRemoteObject.narrow(jndiContext.lookup("java:comp/env/ejb/BasicCmpBeanExplicitPK"), BasicCmpHome.class);

                Integer pk = new Integer(20);
                BasicCmpObject object = home.create(pk, "Same Bean");
                object.remove();
                home.create(pk, "Same Bean");
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    /**
     * execute a create-create of the same CMP within a single TX.
     */
    public void invokeCreateCreateSameCMP() throws TestFailureException {
        try {
            try {
                BasicCmpHome home = (BasicCmpHome) javax.rmi.PortableRemoteObject.narrow(jndiContext.lookup("java:comp/env/ejb/BasicCmpBeanExplicitPK"), BasicCmpHome.class);

                Integer pk = new Integer(20);
                home.create(pk, "Same Bean");
                home.create(pk, "Same Bean");
                Assert.fail("Should have thrown DuplicateKeyException");
            } catch (DuplicateKeyException e) {
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    /**
     * execute a create-find with force cache flush before find.
     */
    public void invokeCreateFindNoForceCacheFlush() throws TestFailureException {
        try {
            try {
                BasicCmpHome home = (BasicCmpHome) javax.rmi.PortableRemoteObject.narrow(jndiContext.lookup("java:comp/env/ejb/BasicCmpBeanExplicitPK"), BasicCmpHome.class);

                Integer pk = new Integer(20);
                home.create(pk, "Same Bean");
                home.findByPrimaryKey(pk);
            } catch (ObjectNotFoundException e) {
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    /**
     * execute a create-find without force cache flush before find.
     */
    public void invokeCreateFindForceCacheFlush() throws TestFailureException {
        try {
            try {
                BasicCmpHome home = (BasicCmpHome) javax.rmi.PortableRemoteObject.narrow(jndiContext.lookup("java:comp/env/ejb/BasicCmpBeanExplicitPK"), BasicCmpHome.class);

                Integer pk = new Integer(20);
                home.create(pk, "Same Bean");
                Collection objects = home.findByLastName("Bean");
                Assert.assertEquals(1, objects.size());
                BasicCmpObject object = (BasicCmpObject) objects.iterator().next();
                Assert.assertEquals(pk, object.getPrimaryKey());
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }
    //
    // Remote interface methods
    //=============================


    //=================================
    // SessionBean interface methods
    //    
    public void ejbCreate() throws javax.ejb.CreateException{
        try {
            jndiContext = new InitialContext(); 
        } catch (Exception e){
            throw new CreateException("Can not get the initial context: "+e.getMessage());
        }
    }

    public void setSessionContext(SessionContext ctx) throws EJBException,RemoteException {
        ejbContext = ctx;
    }

    public void ejbRemove() throws EJBException,RemoteException {
    }

    public void ejbActivate() throws EJBException,RemoteException {
    }

    public void ejbPassivate() throws EJBException,RemoteException {
    }
    //    
    // SessionBean interface methods
    //==================================
    
}
