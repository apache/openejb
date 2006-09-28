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

package org.apache.openejb.deployment;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.xbeans.j2ee.ContainerTransactionType;
import org.apache.geronimo.xbeans.j2ee.JavaTypeType;
import org.apache.geronimo.xbeans.j2ee.MethodIntfType;
import org.apache.geronimo.xbeans.j2ee.MethodType;
import org.apache.openejb.transaction.TransactionPolicyType;
import org.apache.openejb.MethodSpec;

/**
 * @version $Revision$ $Date$
 */
public class TransactionPolicyHelper {
    private final Map ejbNameToTransactionAttributesMap = new HashMap();

    public TransactionPolicyHelper() {
    }

    public TransactionPolicyHelper(ContainerTransactionType[] containerTransactions) {
        for (int i = 0; i < containerTransactions.length; i++) {
            ContainerTransactionType containerTransaction = containerTransactions[i];
            String transactionAttribute = containerTransaction.getTransAttribute().getStringValue();
            MethodType[] methods = containerTransaction.getMethodArray();
            for (int j = 0; j < methods.length; j++) {
                MethodType method = methods[j];
                String ejbName = method.getEjbName().getStringValue();
                MethodTransaction methodTransaction = new MethodTransaction(method, transactionAttribute);
                putMethodTransaction(ejbName, methodTransaction);
            }
        }
    }

    public SortedMap getTransactionPolicies(String ejbName) throws DeploymentException {
        SortedSet methodTransactions = (SortedSet) ejbNameToTransactionAttributesMap.get(ejbName);
        if (methodTransactions == null) return null;

        SortedMap transactionPolicies = new TreeMap();
        for (Iterator iterator = methodTransactions.iterator(); iterator.hasNext();) {
            MethodTransaction methodTransaction = (MethodTransaction) iterator.next();
            MethodSpec methodSpec = new MethodSpec(methodTransaction.getMethodIntf(),
                    methodTransaction.getMethodName(),
                    methodTransaction.getParameterTypes());
            TransactionPolicyType transactionPolicy = methodTransaction.getTransactionPolicyType();
            transactionPolicies.put(methodSpec,  transactionPolicy);
        }
        return transactionPolicies;
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
            transactionPolicyMap.put("NotSupported", TransactionPolicyType.NotSupported);
            transactionPolicyMap.put("Required", TransactionPolicyType.Required);
            transactionPolicyMap.put("Supports", TransactionPolicyType.Supports);
            transactionPolicyMap.put("RequiresNew", TransactionPolicyType.RequiresNew);
            transactionPolicyMap.put("Mandatory", TransactionPolicyType.Mandatory);
            transactionPolicyMap.put("Never", TransactionPolicyType.Never);
            transactionPolicyMap.put("Stateless", TransactionPolicyType.Bean);
            transactionPolicyMap.put("Stateful", TransactionPolicyType.Bean);
        }

        private final TransactionPolicyType transactionPolicyType;
        private final String methodIntf;
        private final String methodName;
        private final String[] parameterTypes;

        public MethodTransaction(MethodType method, String transactionAttribute) {
            transactionPolicyType = (TransactionPolicyType) transactionPolicyMap.get(transactionAttribute);

            // interface type
            MethodIntfType methodInterface = method.getMethodIntf();
            if (methodInterface != null) {
                methodIntf = methodInterface.getStringValue();
            } else {
                methodIntf = null;
            }

            // method name
            methodName = method.getMethodName().getStringValue();

            // parameters
            if (method.getMethodParams() != null) {
                JavaTypeType[] methodParams = method.getMethodParams().getMethodParamArray();
                parameterTypes = new String[methodParams.length];
                for (int i = 0; i < methodParams.length; i++) {
                    parameterTypes[i] = methodParams[i].getStringValue();
                }
            } else {
                parameterTypes = null;
            }

        }

        public TransactionPolicyType getTransactionPolicyType() {
            return transactionPolicyType;
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
            result.append("transaction attribute: ").append(transactionPolicyType);
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
