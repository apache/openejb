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
package org.openejb.alt.config;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.*;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.*;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.exolab.castor.xml.Unmarshaller;
import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.*;
import org.openejb.alt.config.ejb11.*;
import org.openejb.alt.config.sys.*;
import org.openejb.util.Logger;
import org.openejb.util.FileUtils;
import org.openejb.util.JarUtils;
import org.openejb.util.Messages;

/*------------------------------------------------------*/
/* Utility method for reading and writing config files  */
/*------------------------------------------------------*/


public class ConfigUtils  {
    
    public static File defaultServicesJar = null;
    public static String defaultServicesJarName = "default.service-jar.xml";

    private static Map loadedServiceJars = new HashMap();

    private static Messages messages = new Messages( "org.openejb.alt.util.resources" );
    private static Logger _logger = Logger.getInstance( "OpenEJB", "org.openejb.alt.util.resources" );

    public static File getDefaultServiceJar() throws OpenEJBException{

        if (defaultServicesJar == null) {
            defaultServicesJar = JarUtils.getJarContaining(defaultServicesJarName);
        }
        return defaultServicesJar;
    }

    public static ServiceProvider getService(String jar, String id) throws OpenEJBException{

        ServiceProvider provider = null;

        if ( loadedServiceJars.get(jar) == null ) {

            ServicesJar sj = readServicesJar(jar);

            HashMap providers = new HashMap(sj.getServiceProviderCount());

            ServiceProvider[] sp = sj.getServiceProvider();

            for (int i=0; i < sp.length; i++){
                providers.put(sp[i].getId(), sp[i]);
            }

            loadedServiceJars.put(jar, providers);

            provider = (ServiceProvider) providers.get(id);
        } else {

            Map providers = (Map)loadedServiceJars.get(jar);
            provider = (ServiceProvider) providers.get(id);
        }

        if (provider == null) {
            handleException("conf.4901", id, jar);
        }

        return provider;
    }

    /*
        TODO: Use the java.net.URL instead of java.io.File so configs
        and jars can be located remotely in the network
     */
    public static Openejb readConfig(String confFile) throws OpenEJBException{
        Openejb obj = null;
        Reader reader = null;
        try {
            reader = new FileReader(confFile);
            obj = Openejb.unmarshal(reader);
        } catch ( FileNotFoundException e ) {
            handleException("conf.1900", confFile, e.getLocalizedMessage());
        } catch ( MarshalException e ) {
            if (e.getException() instanceof IOException){
                handleException("conf.1110", confFile, e.getLocalizedMessage());
            } else if (e.getException() instanceof UnknownHostException){
                handleException("conf.1121", confFile, e.getLocalizedMessage());
            } else {
                handleException("conf.1120", confFile, e.getLocalizedMessage());
            }
        } catch ( ValidationException e ) {
            /* TODO: Implement informative error handling here. 
               The exception will say "X doesn't match the regular 
               expression Y" 
               This should be checked and more relevant information
               should be given -- not everyone understands regular 
               expressions. 
             */
            /*
            NOTE: This doesn't seem to ever happen, anyone know why?
            */
            handleException("conf.1130",confFile, e.getLocalizedMessage());
        }
        try {
            reader.close();
        } catch ( Exception e ) {
            handleException("file.0020", confFile, e.getLocalizedMessage());
        }
        return obj;
    }
    
    
    public static void writeConfig(String confFile, Openejb confObject) throws OpenEJBException{
        /* TODO:  Just to be picky, the xml file created by
        Castor is really hard to read -- it is all on one line.
        People might want to edit this in the future by hand, so if Castor can 
        make the output look better that would be great!  Otherwise we could
        just spruce the output up by adding a few new lines and tabs.
        */
        Writer writer = null;
        try{
            File file = new File(confFile);
            writer = new FileWriter( file );
            confObject.marshal( writer );
        } catch ( IOException e ) {
                handleException("conf.1040",confFile, e.getLocalizedMessage());
        } catch ( MarshalException e ) {
            if (e.getException() instanceof IOException){
                handleException("conf.1040",confFile, e.getLocalizedMessage());
            } else {
                handleException("conf.1050",confFile, e.getLocalizedMessage());
            }
        } catch ( ValidationException e ) {
            /* TODO: Implement informative error handling here. 
               The exception will say "X doesn't match the regular 
               expression Y" 
               This should be checked and more relevant information
               should be given -- not everyone understands regular 
               expressions. 
             */
            /* NOTE: This doesn't seem to ever happen. When the object graph
             * is invalid, the MarshalException is thrown, not this one as you
             * would think.
             */
            handleException("conf.1060",confFile, e.getLocalizedMessage());
        }
        try {
            writer.close();
        } catch ( Exception e ) {
            handleException("file.0020", confFile, e.getLocalizedMessage());
        }
    }
    
    /**
     * Opens the specified jar file, locates the openejb-jar.xml file,
     * unmarshals it to a java object and returns it. If there is no 
     * openejb-jar.xml in the jar an exception will be thrown.
     * 
     * @param jarFile
     * @return 
     * @exception OpenEJBException
     */
    public static OpenejbJar readOpenejbJar(String jarFile) throws OpenEJBException{

        /*[1.1]  Get the jar ***************/
        JarFile jar = getJarFile(jarFile);

        /*[1.2]  Find the openejb-jar.xml from the jar ***************/
        JarEntry entry = jar.getJarEntry("META-INF/openejb-jar.xml");
        if (entry == null) entry = jar.getJarEntry("openejb-jar.xml");
        if (entry == null) handleException("conf.2900", jarFile, "no message");

        /*[1.3]  Get the openejb-jar.xml from the jar ***************/
        Reader reader = null;
        InputStream stream = null;
        try {
            stream = jar.getInputStream(entry);
            reader = new InputStreamReader( stream );
        } catch ( Exception e ) {
            handleException("conf.2110", jarFile, e.getLocalizedMessage());
        }

        /*[1.4]  Get the OpenejbJar from the openejb-jar.xml ***************/
        OpenejbJar obj = null;
        try {
            obj = OpenejbJar.unmarshal(reader);
        } catch ( MarshalException e ) {
            if (e.getException() instanceof IOException){
                handleException("conf.2110", jarFile, e.getLocalizedMessage());
            } else if (e.getException() instanceof UnknownHostException){
                handleException("conf.2121", jarFile, e.getLocalizedMessage());
            } else {
                handleException("conf.2120", jarFile, e.getLocalizedMessage());
            }
        } catch ( ValidationException e ) {
            handleException("conf.2130",jarFile, e.getLocalizedMessage());
        }
        
        /*[1.5]  Clean up ***************/
        try {
            stream.close();
            reader.close();
            jar.close();
        } catch ( Exception e ) {
            handleException("file.0020", jarFile, e.getLocalizedMessage());
        }
        
        return obj;
    }
    
    public static void writeOpenejbJar(String xmlFile, OpenejbJar openejbJarObject) throws OpenEJBException{
        /* TODO:  Just to be picky, the xml file created by
        Castor is really hard to read -- it is all on one line.
        People might want to edit this in the future by hand, so if Castor can 
        make the output look better that would be great!  Otherwise we could
        just spruce the output up by adding a few new lines and tabs.
        */
        Writer writer = null;
        try{
            File file = new File(xmlFile);
            File dirs = file.getParentFile();
            if (dirs != null) dirs.mkdirs();
            writer = new FileWriter( file );
            openejbJarObject.marshal( writer );
        } catch ( SecurityException e ) {
                handleException("conf.2040",xmlFile, e.getLocalizedMessage());
        } catch ( IOException e ) {
                handleException("conf.2040",xmlFile, e.getLocalizedMessage());
        } catch ( MarshalException e ) {
            if (e.getException() instanceof IOException){
                handleException("conf.2040",xmlFile, e.getLocalizedMessage());
            } else {
                handleException("conf.2050",xmlFile, e.getLocalizedMessage());
            }
        } catch ( ValidationException e ) {
            /* TODO: Implement informative error handling here. 
               The exception will say "X doesn't match the regular 
               expression Y" 
               This should be checked and more relevant information
               should be given -- not everyone understands regular 
               expressions. 
             */
            /* NOTE: This doesn't seem to ever happen. When the object graph
             * is invalid, the MarshalException is thrown, not this one as you
             * would think.
             */
            handleException("conf.2060",xmlFile, e.getLocalizedMessage());
        }
        try {
            writer.close();
        } catch ( Exception e ) {
            handleException("file.0020", xmlFile, e.getLocalizedMessage());
        }
    }

    /**
     * Opens the specified jar file, locates the  service-jar.xml file,
     * unmarshals it to a java object and returns it. If there is no 
     * service-jar.xml in the jar an exception will be thrown.
     * 
     * @param jarFile
     * @return 
     * @exception OpenEJBException
     */
    public static ServicesJar readServicesJar(String jarFile) throws OpenEJBException{

        /*[1.1]  Get the jar ***************/
        JarFile jar = getJarFile(jarFile);

        /*[1.2]  Find the service-jar.xml from the jar ***************/
        JarEntry entry = jar.getJarEntry("META-INF/service-jar.xml");
        if (entry == null) entry = jar.getJarEntry("META-INF/default.service-jar.xml");
        if (entry == null) entry = jar.getJarEntry("service-jar.xml");
        if (entry == null) entry = jar.getJarEntry("default.service-jar.xml");
        
        if (entry == null) handleException("conf.4900", jarFile, "no message");

        /*[1.3]  Get the service-jar.xml from the jar ***************/
        Reader reader = null;
        InputStream stream = null;
        try {
            stream = jar.getInputStream(entry);
            reader = new InputStreamReader( stream );
        } catch ( Exception e ) {
            handleException("conf.4110", jarFile, e.getLocalizedMessage());
        }

        /*[1.4]  Get the ServicesJar from the service-jar.xml ***************/
        ServicesJar obj = null;
        try {
            obj = ServicesJar.unmarshal(reader);
        } catch ( MarshalException e ) {
            if (e.getException() instanceof IOException){
                handleException("conf.4110", jarFile, e.getLocalizedMessage());
            } else if (e.getException() instanceof UnknownHostException){
                handleException("conf.4121", jarFile, e.getLocalizedMessage());
            } else {
                handleException("conf.4120", jarFile, e.getLocalizedMessage());
            }
        } catch ( ValidationException e ) {
            handleException("conf.4130",jarFile, e.getLocalizedMessage());
        }
        
        /*[1.5]  Clean up ***************/
        try {
            stream.close();
            reader.close();
            jar.close();
        } catch ( Exception e ) {
            handleException("file.0020", jarFile, e.getLocalizedMessage());
        }
        
        return obj;
    }
    
    public static void writeServicesJar(String xmlFile, ServicesJar servicesJarObject) throws OpenEJBException{
        /* TODO:  Just to be picky, the xml file created by
        Castor is really hard to read -- it is all on one line.
        People might want to edit this in the future by hand, so if Castor can 
        make the output look better that would be great!  Otherwise we could
        just spruce the output up by adding a few new lines and tabs.
        */
        Writer writer = null;
        try{
            File file = new File(xmlFile);
            writer = new FileWriter( file );
            servicesJarObject.marshal( writer );
        } catch ( IOException e ) {
                handleException("conf.4040",xmlFile, e.getLocalizedMessage());
        } catch ( MarshalException e ) {
            if (e.getException() instanceof IOException){
                handleException("conf.4040",xmlFile, e.getLocalizedMessage());
            } else {
                handleException("conf.4050",xmlFile, e.getLocalizedMessage());
            }
        } catch ( ValidationException e ) {
            /* TODO: Implement informative error handling here. 
               The exception will say "X doesn't match the regular 
               expression Y" 
               This should be checked and more relevant information
               should be given -- not everyone understands regular 
               expressions. 
             */
            /* NOTE: This doesn't seem to ever happen. When the object graph
             * is invalid, the MarshalException is thrown, not this one as you
             * would think.
             */
            handleException("conf.4060",xmlFile, e.getLocalizedMessage());
        }
        try {
            writer.close();
        } catch ( Exception e ) {
            handleException("file.0020", xmlFile, e.getLocalizedMessage());
        }
    }



    public static EjbJar readEjbJar(String jarFile) throws OpenEJBException{
        /*[1.1]  Get the jar ***************/
        JarFile jar = getJarFile(jarFile);

        /*[1.2]  Find the ejb-jar.xml from the jar ***************/
        JarEntry entry = jar.getJarEntry("META-INF/ejb-jar.xml");
        if (entry == null) entry = jar.getJarEntry("ejb-jar.xml");
        
        if (entry == null) handleException("conf.3900", jarFile, "no message");

        /*[1.3]  Get the ejb-jar.xml from the jar ***************/
        Reader reader = null;
        InputStream stream = null;
        try {
            stream = jar.getInputStream(entry);
            reader = new InputStreamReader( stream );
        } catch ( Exception e ) {
            handleException("conf.3110", jarFile, e.getLocalizedMessage());
        }

        /*[1.4]  Get the OpenejbJar from the openejb-jar.xml ***************/
        EjbJar obj = null;
        try {
            obj = unmarshalEjbJar(reader);
        } catch ( MarshalException e ) {
            e.printStackTrace();
            if (e.getException() instanceof UnknownHostException){
                handleException("conf.3121", jarFile, e.getLocalizedMessage());
            } else if (e.getException() instanceof IOException){
                handleException("conf.3110", jarFile, e.getLocalizedMessage());
            } else {
                handleException("conf.3120", jarFile, e.getLocalizedMessage());
            }
        } catch ( ValidationException e ) {
            handleException("conf.3130",jarFile, e.getLocalizedMessage());
        }
        
        /*[1.5]  Clean up ***************/
        try {
            stream.close();
            reader.close();
            jar.close();
        } catch ( Exception e ) {
            handleException("file.0020", jarFile, e.getLocalizedMessage());
        }
        
        return obj;
    }
    
    private static DTDResolver resolver = new DTDResolver();
    
    private static EjbJar unmarshalEjbJar(java.io.Reader reader) 
    throws MarshalException, ValidationException {
        Unmarshaller unmarshaller = new Unmarshaller(org.openejb.alt.config.ejb11.EjbJar.class);
        unmarshaller.setEntityResolver(resolver);

        return (org.openejb.alt.config.ejb11.EjbJar)unmarshaller.unmarshal(reader);
    } 

    public static void writeEjbJar(String xmlFile, EjbJar ejbJarObject) throws OpenEJBException{
        /* TODO:  Just to be picky, the xml file created by
        Castor is really hard to read -- it is all on one line.
        People might want to edit this in the future by hand, so if Castor can 
        make the output look better that would be great!  Otherwise we could
        just spruce the output up by adding a few new lines and tabs.
        */
        Writer writer = null;
        try{
            File file = new File(xmlFile);
            writer = new FileWriter( file );
            ejbJarObject.marshal( writer );
        } catch ( IOException e ) {
                handleException("conf.3040",xmlFile, e.getLocalizedMessage());
        } catch ( MarshalException e ) {
            if (e.getException() instanceof IOException){
                handleException("conf.3040",xmlFile, e.getLocalizedMessage());
            } else {
                handleException("conf.3050",xmlFile, e.getLocalizedMessage());
            }
        } catch ( ValidationException e ) {
            /* TODO: Implement informative error handling here. 
               The exception will say "X doesn't match the regular 
               expression Y" 
               This should be checked and more relevant information
               should be given -- not everyone understands regular 
               expressions. 
             */
            /* NOTE: This doesn't seem to ever happen. When the object graph
             * is invalid, the MarshalException is thrown, not this one as you
             * would think.
             */
            handleException("conf.3060",xmlFile, e.getLocalizedMessage());
        }
        try {
            writer.close();
        } catch ( Exception e ) {
            handleException("file.0020", xmlFile, e.getLocalizedMessage());
        }
    }

    public static JarFile getJarFile(String jarFile) throws OpenEJBException{
        /*[1.1]  Get the jar ***************/
        JarFile jar = null;
        try {
            File file = new File(jarFile);
            jar = new JarFile(file);
        } catch ( FileNotFoundException e ) {
            handleException("conf.0001", jarFile, e.getLocalizedMessage());
        } catch ( IOException e ) {
            handleException("conf.0002", jarFile, e.getLocalizedMessage());
        }
        return jar;
    }

    public static void addFileToJar(String jarFile, String file ) throws OpenEJBException{
        ByteArrayOutputStream errorBytes = new ByteArrayOutputStream();
    
        /* NOTE: Sadly, we have to play this little game 
         * with temporarily switching the standard error
         * stream to capture the errors.
         * Although you can pass in an error stream in 
         * the constructor of the jar tool, they are not
         * used when an error occurs.
         */
        PrintStream newErr = new PrintStream(errorBytes);
        PrintStream oldErr = System.err;
        System.setErr(newErr);
    
        sun.tools.jar.Main jarTool = new sun.tools.jar.Main(newErr, newErr, "config_utils");
    
        String[] args = new String[]{"uf",jarFile,file};
        jarTool.run(args);
    
        System.setErr(oldErr);
    
        try{
        errorBytes.close();
        newErr.close();
        } catch (Exception e){
            handleException("file.0020",jarFile, e.getLocalizedMessage());
        }
    
        String error = new String(errorBytes.toByteArray());
        if (error.indexOf("java.io.IOException") != -1) {
            // an IOException was thrown!
            // clean the error message
            int begin = error.indexOf(':')+1;
            int end = error.indexOf('\n');
            String message = error.substring(begin, end);
            handleException("conf.0003", file, jarFile, message);
        }
    
    }
    
    public static Properties assemblePropertiesFor(String confItem, String itemId, String itemContent, String confFile, String jar, ServiceProvider service) throws OpenEJBException{
        Properties props = new Properties();
        
        try {
            /* 
             * 1. Load properties from the properties file referenced
             *    by the service provider 
             */
            if (service.getPropertiesFile() != null) {
                props = loadProperties(service.getPropertiesFile().getFile());
            }
            /* 
             * 2. Load properties from the content in the service provider 
             *    element of the service-jar.xml
             */
            if ( service.getContent() != null ) {
                StringBufferInputStream in = new StringBufferInputStream(service.getContent());
                props = loadProperties(in, props);
            }
        } catch (OpenEJBException ex){
            ConfigUtils.handleException("conf.0013", service.getId(), jar, ex.getLocalizedMessage());
        }

        /* 3. Load properties from the content in the Container 
         *    element of the configuration file.
         */
        try {
            if ( itemContent != null ) {
                StringBufferInputStream in = new StringBufferInputStream(itemContent);
                props = ConfigUtils.loadProperties(in, props);
            }
        } catch (OpenEJBException ex){
            ConfigUtils.handleException("conf.0014", confItem, itemId , confFile, ex.getLocalizedMessage());
        }
        
        return props;
    }
    

    public static Properties loadProperties(String pFile) throws OpenEJBException{
        return loadProperties(pFile, new Properties());
    }

    public static Properties loadProperties(String propertiesFile, Properties defaults) throws OpenEJBException{
        try{
            File pfile = new File( propertiesFile );
            InputStream in = new FileInputStream(pfile);

            return loadProperties(in, defaults);
        } catch (FileNotFoundException ex){
            ConfigUtils.handleException("conf.0006", propertiesFile, ex.getLocalizedMessage());
        } catch (IOException ex){
            ConfigUtils.handleException("conf.0007", propertiesFile, ex.getLocalizedMessage());
        } catch (SecurityException ex){
            ConfigUtils.handleException("conf.0005", propertiesFile, ex.getLocalizedMessage());
        }
        return defaults;    
    }

    public static Properties loadProperties(InputStream in, Properties defaults) throws OpenEJBException{

        try {
            /*
            This may not work as expected.  The desired effect is that
            the load method will read in the properties and overwrite
            the values of any properties that may have previously been
            defined.
            */

            defaults.load(in);
        } catch (IOException ex){
            ConfigUtils.handleException("conf.0012",  ex.getLocalizedMessage());
        }
        return defaults;
    }
        
    /**
     * Search for the config file.
     * 
     * OPENJB_HOME/conf/openejb.conf
     * OPENJB_HOME/conf/default.openejb.conf
     * 
     * @return 
     */
    public static String searchForConfiguration() throws OpenEJBException{
        return searchForConfiguration(null);
    }

    public static String searchForConfiguration(String path) throws OpenEJBException{
        File file = null;
        try{

            /* [1] Try finding the file relative to the 
             *     current working directory
             */
            try{
                file = new File(path);
                if (file != null && file.exists() && file.isFile()) {
                    return file.getAbsolutePath();
                }
            } catch (NullPointerException e){
            }
            
            /* [2] Try finding the file relative to the 
             *     openejb.home directory
             */
            try{
                file = FileUtils.getFile(path);
                if (file != null && file.exists() && file.isFile()) {
                    return file.getAbsolutePath();
                }
            } catch (NullPointerException e){
            } catch (java.io.FileNotFoundException e){
                _logger.warning("Cannot find the configuration file ["+path+"], Using default OPENEJB_HOME/conf/openejb.conf instead.");
            }

            /* [3] Try finding the standard openejb.conf file 
             *     relative to the openejb.home directory
             */
            try{
                file = FileUtils.getFile("conf/openejb.conf");
                if (file != null && file.exists() && file.isFile()) {
                    return file.getAbsolutePath();
                }
            } catch (java.io.FileNotFoundException e){
            }
                        
            /* [4] No config found! Create a config for them
             *     using the default.openejb.conf file from 
             *     the openejb-x.x.x.jar
             */
            //Gets the conf directory, creating it if needed.
            File confDir = FileUtils.getDirectory("conf");
            
            //TODO:1: We cannot find the user's conf file and
            // are taking the liberty of creating one for them.
            // We should log this.                   
            file = createConfig(new File(confDir, "openejb.conf"));
            
        } catch (java.io.IOException e){
            e.printStackTrace();
            throw new OpenEJBException("Could not locate config file: ", e);
        }
        
        /*TODO:2: Check these too.
        * OPENJB_HOME/lib/openejb-x.x.x.jar
        * OPENJB_HOME/dist/openejb-x.x.x.jar
        */
        return (file == null)? null: file.getAbsolutePath() ;
    }

    public static File createConfig(File config) throws java.io.IOException{
        try{
            URL defaultConfig = new URL("resource:/default.openejb.conf");
            InputStream in = defaultConfig.openStream();
            FileOutputStream out = new FileOutputStream(config);

            int b = in.read();

            while (b != -1) {
                out.write(b);
                b = in.read();
            }

            in.close();
            out.close();

        } catch (Exception e){
            e.printStackTrace();
        }

        return config;
    }

    /*------------------------------------------------------*/
    /*    Methods for easy exception handling               */
    /*------------------------------------------------------*/
    public static void handleException(String errorCode, Object arg0, Object arg1, Object arg2, Object arg3) throws OpenEJBException {
        throw new OpenEJBException( messages.format( errorCode, arg0, arg1, arg2, arg3 ) );
    }

    public static void handleException(String errorCode, Object arg0, Object arg1, Object arg2) throws OpenEJBException {
        throw new OpenEJBException( messages.format( errorCode, arg0, arg1, arg2 ) );
    }

    public static void handleException(String errorCode, Object arg0, Object arg1) throws OpenEJBException {
        throw new OpenEJBException( messages.format( errorCode, arg0, arg1 ) );
    }

    public static void handleException(String errorCode, Object arg0) throws OpenEJBException {
        throw new OpenEJBException( messages.format( errorCode, arg0 ) );
    }

    public static void handleException(String errorCode) throws OpenEJBException {
        throw new OpenEJBException( messages.message( errorCode ) );
    }

    /*------------------------------------------------------*/
    /*  Methods for logging exceptions that are noteworthy  */
    /*  but not bad enough to stop the container system.    */
    /*------------------------------------------------------*/
    public static void logWarning(String errorCode, Object arg0, Object arg1, Object arg2, Object arg3 ) {
        _logger.i18n.warning( errorCode, arg0, arg1, arg2, arg3 );
    }

    public static void logWarning(String errorCode, Object arg0, Object arg1, Object arg2 ) {
        _logger.i18n.warning( errorCode, arg0, arg1, arg2 );
    }
    
    public static void logWarning(String errorCode, Object arg0, Object arg1 ) {
        _logger.i18n.warning( errorCode, arg0, arg1 );
    }

    public static void logWarning(String errorCode, Object arg0 ) {
        _logger.i18n.warning( errorCode, arg0 );
    }

    public static void logWarning(String errorCode ) {
        _logger.i18n.warning( errorCode );
    }


}
