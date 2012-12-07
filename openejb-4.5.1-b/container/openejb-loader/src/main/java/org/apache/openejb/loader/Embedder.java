/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.loader;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * @version $Revision$ $Date$
 */
public class Embedder {
    public static final String ADDITIONAL_LIB_FOLDER = SystemInstance.get().getOptions().get("openejb.additional.lib.folder", "additionallib");

	/**
	 * Represents the name of the class which implements org.apache.openejb.loader.Loader
	 */
    private final String className;
    /**
     * Represents the Class object for the className
     */
    private Class loaderClass;

    public Embedder(String className) {
        this.className = className;
    }
	/**
	 * Loads the Class object for the className.
	 * @return
	 * @throws Exception
	 */
    public Class load() throws Exception {
        if (loaderClass == null) {
            ClassPath classPath = SystemInstance.get().getClassPath();
            ClassLoader classLoader = classPath.getClassLoader();
            try {
                loaderClass = classLoader.loadClass(className);
            } catch (Exception e) {
                loaderClass = forcefulLoad(classPath, classLoader);
            }
        }
        return loaderClass;
    }
    /**
     * Uses reflection to invoke the init(Properties props) method on the loaderClass field
     * @param properties
     * @return
     * @throws Exception
     */
    public Object init(Properties properties) throws Exception {
        Class loaderClass = load();

        try {
            // get the init method
            Method init = loaderClass.getMethod("init", Properties.class);

            // create the instance
            Object instance = loaderClass.newInstance();

            // invoke init method
            Object value = init.invoke(instance, properties);
            return value;
        } catch (NoSuchMethodException e) {
            throw (IllegalStateException) new IllegalStateException("Signatures for Loader are no longer correct.").initCause(e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Error) {
                throw (Error) cause;
            } else {
                throw (Exception) cause;
            }
        }
    }

    private Class forcefulLoad(ClassPath classPath, ClassLoader classLoader) throws Exception {
        try {
            File libsDir;

            String libsPath = SystemInstance.get().getProperty("openejb.libs");
            if (libsPath != null){
                libsDir = new File(libsPath);
            } else {
                checkOpenEjbHome(SystemInstance.get().getHome().getDirectory());
                FileUtils home = SystemInstance.get().getHome();
                libsDir = home.getDirectory("lib");
            }
            classPath.addJarsToPath(libsDir);
        } catch (Exception e2) {
            throw new Exception("Could not load OpenEJB libraries. Exception: " + e2.getClass().getName() + " " + e2.getMessage());
        }

        try {
            final File additionalLib = SystemInstance.get().getBase().getDirectory(ADDITIONAL_LIB_FOLDER);
            if (additionalLib.exists()){
                classPath.addJarsToPath(additionalLib);
            }
        } catch (Exception e2) {
            throw new Exception("Could not load OpenEJB libraries. Exception: " + e2.getClass().getName() + " " + e2.getMessage());
        }

        try {
            return classLoader.loadClass(className);
        } catch (Exception e2) {
            throw new Exception("Could not load class '"+className+"' after embedding libraries. Exception: " + e2.getClass().getName() + " " + e2.getMessage());
        }
    }

    private String NO_HOME = "The openejb.home is not set.";

    private String BAD_HOME = "Invalid openejb.home: ";

    private String NOT_THERE = "The path specified does not exist.";

    private String NOT_DIRECTORY = "The path specified is not a directory.";

    private String NO_LIBS = "The path specified is not correct, it does not contain any OpenEJB libraries.";

    // TODO: move this part back into the LoaderServlet
    private String INSTRUCTIONS = "Please edit the web.xml of the openejb_loader webapp and set the openejb.home init-param to the full path where OpenEJB is installed.";

    private void checkOpenEjbHome(File openejbHome) throws Exception {
        try {

            String homePath = openejbHome.getAbsolutePath();

            // The openejb.home must exist
            if (!openejbHome.exists())
                handleError(BAD_HOME + homePath, NOT_THERE, INSTRUCTIONS);

            // The openejb.home must be a directory
            if (!openejbHome.isDirectory())
                handleError(BAD_HOME + homePath, NOT_DIRECTORY, INSTRUCTIONS);

            // The openejb.home must contain a 'lib' directory
            File openejbHomeLibs = new File(openejbHome, "lib");
            if (!openejbHomeLibs.exists())
                handleError(BAD_HOME + homePath, NO_LIBS, INSTRUCTIONS);

            // The openejb.home there must be openejb*.jar files in the 'dist'
            // directory
            String[] libs = openejbHomeLibs.list();
            boolean found = false;
            for (int i = 0; i < libs.length && !found; i++) {
                found = (libs[i].startsWith("openejb-") && libs[i].endsWith(".jar"));
            }
            if (!found)
                handleError(BAD_HOME + homePath, NO_LIBS, INSTRUCTIONS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleError(String m1, String m2, String m3) throws Exception {
        System.err.println("--[PLEASE FIX]-------------------------------------");
        System.err.println(m1);
        System.err.println(m2);
        System.err.println(m3);
        System.err.println("---------------------------------------------------");
        throw new Exception(m1 + " " + m2 + " " + m3);
    }

    private void handleError(String m1, String m2) throws Exception {
        System.err.println("--[PLEASE FIX]-------------------------------------");
        System.err.println(m1);
        System.err.println(m2);
        System.err.println("---------------------------------------------------");
        throw new Exception(m1 + " " + m2);
    }

}
