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
import java.net.UnknownHostException;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Unmarshaller;
import org.exolab.castor.xml.ValidationException;
import org.openejb.OpenEJBException;
import org.openejb.config.ejb11.EjbJar;
import org.openejb.config.ejb11.EnterpriseBeansItem;
import org.openejb.config.sys.Container;
import org.openejb.util.FileUtils;
import org.openejb.util.JarUtils;
import org.openejb.util.Messages;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class EjbJarUtils {

    static protected Messages _messages = new Messages("org.openejb.util.resources");

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
        Unmarshaller unmarshaller = new Unmarshaller(org.openejb.config.ejb11.EjbJar.class);
        unmarshaller.setEntityResolver(resolver);

        return (org.openejb.config.ejb11.EjbJar) unmarshaller.unmarshal(reader);
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
