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
package org.apache.openejb.config;

import java.io.*;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.sys.ServiceProvider;
import org.apache.openejb.config.sys.ServicesJar;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;

/**
 * Utility methods for reading, writing, and  configuring services
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class ServiceUtils {

    public static String defaultProviderURL = "org.apache.openejb";
    private static Map loadedServiceJars = new HashMap();
    private static Messages messages = new Messages("org.apache.openejb");
    private static Logger _logger = Logger.getInstance("OpenEJB", "org.apache.openejb");

    public static ServiceProvider getServiceProvider(Service service) throws OpenEJBException {
        return getServiceProvider(service.getProvider());
    }

    /**
     * org.apache.openejb#Default JDBC Connector
     * Default JDBC Connector
     * org.postgresql#JDBCService
     *
     * @param id
     * @return
     * @throws OpenEJBException
     */
    public static ServiceProvider getServiceProvider(String id) throws OpenEJBException {
        String providerName = null;
        String serviceName = null;
        if (id.indexOf("#") == -1) {
            providerName = defaultProviderURL;
            serviceName = id;
        } else {
            providerName = id.substring(0, id.indexOf("#"));
            serviceName = id.substring(id.indexOf("#") + 1);
        }
        ServiceProvider service = null;
        if (loadedServiceJars.get(providerName) == null) {
            ServicesJar sj = readServicesJar(providerName);
            ServiceProvider[] sp = sj.getServiceProvider();
            HashMap services = new HashMap(sj.getServiceProviderCount());
            for (int i = 0; i < sp.length; i++) {
                services.put(sp[i].getId(), sp[i]);
            }
            loadedServiceJars.put(providerName, services);
            // This may return null if there is no service
            // with the specified name.
            service = (ServiceProvider) services.get(serviceName);
        } else {
            Map provider = (Map) loadedServiceJars.get(providerName);
            service = (ServiceProvider) provider.get(serviceName);
        }
        if (service == null) {
            handleException("conf.4901", serviceName, providerName);
        }
        return service;
    }

    /**
     * Opens the specified jar file, locates the  service-jar.xml file,
     * unmarshals it to a java object and returns it. If there is no
     * service-jar.xml in the jar an exception will be thrown.
     *
     * @return
     * @throws OpenEJBException
     */
    public static ServicesJar readServicesJar(String providerName) throws OpenEJBException {
        String servicejarPath = providerName.replace('.', '/');
        servicejarPath = "resource2:/" + servicejarPath + "/service-jar.xml";
        Reader reader = null;
        InputStream stream = null;
        try {
            stream = new URL(servicejarPath).openConnection().getInputStream();
            reader = new InputStreamReader(stream);
        } catch (Exception e) {
            handleException("conf.4110", servicejarPath, e.getLocalizedMessage());
        }
        /*[1.4]  Get the ServicesJar from the service-jar.xml ***************/
        ServicesJar obj = null;
        try {
            Unmarshaller unmarshaller = new Unmarshaller(ServicesJar.class);
            unmarshaller.setWhitespacePreserve(true);
            obj = (ServicesJar) unmarshaller.unmarshal(reader);
        } catch (MarshalException e) {
            if (e.getException() instanceof IOException) {
                handleException("conf.4110", servicejarPath, e.getLocalizedMessage());
            } else if (e.getException() instanceof UnknownHostException) {
                handleException("conf.4121", servicejarPath, e.getLocalizedMessage());
            } else {
                handleException("conf.4120", providerName, e.getLocalizedMessage());
            }
        } catch (ValidationException e) {
            handleException("conf.4130", providerName, e.getLocalizedMessage());
        }
        /*[1.5]  Clean up ***************/
        try {
            stream.close();
            reader.close();
        } catch (Exception e) {
            handleException("file.0010", servicejarPath, e.getLocalizedMessage());
        }
        return obj;
    }

    public static void writeServicesJar(String xmlFile, ServicesJar servicesJarObject)
            throws OpenEJBException {
        /* TODO:  Just to be picky, the xml file created by
        Castor is really hard to read -- it is all on one line.
        People might want to edit this in the future by hand, so if Castor can 
        make the output look better that would be great!  Otherwise we could
        just spruce the output up by adding a few new lines and tabs.
        */
        Writer writer = null;
        try {
            File file = new File(xmlFile);
            writer = new FileWriter(file);
            servicesJarObject.marshal(writer);
        } catch (IOException e) {
            handleException("conf.4040", xmlFile, e.getLocalizedMessage());
        } catch (MarshalException e) {
            if (e.getException() instanceof IOException) {
                handleException("conf.4040", xmlFile, e.getLocalizedMessage());
            } else {
                handleException("conf.4050", xmlFile, e.getLocalizedMessage());
            }
        } catch (ValidationException e) {
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
            handleException("conf.4060", xmlFile, e.getLocalizedMessage());
        }
        try {
            writer.close();
        } catch (Exception e) {
            handleException("file.0020", xmlFile, e.getLocalizedMessage());
        }
    }

    public static Properties assemblePropertiesFor(String confItem,
                                                   String itemId,
                                                   String itemContent,
                                                   String confFile,
                                                   ServiceProvider service)
            throws OpenEJBException {

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
            if (service.getContent() != null) {
                StringBufferInputStream in = new StringBufferInputStream(service.getContent());
                props = loadProperties(in, props);
            }
        } catch (OpenEJBException ex) {
            ConfigUtils.handleException("conf.0013",
                    service.getId(),
                    null,
                    ex.getLocalizedMessage());
        }
        /* 3. Load properties from the content in the Container 
         *    element of the configuration file.
         */
        try {
            if (itemContent != null) {
                StringBufferInputStream in = new StringBufferInputStream(itemContent);
                props = loadProperties(in, props);
            }
        } catch (OpenEJBException ex) {
            ConfigUtils.handleException("conf.0014",
                    confItem,
                    itemId,
                    confFile,
                    ex.getLocalizedMessage());
        }
        return props;
    }

    public static Properties loadProperties(String pFile) throws OpenEJBException {
        return loadProperties(pFile, new Properties());
    }

    public static Properties loadProperties(String propertiesFile, Properties defaults)
            throws OpenEJBException {
        try {
            File pfile = new File(propertiesFile);
            InputStream in = new FileInputStream(pfile);
            return loadProperties(in, defaults);
        } catch (FileNotFoundException ex) {
            ConfigUtils.handleException("conf.0006", propertiesFile, ex.getLocalizedMessage());
        } catch (IOException ex) {
            ConfigUtils.handleException("conf.0007", propertiesFile, ex.getLocalizedMessage());
        } catch (SecurityException ex) {
            ConfigUtils.handleException("conf.0005", propertiesFile, ex.getLocalizedMessage());
        }
        return defaults;
    }

    public static Properties loadProperties(InputStream in, Properties defaults)
            throws OpenEJBException {
        try {
            /*
            This may not work as expected.  The desired effect is that
            the load method will read in the properties and overwrite
            the values of any properties that may have previously been
            defined.
            */
            defaults.load(in);
        } catch (IOException ex) {
            ConfigUtils.handleException("conf.0012", ex.getLocalizedMessage());
        }
        return defaults;
    }

    /*------------------------------------------------------*/
    /*    Methods for easy exception handling               */
    /*------------------------------------------------------*/
    public static void handleException(String errorCode,
                                       Object arg0,
                                       Object arg1,
                                       Object arg2,
                                       Object arg3)
            throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1, arg2, arg3));
    }

    public static void handleException(String errorCode, Object arg0, Object arg1, Object arg2)
            throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1, arg2));
    }

    public static void handleException(String errorCode, Object arg0, Object arg1)
            throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1));
    }

    public static void handleException(String errorCode, Object arg0) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0));
    }

    public static void handleException(String errorCode) throws OpenEJBException {
        throw new OpenEJBException(messages.message(errorCode));
    }

    /*------------------------------------------------------*/
    /*  Methods for logging exceptions that are noteworthy  */
    /*  but not bad enough to stop the container system.    */
    /*------------------------------------------------------*/
    public static void logWarning(String errorCode,
                                  Object arg0,
                                  Object arg1,
                                  Object arg2,
                                  Object arg3) {
        _logger.i18n.warning(errorCode, arg0, arg1, arg2, arg3);
    }

    public static void logWarning(String errorCode, Object arg0, Object arg1, Object arg2) {
        _logger.i18n.warning(errorCode, arg0, arg1, arg2);
    }

    public static void logWarning(String errorCode, Object arg0, Object arg1) {
        _logger.i18n.warning(errorCode, arg0, arg1);
    }

    public static void logWarning(String errorCode, Object arg0) {
        _logger.i18n.warning(errorCode, arg0);
    }

    public static void logWarning(String errorCode) {
        _logger.i18n.warning(errorCode);
    }
}

