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
 * Copyright 2003 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.acme.clients;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;

import org.acme.hello.HelloHome;
import org.acme.hello.HelloObject;


public class HelloWorld {

    public static void main( String args[]) {
	try {
	
	    Properties p = new Properties();
	    
	    //The JNDI properties you set depend
	    //on which server you are using.
	    //These properties are for the Local Server.
	    p.put("java.naming.factory.initial", "org.openejb.client.LocalInitialContextFactory");
	    p.put("java.naming.security.principal", "myuser");
	    p.put("java.naming.security.credentials", "mypass");
	    
	    //Now use those properties to create
	    //a JNDI InitialContext with the server.
	    InitialContext ctx = new InitialContext( p );
	    
	    //Lookup the bean using it's deployment id
	    Object obj = ctx.lookup("/Hello");
	    
	    //Be good and use RMI remote object narrowing
	    //as required by the EJB specification.
	    HelloHome ejbHome = (HelloHome)
	    PortableRemoteObject.narrow(obj,HelloHome.class);
	    
	    //Use the HelloHome to create a HelloObject
	    HelloObject ejbObject = ejbHome.create();
	    
	    //The part we've all been wainting for...
	    String message = ejbObject.sayHello();
	    
	    //A drum roll please.
	    System.out.println( message );
	    
	} catch (Exception e){
	    e.printStackTrace();
	}
    }
}
    
