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
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;

/**
 * Works around deficencies in Sun's URLClassLoader implementation.
 * Unfortunately, the URLClassLoader doesn't like it when the original
 * JAR file changes, and reportedly on Windows it keeps the JAR file
 * locked too.  As well, it seems that you can't make a URLClassLoader
 * using URLs from Resources in a previous URLClassLoader.  So this
 * ClassLoader loads the contents of the JAR(s) into memory immediately
 * and then releases the files.  The classes are flushed as they are used,
 * but other files stay in memory permanently. Note that you cannot
 * acquire a class file as a resource (URL or stream).
 *
 * <p><font color="red"><b>Warning:</b></font> URLs for this are not
 * yet implemented!  You cannot call getResource() or getResources()!</p>
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class MemoryClassLoader extends ClassLoader {
    private final static int BUFFER_SIZE = 1024;
    private HashMap classes = new HashMap();
    private HashMap others = new HashMap();

    public MemoryClassLoader(ClassLoader parent, JarFile file) {
        this(parent, new JarFile[]{file});
    }

    public MemoryClassLoader(ClassLoader parent, JarFile[] file) {
        super(parent);
        for(int i=0; i<file.length; i++) {
            addJar(file[i]);
            try {
                file[i].close();
            } catch(IOException e) {}
        }
    }

    /**
     * Note that you must close the stream after the constructor
     * returns, in case it is itself a JarInputStream or something.
     */
    public MemoryClassLoader(ClassLoader parent, JarInputStream stream) {
        this(parent, new JarInputStream[]{stream});
    }
    /**
     * Note that you must close the streams after the constructor
     * returns, in case they are also from a JarInputStream or something.
     */
    public MemoryClassLoader(ClassLoader parent, JarInputStream[] stream) {
        super(parent);
        for(int i=0; i<stream.length; i++) {
            addJar(stream[i]);
        }
    }

/* ********** ClassLoader Overrides ********** */
    public InputStream getResourceAsStream(String name) {
        InputStream stream = getParent().getResourceAsStream(name);
        if(stream == null) {
            byte[] buf = (byte[])others.get(name);
            if(buf != null) {
                stream = new ByteArrayInputStream(buf);
            }
        }
        return stream;
    }

    public URL getResource(String name) {
        throw new Error("Not Yet Implemented!");
        // AMM - FIXME
        // Must write a new URL protocol handler, and
        // register the package prefix with the correct
        // system property (see java.net.URL)
    }

    protected Enumeration findResources(String name) throws IOException {
        throw new Error("Not Yet Implemented!");
        // AMM - FIXME
    }

    public boolean equals(Object o) {
        if(o instanceof MemoryClassLoader) {
            return ((MemoryClassLoader)o).getParent() == getParent();
        }
        return false;
    }

    public int hashCode() {
        return getParent().hashCode();
    }

    public Class findClass(String name) throws ClassNotFoundException {
        byte[] data = findClassData(name);
        if(data != null) {
            return defineClass(name, data, 0, data.length);
        } else {
            throw new ClassNotFoundException();
        }
    }

/* ******** End ClassLoader Overrides ******** */

    /**
     * Adds a new JAR to this ClassLoader.  This may be called at any time.
     */
    public void addJar(JarFile jar) {
        Enumeration entries = jar.entries();
        while(entries.hasMoreElements()) {
            JarEntry entry = (JarEntry)entries.nextElement();
            if(entry.getName().endsWith(".class")) {
                try {
                    addClassFile(jar, entry);
                } catch(IOException e) {e.printStackTrace();}
            } else {
                try {
                    addOtherFile(jar, entry);
                } catch(IOException e) {e.printStackTrace();}
            }
        }
    }

    /**
     * Adds a new JAR to this ClassLoader.  This may be called at any time.
     */
    public void addJar(JarInputStream stream) {
        byte[] buf = new byte[BUFFER_SIZE];
        int count;
        try {
            while(true) {
                JarEntry entry = stream.getNextJarEntry();
                if(entry == null)
                    break;
                String name = entry.getName();
                int size = (int)entry.getSize();
                ByteArrayOutputStream out =
                    size >= 0 ? new ByteArrayOutputStream(size)
                              : new ByteArrayOutputStream(BUFFER_SIZE);
                while((count = stream.read(buf)) > -1)
                    out.write(buf, 0, count);
                out.close();
                if(name.endsWith(".class")) {
                    classes.put(getClassName(name), out.toByteArray());
                } else {
                    others.put(name, out.toByteArray());
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    private byte[] findClassData(String name) {
        return (byte[])classes.remove(name);
    }

    private void addClassFile(JarFile jar, JarEntry entry) throws IOException {
        classes.put(getClassName(entry.getName()), getFileBytes(jar, entry));
    }

    private void addOtherFile(JarFile jar, JarEntry entry) throws IOException {
       others.put(entry.getName(), getFileBytes(jar, entry));
    }

    private static String getClassName(String fileName) {
        return fileName.substring(0, fileName.length()-6).replace('/','.');
    }

    private static byte[] getFileBytes(JarFile jar, JarEntry entry) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream((int)entry.getSize());
        byte[] buf = new byte[BUFFER_SIZE];
        BufferedInputStream in = new BufferedInputStream(jar.getInputStream(entry));
        int count;
        while((count = in.read(buf)) > -1)
            stream.write(buf, 0, count);
        in.close();
        stream.close();

        return stream.toByteArray();
    }
}