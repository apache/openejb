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

package org.openejb.nova.deployment;

import java.util.List;
import java.util.ArrayList;

import junit.framework.TestCase;
import org.apache.geronimo.deployment.model.ejb.ContainerTransaction;
import org.apache.geronimo.deployment.model.ejb.Method;
import org.openejb.nova.transaction.TxnPolicy;
import org.openejb.nova.transaction.ContainerPolicy;
import org.openejb.nova.dispatch.MethodSignature;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class TransactionPolicyHelperTest extends TestCase {

    private ContainerTransaction[] containerTransaction;
    private TransactionPolicyHelper transactionPolicyHelper;
    private TransactionPolicySource transactionPolicySource;

    protected void setUp() throws Exception {
        List containerTransactionList = new ArrayList();
        ContainerTransaction containerTxn = new ContainerTransaction();
        containerTxn.setTransAttribute("Mandatory");
        Method method1 = new Method();
        method1.setEjbName("Ejb1");
        method1.setMethodName("*");
        containerTxn.setMethod(new Method[] {method1});
        containerTransactionList.add(containerTxn);
        containerTxn = new ContainerTransaction();
        containerTxn.setTransAttribute("Supports");
        method1 = new Method();
        method1.setEjbName("Ejb1");
        method1.setMethodIntf("Remote");
        method1.setMethodName("*");
        containerTxn.setMethod(new Method[] {method1});
        containerTransactionList.add(containerTxn);
        containerTxn = new ContainerTransaction();
        containerTxn.setTransAttribute("RequiresNew");
        method1 = new Method();
        method1.setEjbName("Ejb1");
        //method1.setMethodIntf("Remote");
        method1.setMethodName("foo");
        containerTxn.setMethod(new Method[] {method1});
        containerTransactionList.add(containerTxn);
        containerTxn = new ContainerTransaction();
        containerTxn.setTransAttribute("Never");
        method1 = new Method();
        method1.setEjbName("Ejb1");
        method1.setMethodIntf("Local");
        method1.setMethodName("bar");
        method1.setMethodParam(new String[] {"foo"});
        containerTxn.setMethod(new Method[] {method1});
        containerTransactionList.add(containerTxn);
        containerTxn = new ContainerTransaction();
        containerTxn.setTransAttribute("NotSupported");
        method1 = new Method();
        method1.setEjbName("Ejb1");
        method1.setMethodIntf("Local");
        method1.setMethodName("foo");
        method1.setMethodParam(new String[] {"foo", "foo"});
        containerTxn.setMethod(new Method[] {method1});
        containerTransactionList.add(containerTxn);
        containerTransaction = (ContainerTransaction[])containerTransactionList.toArray(new ContainerTransaction[containerTransactionList.size()]);
        transactionPolicyHelper = new TransactionPolicyHelper(containerTransaction);
        transactionPolicySource = transactionPolicyHelper.getTransactionPolicySource("Ejb1");
    }

    public void testDefault() throws Exception {
        TxnPolicy policy = transactionPolicySource.getTransactionPolicy("Home", new MethodSignature("foo2", new String[] {}));
        assertEquals("Expected Mandatory default", ContainerPolicy.Mandatory, policy);
    }

    public void testInterfaceOverride() throws Exception {
        TxnPolicy policy = transactionPolicySource.getTransactionPolicy("Remote", new MethodSignature("foo2", new String[] {}));
        assertEquals("Expected Supports", ContainerPolicy.Supports, policy);
    }

    public void testMethodNoInterfaceOverride() throws Exception {
        TxnPolicy policy = transactionPolicySource.getTransactionPolicy("LocalHome", new MethodSignature("foo", new String[] {"bar"}));
        assertEquals("Expected Supports", ContainerPolicy.RequiresNew, policy);
    }

    public void testMethodInterfaceOverride() throws Exception {
        TxnPolicy policy = transactionPolicySource.getTransactionPolicy("Local", new MethodSignature("bar", new String[] {"foo"}));
        assertEquals("Expected Supports", ContainerPolicy.Never, policy);
    }

    public void testMethodInterfaceTwoParamOverride() throws Exception {
        TxnPolicy policy = transactionPolicySource.getTransactionPolicy("Local", new MethodSignature("foo", new String[] {"foo", "foo"}));
        assertEquals("Expected Supports", ContainerPolicy.NotSupported, policy);
    }

}


