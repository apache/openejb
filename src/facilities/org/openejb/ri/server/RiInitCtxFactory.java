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


import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;

import org.openejb.util.Messages;


/**
 * Invoked by client-side JNDI lookup.
 * 
 * @author David Blevins
 * @author Richard Monson-Haefel
 */
public class RiInitCtxFactory implements InitialContextFactory {

    static protected Messages _messages = new Messages( "org.openejb.alt.util.resources" );


    // The ClassLoader could be implemented as static.
    //static RiClassLoader loader;
    
    public Context getInitialContext(Hashtable env) throws NamingException {
        String CURRENT_OPPERATION = _messages.message( "riInitCtxFactory.instantiatingInitialContext" );
        try{
        // The ClassLoader could be implemented as static.
        //if (loader == null) loader =  new RiClassLoader(env);
        RiClassLoader loader = new RiClassLoader(env);
        Class clazz = loader.loadClass("org.openejb.ri.server.RiContext");
        DynamicContext context = (DynamicContext)clazz.newInstance();
        context.init(env);

        return context;
        
        } catch (ClassNotFoundException cnfe){
            System.out.println( _messages.format( "riInitCtxFactory.classNoFound", CURRENT_OPPERATION, cnfe.getMessage() ) );
            throw new NamingException(cnfe.getMessage());
        } catch (IllegalAccessException iae){
            System.out.println( _messages.format( "riInitCtxFactory.inadequateAccess", CURRENT_OPPERATION, iae.getMessage() ) );
            //iae.printStackTrace();
            throw new NamingException(iae.getMessage());
        } catch (InstantiationException ie){
            System.out.println( _messages.format( "riInitCtxFactory.classCannotBeInstantiated", CURRENT_OPPERATION, ie.getMessage() ) );
            //ie.printStackTrace();
            throw new NamingException( _messages.format( "riInitCtxFactory.cannotInstantiateContext", CURRENT_OPPERATION, ie.getMessage() ) );
        }

    }
}


