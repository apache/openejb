/*
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
package org.apache.openejb.core.singleton;

import org.apache.openejb.core.BaseSessionContext;
import org.apache.openejb.core.Operation;
import org.apache.openejb.core.ThreadContext;
import org.apache.openejb.spi.SecurityService;

/**
 * @version $Rev$ $Date$
 */
public class SingletonContext extends BaseSessionContext {

    public SingletonContext(SecurityService securityService) {
        super(securityService);
    }

    @Override
    public void check(Call call) {
        final Operation operation = ThreadContext.getThreadContext().getCurrentOperation();

        switch (call) {
            case getEJBLocalObject:
            case getEJBObject:
            case getBusinessObject:
            case getUserTransaction:
            case getTimerService:
            case getContextData:
                switch (operation) {
                    case INJECTION:
                        throw illegal(call, operation);
                    default:
                        return;
                }
            case getCallerPrincipal:
            case isCallerInRole:
                switch (operation) {
                case INJECTION:
                case CREATE:
                case POST_CONSTRUCT:
                case PRE_DESTROY:
                    throw illegal(call, operation);
                default:
                    return;
                }
            case timerMethod:
            case setRollbackOnly:
            case getRollbackOnly:
            case UserTransactionMethod:
                switch (operation) {
                    case INJECTION:
                    case CREATE:
                        throw illegal(call, operation);
                    default:
                        return;
                }
            case getInvokedBusinessInterface:
                switch (operation) {
                    case BUSINESS:
                        return;
                    default:
                        throw illegal(call, operation);
                }
            case getMessageContext:
                switch (operation) {
                    case BUSINESS_WS:
                        return;
                    default:
                        throw illegal(call, operation);
                }
        }
    }
}
