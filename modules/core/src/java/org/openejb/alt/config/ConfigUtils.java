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
 *    please contact dev@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://www.openejb.org/).
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

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.openejb.OpenEJBException;
import org.openejb.alt.config.sys.Deployments;
import org.openejb.alt.config.sys.Openejb;
import org.openejb.loader.SystemInstance;
import org.openejb.util.Logger;
import org.openejb.util.Messages;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Utility methods for reading and writing config files
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class ConfigUtils {

    public static Messages messages = new Messages("org.openejb.util.resources");
    public static Logger logger = Logger.getInstance("OpenEJB", "org.openejb.util.resources");

    public static Openejb readConfig() throws OpenEJBException {
        return readConfig(searchForConfiguration());
    }

    public static Openejb readConfig(String confFile) throws OpenEJBException {
        File file = new File(confFile);
        return (Openejb) Unmarshaller.unmarshal(Openejb.class, file.getName(), file.getParent());
    }
    /*
     * TODO: Use the java.net.URL instead of java.io.File so configs
     * and jars can be located remotely in the network
     */
    public static Openejb _readConfig(String confFile) throws OpenEJBException {
        Openejb obj = null;
        Reader reader = null;
        try {
            reader = new FileReader(confFile);
            org.exolab.castor.xml.Unmarshaller unmarshaller = new org.exolab.castor.xml.Unmarshaller(Openejb.class);
            unmarshaller.setWhitespacePreserve(true);
            obj = (Openejb) unmarshaller.unmarshal(reader);
        } catch (FileNotFoundException e) {
            throw new OpenEJBException(messages.format("conf.1900", confFile, e.getLocalizedMessage()));
        } catch (MarshalException e) {
            if (e.getCause() instanceof IOException) {
                throw new OpenEJBException(messages.format("conf.1110", confFile, e.getLocalizedMessage()));
            } else if (e.getCause() instanceof UnknownHostException) {
                throw new OpenEJBException(messages.format("conf.1121", confFile, e.getLocalizedMessage()));
            } else {
                throw new OpenEJBException(messages.format("conf.1120", confFile, e.getLocalizedMessage()));
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
            throw new OpenEJBException(messages.format("conf.1130", confFile, e.getLocalizedMessage()));
        }
        try {
            reader.close();
        } catch (Exception e) {
            throw new OpenEJBException(messages.format("file.0020", confFile, e.getLocalizedMessage()));
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
            throw new OpenEJBException(messages.format("conf.1040", confFile, e.getLocalizedMessage()));
        } catch (MarshalException e) {
            if (e.getCause() instanceof IOException) {
                throw new OpenEJBException(messages.format("conf.1040", confFile, e.getLocalizedMessage()));
            } else {
                throw new OpenEJBException(messages.format("conf.1050", confFile, e.getLocalizedMessage()));
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
            throw new OpenEJBException(messages.format("conf.1060", confFile, e.getLocalizedMessage()));
        }
        try {
            writer.close();
        } catch (Exception e) {
            throw new OpenEJBException(messages.format("file.0020", confFile, e.getLocalizedMessage()));
        }
    }

    /**
     * Search for the config file.
     * <p/>
     * OPENJB_HOME/conf/openejb.conf
     * OPENJB_HOME/conf/default.openejb.conf
     *
     * @return String
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
                file = SystemInstance.get().getBase().getFile(path);
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
                file = SystemInstance.get().getHome().getFile(path);
                if (file != null && file.exists() && file.isFile()) {
                    return file.getAbsolutePath();
                }
            } catch (FileNotFoundException ignored) {
            } catch (IOException ignored) {
            }

        }

        logger.warning("Cannot find the configuration file [" + path + "], Trying conf/openejb.conf instead.");

        try {
            /*
             * [4] Try finding the standard openejb.conf file relative to the
             * openejb.base directory
             */
            try {
                file = SystemInstance.get().getBase().getFile("conf/openejb.conf");
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
                file = SystemInstance.get().getHome().getFile("conf/openejb.conf");
                if (file != null && file.exists() && file.isFile()) {
                    return file.getAbsolutePath();
                }
            } catch (java.io.FileNotFoundException e) {
            }

            logger.warning("Cannot find the configuration file [conf/openejb.conf], Creating one.");

            /* [6] No config found! Create a config for them
             *     using the default.openejb.conf file from 
             *     the openejb-x.x.x.jar
             */
            //Gets the conf directory, creating it if needed.
            File confDir = SystemInstance.get().getBase().getDirectory("conf", true);
            
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
        Enumeration enumeration = config.enumerateDeployments();
        File jar = new File(jarLocation);

        /* Check to see if the entry is already listed */
        while (enumeration.hasMoreElements()) {
            Deployments d = (Deployments) enumeration.nextElement();

            if (d.getJar() != null) {
                try {
                    File target = SystemInstance.get().getBase().getFile(d.getJar(), false);
                    
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
                    File target = SystemInstance.get().getBase().getFile(d.getDir(), false);
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
}
