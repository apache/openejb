/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb;

import java.lang.reflect.Method;

import javax.ejb.EJBHome;
import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;
import javax.ejb.EJBObject;

import org.apache.geronimo.core.service.Interceptor;
import org.apache.geronimo.naming.java.ReadOnlyContext;

import org.openejb.transaction.EJBUserTransaction;

/**
 * Interface exposed by server side EJB Containers to allow the interceptor
 * stack to interact with them.
 *
 * @version $Revision$ $Date$
 */
public interface EJBContainer extends Interceptor {
    
    /**
     * Unique id used for locating the container
     * No assumptions are made about the type of
     * this object other than it can hash uniquely
     * @return the id of the container
     */
    Object getContainerID();
    
    /**
     * Return the name of the EJB
     * @return the name of the EJB
     */
    String getEJBName();

    /**
     * Return the implementation class
     * @return the EJB's implementation class
     */
    Class getBeanClass();

    /**
     * Return a proxy for the EJB's home interface. This can be passed back
     * to any client that wishes to access the EJB (e.g. in response to a
     * call to EJBContext.getEJBHome() )
     * @return the proxy for this EJB's home interface
     */
    EJBHome getEJBHome();

    /**
     * Return a proxy for the EJB's remote interface. This can be passed back
     * to any client that wishes to access the EJB (e.g. in response to a
     * call to SessionContext.getEJBObject() )
     * @return the proxy for this EJB's home interface
     */
    EJBObject getEJBObject(Object primaryKey);

    /**
     * Return a proxy for the EJB's local home interface. This can be
     * passed back to any client that wishes to access the EJB
     * (e.g. in response to a call to EJBContext.getEJBLocalHome() )
     * @return the proxy for this EJB's local home interface
     */
    EJBLocalHome getEJBLocalHome();

    /**
     * Return a proxy for the EJB's local interface. This can be passed back
     * to any client that wishes to access the EJB (e.g. in response to a
     * call to SessionContext.getEJBLocalObject() )
     * @return the proxy for this EJB's local interface
     */
    EJBLocalObject getEJBLocalObject(Object primaryKey);

    /**
     * Return the type of transaction demarcation this EJB is using.
     * @see org.openejb.TransactionDemarcation
     * @return this EJB's demaraction mechanism
     */
    TransactionDemarcation getDemarcation();
    
    /**
     * Return's the type of EJB component this contianer is represents
     * The constants are from EJBComponentType
     * @return appropriate constant from EJBComponentType
     */
    int getComponentType();
    
    /**
     * Return the UserTransaction implementation for this EJB.
     * @return this EJB's UserTransaction; null if using CMT
     */
    EJBUserTransaction getUserTransaction();

    /**
     * Return the JNDI Context for java:comp/env
     * @return this EJB's JNDI ComponentContext
     */
    ReadOnlyContext getComponentContext();
    
    Object invoke(Method callMethod, Object[] args, Object primKey) throws Throwable;
}
