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
package org.openejb.loader;

import java.io.File;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

/**
 */
public class LoaderServlet extends HttpServlet {
    
    public void init(ServletConfig config) throws ServletException {
        try{
        
        String home = config.getInitParameter("openejb.home");
        String conf = config.getInitParameter("openejb.configuration");
        String copy = config.getInitParameter("openejb.localcopy");

        Properties p = new Properties();
        
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.openejb.client.LocalInitialContextFactory");
        p.put("openejb.loader", "embed");
        
        if (home != null) {
            System.setProperty("openejb.home",home);
            p.put("openejb.home",home);
        }
        if (conf != null) {
            System.setProperty("openejb.configuration",conf);
            p.put("openejb.configuration",conf);
        }
        if (copy != null) {
            System.setProperty("openejb.localcopy", copy);
            p.put("openejb.localcopy", copy);
        }
        InitialContext ctx = new InitialContext( p );

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    String NO_HOME = "The openejb.home is not set.";
    String BAD_HOME = "Invalid openejb.home: ";
    String NOT_THERE = "The path specified does not exist.";
    String NOT_DIRECTORY = "The path specified is not a directory.";
    String NO_DIST = "The path specified is not correct, it does not contain a 'dist' directory.";
    String NO_LIBS = "The path specified is not correct, it does not contain any OpenEJB libraries.";
    String INSTRUCTIONS = "Please edit the web.xml of the openejb_loader webapp and set the openejb.home init-param to the full path where OpenEJB is installed.";

    private void checkOpenEjbHome() throws ServletException{
        try{

            // The openejb.home must be set
            String homePath = System.getProperty("openejb.home");
            if (homePath == null) handleError(NO_HOME, INSTRUCTIONS);

            // The openejb.home must exist
            File openejbHome = new File(homePath);
            if (!openejbHome.exists()) handleError(BAD_HOME+homePath, NOT_THERE, INSTRUCTIONS);
            
            // The openejb.home must be a directory
            if (!openejbHome.isDirectory()) handleError(BAD_HOME+homePath, NOT_DIRECTORY, INSTRUCTIONS);

            // The openejb.home must contain a 'dist' directory
            File openejbHomeDist = new File(openejbHome, "dist");
            if ( !openejbHomeDist.exists() ) handleError(BAD_HOME+homePath, NO_DIST, INSTRUCTIONS);

            // The openejb.home there must be openejb*.jar files in the 'dist' directory
            String[] libs = openejbHomeDist.list();
            boolean found = false;
            for (int i=0; i < libs.length && !found; i++){
                found = (libs[i].startsWith("openejb-") && libs[i].endsWith(".jar"));
            }
            if ( !found ) handleError(BAD_HOME+homePath, NO_LIBS, INSTRUCTIONS);
        
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void handleError(String m1, String m2, String m3) throws ServletException{
        System.err.println("--[PLEASE FIX]-------------------------------------");
        System.err.println(m1);
        System.err.println(m2);
        System.err.println(m3);
        System.err.println("---------------------------------------------------");
        throw new ServletException(m1+" "+m2+" "+m3);
    }
    private void handleError(String m1, String m2) throws ServletException{
        System.err.println("--[PLEASE FIX]-------------------------------------");
        System.err.println(m1);
        System.err.println(m2);
        System.err.println("---------------------------------------------------");
        throw new ServletException(m1+" "+m2);
    }
}



