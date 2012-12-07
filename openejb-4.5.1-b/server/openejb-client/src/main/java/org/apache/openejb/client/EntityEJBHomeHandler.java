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

import javax.ejb.EJBObject;
import javax.ejb.Handle;

public class EntityEJBHomeHandler extends EJBHomeHandler {

    public EntityEJBHomeHandler() {
    }

    public EntityEJBHomeHandler(EJBMetaDataImpl ejb, ServerMetaData server, ClientMetaData client) {
        super(ejb, server, client);
    }

    protected Object findX(Method method, Object[] args, Object proxy) throws Throwable {
        EJBRequest req = new EJBRequest(RequestMethodCode.EJB_HOME_FIND, ejb, method, args, null);

        EJBResponse res = request(req);

        Object primKey = null;
        EJBObjectHandler handler = null;
        Object[] primaryKeys = null;

        switch (res.getResponseCode()) {
            case ResponseCodes.EJB_ERROR:
                throw new SystemError((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_SYS_EXCEPTION:
                throw new SystemException((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_APP_EXCEPTION:
                throw new ApplicationException((ThrowableArtifact) res.getResult());

            case ResponseCodes.EJB_OK_FOUND:
                primKey = res.getResult();
                if (primKey == null) {
                    return null;
                } else {
                    handler = EJBObjectHandler.createEJBObjectHandler(ejb, server, client, primKey);
                    handler.setEJBHomeProxy((EJBHomeProxy) proxy);
                    registerHandler(ejb.deploymentID + ":" + primKey, handler);
                    return handler.createEJBObjectProxy();
                }

            case ResponseCodes.EJB_OK_FOUND_COLLECTION:

                primaryKeys = (Object[]) res.getResult();

                for (int i = 0; i < primaryKeys.length; i++) {
                    primKey = primaryKeys[i];
                    if (primKey != null) {
                        handler = EJBObjectHandler.createEJBObjectHandler(ejb, server, client, primKey);
                        handler.setEJBHomeProxy((EJBHomeProxy) proxy);
                        registerHandler(ejb.deploymentID + ":" + primKey, handler);
                        primaryKeys[i] = handler.createEJBObjectProxy();
                    }
                }
                return java.util.Arrays.asList(primaryKeys);
            case ResponseCodes.EJB_OK_FOUND_ENUMERATION:

                primaryKeys = (Object[]) res.getResult();

                for (int i = 0; i < primaryKeys.length; i++) {
                    primKey = primaryKeys[i];
                    if (primKey != null) {
                        handler = EJBObjectHandler.createEJBObjectHandler(ejb, server, client, primKey);
                        handler.setEJBHomeProxy((EJBHomeProxy) proxy);
                        registerHandler(ejb.deploymentID + ":" + primKey, handler);
                        primaryKeys[i] = handler.createEJBObjectProxy();
                    }
                }

                return new ArrayEnumeration(java.util.Arrays.asList(primaryKeys));
            default:
                throw new RemoteException("Received invalid response code from server: " + res.getResponseCode());
        }
    }

    protected Object removeByPrimaryKey(Method method, Object[] args, Object proxy) throws Throwable {
        Object primKey = args[0];

        if (primKey == null) throw new NullPointerException("The primary key is null.");

        EJBRequest req = new EJBRequest(RequestMethodCode.EJB_HOME_REMOVE_BY_PKEY, ejb, method, args, primKey);

        EJBResponse res = request(req);

        switch (res.getResponseCode()) {
            case ResponseCodes.EJB_ERROR:
                throw new SystemError((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_SYS_EXCEPTION:
                throw new SystemException((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_APP_EXCEPTION:
                throw new ApplicationException((ThrowableArtifact) res.getResult());
            case ResponseCodes.EJB_OK:
                invalidateAllHandlers(ejb.deploymentID + ":" + primKey);
                return null;
            default:
                throw new RemoteException("Received invalid response code from server: " + res.getResponseCode());
        }
    }

    protected Object removeWithHandle(Method method, Object[] args, Object proxy) throws Throwable {
        if (args[0] == null) throw new RemoteException("Handler is null");

        Handle handle = (Handle) args[0];

        EJBObject ejbObject = handle.getEJBObject();
        if (ejbObject == null) throw new NullPointerException("The handle.getEJBObject() is null.");

        Object primKey = ejbObject.getPrimaryKey();
        if (primKey == null) throw new NullPointerException("The handle.getEJBObject().getPrimaryKey() is null.");

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
                invalidateAllHandlers(ejb.deploymentID + ":" + primKey);
                return null;
            default:
                throw new RemoteException("Received invalid response code from server: " + res.getResponseCode());
        }
    }
}
