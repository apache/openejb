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
package org.openejb.servlet;


import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class DebugServlet extends HttpServlet {

    //====================================================//
    // Nothing to configure beyond this point             //
    //====================================================//


    InitialContext ctx;

    /**
     * This method does the actual integration
     * 
     * As you can see, it's noting more than setting the
     * openejb.home System property and then getting
     * an InitialContext.
     * 
     * That's really all there is to it.  The rest
     * happens automatically.
     */
    public void init(ServletConfig config) throws ServletException {
        try{
        
        Properties p = new Properties();
        
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.openejb.client.LocalInitialContextFactory");
        p.put("openejb.loader", "embed");
        
        ctx = new InitialContext( p );

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * This method does a quick diagnostic of the integration
     */
    public void doGet(HttpServletRequest req, HttpServletResponse res) 
    throws ServletException,IOException {
        res.setContentType("text/plain");

        PrintWriter out = res.getWriter();

        // ---------------------------------------------------
        //  Were is the OpenEJB home?
        // ---------------------------------------------------

        out.print("[] Making sure that openejb.home is set ... ");
	
	if ( System.getProperty("openejb.home") == null ) {
            out.println("FAIL");
            return;
	} else {
            out.println("OK");
	}
    
        // ---------------------------------------------------
        //  Were the OpenEJB classes installed?
        // ---------------------------------------------------

        out.print("[] Checking for OpenEJB class definition ... ");
        
        ClassLoader myLoader;
        Class openejb;
        try{
            myLoader = this.getClass().getClassLoader();
            openejb = Class.forName("org.openejb.OpenEJB",true, myLoader);
            out.println("OK");
        } catch (Exception e){
            out.println("FAIL");
            return;
        }
    
        // ---------------------------------------------------
        //  Are the EJB libraries visible?
        // ---------------------------------------------------

        out.print("[] Checking for the EJB libraries  ... ");
        
        try{
            Class.forName("javax.ejb.EJBHome",true, myLoader);
            out.println("OK");
        } catch (Exception e){
            out.println("FAIL");
            return;
        }

        // ---------------------------------------------------
        //  Was OpenEJB initialized (aka started)?
        // ---------------------------------------------------

        out.print("[] Checking if OpenEJB is initialized ... ");
        
        try{
            Class[] params = new Class[0];
            Method isInitialized = null;
            isInitialized = openejb.getDeclaredMethod("isInitialized", params);
            Object val = isInitialized.invoke(openejb, new Object[0]);
            boolean running = ((Boolean)val).booleanValue();

            if (running) out.println("OK");
        } catch (Exception e){
            out.println("FAIL");
            return;
        }

        // ---------------------------------------------------
        //  Can I lookup anything?
        // ---------------------------------------------------

        out.print("[] Looking up a sub context ... ");
        
        try{
            Object obj = ctx.lookup( "client" );
            if (obj instanceof Context) out.println("OK");
        } catch (Exception e){
            out.println("FAIL");
            return;
        }


        // ---------------------------------------------------
        //  Can I lookup a home interface from the testsuite?
        // ---------------------------------------------------

        out.print("[] Looking up an ejb home  ... ");
        
        Object ejbHome = null;
        try{
            ejbHome = ctx.lookup( "client/tests/stateless/BasicStatelessHome" );
            if (ejbHome instanceof java.rmi.Remote) out.println("OK");
        } catch (Exception e){
            out.println("FAIL");
            return;
        }
        
        // ---------------------------------------------------
        //  Is the home interface visible?
        // ---------------------------------------------------

        out.print("[] Checking for the home interface class definition ... ");
        
        Class homeInterface;
        try{
            homeInterface = Class.forName("org.openejb.test.stateless.BasicStatelessHome",true, myLoader);
            out.println("OK");
        } catch (Exception e){
            out.println("FAIL");
            return;
        }
    
        // ---------------------------------------------------
        //  Can I invoke a create method on the ejb home?
        // ---------------------------------------------------

        out.print("[] Invoking the create method on the ejb home  ... ");
        
        Object ejbObject = null;
        try{
            Class[] params = new Class[0];
            Method create = null;
            create = homeInterface.getDeclaredMethod("create", params);
            ejbObject = create.invoke(ejbHome, new Object[0]);

            if (ejbObject instanceof java.rmi.Remote) out.println("OK");
            
        } catch (Exception e){
            out.println("FAIL");
            return;
        }

        // ---------------------------------------------------
        //  Is the remote interface visible?
        // ---------------------------------------------------

        out.print("[] Checking for the remote interface class definition ... ");
        
        Class remoteInterface;
        try{
            remoteInterface = Class.forName("org.openejb.test.stateless.BasicStatelessObject",true, myLoader);
            out.println("OK");
        } catch (Exception e){
            out.println("FAIL");
            return;
        }
    
        // ---------------------------------------------------
        //  Can I invoke a business method on the ejb object?
        // ---------------------------------------------------

        out.print("[] Invoking a business method on the ejb object ... ");
        
        Object returnValue = null;
        try{
            String message = "!snoitalutargnoC ,skroW gnihtyrevE";
            Class[] params = new Class[]{String.class};
            Method businessMethod = null;
            businessMethod = remoteInterface.getDeclaredMethod("businessMethod", params);
            returnValue = businessMethod.invoke(ejbObject, new Object[]{message});

            if (returnValue instanceof java.lang.String) out.println("OK");
            
        } catch (Exception e){
            out.println("FAIL");
            return;
        }

        out.println("[] The Enterprise Bean returned the following message\n");
        out.println("-----------------------------------------------------");
        out.println(returnValue);
        out.println("-----------------------------------------------------");

    }

}



