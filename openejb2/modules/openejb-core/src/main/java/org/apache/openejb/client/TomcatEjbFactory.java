/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
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
 *    (http://openejb.sf.net/).
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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
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
