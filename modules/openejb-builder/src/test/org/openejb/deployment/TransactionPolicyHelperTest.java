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

package org.openejb.deployment;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;
import org.apache.geronimo.xbeans.j2ee.ContainerTransactionType;
import org.apache.geronimo.xbeans.j2ee.MethodParamsType;
import org.apache.geronimo.xbeans.j2ee.MethodType;
import org.openejb.dispatch.InterfaceMethodSignature;
import org.openejb.transaction.TransactionPolicySource;
import org.openejb.transaction.TransactionPolicyType;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class TransactionPolicyHelperTest extends TestCase {

    private ContainerTransactionType[] containerTransaction;
    private TransactionPolicyHelper transactionPolicyHelper;
    private TransactionPolicySource transactionPolicySource;

    protected void setUp() throws Exception {
        List containerTransactionList = new ArrayList();
        ContainerTransactionType containerTxn = ContainerTransactionType.Factory.newInstance();
        containerTxn.addNewTransAttribute().setStringValue("Mandatory");
        MethodType method1 = containerTxn.addNewMethod();
        method1.addNewEjbName().setStringValue("Ejb1");
        method1.addNewMethodName().setStringValue("*");
        containerTransactionList.add(containerTxn);
        containerTxn = ContainerTransactionType.Factory.newInstance();
        containerTxn.addNewTransAttribute().setStringValue("Supports");
        method1 = containerTxn.addNewMethod();
        method1.addNewEjbName().setStringValue("Ejb1");
        method1.addNewMethodIntf().setStringValue("Remote");
        method1.addNewMethodName().setStringValue("*");
        containerTransactionList.add(containerTxn);
        containerTxn = ContainerTransactionType.Factory.newInstance();
        containerTxn.addNewTransAttribute().setStringValue("RequiresNew");
        method1 = containerTxn.addNewMethod();
        method1.addNewEjbName().setStringValue("Ejb1");
        //method1.setMethodIntf("Remote");
        method1.addNewMethodName().setStringValue("foo");
        containerTransactionList.add(containerTxn);
        containerTxn = ContainerTransactionType.Factory.newInstance();
        containerTxn.addNewTransAttribute().setStringValue("Never");
        method1 = containerTxn.addNewMethod();
        method1.addNewEjbName().setStringValue("Ejb1");
        method1.addNewMethodIntf().setStringValue("Local");
        method1.addNewMethodName().setStringValue("bar");
        method1.addNewMethodParams().addNewMethodParam().setStringValue("foo");
        containerTransactionList.add(containerTxn);
        containerTxn = ContainerTransactionType.Factory.newInstance();
        containerTxn.addNewTransAttribute().setStringValue("NotSupported");
        method1 = containerTxn.addNewMethod();
        method1.addNewEjbName().setStringValue("Ejb1");
        method1.addNewMethodIntf().setStringValue("Local");
        method1.addNewMethodName().setStringValue("foo");
        MethodParamsType methodParams = method1.addNewMethodParams();
        methodParams.addNewMethodParam().setStringValue("foo");
        methodParams.addNewMethodParam().setStringValue("foo");
        containerTransactionList.add(containerTxn);
        containerTransaction = (ContainerTransactionType[]) containerTransactionList.toArray(new ContainerTransactionType[containerTransactionList.size()]);
        transactionPolicyHelper = new TransactionPolicyHelper(containerTransaction);
        transactionPolicySource = transactionPolicyHelper.getTransactionPolicySource("Ejb1");
    }

    public void testDefault() throws Exception {
        TransactionPolicyType policy = transactionPolicySource.getTransactionPolicy("Home", new InterfaceMethodSignature("foo2", new String[]{}, true));
        assertEquals("Expected Mandatory default", TransactionPolicyType.Mandatory, policy);
    }

    public void testInterfaceOverride() throws Exception {
        TransactionPolicyType policy = transactionPolicySource.getTransactionPolicy("Remote", new InterfaceMethodSignature("foo2", new String[]{}, false));
        assertEquals("Expected Supports", TransactionPolicyType.Supports, policy);
    }

    public void testMethodNoInterfaceOverride() throws Exception {
        TransactionPolicyType policy = transactionPolicySource.getTransactionPolicy("LocalHome", new InterfaceMethodSignature("foo", new String[]{"bar"}, true));
        assertEquals("Expected Supports", TransactionPolicyType.RequiresNew, policy);
    }

    public void testMethodInterfaceOverride() throws Exception {
        TransactionPolicyType policy = transactionPolicySource.getTransactionPolicy("Local", new InterfaceMethodSignature("bar", new String[]{"foo"}, false));
        assertEquals("Expected Supports", TransactionPolicyType.Never, policy);
    }

    public void testMethodInterfaceTwoParamOverride() throws Exception {
        TransactionPolicyType policy = transactionPolicySource.getTransactionPolicy("Local", new InterfaceMethodSignature("foo", new String[]{"foo", "foo"}, false));
        assertEquals("Expected Supports", TransactionPolicyType.NotSupported, policy);
    }
}


