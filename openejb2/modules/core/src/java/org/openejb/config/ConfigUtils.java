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
package org.openejb.config;

import java.io.*;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.openejb.OpenEJBException;
import org.openejb.config.ejb11.OpenejbJar;
import org.openejb.config.sys.Deployments;
import org.openejb.config.sys.Openejb;
import org.openejb.util.FileUtils;
import org.openejb.util.JarUtils;
import org.openejb.util.Logger;
import org.openejb.util.Messages;

/**
 * Utility methods for reading and writing config files
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class ConfigUtils {

    private static Messages messages = new Messages("org.openejb.util.resources");
    private static Logger _logger = Logger.getInstance("OpenEJB", "org.openejb.util.resources");

    public static Openejb readConfig() throws OpenEJBException {
        return readConfig(searchForConfiguration());
    }

    /*
        TODO: Use the java.net.URL instead of java.io.File so configs
        and jars can be located remotely in the network
     */
    public static Openejb readConfig(String confFile) throws OpenEJBException {
        Openejb obj = null;
        Reader reader = null;
        try {
            reader = new FileReader(confFile);
            Unmarshaller unmarshaller = new Unmarshaller(Openejb.class);
            unmarshaller.setWhitespacePreserve(true);
            obj = (Openejb) unmarshaller.unmarshal(reader);
        } catch (FileNotFoundException e) {
            handleException("conf.1900", confFile, e.getLocalizedMessage());
        } catch (MarshalException e) {
            if (e.getException() instanceof IOException) {
                handleException("conf.1110", confFile, e.getLocalizedMessage());
            } else if (e.getException() instanceof UnknownHostException) {
                handleException("conf.1121", confFile, e.getLocalizedMessage());
            } else {
                handleException("conf.1120", confFile, e.getLocalizedMessage());
            }
        } catch (ValidationException e) {
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
            handleException("conf.1130", confFile, e.getLocalizedMessage());
        }
        try {
            reader.close();
        } catch (Exception e) {
            handleException("file.0020", confFile, e.getLocalizedMessage());
        }
        return obj;
    }


    public static void writeConfig(String confFile, Openejb confObject) throws OpenEJBException {
        /* TODO:  Just to be picky, the xml file created by
        Castor is really hard to read -- it is all on one line.
        People might want to edit this in the future by hand, so if Castor can 
        make the output look better that would be great!  Otherwise we could
        just spruce the output up by adding a few new lines and tabs.
        */
        Writer writer = null;
        try {
            File file = new File(confFile);
            writer = new FileWriter(file);
            confObject.marshal(writer);
        } catch (IOException e) {
            handleException("conf.1040", confFile, e.getLocalizedMessage());
        } catch (MarshalException e) {
            if (e.getException() instanceof IOException) {
                handleException("conf.1040", confFile, e.getLocalizedMessage());
            } else {
                handleException("conf.1050", confFile, e.getLocalizedMessage());
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
            handleException("conf.1060", confFile, e.getLocalizedMessage());
        }
        try {
            writer.close();
        } catch (Exception e) {
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
     * @throws OpenEJBException
     */
    public static OpenejbJar readOpenejbJar(String jarFile) throws OpenEJBException {

        /*[1.1]  Get the jar ***************/
        JarFile jar = JarUtils.getJarFile(jarFile);

        /*[1.2]  Find the openejb-jar.xml from the jar ***************/
        JarEntry entry = jar.getJarEntry("META-INF/openejb-jar.xml");
        if (entry == null) entry = jar.getJarEntry("openejb-jar.xml");
//        if (entry == null) handleException("conf.2900", jarFile, "no message");
        if (entry == null) return null;

        /*[1.3]  Get the openejb-jar.xml from the jar ***************/
        Reader reader = null;
        InputStream stream = null;
        try {
            stream = jar.getInputStream(entry);
            reader = new InputStreamReader(stream);
        } catch (Exception e) {
            handleException("conf.2110", jarFile, e.getLocalizedMessage());
        }

        /*[1.4]  Get the OpenejbJar from the openejb-jar.xml ***************/
        OpenejbJar obj = null;
        try {
            Unmarshaller unmarshaller = new Unmarshaller(OpenejbJar.class);
            unmarshaller.setWhitespacePreserve(true);
            obj = (OpenejbJar) unmarshaller.unmarshal(reader);
        } catch (MarshalException e) {
            if (e.getException() instanceof IOException) {
                handleException("conf.2110", jarFile, e.getLocalizedMessage());
            } else if (e.getException() instanceof UnknownHostException) {
                handleException("conf.2121", jarFile, e.getLocalizedMessage());
            } else {
                handleException("conf.2120", jarFile, e.getLocalizedMessage());
            }
        } catch (ValidationException e) {
            handleException("conf.2130", jarFile, e.getLocalizedMessage());
        }
        
        /*[1.5]  Clean up ***************/
        try {
            stream.close();
            reader.close();
            jar.close();
        } catch (Exception e) {
            handleException("file.0020", jarFile, e.getLocalizedMessage());
        }

        return obj;
    }

    public static boolean checkForOpenejbJar(String jarFile) throws OpenEJBException {
        /*[1.1]  Get the jar ***************/
        JarFile jar = JarUtils.getJarFile(jarFile);

        /*[1.2]  Find the openejb-jar.xml from the jar ***************/
        JarEntry entry = jar.getJarEntry("META-INF/openejb-jar.xml");
        if (entry == null) entry = jar.getJarEntry("openejb-jar.xml");
        if (entry == null) return false;

        return true;
    }

    public static void writeOpenejbJar(String xmlFile, OpenejbJar openejbJarObject) throws OpenEJBException {
        /* TODO:  Just to be picky, the xml file created by
        Castor is really hard to read -- it is all on one line.
        People might want to edit this in the future by hand, so if Castor can 
        make the output look better that would be great!  Otherwise we could
        just spruce the output up by adding a few new lines and tabs.
        */
        Writer writer = null;
        try {
            File file = new File(xmlFile);
            File dirs = file.getParentFile();
            if (dirs != null) dirs.mkdirs();
            writer = new FileWriter(file);
            openejbJarObject.marshal(writer);
        } catch (SecurityException e) {
            handleException("conf.2040", xmlFile, e.getLocalizedMessage());
        } catch (IOException e) {
            handleException("conf.2040", xmlFile, e.getLocalizedMessage());
        } catch (MarshalException e) {
            if (e.getException() instanceof IOException) {
                handleException("conf.2040", xmlFile, e.getLocalizedMessage());
            } else {
                handleException("conf.2050", xmlFile, e.getLocalizedMessage());
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
            handleException("conf.2060", xmlFile, e.getLocalizedMessage());
        }
        try {
            writer.close();
        } catch (Exception e) {
            handleException("file.0020", xmlFile, e.getLocalizedMessage());
        }
    }

    /**
     * Search for the config file.
     * <p/>
     * OPENJB_HOME/conf/openejb.conf
     * OPENJB_HOME/conf/default.openejb.conf
     *
     * @return
     */
    public static String searchForConfiguration() throws OpenEJBException {
        return searchForConfiguration(System.getProperty("openejb.configuration"));
    }

    public static String searchForConfiguration(String path) throws OpenEJBException {
        return ConfigUtils.searchForConfiguration(path, System.getProperties());
    }

    public static String searchForConfiguration(String path, Properties props) throws OpenEJBException {
        File file = null;
        if (path != null) {
            /*
             * [1] Try finding the file relative to the current working
             * directory
             */
            file = new File(path);
            if (file != null && file.exists() && file.isFile()) {
                return file.getAbsolutePath();
            }
            
            /*
             * [2] Try finding the file relative to the openejb.base directory
             */
            try {
                file = FileUtils.getBase().getFile(path);
                if (file != null && file.exists() && file.isFile()) {
                    return file.getAbsolutePath();
                }
            } catch (FileNotFoundException ignored) {
            } catch (IOException ignored) {
            }
            
            /*
             * [3] Try finding the file relative to the openejb.home directory
             */
            try {
                file = FileUtils.getHome().getFile(path);
                if (file != null && file.exists() && file.isFile()) {
                    return file.getAbsolutePath();
                }
            } catch (FileNotFoundException ignored) {
            } catch (IOException ignored) {
            }

        }

        _logger.warning("Cannot find the configuration file [" + path + "], Trying conf/openejb.conf instead.");

        try {
            /*
             * [4] Try finding the standard openejb.conf file relative to the
             * openejb.base directory
             */
            try {
                file = FileUtils.getBase().getFile("conf/openejb.conf");
                if (file != null && file.exists() && file.isFile()) {
                    return file.getAbsolutePath();
                }
            } catch (java.io.FileNotFoundException e) {
            }
                        
            /*
             * [5] Try finding the standard openejb.conf file relative to the
             * openejb.home directory
             */
            try {
                file = FileUtils.getHome().getFile("conf/openejb.conf");
                if (file != null && file.exists() && file.isFile()) {
                    return file.getAbsolutePath();
                }
            } catch (java.io.FileNotFoundException e) {
            }

            _logger.warning("Cannot find the configuration file [conf/openejb.conf], Creating one.");

            /* [6] No config found! Create a config for them
             *     using the default.openejb.conf file from 
             *     the openejb-x.x.x.jar
             */
            //Gets the conf directory, creating it if needed.
            File confDir = FileUtils.getBase().getDirectory("conf", true);
            
            //TODO:1: We cannot find the user's conf file and
            // are taking the liberty of creating one for them.
            // We should log this.                   
            file = createConfig(new File(confDir, "openejb.conf"));

        } catch (java.io.IOException e) {
            e.printStackTrace();
            throw new OpenEJBException("Could not locate config file: ", e);
        }
        
        /*TODO:2: Check these too.
        * OPENJB_HOME/lib/openejb-x.x.x.jar
        * OPENJB_HOME/dist/openejb-x.x.x.jar
        */
        return (file == null) ? null : file.getAbsolutePath();
    }

    public static File createConfig(File config) throws java.io.IOException {
        InputStream in = null;
        OutputStream out = null;
        try {
            URL defaultConfig = new URL("resource:/default.openejb.conf");
            in = defaultConfig.openStream();
            out = new FileOutputStream(config);

            int b;
            while ((b = in.read()) != -1) {
                out.write(b);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }

        }
        return config;
    }

    public static boolean addDeploymentEntryToConfig(String jarLocation, Openejb config) {
        Enumeration deployments = config.enumerateDeployments();
        File jar = new File(jarLocation);

        /* Check to see if the entry is already listed */
        while (deployments.hasMoreElements()) {
            Deployments d = (Deployments) deployments.nextElement();

            if (d.getJar() != null) {
                try {
                    File target = FileUtils.getBase().getFile(d.getJar(), false);
                    
                    /* 
                     * If the jar entry is already there, no need 
                     * to add it to the config or go any futher.
                     */
                    if (jar.equals(target)) return false;
                } catch (java.io.IOException e) {
                    /* No handling needed.  If there is a problem
                     * resolving a config file path, it is better to 
                     * just add this jars path explicitly.
                     */
                }
            } else if (d.getDir() != null) {
                try {
                    File target = FileUtils.getBase().getFile(d.getDir(), false);
                    File jarDir = jar.getAbsoluteFile().getParentFile();

                    /* 
                     * If a dir entry is already there, the jar
                     * will be loaded automatically.  No need 
                     * to add it explicitly to the config or go
                     * any futher.
                     */
                    if (jarDir != null && jarDir.equals(target)) return false;
                } catch (java.io.IOException e) {
                    /* No handling needed.  If there is a problem
                     * resolving a config file path, it is better to 
                     * just add this jars path explicitly.
                     */
                }
            }
        }

        /* Create a new Deployments entry */
        Deployments dep = new Deployments();
        dep.setJar(jarLocation);
        config.addDeployments(dep);
        return true;
    }

    /*------------------------------------------------------*/
    /*    Methods for easy exception handling               */
    /*------------------------------------------------------*/
    public static void handleException(String errorCode, Object arg0, Object arg1, Object arg2, Object arg3) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1, arg2, arg3));
    }

    public static void handleException(String errorCode, Object arg0, Object arg1, Object arg2) throws OpenEJBException {
        throw new OpenEJBException(messages.format(errorCode, arg0, arg1, arg2));
    }

    public static void handleException(String errorCode, Object arg0, Object arg1) throws OpenEJBException {
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
    public static void logWarning(String errorCode, Object arg0, Object arg1, Object arg2, Object arg3) {
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
