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
 * Copyright 2002 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */

package org.openejb.security;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.*;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.*;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import org.openejb.security.castor.security.*;
import org.openejb.security.castor.securityjar.*;
import org.openejb.util.JarUtils;
import org.openejb.util.Logger;
import org.openejb.util.Messages;
import org.openejb.OpenEJBException;

/*------------------------------------------------------*/
/* Utility method for reading and writing config files  */
/*------------------------------------------------------*/


public class ConfigUtils  {
    private static Messages _messages =  new Messages( "org.openejb.security.util.resources" );
    private static Logger _logger = Logger.getInstance( "OpenEJB.Security", "org.openejb.security.util.resources" );
    
    public static File defaultSecurityJar = null;
    public static String defaultSecurityJarName = "default.security-jar.xml";
    public static String defaultProviderURL = "org.openejb";
    
    protected static HashMap codebases = new HashMap();

    private static Map loadedRealmJars = new HashMap();

    public static File getDefaultSecurityJar() throws OpenEJBException {

        if ( defaultSecurityJar == null ) {
            defaultSecurityJar = JarUtils.getJarContaining( defaultSecurityJarName );
        }
        return defaultSecurityJar;
    }
    
    /**
     *
     * org.openejb#Default JDBC Connector
     * Default JDBC Connector
     * org.postgresql#JDBCService
     *
     * @param id
     * @return
     * @exception OpenEJBException
     */
    public static RealmProvider getRealmProvider( String id ) throws OpenEJBException {

        String providerName  = null;
        String realmTypeName   = null;

        if ( id.indexOf("#") == -1 ) {
            providerName = defaultProviderURL;
            realmTypeName  = id;
        } else {
            providerName = id.substring( 0, id.indexOf("#") );
            realmTypeName  = id.substring( id.indexOf("#")+1 );
        }

        RealmProvider realm = null;

        if ( loadedRealmJars.get(providerName) == null ) {

            SecurityJar sj = readSecurityJar( providerName );
            RealmProvider[] rps = sj.getRealmProvider();

            HashMap realms = new HashMap( rps.length );

            for ( int i=0; i < rps.length; i++ ){
                realms.put( rps[i].getId(), rps[i] );
            }

            loadedRealmJars.put( providerName, realms );

            // This may return null if there is no service
            // with the specified name.
            realm = (RealmProvider) realms.get( realmTypeName );
        } else {
            Map provider = (Map)loadedRealmJars.get( providerName );
            realm = (RealmProvider) provider.get( realmTypeName );
        }

        if (realm == null) {
            handleException( "conf.4901", realmTypeName, providerName );
        }

        return realm;
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
    public static SecurityJar readSecurityJar( String providerName ) throws OpenEJBException {
        String securityJarPath = providerName.replace('.','/');
        securityJarPath = "resource:/"+securityJarPath+"/security-jar.xml";

        Reader reader = null;
        InputStream stream = null;
        try {
            stream = new URL( securityJarPath ).openConnection().getInputStream();
            reader = new InputStreamReader( stream );
        } catch ( Exception e ) {
            handleException( "conf.4110", securityJarPath, e.getLocalizedMessage() );
        }

        /*[1.4]  Get the ServicesJar from the service-jar.xml ***************/
        SecurityJar obj = null;
        try {
            obj = SecurityJar.unmarshal(reader);
        } catch ( MarshalException e ) {
            if ( e.getException() instanceof IOException ) {
                handleException( "conf.4110", securityJarPath, e.getLocalizedMessage() );
            } else if (e.getException() instanceof UnknownHostException){
                handleException( "conf.4121", securityJarPath, e.getLocalizedMessage() );
            } else {
                handleException( "conf.4120", providerName, e.getLocalizedMessage() );
            }
        } catch ( ValidationException e ) {
            handleException( "conf.4130", providerName, e.getLocalizedMessage() );
        }

        /*[1.5]  Clean up ***************/
        try {
            stream.close();
            reader.close();
        } catch ( Exception e ) {
            handleException( "file.0010", securityJarPath, e.getLocalizedMessage() );
        }

        return obj;
    }
    
    public static Properties assemblePropertiesFor( String itemId, String itemContent, String confFile, RealmProvider realmProvider ) throws OpenEJBException {
        Properties props = new Properties();

        try {
            /*
             * 1. Load properties from the content in the service provider
             *    element of the service-jar.xml
             */
            if ( realmProvider.getContent() != null ) {
                StringBufferInputStream in = new StringBufferInputStream( realmProvider.getContent() );
                props = loadProperties(in, props);
            }
        } catch ( OpenEJBException ex ) {
            handleException("conf.0013", realmProvider.getId(), null, ex.getLocalizedMessage() );
        }

        /* 2. Load properties from the content in the Container
         *    element of the configuration file.
         */
        try {
            if ( itemContent != null ) {
                StringBufferInputStream in = new StringBufferInputStream( itemContent );
                props = loadProperties( in, props );
            }
        } catch ( OpenEJBException ex ) {
            ConfigUtils.handleException( "conf.0014", itemId , confFile, ex.getLocalizedMessage() );
        }

        return props;
    }

    public static Security readConfig(String confFile) throws OpenEJBException {
        Security obj = null;
        
        try {
            obj = Security.unmarshal( new InputStreamReader( (new URL( confFile )).openStream() ) );
        } catch ( MalformedURLException e ) {
            handleException("conf.MalformedURLException", confFile, e.getLocalizedMessage());
        } catch ( IOException e ) {
            handleException("conf.IOException", confFile, e.getLocalizedMessage());
        } catch ( MarshalException e ) {
            if (e.getException() instanceof IOException){
                handleException("conf.1110", confFile, e.getLocalizedMessage());
            } else if (e.getException() instanceof UnknownHostException){
                handleException("conf.1121", confFile, e.getLocalizedMessage());
            } else {
                handleException("conf.1120", confFile, e.getLocalizedMessage());
            }
        } catch ( ValidationException e ) {
            /*
              NOTE: This doesn't seem to ever happen, anyone know why?
            */
            handleException("conf.1130",confFile, e.getLocalizedMessage());
        }
        return obj;
    }
    
    
    public static void writeConfig(String confFile, Security confObject) throws OpenEJBException{
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

    
        
    public static java.util.jar.JarFile getJarFile(String jarFile) throws OpenEJBException {
        /*[1.1]  Get the jar ***************/
        java.util.jar.JarFile jar = null;
        try {
            File file = new File(jarFile);
            jar = new java.util.jar.JarFile(file);
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
    
    
    public static Properties assemblePropertiesFor(String confItem, String itemId, String itemContent, String confFile, String jar, RealmProvider service) throws OpenEJBException {
        Properties props = new Properties();
        
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

    /*------------------------------------------------------*/
    /*    Methods for easy exception handling               */
    /*------------------------------------------------------*/
    public static void handleException(String errorCode, Object arg0, Object arg1, Object arg2, Object arg3 ) throws OpenEJBException{
        throw new OpenEJBException( _messages.format(  errorCode, arg0, arg1, arg2, arg3 ) );
    }

    public static void handleException(String errorCode, Object arg0, Object arg1, Object arg2 ) throws OpenEJBException{
        throw new OpenEJBException( _messages.format(  errorCode, arg0, arg1, arg2 ) );
    }
    
    public static void handleException(String errorCode, Object arg0, Object arg1 ) throws OpenEJBException{
        throw new OpenEJBException( _messages.format(  errorCode, arg0, arg1 ) );
    }

    public static void handleException(String errorCode, Object arg0 ) throws OpenEJBException{
        throw new OpenEJBException( _messages.format(  errorCode, arg0 ) );
    }
    
    public static void handleException(String errorCode ) throws OpenEJBException{
        throw new OpenEJBException( _messages.message(  errorCode ) );
    }

}
