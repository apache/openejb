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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.Name;
import javax.naming.RefAddr;
import javax.naming.Reference;
import javax.naming.spi.ObjectFactory;

/**
 * Tomcat EJB factory. The factory allows a web application deployed in Tomcat to look up a bean deployed in OpenEJB.
 * Depending on the factory's configuration OpenEJB will start up in the local mode (inside the JVM Tomcat runs in) or
 * the request for the bean will be passed along to OpenEJB remote instance.
 * <p>
 * Declaration of the factory in Tomcat's server.xml:
 * <blockquote><pre>
 * &lt;Context ...&gt;
 *   ...
 *   &lt;Ejb name="ejb/hello"
 *        type="Session"
 *        home="org.acme.HelloHome"
 *        remote="org.acme.Hello"/&gt;
 *   &lt;ResourceParams name="ejb/hello"&gt;
 *     &lt;parameter&gt;
 *       &lt;name&gt;factory&lt;/name&gt;
 *       &lt;value&gt;org.apache.openejb.client.TomcatEjbFactory&lt;/value&gt;
 *     &lt;/parameter&gt;
 *     &lt;parameter&gt;
 *       &lt;name&gt;openejb.naming.factory.initial&lt;/name&gt;
 *       &lt;value&gt;org.apache.openejb.client.RemoteInitialContextFactory&lt;/value&gt;
 *     &lt;/parameter&gt;
 *     &lt;parameter&gt;
 *       &lt;name&gt;openejb.naming.security.principal&lt;/name&gt;
 *       &lt;value&gt;username&lt;/value&gt;
 *     &lt;/parameter&gt;
 *     &lt;parameter&gt;
 *       &lt;name&gt;openejb.naming.security.credentials&lt;/name&gt;
 *       &lt;value&gt;password&lt;/value&gt;
 *     &lt;/parameter&gt;
 *     &lt;parameter&gt;
 *       &lt;name&gt;openejb.naming.provider.url&lt;/name&gt;
 *       &lt;value&gt;localhost:4201&lt;/value&gt;
 *     &lt;/parameter&gt;
 *     &lt;parameter&gt;
 *       &lt;name&gt;openejb.ejb-link&lt;/name&gt;
 *       &lt;value&gt;Hello&lt;/value&gt;
 *     &lt;/parameter&gt;
 *   &lt;/ResourceParams&gt;
 *   ...
 * &lt;/Context&gt;
 * </pre></blockquote>
 * Changing RemoteInitialContextFactory (<i>openejb.naming.factory.initial</i> parametr's value) into LocalInitialContextFactory incurs starting OpenEJB in the local mode.
 * <p>
 * Make sure to read OpenEJB documentation for more information on the factory -
 * <a href="http://openejb.sf.net">http://openejb.sf.net/tomcat.html</a>
 *
 * @since 01/12/2003
 */
public final class TomcatEjbFactory implements ObjectFactory
{
    private final static String OPENEJB_PREFIX = "openejb.";

    private final static String JAVA_PREFIX = "java.";

    private final static String OPENEJB_EJB_LINK = "openejb.ejb-link";

    private final static int OPENEJB_PREFIX_LENGTH = OPENEJB_PREFIX.length();

    public Object getObjectInstance( Object obj,
                                     Name name,
                                     Context nameCtx,
                                     Hashtable environment )
            throws Exception
    {
        Object beanObj = null;
        Class ejbRefClass = Class.forName( "org.apache.naming.EjbRef" );
        if ( ejbRefClass.isAssignableFrom( obj.getClass() ) )
        {
            RefAddr refAddr = null;
            String addrType = null;
            Properties env = new Properties();
            String bean = null;

            Reference ref = ( Reference ) obj;

            Enumeration addresses = ref.getAll();
            while ( addresses.hasMoreElements() )
            {
                refAddr = ( RefAddr ) addresses.nextElement();
                addrType = refAddr.getType();
                if ( addrType.startsWith( OPENEJB_PREFIX ) )
                {
                    String value = refAddr.getContent().toString();
                    if ( addrType.equals( OPENEJB_EJB_LINK ) )
                    {
                        bean = value;
                        continue;
                    }
                    String key = addrType.substring( OPENEJB_PREFIX_LENGTH );
                    key = JAVA_PREFIX + key;
                    env.put( key, value );
                }
            }

            if ( bean != null )
            {
                beanObj = ( new InitialContext( env ) ).lookup( bean );
            }
        }
        return beanObj;
    }
}
