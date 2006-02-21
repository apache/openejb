package org.openejb.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Iterator;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ResourceFinder {

    private final String path;
    private final ClassLoader classLoader;

    public ResourceFinder(String path) {
        this(path, Thread.currentThread().getContextClassLoader());
    }

    public ResourceFinder(String path, ClassLoader classLoader) {
        this.path = path;
        this.classLoader = classLoader;
    }


    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Find String
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public String findString(String key) throws IOException {
        String uri = path + key;

        URL resource = classLoader.getResource(uri);
        if (resource == null) {
            throw new IOException("Could not find command in : " + uri);
        }

        return readContents(resource);
    }

    public List findAllStrings(String key) throws IOException {
        String uri = path + key;

        List strings = new ArrayList();

        Enumeration resources = classLoader.getResources(uri);
        while (resources.hasMoreElements()) {
            URL url = (URL) resources.nextElement();
            String string = readContents(url);
            strings.add(string);
        }
        return strings;
    }

    public List findAvailableStrings(String key) throws IOException {
        String uri = path + key;

        List strings = new ArrayList();

        Enumeration resources = classLoader.getResources(uri);
        while (resources.hasMoreElements()) {
            try {
                URL url = (URL) resources.nextElement();
                String string = readContents(url);
                strings.add(string);
            } catch (Exception notAvailable) {
            }
        }
        return strings;
    }

    public Map mapAllStrings(String key) throws IOException {
        Map strings = new HashMap();
        Map resourcesMap = getResourcesMap(key);
        for (Iterator iterator = resourcesMap.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String name = (String) entry.getKey();
            URL url = (URL) entry.getValue();
            String value = readContents(url);
            strings.put(name,value);
        }
        return strings;
    }

    public Map mapAvailableStrings(String key) throws IOException {
        Map strings = new HashMap();
        Map resourcesMap = getResourcesMap(key);
        for (Iterator iterator = resourcesMap.entrySet().iterator(); iterator.hasNext();) {
            try {
                Map.Entry entry = (Map.Entry) iterator.next();
                String name = (String) entry.getKey();
                URL url = (URL) entry.getValue();
                String value = readContents(url);
                strings.put(name,value);
            } catch (Exception notAvailable) {
            }
        }
        return strings;
    }


    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Find Class
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public Class findClass(String key) throws IOException, ClassNotFoundException {
        String className = findString(key);
        Class clazz = classLoader.loadClass(className);
        return clazz;
    }

    public List findAllClasses(String key) throws IOException, ClassNotFoundException {
        List classes = new ArrayList();
        List strings = findAllStrings(key);
        for (int i = 0; i < strings.size(); i++) {
            String className = (String) strings.get(i);
            Class clazz = classLoader.loadClass(className);
            classes.add(clazz);
        }
        return classes;
    }

    public List findAvailableClasses(String key) throws IOException {
        List classes = new ArrayList();
        List strings = findAvailableStrings(key);
        for (int i = 0; i < strings.size(); i++) {
            String className = (String) strings.get(i);
            try {
                Class clazz = classLoader.loadClass(className);
                classes.add(clazz);
            } catch (Exception notAvailable) {
            }
        }
        return classes;
    }

    public Map mapAllClasses(String key) throws IOException, ClassNotFoundException {
        Map classes = new HashMap();
        Map map = mapAllStrings(key);
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String string = (String) entry.getKey();
            String className = (String) entry.getValue();
            Class clazz = classLoader.loadClass(className);
            classes.put(string, clazz);
        }
        return classes;
    }

    public Map mapAvailableClasses(String key) throws IOException {
        Map classes = new HashMap();
        Map map = mapAvailableStrings(key);
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            try {
                Map.Entry entry = (Map.Entry) iterator.next();
                String string = (String) entry.getKey();
                String className = (String) entry.getValue();
                Class clazz = classLoader.loadClass(className);
                classes.put(string, clazz);
            } catch (Exception notAvailable) {
            }
        }
        return classes;
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Find Implementation
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public Class findImplementation(Class interfase) throws IOException, ClassNotFoundException {
        String className = findString(interfase.getName());
        Class impl = classLoader.loadClass(className);
        if (!interfase.isAssignableFrom(impl)) {
            throw new ClassCastException("Class not of type: " + interfase.getName());
        }
        return impl;
    }

    public List findAllImplementations(Class interfase) throws IOException, ClassNotFoundException {
        List implementations = new ArrayList();
        List strings = findAllStrings(interfase.getName());
        for (int i = 0; i < strings.size(); i++) {
            String className = (String) strings.get(i);
            Class impl = classLoader.loadClass(className);
            if (!interfase.isAssignableFrom(impl)) {
                throw new ClassCastException("Class not of type: " + interfase.getName());
            }
            implementations.add(impl);
        }
        return implementations;
    }

    public List findAvailableImplementations(Class interfase) throws IOException {
        List implementations = new ArrayList();
        List strings = findAvailableStrings(interfase.getName());
        for (int i = 0; i < strings.size(); i++) {
            String className = (String) strings.get(i);
            try {
                Class impl = classLoader.loadClass(className);
                if (interfase.isAssignableFrom(impl)) {
                    implementations.add(impl);
                }
            } catch (Exception notAvailable) {
            }
        }
        return implementations;
    }

    public Map mapAllImplementations(Class interfase) throws IOException, ClassNotFoundException {
        Map implementations = new HashMap();
        Map map = mapAllStrings(interfase.getName());
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String string = (String) entry.getKey();
            String className = (String) entry.getValue();
            Class impl = classLoader.loadClass(className);
            if (!interfase.isAssignableFrom(impl)) {
                throw new ClassCastException("Class not of type: " + interfase.getName());
            }
            implementations.put(string, impl);
        }
        return implementations;
    }

    public Map mapAvailableImplementations(Class interfase) throws IOException {
        Map implementations = new HashMap();
        Map map = mapAvailableStrings(interfase.getName());
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            try {
                Map.Entry entry = (Map.Entry) iterator.next();
                String string = (String) entry.getKey();
                String className = (String) entry.getValue();
                Class impl = classLoader.loadClass(className);
                if (interfase.isAssignableFrom(impl)) {
                    implementations.put(string, impl);
                }
            } catch (Exception notAvailable) {
            }
        }
        return implementations;
    }


    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Find Properties
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public Properties findProperties(String key) throws IOException {
        String uri = path + key;

        URL resource = classLoader.getResource(uri);
        if (resource == null) {
            throw new IOException("Could not find command in : " + uri);
        }

        return loadProperties(resource);
    }

    public List findAllProperties(String key) throws IOException {
        String uri = path + key;

        List properties = new ArrayList();

        Enumeration resources = classLoader.getResources(uri);
        while (resources.hasMoreElements()) {
            URL url = (URL) resources.nextElement();
            Properties props = loadProperties(url);
            properties.add(props);
        }
        return properties;
    }

    public List findAvailableProperties(String key) throws IOException {
        String uri = path + key;

        List properties = new ArrayList();

        Enumeration resources = classLoader.getResources(uri);
        while (resources.hasMoreElements()) {
            try {
                URL url = (URL) resources.nextElement();
                Properties props = loadProperties(url);
                properties.add(props);
            } catch (Exception notAvailable) {
            }
        }
        return properties;
    }

    public Map mapAllProperties(String key) throws IOException {
        Map propertiesMap = new HashMap();
        Map map = getResourcesMap(key);
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            Map.Entry entry = (Map.Entry) iterator.next();
            String string = (String) entry.getKey();
            URL url = (URL) entry.getValue();
            Properties properties = loadProperties(url);
            propertiesMap.put(string, properties);
        }
        return propertiesMap;
    }

    public Map mapAvailableProperties(String key) throws IOException {
        Map propertiesMap = new HashMap();
        Map map = getResourcesMap(key);
        for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
            try {
                Map.Entry entry = (Map.Entry) iterator.next();
                String string = (String) entry.getKey();
                URL url = (URL) entry.getValue();
                Properties properties = loadProperties(url);
                propertiesMap.put(string, properties);
            } catch (Exception notAvailable) {
            }
        }
        return propertiesMap;
    }

    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
    //
    //   Map Resources
    //
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *

    public Map getResourcesMap(String key) throws IOException {
        String basePath = path + key;

        if (!basePath.endsWith("/")){
            basePath += "/";
        }
        
        Map resources = new HashMap();
        Enumeration urls = classLoader.getResources(basePath);

        while (urls.hasMoreElements()) {
            URL location = (URL) urls.nextElement();

            try {
                if (location.getProtocol().equals("jar")) {

                    readJarEntries(location, basePath, resources);

                } else if (location.getProtocol().equals("file")) {

                    readDirectoryEntries(location, resources);

                }
            } catch (Exception e) {
            }
        }

        return resources;
    }

    private static void readDirectoryEntries(URL location, Map resources) throws MalformedURLException {
        File dir = new File(location.getPath());
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file = files[i];
                if (!file.isDirectory()){
                    String name = file.getName();
                    URL url = file.toURL();
                    resources.put(name, url);
                }
            }
        }
    }

    private static void readJarEntries(URL location, String basePath, Map resources) throws IOException {
        JarURLConnection conn = (JarURLConnection) location.openConnection();
        JarFile jarfile = conn.getJarFile();

        Enumeration entries = jarfile.entries();
        while (entries != null && entries.hasMoreElements()) {
            JarEntry entry = (JarEntry) entries.nextElement();
            String name = entry.getName();

            if (entry.isDirectory() || !name.startsWith(basePath) || name.length() == basePath.length()) {
                continue;
            }

            name = name.substring(basePath.length());

            if (name.indexOf("/") != -1) {
                continue;
            }

            URL resource = new URL(location, name);
            resources.put(name, resource);
        }
    }

    private Properties loadProperties(URL resource) throws IOException {
        InputStream in = resource.openStream();

        BufferedInputStream reader = null;
        try {
            reader = new BufferedInputStream(in);
            Properties properties = new Properties();
            properties.load(reader);

            return properties;
        } finally {
            try {
                in.close();
                reader.close();
            } catch (Exception e) {
            }
        }
    }

    private String readContents(URL resource) throws IOException {
        InputStream in = resource.openStream();
        BufferedInputStream reader = null;
        StringBuffer sb = new StringBuffer();

        try {
            reader = new BufferedInputStream(in);

            int b = reader.read();
            while (b != -1) {
                sb.append((char) b);
                b = reader.read();
            }

            return sb.toString().trim();
        } finally {
            try {
                in.close();
                reader.close();
            } catch (Exception e) {
            }
        }
    }
}