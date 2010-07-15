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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * @version $Rev$ $Date$
 */
public class Classes {

    private static final HashMap<String, Class> primitives = new HashMap();
    static {
        Classes.primitives.put("boolean", boolean.class);
        Classes.primitives.put("byte", byte.class);
        Classes.primitives.put("char", char.class);
        Classes.primitives.put("short", short.class);
        Classes.primitives.put("int", int.class);
        Classes.primitives.put("long", long.class);
        Classes.primitives.put("float", float.class);
        Classes.primitives.put("double", double.class);
    }

    public static Class forName(String string, ClassLoader classLoader) throws ClassNotFoundException {
        int arrayDimentions = 0;
        while (string.endsWith("[]")){
            string = string.substring(0, string.length() - 2);
            arrayDimentions++;
        }

        Class clazz = primitives.get(string);

        if (clazz == null) clazz = Class.forName(string, true, classLoader);

        if (arrayDimentions == 0){
            return clazz;
        }
        return Array.newInstance(clazz, new int[arrayDimentions]).getClass();
    }

    public static String packageName(Class clazz){
        return packageName(clazz.getName());
    }

    public static String packageName(String clazzName){
        int i = clazzName.lastIndexOf('.');
        if (i > 0){
            return clazzName.substring(0, i);
        } else {
            return "";
        }
    }

    public static List<String> getSimpleNames(Class... classes){
        List<String> list = new ArrayList<String>();
        for (Class aClass : classes) {
            list.add(aClass.getSimpleName());
        }

        return list;
    }
    
    
    public static List<String> getClassesFromUrls(Set<URL> locations, String markerName, String appendPackageName) throws IOException{
	
        if(locations == null || locations.size() == 0) return Collections.emptyList();
        
        List<String> classNames = new ArrayList<String>();
        for (URL location : locations) {
            try {
                if (location.getProtocol().equals("jar")) {
                    classNames.addAll(jar(location));
                } else if (location.getProtocol().equals("file")) {
                    try {
                        // See if it's actually a jar
                        URL jarUrl = new URL("jar", "", location.toExternalForm() + "!/");
                        JarURLConnection juc = (JarURLConnection) jarUrl.openConnection();
                        juc.getJarFile();
                        classNames.addAll(jar(jarUrl));
                    } catch (IOException e) {
                        classNames.addAll(file(location, markerName));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        List<String> newStrs = new ArrayList<String>();
        
        if(appendPackageName != null){
            for(String classN : classNames){
                StringBuilder builder = new StringBuilder(appendPackageName);
                builder.append(".").append(classN);
                newStrs.add(builder.toString());
            }
            
            return newStrs;
        }
                
        return classNames;
    }
    
    private static List<String> file(URL location, String marker) {
	
	URL newLoc = null;
	if(location.getPath().contains(marker)){
	    int index = location.getPath().lastIndexOf(marker);
	    try {
		newLoc = new File(location.getPath().substring(0,index)).toURI().toURL();
	    } catch (MalformedURLException e) {
		newLoc = location;
	    }
	    
	}
	else{
	    newLoc = location;
	}
	
        List<String> classNames = new ArrayList<String>();
        File dir = new File(URLDecoder.decode(newLoc.getPath()));
        if (dir.getName().equals("META-INF")) {
                dir = dir.getParentFile(); // Scrape "META-INF" off
        }
        if (dir.isDirectory()) {
            scanDir(dir, classNames, "");
        }
        return classNames;
    }
    
    private static void scanDir(File dir, List<String> classNames, String packageName) {
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                scanDir(file, classNames, packageName + file.getName() + ".");
            } else if (file.getName().endsWith(".class")) {
                String name = file.getName();
                name = name.replaceFirst(".class$", "");
                if (name.contains(".")) continue;
                classNames.add(packageName + name);
            }
        }
    }
    
    
    
    public static List<String> getClassesFromUrl(URL location) throws IOException{
        return jar(location);
    }
    
    private static List<String> jar(URL location) throws IOException {
        String jarPath = location.getFile();
        if (jarPath.indexOf("!") > -1){
            jarPath = jarPath.substring(0, jarPath.indexOf("!"));
        }
        URL url = new URL(jarPath);
        InputStream in = url.openStream();
        try {
            JarInputStream jarStream = new JarInputStream(in);
            return jar(jarStream);
        } finally {
            in.close();
        }
    }
    
    private static List<String> jar(JarInputStream jarStream) throws IOException {
        List<String> classNames = new ArrayList<String>();

        JarEntry entry;
        while ((entry = jarStream.getNextJarEntry()) != null) {
            if (entry.isDirectory() || !entry.getName().endsWith(".class")) {
                continue;
            }
            String className = entry.getName();
            className = className.replaceFirst(".class$", "");
            if (className.contains(".")) continue;
            className = className.replace('/', '.');
            classNames.add(className);
        }

        return classNames;
    }
    
    
}
