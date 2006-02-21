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

import java.util.StringTokenizer;
import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.naming.InitialContext;

import junit.framework.Assert;
import junit.framework.AssertionFailedError;
import org.openejb.test.TestFailureException;
import org.openejb.test.stateful.BasicStatefulHome;
import org.openejb.test.stateful.BasicStatefulObject;
import org.openejb.test.stateless.BasicStatelessHome;
import org.openejb.test.stateless.BasicStatelessObject;

public abstract class EncCmp2Bean implements EntityBean {

    public static int key = 20;

    public EntityContext ejbContext;

    public abstract Integer getId();

    public abstract void setId(Integer primaryKey);

    public abstract String getFirstName();

    public abstract void setFirstName(String firstName);

    public abstract String getLastName();

    public abstract void setLastName(String lastName);

    //=============================
    // Home interface methods
    //

    /**
     * Maps to EncCmpHome.create
     */
    public Integer ejbCreate(String name) throws CreateException {
        StringTokenizer st = new StringTokenizer(name, " ");
        setFirstName(st.nextToken());
        setLastName(st.nextToken());
        setId(new Integer(key++));
        return null;
    }

    public void ejbPostCreate(String name) {
    }


    //
    // Home interface methods
    //=============================


    //=============================
    // Remote interface methods
    //


    public void lookupEntityBean() throws TestFailureException {
        try {
            try {
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                BasicCmpHome home = (BasicCmpHome) javax.rmi.PortableRemoteObject.narrow(ctx.lookup("java:comp/env/entity/cmp/beanReferences/cmp_entity"), BasicCmpHome.class);
                Assert.assertNotNull("The EJBHome looked up is null", home);

                BasicCmpObject object = home.create("Enc Bean");
                Assert.assertNotNull("The EJBObject is null", object);
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatefulBean() throws TestFailureException {
        try {
            try {
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                BasicStatefulHome home = (BasicStatefulHome) javax.rmi.PortableRemoteObject.narrow(ctx.lookup("java:comp/env/entity/cmp/beanReferences/stateful"), BasicStatefulHome.class);
                Assert.assertNotNull("The EJBHome looked up is null", home);

                BasicStatefulObject object = home.create("Enc Bean");
                Assert.assertNotNull("The EJBObject is null", object);
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStatelessBean() throws TestFailureException {
        try {
            try {
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                BasicStatelessHome home = (BasicStatelessHome) javax.rmi.PortableRemoteObject.narrow(ctx.lookup("java:comp/env/entity/cmp/beanReferences/stateless"), BasicStatelessHome.class);
                Assert.assertNotNull("The EJBHome looked up is null", home);

                BasicStatelessObject object = home.create();
                Assert.assertNotNull("The EJBObject is null", object);
            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupStringEntry() throws TestFailureException {
        try {
            try {
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                String expected = new String("1");
                String actual = (String) ctx.lookup("java:comp/env/entity/cmp/references/String");

                Assert.assertNotNull("The String looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupDoubleEntry() throws TestFailureException {
        try {
            try {
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                Double expected = new Double(1.0D);
                Double actual = (Double) ctx.lookup("java:comp/env/entity/cmp/references/Double");

                Assert.assertNotNull("The Double looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupLongEntry() throws TestFailureException {
        try {
            try {
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                Long expected = new Long(1L);
                Long actual = (Long) ctx.lookup("java:comp/env/entity/cmp/references/Long");

                Assert.assertNotNull("The Long looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupFloatEntry() throws TestFailureException {
        try {
            try {
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                Float expected = new Float(1.0F);
                Float actual = (Float) ctx.lookup("java:comp/env/entity/cmp/references/Float");

                Assert.assertNotNull("The Float looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupIntegerEntry() throws TestFailureException {
        try {
            try {
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                Integer expected = new Integer(1);
                Integer actual = (Integer) ctx.lookup("java:comp/env/entity/cmp/references/Integer");

                Assert.assertNotNull("The Integer looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupShortEntry() throws TestFailureException {
        try {
            try {
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                Short expected = new Short((short) 1);
                Short actual = (Short) ctx.lookup("java:comp/env/entity/cmp/references/Short");

                Assert.assertNotNull("The Short looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupBooleanEntry() throws TestFailureException {
        try {
            try {
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                Boolean expected = new Boolean(true);
                Boolean actual = (Boolean) ctx.lookup("java:comp/env/entity/cmp/references/Boolean");

                Assert.assertNotNull("The Boolean looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupByteEntry() throws TestFailureException {
        try {
            try {
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);

                Byte expected = new Byte((byte) 1);
                Byte actual = (Byte) ctx.lookup("java:comp/env/entity/cmp/references/Byte");

                Assert.assertNotNull("The Byte looked up is null", actual);
                Assert.assertEquals(expected, actual);

            } catch (Exception e) {
                Assert.fail("Received Exception " + e.getClass() + " : " + e.getMessage());
            }
        } catch (AssertionFailedError afe) {
            throw new TestFailureException(afe);
        }
    }

    public void lookupResource() throws TestFailureException {
        try {
            try {
                InitialContext ctx = new InitialContext();
                Assert.assertNotNull("The InitialContext is null", ctx);
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


    //================================
    // EntityBean interface methods
    //

    /**
     * A container invokes this method to instruct the
     * instance to synchronize its state by loading it state from the
     * underlying database.
     */
    public void ejbLoad() {
    }

    /**
     * Set the associated entity context. The container invokes this method
     * on an instance after the instance has been created.
     */
    public void setEntityContext(EntityContext ctx) {
        ejbContext = ctx;
    }

    /**
     * Unset the associated entity context. The container calls this method
     * before removing the instance.
     */
    public void unsetEntityContext() {
    }

    /**
     * A container invokes this method to instruct the
     * instance to synchronize its state by storing it to the underlying
     * database.
     */
    public void ejbStore() {
    }

    /**
     * A container invokes this method before it removes the EJB object
     * that is currently associated with the instance. This method
     * is invoked when a client invokes a remove operation on the
     * enterprise Bean's home interface or the EJB object's remote interface.
     * This method transitions the instance from the ready state to the pool
     * of available instances.
     */
    public void ejbRemove() {
    }

    /**
     * A container invokes this method when the instance
     * is taken out of the pool of available instances to become associated
     * with a specific EJB object. This method transitions the instance to
     * the ready state.
     */
    public void ejbActivate() {
    }

    /**
     * A container invokes this method on an instance before the instance
     * becomes disassociated with a specific EJB object. After this method
     * completes, the container will place the instance into the pool of
     * available instances.
     */
    public void ejbPassivate() {
    }
    //
    // EntityBean interface methods
    //================================
}
