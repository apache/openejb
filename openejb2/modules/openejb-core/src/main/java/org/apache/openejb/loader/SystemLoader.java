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

import java.util.Hashtable;


/**
 * Checks to see if OpenEJB is available through the system 
 * classpath.  If it isn't, then the required libraries are
 * added to the system classpath.  A call is then made
 * to the EmbeddedLoader to load OpenEJB fully.
 * 
 */
public class SystemLoader implements Loader {
    
    static boolean loaded = false;
    /**
     * Checks to see if OpenEJB is available through the system 
     * classpath.  If it isn't, then the required libraries are
     * added and OpenEJB is pulled in and instantiated.
     * 
     * @param env
     * @exception Exception
     */
    public void load( Hashtable env ) throws Exception{
        if (loaded) return;

        try{
            Class.forName("org.apache.openejb.OpenEJB");
        } catch (Exception e){
            importOpenEJBLibraries( env );
        } 
        try{
            Loader embedded = null;
            
            Class loaderClass = Class.forName( "org.apache.openejb.loader.EmbeddedLoader" );
            
            embedded = (Loader)loaderClass.newInstance();
            embedded.load( env );
            
            org.apache.openejb.util.ClasspathUtils.rebuildJavaClassPathVariable();
        } catch (Exception e){
            throw new Exception( "Cannot embed OpenEJB. Exception: "+
                                 e.getClass().getName()+" "+ e.getMessage());
        }
        loaded = true;
    }
    
    private void importOpenEJBLibraries(  Hashtable env ) throws Exception{
        // Sets the openejb.home system variable
        try{
            if ( env.get("openejb.home") != null ) {
                System.setProperty("openejb.home", (String)env.get("openejb.home"));
            }
        } catch (Exception e){}
        
        try{
            // Loads all the libraries in the openejb.home/lib directory
            org.apache.openejb.util.ClasspathUtils.addJarsToPath("lib", "system");

            // Loads all the libraries in the openejb.home/dist directory
            org.apache.openejb.util.ClasspathUtils.addJarsToPath("dist", "system");
        } catch (Exception e){
            throw new Exception( "Could not load OpenEJB libraries. Exception: "+
                                 e.getClass().getName()+" "+ e.getMessage());
        }
    }
}

 
