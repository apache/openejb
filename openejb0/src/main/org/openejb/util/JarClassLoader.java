package org.openejb.util;

import java.net.*;
import java.io.File;

/**
 * A ClassLoader for JAR files on disk.  In the past, URLClassLoader has had
 * problems erroneously caching JAR content and locking files and so on.  If
 * that proves to still be a problem, we'll re-implement it here to avoid
 * that.  But for now, this is a simple extension of URLClassLoader.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class JarClassLoader extends URLClassLoader {
    public JarClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public JarClassLoader(URL[] urls) {
        super(urls);
    }

    public JarClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    public JarClassLoader(File[] files) {
        super(convertFiles(files));
    }

    public JarClassLoader(File[] files, ClassLoader parent) {
        super(convertFiles(files), parent);
    }

    private static URL[] convertFiles(File[] files) {
        URL[] u = new URL[files.length];
        try {
            for(int i=0; i<files.length; u[i] = files[i++].toURL());
        } catch(MalformedURLException e) {
            throw new IllegalArgumentException("Could not process JAR file: "+e.getMessage());
        }
        return u;
    }
}
