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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.geronimo.deployment.model.ejb.ContainerTransaction;
import org.apache.geronimo.deployment.model.ejb.Method;
import org.apache.geronimo.deployment.DeploymentException;
import org.openejb.dispatch.MethodSignature;
import org.openejb.transaction.BeanPolicy;
import org.openejb.transaction.ContainerPolicy;
import org.openejb.transaction.TransactionPolicy;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class TransactionPolicyHelper {

    public final static TransactionPolicySource StatefulBeanPolicySource = new TransactionPolicySource() {
        public TransactionPolicy getTransactionPolicy(String methodIntf, MethodSignature signature) {
            return BeanPolicy.Stateful;
        }
    };

    public final static TransactionPolicySource StatelessBeanPolicySource = new TransactionPolicySource() {
        public TransactionPolicy getTransactionPolicy(String methodIntf, MethodSignature signature) {
            return BeanPolicy.Stateless;
        }
    };

    private final Map ejbNameToTransactionAttributesMap = new HashMap();

    public TransactionPolicyHelper(ContainerTransaction[] containerTransactions) {
        for (int i = 0; i < containerTransactions.length; i++) {
            ContainerTransaction containerTransaction = containerTransactions[i];
            String transactionAttribute = containerTransaction.getTransAttribute();
            Method[] methods = containerTransaction.getMethod();
            for (int j = 0; j < methods.length; j++) {
                Method method = methods[j];
                String ejbName = method.getEjbName();
                MethodTransaction methodTransaction = new MethodTransaction(method, transactionAttribute);
                putMethodTransaction(ejbName, methodTransaction);
            }
        }
    }

    public TransactionPolicySource getTransactionPolicySource(String ejbName) throws DeploymentException {
        return new TransactionPolicySourceImpl((SortedSet) ejbNameToTransactionAttributesMap.get(ejbName));
    }

    private static class TransactionPolicySourceImpl implements TransactionPolicySource {
        private final SortedSet transactionPolicies;

        public TransactionPolicySourceImpl(SortedSet transactionPolicies) throws DeploymentException {
            //To allow more lenient spec interpretations, with default of Requires, substitute an empty sorted set here.
            if (transactionPolicies == null) {
                throw new DeploymentException("You must specify transaction attributes, see ejb 2.1 spec 17.4.1");
            }
            this.transactionPolicies = transactionPolicies;
        }

        public TransactionPolicy getTransactionPolicy(String methodIntf, MethodSignature signature) {
            for (Iterator iterator = transactionPolicies.iterator(); iterator.hasNext();) {
                MethodTransaction methodTransaction = (MethodTransaction) iterator.next();
                if (methodTransaction.matches(methodIntf, signature.getMethodName(), signature.getParameterTypes())) {
                    return methodTransaction.getTransactionPolicy();
                }
            }
            //default
            return ContainerPolicy.Required;
        }
    }

    private void putMethodTransaction(String ejbName, MethodTransaction methodTransaction) {
        SortedSet methodTransactions = (SortedSet) ejbNameToTransactionAttributesMap.get(ejbName);
        if (methodTransactions == null) {
            methodTransactions = new TreeSet();
            ejbNameToTransactionAttributesMap.put(ejbName, methodTransactions);
        }
        methodTransactions.add(methodTransaction);
    }

    private static class MethodTransaction implements Comparable {

        private static final int AFTER_OTHER = 1;
        private static final int BEFORE_OTHER = -1;
        private final static Map transactionPolicyMap;

        static {
            transactionPolicyMap = new HashMap();
            transactionPolicyMap.put("NotSupported", ContainerPolicy.NotSupported);
            transactionPolicyMap.put("Required", ContainerPolicy.Required);
            transactionPolicyMap.put("Supports", ContainerPolicy.Supports);
            transactionPolicyMap.put("RequiresNew", ContainerPolicy.RequiresNew);
            transactionPolicyMap.put("Mandatory", ContainerPolicy.Mandatory);
            transactionPolicyMap.put("Never", ContainerPolicy.Never);
            transactionPolicyMap.put("Stateless", BeanPolicy.Stateless);
            transactionPolicyMap.put("Stateful", BeanPolicy.Stateful);
        }

        private final TransactionPolicy transactionPolicy;
        private final String methodIntf;
        private final String methodName;
        private final String[] parameterTypes;

        public MethodTransaction(Method method, String transactionAttribute) {
            this.transactionPolicy = (TransactionPolicy) transactionPolicyMap.get(transactionAttribute);
            this.methodIntf = method.getMethodIntf();
            this.methodName = method.getMethodName();
            this.parameterTypes = method.getMethodParam();
        }

        public TransactionPolicy getTransactionPolicy() {
            return transactionPolicy;
        }

        public String getMethodIntf() {
            return methodIntf;
        }

        public String getMethodName() {
            return methodName;
        }

        public String[] getParameterTypes() {
            return parameterTypes;
        }

        public int compareTo(Object o) {
            if (!(o instanceof MethodTransaction)) {
                return -1;
            }
            if (this == o) {
                return 0;
            }
            MethodTransaction other = (MethodTransaction) o;
            if (parameterTypes != null) {
                if (other.parameterTypes == null) {
                    //parameter types always come before no param types
                    return BEFORE_OTHER;
                }
                //both have param types
                if (methodIntf != null) {
                    if (other.methodIntf == null) {
                        //method intf comes before no method intf.
                        return BEFORE_OTHER;
                    }
                    //both have method interfaces
                    int intfOrder = methodIntf.compareTo(other.methodIntf);
                    if (intfOrder != 0) {
                        return intfOrder;
                    }
                    //same interfaces
                    return compareMethod(other);
                }
                if (other.methodIntf != null) {
                    //they have method intf, we don't, they are first
                    return AFTER_OTHER;
                }
                //neither has methodIntf: sort by method name
                return compareMethod(other);
            }
            //we don't have param types
            if (other.parameterTypes != null) {
                //they do, they are first
                return AFTER_OTHER;
            }
            //neither has param types.
            //explicit method name comes first
            if (!methodName.equals("*")) {
                if (other.methodName.equals("*")) {
                    return BEFORE_OTHER;
                }
                //both explicit method names.
                //explicit method interface comes first
                if (methodIntf != null) {
                    if (other.methodIntf == null) {
                        return BEFORE_OTHER;
                    }
                    //both explicit method intf. sort by intf, then methodName
                    int intfOrder = methodIntf.compareTo(other.methodIntf);
                    if (intfOrder != 0) {
                        return intfOrder;
                    }
                    //same interfaces
                    return methodName.compareTo(other.methodName);
                }
                if (other.methodIntf != null) {
                    //they have explicit method inft, we dont, they are first
                    return AFTER_OTHER;
                }
                //neither have explicit method intf.
                return methodName.compareTo(other.methodName);
            }
            //we don't have explicit method name
            if (!other.methodName.equals("*")) {
                //they do, they are first
                return AFTER_OTHER;
            }
            //neither has explicit method name
            if (methodIntf != null) {
                if (other.methodIntf == null) {
                    return BEFORE_OTHER;
                }
                return methodIntf.compareTo(other.methodIntf);
            }
            if (other.methodIntf != null) {
                return AFTER_OTHER;
            }
            //neither has methodIntf or explicit methodName.  problem.
            throw new IllegalStateException("Cannot compare " + this + " and " + other);
        }

        private int compareMethod(MethodTransaction other) {
            int methodOrder = methodName.compareTo(other.methodName);
            if (methodOrder != 0) {
                return methodOrder;
            }
            //same method name, sort by params lexicographically
            for (int i = 0; i < parameterTypes.length; i++) {
                if (i == other.parameterTypes.length) {
                    //the other list is shorter, they are first
                    return AFTER_OTHER;
                }
                int paramOrder = parameterTypes[i].compareTo(other.parameterTypes[i]);
                if (paramOrder != 0) {
                    return paramOrder;
                }
            }
            //our list is shorter, we are first
            return BEFORE_OTHER;
        }

        public String toString() {
            StringBuffer result = new StringBuffer("MethodTransaction: interface ").append(methodIntf).append(", methodName ").append(methodName).append(", parameters: ");
            if (parameterTypes != null) {
                for (int i = 0; i < parameterTypes.length; i++) {
                    String parameterType = parameterTypes[i];
                    result.append(parameterType).append(", ");
                }
            }
            result.append("transaction attribute: ").append(transactionPolicy);
            return result.toString();
        }

        public boolean matches(String methodIntf, String methodName, String[] parameterTypes) {
            assert methodIntf != null;
            assert methodName != null;
            assert parameterTypes != null;
            if (this.methodIntf != null && !methodIntf.equals(this.methodIntf)) {
                //definitely wrong interface
                return false;
            }
            //our interface is not specified or matches.
            if (this.methodName.equals("*")) {
                return true;
            }
            if (!methodName.equals(this.methodName)) {
                //explicitly different method names
                return false;
            }
            //same method names.
            if (this.parameterTypes == null) {
                return true;
            }
            return Arrays.equals(parameterTypes, this.parameterTypes);
        }

    }
}
