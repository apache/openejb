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
package org.apache.openejb.client;

import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.RemoveException;

public class StatefulEJBHomeHandler extends EJBHomeHandler {

    public StatefulEJBHomeHandler() {
    }

    public StatefulEJBHomeHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client) {
        super(ejb, server, client);
    }

    protected Object findX(Method method, Object[] args, Object proxy) throws Throwable {
        throw new SystemException(new UnsupportedOperationException("Session beans may not have find methods"));
    }

    protected Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        throw new ApplicationException(new RemoveException("Session objects are private resources and do not have primary keys"));
    }

    protected Object removeWithHandle(Method method, Object[] args, Object proxy) throws Throwable {

        EJBObjectHandle handle = (EJBObjectHandle) args[0];

        if (handle == null) throw new NullPointerException("The handle is null");

        EJBObjectHandler handler = handle.handler;
        Object primKey = handler.primaryKey;

        if (!handler.ejb.deploymentID.equals(this.ejb.deploymentID)) {
            throw new SystemException(new IllegalArgumentException("The handle is not from the same deployment"));
        }

        EJBRequest req = new EJBRequest(RequestMethodCode.EJB_HOME_REMOVE_BY_HANDLE, ejb, method, args, primKey);

        EJBResponse res = request(req);

        switch (res.getResponseCode()) {
            case ResponseCodes.EJB_ERROR:
                throw new SystemError((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_SYS_EXCEPTION:
                throw new SystemException((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_APP_EXCEPTION:
                throw new ApplicationException((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_OK:
                invalidateAllHandlers(handler.getRegistryId());
                handler.invalidateReference();
                return null;
            default:
                throw new RemoteException("Received invalid response code from server: " + res.getResponseCode());
        }

    }
}
