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
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.ri.server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.URL;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;

import org.openejb.util.Messages;

/**
 * Dynamic ClassLoader that loads classes from the NamingServer.
 * 
 * Classes from the NamingSever are .class files from the server's classpath
 * or byte-code of generated proxies.
 * 
 * @author David Blevins
 */
public class RiClassLoader extends ClassLoader {

    static protected Messages _messages = new Messages( "org.openejb.alt.util.resources" );


    private Hashtable env;
    private Socket hostSocket;
    final static Byte CLASS_REQUEST = new Byte((byte)3);
    final static Byte CLOSE_CONNECTION = new Byte((byte)4);
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private String host;
    private int port;
    
    RiClassLoader(Hashtable environment) throws NamingException {
        if ( environment ==null ) throw new NamingException( _messages.message( "riClassLoader.invalidArgument" ) );
        else env = (Hashtable)environment.clone();
        
        String userID = (String)env.get(Context.SECURITY_PRINCIPAL);
        String psswrd = (String)env.get(Context.SECURITY_CREDENTIALS);
        AuthenticationRequest authRequest = new AuthenticationRequest(userID, psswrd);
        
        try {
            URL url; 
            Object providerUrl = env.get(Context.PROVIDER_URL); 
            if ( providerUrl instanceof String ) {
                url = new URL(providerUrl.toString()); 
            } else {
                url =  (URL)providerUrl; 
            } 
            hostSocket = new Socket(url.getHost(), url.getPort());
            oos = new ObjectOutputStream(hostSocket.getOutputStream());
            ois = new ObjectInputStream(hostSocket.getInputStream());
        
            oos.writeObject(authRequest);
            oos.flush();
        
            Object answer = ois.readObject();
            if ( answer instanceof NamingException ) throw (NamingException)answer;

        } catch ( Exception e ) {
            System.out.println( _messages.message( "riClassLoader.cannotAuthenticate" ) );
            //e.printStackTrace();
            throw new NamingException(e.getMessage());
        }
    }
    
    protected void finalize() throws Throwable{
        oos.writeObject(CLOSE_CONNECTION);
        oos.flush();
        oos.close();
        ois.close();
        hostSocket.close();
        hostSocket = null;
        env = null;
        host = null;
        port = 0;
    }

    /**
     * This method should not need to be overridden by subclasses.  
     * It is done here because of a bug in the VM.  This method was 
     * overriden using the algorithm of it's superclass with one
     * workaround that reliably calls the findClass method.
     * No other changes should be made in this method.
     * 
     * @param name
     * @param resolve
     * @return 
     * @exception ClassNotFoundException
     */
    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
        //System.out.println("\n[Ri Class Loader] loadClass: "+name);
	// First, check if the class has already been loaded
	Class c = findLoadedClass(name);
	if (c == null) {
	    try {
                ClassLoader parent = getParent();
		if (parent != null) {
                    c = parent.loadClass(name);
		} else {
		    c = findSystemClass(name);
		}
            
	    } catch (ClassNotFoundException e) {
	        // If still not found, then call findClass in order
	        // to find the class.
                c = findClass(name);
            } catch (NoClassDefFoundError bug) {
                /* [DMB] Problem: 
                 *       The VM does not consistently throw ClassNotFoundException
                 *       when classes are not found.  The sublcass' findClass method 
                 *       is not called in these situations.  This behavior is not in
                 *       accordance with the ClassLoader delegation model.
                 *       Workaround: 
                 *       Catch the NoClassDefFoundError that is actually thrown and
                 *       call the findClass method according to the delegation model.
                 */
                c = findClass(name);
	    }
	}
	if (resolve) {
            resolveClass(c);
	}
	return c;
    }

    /**
     * Finds the specified class. This method should be overridden
     * by class loader implementations that follow the new delegation model
     * for loading classes, and will be called by the <code>loadClass</code>
     * method after checking the parent class loader for the requested class.
     * The default implementation throws <code>ClassNotFoundException</code>.
     *
     * @param  name the name of the class
     * @return the resulting <code>Class</code> object
     * @exception ClassNotFoundException if the class could not be found
     * @since  JDK1.2
     */
    protected Class findClass(String name) throws ClassNotFoundException {
        try{
        byte[] b = loadClassData(name);
        Class clazz = defineClass(name, b, 0, b.length);
        resolveClass(clazz);

        //System.out.println("[RI Class Loader] Loaded class: "+name);
        //System.out.println("@");
        return clazz;
        } catch(ClassNotFoundException e){
            System.out.println( _messages.format( "riClassLoader.cannotAuthenticate", name ) );
            throw e;
        }
        
    }

    private byte[] loadClassData(String name) throws ClassNotFoundException{
        // load the class data from the connection
        try{
            oos.writeObject( CLASS_REQUEST );
            oos.writeUTF( name );
            oos.flush();
            byte[] byteCode = new byte[ois.readInt()];
            ois.readFully(byteCode);
            return byteCode;
        } catch( IOException ioe){
            throw new ClassNotFoundException( _messages.message( "riClassLoader.problemReadingClass" ) );
        }
    }
}
