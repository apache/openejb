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

package org.apache.openejb.deployment;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;

import junit.framework.TestCase;
import org.apache.geronimo.xbeans.j2ee.ContainerTransactionType;
import org.apache.geronimo.xbeans.j2ee.MethodParamsType;
import org.apache.geronimo.xbeans.j2ee.MethodType;
import org.apache.openejb.dispatch.InterfaceMethodSignature;
import org.apache.openejb.transaction.TransactionPolicyManager;
import org.apache.openejb.transaction.TransactionPolicyType;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class TransactionPolicyHelperTest extends TestCase {

    private ContainerTransactionType[] containerTransaction;
    private TransactionPolicyHelper transactionPolicyHelper;
    private SortedMap transactionPolicies;

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
        transactionPolicies = transactionPolicyHelper.getTransactionPolicies("Ejb1");
    }

    public void testDefault() throws Exception {
        TransactionPolicyType policy = TransactionPolicyManager.getTransactionPolicy(transactionPolicies, "Home", new InterfaceMethodSignature("foo2", new String[]{}, true));
        assertEquals("Expected Mandatory default", TransactionPolicyType.Mandatory, policy);
    }

    public void testInterfaceOverride() throws Exception {
        TransactionPolicyType policy = TransactionPolicyManager.getTransactionPolicy(transactionPolicies, "Remote", new InterfaceMethodSignature("foo2", new String[]{}, false));
        assertEquals("Expected Supports", TransactionPolicyType.Supports, policy);
    }

    public void testMethodNoInterfaceOverride() throws Exception {
        TransactionPolicyType policy = TransactionPolicyManager.getTransactionPolicy(transactionPolicies, "LocalHome", new InterfaceMethodSignature("foo", new String[]{"bar"}, true));
        assertEquals("Expected Supports", TransactionPolicyType.RequiresNew, policy);
    }

    public void testMethodInterfaceOverride() throws Exception {
        TransactionPolicyType policy = TransactionPolicyManager.getTransactionPolicy(transactionPolicies, "Local", new InterfaceMethodSignature("bar", new String[]{"foo"}, false));
        assertEquals("Expected Supports", TransactionPolicyType.Never, policy);
    }

    public void testMethodInterfaceTwoParamOverride() throws Exception {
        TransactionPolicyType policy = TransactionPolicyManager.getTransactionPolicy(transactionPolicies, "Local", new InterfaceMethodSignature("foo", new String[]{"foo", "foo"}, false));
        assertEquals("Expected Supports", TransactionPolicyType.NotSupported, policy);
    }
}


