/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.openejb.test;

import org.openejb.client.RemoteInitialContextFactory;

import java.net.URL;
import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public class TomcatRemoteTestServer implements TestServer {
    private Properties properties;
    private String servletUrl;

    public void init(Properties props) {
        properties = props;
        servletUrl = System.getProperty("remote.serlvet.url", "http://127.0.0.1:8080/openejb/remote");
        props.put("test.server.class", TomcatRemoteTestServer.class.getName());
        props.put("java.naming.factory.initial", RemoteInitialContextFactory.class.getName());
        props.put("java.naming.provider.url", servletUrl);
    }

    public void start() {
        System.out.println("Note: Tomcat should be started before running these tests");
        if (!connect()) {
            throw new IllegalStateException("Unable to connect to Tomcat at localhost port 8080");
        }

        // Wait a wee bit longer for good measure
        try {
            Thread.sleep(5000);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void stop() {
    }


    public Properties getContextEnvironment() {
        return (Properties) properties.clone();
    }

    private boolean connect() {
        return connect(10);
    }

    private boolean connect(int tries) {
        //System.out.println("CONNECT "+ tries);
        try {
            URL url = new URL(servletUrl);
            url.openStream();
        } catch (Exception e) {
            System.out.println("Attempting to connect to " + servletUrl);
            //System.out.println(e.getMessage());
            if (tries < 2) {
                return false;
            } else {
                try {
                    Thread.sleep(5000);
                } catch (Exception e2) {
                    e.printStackTrace();
                }
                return connect(--tries);
            }
        }

        return true;
    }
}
