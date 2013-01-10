/*
 *     Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */
package org.apache.openejb.server.cxf.rs;

import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.cxf.transport.HttpTransportFactory;
import org.apache.openejb.server.cxf.transport.util.CxfUtil;
import org.apache.openejb.server.rest.RESTService;
import org.apache.openejb.server.rest.RsHttpListener;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;

public class CxfRSService extends RESTService {

    private static final String NAME = "cxf-rs";
    private HttpTransportFactory httpTransportFactory;

    @Override
    public void service(final InputStream in, final OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException(getClass().getName() + " cannot be invoked directly");
    }

    @Override
    public void service(final Socket socket) throws ServiceException, IOException {
        throw new UnsupportedOperationException(getClass().getName() + " cannot be invoked directly");
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public void init(final Properties properties) throws Exception {
        super.init(properties);
        CxfUtil.configureBus();
    }

    @Override
    protected void beforeStart() {
        super.beforeStart();
        httpTransportFactory = new HttpTransportFactory(CxfUtil.getBus());
    }

    @Override
    protected boolean containsJaxRsConfiguration(final Properties properties) {
        return properties.containsKey(CxfRsHttpListener.PROVIDERS_KEY)
               || properties.containsKey(CxfRsHttpListener.CXF_JAXRS_PREFIX + CxfUtil.IN_FAULT_INTERCEPTORS)
               || properties.containsKey(CxfRsHttpListener.CXF_JAXRS_PREFIX + CxfUtil.IN_INTERCEPTORS)
               || properties.containsKey(CxfRsHttpListener.CXF_JAXRS_PREFIX + CxfUtil.OUT_FAULT_INTERCEPTORS)
               || properties.containsKey(CxfRsHttpListener.CXF_JAXRS_PREFIX + CxfUtil.OUT_INTERCEPTORS)
               || properties.containsKey(CxfRsHttpListener.CXF_JAXRS_PREFIX + CxfUtil.DATABINDING)
               || properties.containsKey(CxfRsHttpListener.CXF_JAXRS_PREFIX + CxfUtil.FEATURES)
               || properties.containsKey(CxfRsHttpListener.CXF_JAXRS_PREFIX + CxfUtil.ADDRESS)
               || properties.containsKey(CxfRsHttpListener.CXF_JAXRS_PREFIX + CxfUtil.ENDPOINT_PROPERTIES);
    }

    @Override
    protected RsHttpListener createHttpListener() {
        return new CxfRsHttpListener(httpTransportFactory, getWildcard());
    }
}
