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

import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.naming.java.RootContext;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.openejb.client.naming.java.javaURLContextFactory;

import javax.naming.Context;
import javax.naming.NamingException;

/**
 * @version $Revision$ $Date$
 */
public class AppClientJNDIContext implements org.apache.geronimo.client.AppClientPlugin {

    private final String host;
    private final int port;

    private Context context;

    public AppClientJNDIContext(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void startClient(AbstractName appClientModuleName, Kernel kernel, ClassLoader classLoader) throws Exception {
        try {
            ServerMetaData serverMetaData = new ServerMetaData("BOOT", host, port);
            JNDIResponse res = new JNDIResponse();
            ResponseInfo resInfo = new ResponseInfo(res);
            JNDIRequest req = new JNDIRequest(JNDIRequest.JNDI_LOOKUP, appClientModuleName.toString(), "");
            RequestInfo reqInfo = new RequestInfo(req, new ServerMetaData[]{serverMetaData});

            Client.request(reqInfo, resInfo);

            context = (Context) res.getResult();
        } catch (Exception e) {
            NamingException namingException = new NamingException("Unable to retrieve J2EE AppClient's JNDI Context");
            namingException.initCause(e);
            throw namingException;
        }

        if (context == null) {
            throw new IllegalStateException("Server returned a null JNDI context");
        }

        RootContext.setComponentContext(context);

        System.setProperty(Context.URL_PKG_PREFIXES, "org.apache.openejb.client.naming");
        System.setProperty(Context.INITIAL_CONTEXT_FACTORY, javaURLContextFactory.class.getName());
    }

    public void stopClient(AbstractName appClientModuleName) throws Exception {
        RootContext.setComponentContext(null);
    }

    public Context getContext() {
        return context;
    }


}
