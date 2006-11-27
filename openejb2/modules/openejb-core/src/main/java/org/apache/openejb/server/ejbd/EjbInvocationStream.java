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
package org.apache.openejb.server.ejbd;

import java.io.IOException;
import java.io.ObjectInput;
import java.lang.reflect.Method;

import org.apache.geronimo.interceptor.InvocationKey;
import org.apache.geronimo.interceptor.InvocationResult;
import org.apache.geronimo.interceptor.SimpleInvocationResult;
import org.apache.openejb.transaction.EjbTransactionContext;
import org.apache.openejb.EJBInstanceContext;
import org.apache.openejb.EJBInterfaceType;
import org.apache.openejb.EjbInvocation;
import org.apache.openejb.EjbInvocationImpl;
import org.apache.openejb.ExtendedEjbDeployment;
import org.apache.openejb.corba.ORBRef;
import org.apache.openejb.client.EJBRequest;

public class EjbInvocationStream extends EJBRequest implements EjbInvocation {
    // The container that we are invoking, this is set in the container before sending the invocation to the interceptor stack
    private transient ExtendedEjbDeployment ejbDeployment;

    private ObjectInput in;

    private final EjbInvocation invocationState = new EjbInvocationImpl(null, -1, null);
    private EJBInterfaceType interfaceType;

    private int methodIndex = -1;

    public EjbInvocationStream() {
        super();
    }

    public EjbInvocationStream(ORBRef orbRef) {
        super(orbRef);
    }

    public EjbInvocationStream(int requestMethod) {
        super(requestMethod);
    }

    public ExtendedEjbDeployment getEjbDeployment() {
        return ejbDeployment;
    }

    public void setEjbDeployment(ExtendedEjbDeployment ejbDeployment) {
        this.ejbDeployment = ejbDeployment;
    }

    public Object[] getArguments() {
        return getMethodParameters();
    }

    public Object getId() {
        return getPrimaryKey();
    }

    public int getMethodIndex() {
        return methodIndex;
    }

    public EJBInterfaceType getType() {
        return interfaceType;
    }

    public Class getMethodClass() {
        checkState();
        return super.getMethodClass();
    }

    public Method getMethodInstance() {
        checkState();
        return super.getMethodInstance();
    }

    public String getMethodName() {
        checkState();
        return super.getMethodName();
    }

    public Object[] getMethodParameters() {
        checkState();
        return super.getMethodParameters();
    }

    public Class[] getMethodParamTypes() {
        checkState();
        return super.getMethodParamTypes();
    }

    public Object getPrimaryKey() {
        checkState();
        return super.getPrimaryKey();
    }

    public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
        clearState();

        this.in = in;

        readRequestMethod(in);

        readContainerId(in);

        readClientIdentity(in);

        switch (super.getRequestMethod()){
            case EJB_HOME_METHOD:
            case EJB_HOME_CREATE:
            case EJB_HOME_FIND:
            case EJB_HOME_GET_EJB_META_DATA:
            case EJB_HOME_GET_HOME_HANDLE:
            case EJB_HOME_REMOVE_BY_HANDLE:
            case EJB_HOME_REMOVE_BY_PKEY:
                interfaceType = EJBInterfaceType.HOME; break;
            default:
                interfaceType = EJBInterfaceType.REMOTE;
        }
//        finishReadExternal();
   }

    private void checkState(){
        if (super.getMethodInstance() == null){
            try {
                finishReadExternal();
            } catch (IOException e) {
                IllegalStateException ise = new IllegalStateException("Invalid EJBRequest stream.");
                ise.initCause(e);
                throw ise;
            } catch (ClassNotFoundException e) {
//                IllegalAccessError iae = new IllegalAccessError("Class only accessible from classloader of an EJBContainer.");
                RuntimeException iae = new RuntimeException(e);
//                iae.initCause(e);
                throw iae;
            }
        }
    }

    private void finishReadExternal()
    throws IOException, ClassNotFoundException {
        readPrimaryKey(in);

        readMethod(in);

        readMethodParameters(in);

        loadMethodInstance();
    }

    public Object get(InvocationKey arg0) {
        return invocationState.get(arg0);
    }

    public void put(InvocationKey arg0, Object arg1) {
        invocationState.put(arg0, arg1);
    }

    public void setEJBInstanceContext(EJBInstanceContext instanceContext) {
        invocationState.setEJBInstanceContext(instanceContext);
    }

    public EJBInstanceContext getEJBInstanceContext() {
        return invocationState.getEJBInstanceContext();
    }

    public EjbTransactionContext getEjbTransactionData() {
        return invocationState.getEjbTransactionData();
    }

    public void setEjbTransactionData(EjbTransactionContext ejbTransactionContext) {
        invocationState.setEjbTransactionData(ejbTransactionContext);
    }

    public void setMethodIndex(int methodIndex) {
        this.methodIndex = methodIndex;
    }

    public InvocationResult createResult(Object object) {
        return new SimpleInvocationResult(true, object);
    }

    public InvocationResult createExceptionResult(Exception exception) {
        return new SimpleInvocationResult(false, exception);
    }

}
