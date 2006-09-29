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
package org.apache.openejb.loader;

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
        
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.LocalInitialContextFactory");
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



