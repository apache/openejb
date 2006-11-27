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
package org.apache.openejb.config;

import java.io.*;
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.ejb11.EjbJar;
import org.apache.openejb.config.ejb11.EnterpriseBeansItem;
import org.apache.openejb.config.sys.Container;
import org.apache.openejb.util.FileUtils;
import org.apache.openejb.util.JarUtils;
import org.apache.openejb.util.Messages;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class EjbJarUtils {

    static protected Messages _messages = new Messages("org.apache.openejb.util.resources");

    public static EjbJar readEjbJar(String jarFile) throws OpenEJBException {
        /*[1.1]  Get the jar ***************/
        JarFile jar = JarUtils.getJarFile(jarFile);

        /*[1.2]  Find the ejb-jar.xml from the jar ***************/
        JarEntry entry = jar.getJarEntry("META-INF/ejb-jar.xml");
        if (entry == null) entry = jar.getJarEntry("ejb-jar.xml");

        if (entry == null) handleException("conf.3900", jarFile, "no message");

        /*[1.3]  Get the ejb-jar.xml from the jar ***************/
        Reader reader = null;
        InputStream stream = null;
        try {
            stream = jar.getInputStream(entry);
            reader = new InputStreamReader(stream);
        } catch (Exception e) {
            handleException("conf.3110", jarFile, e.getLocalizedMessage());
        }

        /*[1.4]  Get the OpenejbJar from the openejb-jar.xml ***************/
        EjbJar obj = null;
        try {
            obj = unmarshalEjbJar(reader);
        } catch (MarshalException e) {
            if (e.getException() instanceof UnknownHostException) {
                handleException("conf.3121", jarFile, e.getLocalizedMessage());
            } else if (e.getException() instanceof org.xml.sax.SAXException) {
                handleException("conf.3140", jarFile, e.getLocalizedMessage());
            } else if (e.getException() instanceof IOException) {
                handleException("conf.3110", jarFile, e.getLocalizedMessage());
            } else {
                handleException("conf.3120", jarFile, e.getLocalizedMessage());
            }
        } catch (ValidationException e) {
            handleException("conf.3130", jarFile, e.getLocalizedMessage());
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

    private static DTDResolver resolver = new DTDResolver();

    private static EjbJar unmarshalEjbJar(java.io.Reader reader)
            throws MarshalException, ValidationException {
        Unmarshaller unmarshaller = new Unmarshaller(org.apache.openejb.config.ejb11.EjbJar.class);
        unmarshaller.setEntityResolver(resolver);

        return (org.apache.openejb.config.ejb11.EjbJar) unmarshaller.unmarshal(reader);
    }

    public static void writeEjbJar(String xmlFile, EjbJar ejbJarObject) throws OpenEJBException {
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
            ejbJarObject.marshal(writer);
        } catch (IOException e) {
            handleException("conf.3040", xmlFile, e.getLocalizedMessage());
        } catch (MarshalException e) {
            if (e.getException() instanceof IOException) {
                handleException("conf.3040", xmlFile, e.getLocalizedMessage());
            } else {
                handleException("conf.3050", xmlFile, e.getLocalizedMessage());
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
            handleException("conf.3060", xmlFile, e.getLocalizedMessage());
        }
        try {
            writer.close();
        } catch (Exception e) {
            handleException("file.0020", xmlFile, e.getLocalizedMessage());
        }
    }

    public static String moveJar(String jar, boolean overwrite) throws OpenEJBException {
        File origFile = new File(jar);
        
        // Safety checks
        if (!origFile.exists()) {
            handleException("deploy.m.010", origFile.getAbsolutePath());
        }

        if (origFile.isDirectory()) {
            handleException("deploy.m.020", origFile.getAbsolutePath());
        }

        if (!origFile.isFile()) {
            handleException("deploy.m.030", origFile.getAbsolutePath());
        }

        // Move file
        String jarName = origFile.getName();
        File beansDir = null;
        try {
            beansDir = FileUtils.getBase().getDirectory("beans");
        } catch (java.io.IOException ioe) {
            handleException("deploy.m.040", origFile.getAbsolutePath(), ioe.getMessage());
        }

        File newFile = new File(beansDir, jarName);
        boolean moved = false;

        try {
            if (newFile.exists()) {
                if (overwrite) {
                    newFile.delete();
                } else {
                    throw new OpenEJBException(_messages.format("deploy.m.061", origFile.getAbsolutePath(), beansDir.getAbsolutePath()));
                }
            }
            moved = origFile.renameTo(newFile);
        } catch (SecurityException se) {
            handleException("deploy.m.050", origFile.getAbsolutePath(), se.getMessage());
        }

        if (!moved) {
            handleException("deploy.m.060", origFile.getAbsolutePath(), newFile.getAbsoluteFile());
        }
        return newFile.getAbsolutePath();
    }

    public static String copyJar(String jar, boolean overwrite) throws OpenEJBException {
        File origFile = new File(jar);
        
        // Safety checks
        if (!origFile.exists()) {
            handleException("deploy.c.010", origFile.getAbsolutePath());
            return jar;
        }

        if (origFile.isDirectory()) {
            handleException("deploy.c.020", origFile.getAbsolutePath());
            return jar;
        }

        if (!origFile.isFile()) {
            handleException("deploy.c.030", origFile.getAbsolutePath());
            return jar;
        }

        // Move file
        String jarName = origFile.getName();
        File beansDir = null;
        try {
            beansDir = FileUtils.getBase().getDirectory("beans");
        } catch (java.io.IOException ioe) {
            handleException("deploy.c.040", origFile.getAbsolutePath(), ioe.getMessage());
            return jar;
        }

        File newFile = new File(beansDir, jarName);

        try {
            if (newFile.exists()) {
                if (overwrite) {
                    newFile.delete();
                } else {
                    throw new OpenEJBException(_messages.format("deploy.c.061", origFile.getAbsolutePath(), beansDir.getAbsolutePath()));
                }
            }

            FileInputStream in = new FileInputStream(origFile);
            FileOutputStream out = new FileOutputStream(newFile);

            int b = in.read();
            while (b != -1) {
                out.write(b);
                b = in.read();
            }

            in.close();
            out.close();

        } catch (SecurityException e) {
            handleException("deploy.c.050", origFile.getAbsolutePath(), beansDir.getAbsolutePath(), e.getMessage());
        } catch (IOException e) {
            handleException("deploy.c.060", origFile.getAbsolutePath(), newFile.getAbsolutePath(), e.getClass().getName(), e.getMessage());
        }

        return newFile.getAbsolutePath();
    }

    public static Container[] getUsableContainers(Container[] containers, Bean bean) {
        Vector c = new Vector();

        for (int i = 0; i < containers.length; i++) {
            if (containers[i].getCtype().equals(bean.getType())) {
                c.add(containers[i]);
            }
        }

        Container[] useableContainers = new Container[c.size()];
        c.copyInto(useableContainers);

        return useableContainers;
    }

    /*------------------------------------------------------*/
    /*    Methods for collecting beans                      */
    /*------------------------------------------------------*/
    public static Bean[] getBeans(EjbJar jar) {
        EnterpriseBeansItem[] items = jar.getEnterpriseBeans().getEnterpriseBeansItem();
        Bean[] beans = new Bean[items.length];
        for (int i = 0; i < items.length; i++) {
            if (items[i].getEntity() == null) {
                beans[i] = new SessionBean(items[i].getSession());
            } else {
                beans[i] = new EntityBean(items[i].getEntity());
            }
        }
        return beans;
    }

    /*------------------------------------------------------*/
    /*    Methods for easy exception handling               */
    /*------------------------------------------------------*/
    public static void handleException(String errorCode, Object arg0, Object arg1, Object arg2, Object arg3) throws OpenEJBException {
        throw new OpenEJBException(_messages.format(errorCode, arg0, arg1, arg2, arg3));
    }

    public static void handleException(String errorCode, Object arg0, Object arg1, Object arg2) throws OpenEJBException {
        throw new OpenEJBException(_messages.format(errorCode, arg0, arg1, arg2));
    }

    public static void handleException(String errorCode, Object arg0, Object arg1) throws OpenEJBException {
        throw new OpenEJBException(_messages.format(errorCode, arg0, arg1));
    }

    public static void handleException(String errorCode, Object arg0) throws OpenEJBException {
        throw new OpenEJBException(_messages.format(errorCode, arg0));
    }

    public static void handleException(String errorCode) throws OpenEJBException {
        throw new OpenEJBException(_messages.message(errorCode));
    }

}
