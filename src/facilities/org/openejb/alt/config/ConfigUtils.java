package org.openejb.alt.config;

import java.io.*;
import java.net.*;
import java.util.Enumeration;
import java.util.*;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.*;
import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.*;
import org.openejb.alt.config.ejb11.*;
import org.openejb.alt.config.sys.*;
import org.openejb.util.FileUtils;

/*------------------------------------------------------*/
/* Utility method for reading and writing config files  */
/*------------------------------------------------------*/


public class ConfigUtils  {
    
    private static Map loadedServiceJars = new HashMap();

    private static Category logger = Category.getInstance("OpenEJB");

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

    public static Openejb readConfig(String confFileURL) throws OpenEJBException {
        Openejb obj = null;
        URL url = null;
        InputStream strm = null;
        try {
            url = new URL(confFileURL);
            strm = url.openConnection().getInputStream();
            obj = Openejb.unmarshal(new InputStreamReader(strm));

            /*
             * If we loaded the configuration from a jar, either from a jar:
             * URL or a resource: URL, we must strip off the config file location
             * from the URL.
             */
            String jarURL = null;
            URL realURL = null;
            if ( url.getProtocol().compareTo("jar") == 0 ) {
                realURL = url;
            } else if ( url.getProtocol().compareTo("resource") == 0 ) {
                realURL = ClassLoader.getSystemResource( url.getFile().substring( 1 ) );
            }
            if ( realURL != null  ) {
                jarURL = realURL.getPath();
                jarURL = jarURL.substring( 0, jarURL.indexOf('!') );
                jarURL = jarURL.substring( 6 );
            }

	    // Need to fill in the default Jars
	    Enumeration enum = obj.enumerateConnector();
            Connector connector;
	    while (enum.hasMoreElements()) {
                connector = (Connector)enum.nextElement();
                if ( connector.getJar() == null ) {
                    if ( jarURL != null ) {
                        connector.setJar(jarURL);
                    } else {
                        handleException( "conf.1411", confFileURL, "Connector", connector.getId() );
                    }
                }
	    }

	    enum = obj.enumerateContainer();
            Container container;
	    while (enum.hasMoreElements()) {
                container = (Container)enum.nextElement();
                if ( container.getJar() == null ) {
                    if ( jarURL != null ) {
                        container.setJar(jarURL);
                    } else {
                        handleException( "conf.1411", confFileURL, "Container", container.getId() );
                    }
                }
	    }

	    enum = obj.enumerateJndiProvider();
            JndiProvider provider;
	    while (enum.hasMoreElements()) {
                provider = (JndiProvider)enum.nextElement();
                if ( provider.getJar() == null ) {
                    if ( jarURL != null ) {
                        provider.setJar(jarURL);
                    } else {
                        handleException( "conf.1411", confFileURL, "JndiProvider", provider.getId() );
                    }
                }
	    }

            ProxyFactory factory = obj.getProxyFactory();
	    if ( factory != null && factory.getJar() == null ) {
                if ( jarURL != null ) {
                    factory.setJar(jarURL);
                } else {
                    handleException( "conf.1411", confFileURL, "ProxyFactory", factory.getId() );
                }
 	    }

            SecurityService security = obj.getSecurityService();
	    if ( security != null && security.getJar() == null ) {
                if ( jarURL != null ) {
                    security.setJar(jarURL);
                } else {
                    handleException( "conf.1411", confFileURL, "SecurityService", security.getId() );
                }
 	    }

            TransactionService transaction = obj.getTransactionService();
	    if ( transaction != null && transaction.getJar() == null ) {
                if ( jarURL != null ) {
                    transaction.setJar(jarURL);
                } else {
                    handleException( "conf.1411", confFileURL, "TransactionService", transaction.getId() );
                }
 	    }

            ConnectionManager manager = obj.getConnectionManager();
	    if ( manager != null && manager.getJar() == null ) {
                if ( jarURL != null ) {
                    manager.setJar(jarURL);
                } else {
                    handleException( "conf.1411", confFileURL, "ConnectionManager", manager.getId() );
                }
 	    }

        } catch ( IOException e ) {
            handleException("conf.1900", confFileURL, e.getLocalizedMessage());
        } catch ( MarshalException e ) {
            if (e.getException() instanceof IOException){
                handleException("conf.1110", confFileURL, e.getLocalizedMessage());
            } else if (e.getException() instanceof UnknownHostException){
                handleException("conf.1121", confFileURL, e.getLocalizedMessage());
            } else {
                handleException("conf.1120", confFileURL, e.getLocalizedMessage());
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
            handleException("conf.1130", confFileURL, e.getLocalizedMessage());
        }
        try {
            strm.close();
        } catch ( Exception e ) {
            handleException("file.0020", confFileURL, e.getLocalizedMessage());
        }
        return obj;
    }
    
    
    public static void writeConfig(String confFileURL, Openejb confObject) throws OpenEJBException{
        /* TODO:  Just to be picky, the xml file created by
        Castor is really hard to read -- it is all on one line.
        People might want to edit this in the future by hand, so if Castor can 
        make the output look better that would be great!  Otherwise we could
        just spruce the output up by adding a few new lines and tabs.
        */
        URL url = null;
        OutputStream strm = null;
        try {
            url = new URL(confFileURL);
            strm = url.openConnection().getOutputStream();
            confObject.marshal(new OutputStreamWriter(strm));
        } catch ( IOException e ) {
                handleException("conf.1040", confFileURL, e.getLocalizedMessage());
        } catch ( MarshalException e ) {
            if (e.getException() instanceof IOException){
                handleException("conf.1040", confFileURL, e.getLocalizedMessage());
            } else {
                handleException("conf.1050", confFileURL, e.getLocalizedMessage());
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
            handleException("conf.1060", confFileURL, e.getLocalizedMessage());
        }
        try {
            strm.close();
        } catch ( Exception e ) {
            handleException("file.0020", confFileURL, e.getLocalizedMessage());
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
            obj = EjbJar.unmarshal(reader);
        } catch ( MarshalException e ) {
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
            handleException("conf.0013", service.getId(), jar, ex.getLocalizedMessage());
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
     * @return URL, in string form, of the config file if it is found
     */
    private static String searchForConfiguration( String configURL ) throws OpenEJBException {
	InputStream strm = null;
	URL url = null;

	try {
	    url = new URL( configURL );
	    strm = url.openConnection().getInputStream();
	} catch (java.io.IOException e) {
	}
        
	return  (strm == null)? null: url.toExternalForm();
    }

    public static Openejb loadAndResolveConfigFiles(String configURL) throws OpenEJBException {
	String configLocation = configURL;
	String defaultConfigLocation = "";

	if (configLocation == null) {
	    try{
		configLocation = System.getProperty("openejb.configuration");
	    } catch (Exception e){}
	}

        if ( configLocation == null ) {
            configLocation = searchForConfiguration( "file://conf/openejb.conf" );
        }
        if ( configLocation == null ) {
            configLocation = searchForConfiguration( "file://conf/default.openejb.conf" );
        }
        if ( configLocation == null ) {
            configLocation = searchForConfiguration( "resource:/openejb.conf" );
        }
	
        defaultConfigLocation = searchForConfiguration( "resource:/default.openejb.conf" );
	if ( defaultConfigLocation == null ) {
	    handleException( "config.noDefaultConfig" );
	}
	
	Openejb openejb = null;
	Openejb defaultOpenejb = null;

	if ( configLocation != null ) {
	    logInfo( "config.usingConfigWithDefault", configLocation, defaultConfigLocation );

	    openejb = readConfig( configLocation );
	    defaultOpenejb = readConfig( defaultConfigLocation );

	    // resolve defaults
	    Enumeration enum =null;
	    HashMap map;

	    Container container;
	    map = new HashMap();
	    enum = openejb.enumerateContainer();
	    while ( enum.hasMoreElements() ) {
		container = (Container)enum.nextElement();

		map.put( container.getId(), container );
	    }
	    enum = defaultOpenejb.enumerateContainer();
	    while ( enum.hasMoreElements() ) {
		container = (Container)enum.nextElement();

		if ( !map.containsKey( container.getId() ) ) {
		    logInfo( "config.addingFromDefault", container.getId(), "Container" );
		    openejb.addContainer( container );
		}
	    }

	    JndiProvider provider;
	    map = new HashMap();
	    enum = openejb.enumerateJndiProvider();
	    while ( enum.hasMoreElements() ) {
		provider = (JndiProvider)enum.nextElement();

		map.put( provider.getId(), provider );
	    }
	    enum = defaultOpenejb.enumerateJndiProvider();
	    while ( enum.hasMoreElements() ) {
		provider = (JndiProvider)enum.nextElement();

		if ( !map.containsKey( provider.getId() ) ) {
		    logInfo( "config.addingFromDefault", provider.getId(), "JndiProvider" );
		    openejb.addJndiProvider( provider );
		}

	    }

	    if ( openejb.getSecurityService() == null ) {
		if ( defaultOpenejb.getSecurityService() == null ) {
		    handleException( "config.defaultServiceMissing", "SecurityService" );
		}
		logInfo( "config.gettingFromDefault", "SecurityService" );
		openejb.setSecurityService( defaultOpenejb.getSecurityService() );
	    }

	    if ( openejb.getTransactionService() == null ) {
		if ( defaultOpenejb.getTransactionService() == null ) {
		    handleException( "config.defaultServiceMissing", "TransactionService" );
		}
		logInfo( "config.gettingFromDefault", "TransactionService" );
		openejb.setTransactionService( defaultOpenejb.getTransactionService() );
	    }

	    if ( openejb.getConnectionManager() == null ) {
		if ( defaultOpenejb.getConnectionManager() == null ) {
		    handleException( "config.defaultServiceMissing", "ConnectionManager" );
		}
		logInfo( "config.gettingFromDefault", "ConnectionManager" );
		openejb.setConnectionManager( defaultOpenejb.getConnectionManager() );
	    }

	    if ( openejb.getProxyFactory() == null ) {
		if ( defaultOpenejb.getProxyFactory() == null ) {
		    handleException( "config.defaultServiceMissing", "ProxyFactory" );
		}
		logInfo( "config.gettingFromDefault", "ProxyFactory" );
		openejb.setProxyFactory( defaultOpenejb.getProxyFactory() );
	    }

	    Connector connector;
	    map = new HashMap();
	    enum = openejb.enumerateConnector();
	    while ( enum.hasMoreElements() ) {
		connector = (Connector)enum.nextElement();

		map.put( connector.getId(), connector );
	    }
	    enum = defaultOpenejb.enumerateConnector();
	    while ( enum.hasMoreElements() ) {
		connector = (Connector)enum.nextElement();

		if ( !map.containsKey( connector.getId() ) ) {
		    logInfo( "config.addingFromDefault", connector.getId(), "Connector" );
		    openejb.addConnector( connector );
		}

	    }

	    Resource resource;
	    map = new HashMap();
	    enum = openejb.enumerateResource();
	    while ( enum.hasMoreElements() ) {
		resource = (Resource)enum.nextElement();

		map.put( resource.getId(), resource );
	    }
	    enum = defaultOpenejb.enumerateResource();
	    while ( enum.hasMoreElements() ) {
		resource = (Resource)enum.nextElement();

		if ( !map.containsKey( resource.getId() ) ) {
		    logInfo( "config.addingFromDefault", resource.getId(), "Resource" );
		    openejb.addResource( resource );
		}

	    }

	    Deployments deployments;
	    map = new HashMap();
	    enum = openejb.enumerateDeployments();
	    while ( enum.hasMoreElements() ) {
		deployments = (Deployments)enum.nextElement();

		map.put( deployments.getDir(), deployments );
	    }
	    enum = defaultOpenejb.enumerateDeployments();
	    while ( enum.hasMoreElements() ) {
		deployments = (Deployments)enum.nextElement();

		if ( !map.containsKey( deployments.getDir() ) ) {
		    logInfo( "config.addingFromDefault", deployments.getDir(), "Deployments" );
		    openejb.addDeployments( deployments );
		}

	    }

	} else {
	    logInfo( "config.usingDefault" );
	    openejb = ConfigUtils.readConfig( defaultConfigLocation );

	    if ( openejb.getSecurityService() == null ) {
		handleException( "config.defaultServiceMissing", "SecurityService" );
	    }

	    if ( openejb.getTransactionService() == null ) {
		handleException( "config.defaultServiceMissing", "TransactionService" );
	    }

	    if ( openejb.getConnectionManager() == null ) {
		handleException( "config.defaultServiceMissing", "ConnectionManager" );
	    }

	    if ( openejb.getProxyFactory() == null ) {
		handleException( "config.defaultServiceMissing", "ProxyFactory" );
	    }
	}

	return openejb;

    }


    /*------------------------------------------------------*/
    /*    Methods for easy exception handling               */
    /*------------------------------------------------------*/
    public static void handleException( String errorCode, Object arg0, Object arg1, Object arg2, Object arg3 ) throws OpenEJBException {
        Object[] args = { arg0, arg1, arg2, arg3 };
        OpenEJBException e =  new OpenEJBException( errorCode, args );
        logger.error( e.getMessage() );
	throw e;
    }

    public static void handleException( String errorCode, Object arg0, Object arg1, Object arg2 ) throws OpenEJBException {
        Object[] args = { arg0, arg1, arg2 };
        OpenEJBException e =  new OpenEJBException( errorCode, args );
        logger.error( e.getMessage() );
	throw e;
    }
    
    public static void handleException( String errorCode, Object arg0, Object arg1 ) throws OpenEJBException {
        Object[] args = { arg0, arg1 };
        OpenEJBException e =  new OpenEJBException( errorCode, args );
        logger.error( e.getMessage() );
	throw e;
    }

    public static void handleException( String errorCode, Object arg0 ) throws OpenEJBException {
        Object[] args = { arg0 };
        OpenEJBException e =  new OpenEJBException( errorCode, args );
        logger.error( e.getMessage() );
	throw e;
    }
    
    public static void handleException( String errorCode ) throws OpenEJBException {
        OpenEJBException e =  new OpenEJBException( errorCode );
        logger.error( e.getMessage() );
	throw e;
    }


    /*------------------------------------------------------*/
    /*  Methods for logging exceptions that are noteworthy  */
    /*  but not bad enough to stop the container system.    */
    /*------------------------------------------------------*/
    public static void logWarning(String errorCode, Object arg0, Object arg1, Object arg2, Object arg3 ) {
        Object[] args = { arg0, arg1, arg2, arg3 };
        OpenEJBException e = new OpenEJBException(errorCode, args);
        logger.warn( e.getMessage() );
    }

    public static void logWarning(String errorCode, Object arg0, Object arg1, Object arg2 ) {
        Object[] args = { arg0, arg1, arg2 };
        OpenEJBException e = new OpenEJBException(errorCode, args);
        logger.warn( e.getMessage() );
    }
    
    public static void logWarning(String errorCode, Object arg0, Object arg1 ) {
        Object[] args = { arg0, arg1 };
        OpenEJBException e = new OpenEJBException(errorCode, args);
        logger.warn( e.getMessage() );
    }

    public static void logWarning(String errorCode, Object arg0 ) {
        Object[] args = { arg0 };
        OpenEJBException e = new OpenEJBException(errorCode, args);
        logger.warn( e.getMessage() );
    }

    public static void logWarning(String errorCode ) {
        OpenEJBException e = new OpenEJBException(errorCode);
        logger.warn( e.getMessage() );
    }

    public static void logInfo(String errorCode, Object arg0, Object arg1, Object arg2, Object arg3 ) {
        Object[] args = { arg0, arg1, arg2, arg3 };
        OpenEJBException e = new OpenEJBException( errorCode, args );
        logger.info( e.getMessage() );
    }

    public static void logInfo(String errorCode, Object arg0, Object arg1, Object arg2 ) {
        Object[] args = { arg0, arg1, arg2 };
        OpenEJBException e = new OpenEJBException( errorCode, args );
        logger.info( e.getMessage() );
    }
    
    public static void logInfo(String errorCode, Object arg0, Object arg1 ) {
        Object[] args = { arg0, arg1 };
        OpenEJBException e = new OpenEJBException( errorCode, args );
        logger.info( e.getMessage() );
    }

    public static void logInfo(String errorCode, Object arg0 ) {
        Object[] args = { arg0 };
        OpenEJBException e = new OpenEJBException( errorCode, args );
        logger.info( e.getMessage() );
    }

    public static void logInfo(String errorCode ) {
        OpenEJBException e = new OpenEJBException( errorCode );
        logger.info( e.getMessage() );
    }


}
