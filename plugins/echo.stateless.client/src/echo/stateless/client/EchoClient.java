/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package echo.stateless.client;

import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import echo.stateless.bean.EchoServer;

public class EchoClient {

    private EchoServer server;

    public EchoClient() {
	try {
	    Properties p = new Properties();
	    p.put(Context.INITIAL_CONTEXT_FACTORY,
		    "org.apache.openejb.client.LocalInitialContextFactory");
	    InitialContext initialContext = new InitialContext(p);
	    server = (EchoServer) initialContext.lookup("EchoServerBeanRemote");
	} catch (NamingException e) {
	    server = null;
	}
    }

    public String echo(String message) {
	return server.echo(message);
    }
}
