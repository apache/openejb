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
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */

package org.openejb.loader;

import java.util.Properties;
import java.util.HashMap;

import org.openejb.util.FileUtils;
import org.openejb.util.ClasspathUtils;

/**
 * This class aims to be the one and only static in the entire system
 * A static, singleton, instance of this class can be created with the init(props) method
 *
 * It is assumed that only one singleton per classloader is possible in any given VM
 * Thus loading this instance in a classloader will mean there can only be one OpenEJB
 * instance for that classloader and all children classloaders.
 *
 * @version $Revision$ $Date$
 */
public class SystemInstance {

    private final long startTime = System.currentTimeMillis();
    private final Properties properties;
    private final FileUtils home;
    private final FileUtils base;
    private ClassLoader classLoader;
    private final HashMap components;
    private final ClasspathUtils.Loader loader;

    private SystemInstance(Properties properties) throws Exception {
        this.properties = properties;
        this.home = new FileUtils("openejb.home", "user.dir", properties);
        this.base = new FileUtils("openejb.base", "openejb.home", properties);
        this.components = new HashMap();
        loader = ClasspathUtils.LoaderFactory.createLoader(properties.getProperty("openejb.loader", "context"));
    }


    public long getStartTime() {
        return startTime;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public Object setProperty(String key, String value) {
        return properties.setProperty(key, value);
    }

    public FileUtils getHome() {
        return home;
    }

    public FileUtils getBase() {
        return base;
    }

    public ClasspathUtils.Loader getLoader() {
        return loader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public Object getObject(String name) {
        return components.get(name);
    }

    public Object setObject(String name, Object value) {
        return components.put(name, value);
    }

    //----------------------------------------------------//
    // Static uglyness
    //----------------------------------------------------//

    private static SystemInstance system;
    static {
        try {
            system = new SystemInstance(System.getProperties());
        } catch (Exception e) {
            throw new RuntimeException("Failed to create default instance of SystemInstance",e);
        }
    }

    public static void init(Properties properties) throws Exception{
        system = new SystemInstance(properties);
    }

    public static SystemInstance get(){
        return system;
    }

}
