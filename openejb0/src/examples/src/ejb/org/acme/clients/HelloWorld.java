package org.acme.clients;

import javax.rmi.*;
import javax.naming.*;
import java.util.*;

import org.acme.hello.*;


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
    
