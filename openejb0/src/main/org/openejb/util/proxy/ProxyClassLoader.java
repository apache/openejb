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
package org.openejb.util.proxy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.URLStreamHandler;

/**
 * ClassLoader for dynamically generating byte-code and loading classes.
 *
 * @author David Blevins
 */
public class ProxyClassLoader extends URLClassLoader{

    private ProxyByteCodeStreamHandler streamHandler;


    public ProxyClassLoader(){
        super(new URL[]{}, ProxyClassLoader.class.getClassLoader());

        streamHandler = new ProxyByteCodeStreamHandler();
    }

    /**
     * Converts an array of bytes into an instance of class <code>Class</code>.
     * Before the Class can be used it must be resolved.
     * <p>
     * This method assigns a default <code>ProtectionDomain</code> to
     * the newly defined class. The <code>ProtectionDomain</code>
     * contains the set of permissions granted when
     * a call to <code>Policy.getPolicy().getPermissions()</code> is made with
     * a Codesource of <code>null,null</code>. The default domain is
     * created on the first invocation of <code>defineClass</code>, and
     * re-used on subsequent calls.
     * <p>
     * To assign a specific <code>ProtectionDomain</code> to the class,
     * use the <code>defineClass</code> method that takes a
     * <code>ProtectionDomain</code> as one of its arguments.
     *
     * @param	   name the expected name of the class, or <code>null</code>
     *                  if not known, using '.' and not '/' as the separator
     *                  and without a trailing ".class" suffix.
     * @param      b    the bytes that make up the class data. The bytes in
     *             positions <code>off</code> through <code>off+len-1</code>
     *             should have the format of a valid class file as defined
     *             by the
     *             <a href="http://java.sun.com/docs/books/vmspec/">Java
     *             Virtual Machine Specification</a>.
     * @return     the <code>Class</code> object that was created from the
     *             specified class data
     * @exception  ClassFormatError if the data did not contain a valid class
     * @exception  SecurityException if an attempt is made to add this class
     *             to a package that contains classes that were signed by
     *             a different set of certificates then this class, which
     *             is unsigned.
     *
     * @see        ClassLoader#loadClass(java.lang.String, boolean)
     * @see        ClassLoader#resolveClass(java.lang.Class)
     * @see        java.security.ProtectionDomain
     * @see        java.security.Policy
     * @see        java.security.CodeSource
     * @see        java.security.SecureClassLoader
     */
    public Class defineClass( String name, byte[] byteCode ) throws ClassFormatError{
        Class clazz = null;
        try{
            clazz = defineClass(name, byteCode, 0, byteCode.length);
            resolveClass(clazz);
            cacheByteCode(clazz, byteCode);
        } catch (ClassFormatError cfe){
            System.out.println("[Proxy Class Loader] ERROR: "+name);
            throw cfe;
        }
        return clazz;
    }

    /**
     * Finds the resource with the given name. Class loader
     * implementations should override this method to specify where to
     * find resources.
     *
     * Find the byte-code for the specified class.
     *
     * The proxy classes are generated at runtime and are created from a byte
     * array and not a .class file.  The byte array containing the byte-code
     * is discarded by the VM after a Class is created using
     * <CODE>ClassLoader.defineClass(...)</CODE>. Calling
     * Class.getResourceAsStream(proxyClassName) will return null because
     * there is no .class definition in the file system.  In this situation,
     * it is the responsibility of the class loader (ProxyClassLoader) that
     * loaded the class definition to implement ClassLoader.findResource(...)
     * and return a URL that can be used to retreive the proxy class byte-code.
     *
     * @param  name the resource name
     * @return a URL for reading the resource, or <code>null</code>
     *         if the resource could not be found
     * @since  JDK1.2
     * @see java.lang.ClassLoader.defineClass
     * @see java.lang.ClassLoader.getSystemResourceAsStream()
     * @see org.openejb.util.proxy.ProxyClassLoader
     * @see org.openejb.util.proxy.ProxyClassLoader.findResource
     */
    public URL findResource(String name) {
        URL resource = super.findResource(name);
        if (resource != null) return resource;

        if (!name.endsWith(".class")) return null;

        try{
            name = name.substring(0,name.lastIndexOf(".class"));
            byte[] byteCode = getCachedByteCode(name);
            if (byteCode == null || byteCode.length < 1) return null;
            resource = new URL(null,null,0, name, streamHandler);
        } catch (MalformedURLException mue){
            // This exception will never be thrown.
        }
        return resource;
    }





    public void addURL(File directory) throws java.net.MalformedURLException{
        super.addURL(directory.toURL());
    }

    public class ProxyByteCodeStreamHandler extends URLStreamHandler {
        /**
         * Opens a connection to the object referenced by the
         * <code>URL</code> argument.
         * This method should be overridden by a subclass.
         *
         * <p>If for the handler's protocol (such as HTTP or JAR), there
         * exists a public, specialized URLConnection subclass belonging
         * to one of the following packages or one of their subpackages:
         * java.lang, java.io, java.util, java.net, the connection
         * returned will be of that subclass. For example, for HTTP an
         * HttpURLConnection will be returned, and for JAR a
         * JarURLConnection will be returned.
         *
         * @param      u   the URL that this connects to.
         * @return     a <code>URLConnection</code> object for the <code>URL</code>.
         * @exception  IOException  if an I/O error occurs while opening the
         *               connection.
         */
        protected URLConnection openConnection(URL u) throws IOException {
            byte[] byteCode = getCachedByteCode(u.getFile());
            return new ProxyByteCodeURLConnection(byteCode);
        }

    }

    public class ProxyByteCodeURLConnection extends URLConnection {


        byte[] byteCode;

        public ProxyByteCodeURLConnection(byte[] byteCode){
            super(null);
            this.byteCode = byteCode;
        }

        /**
         * Required to be implemented by subclasses of URLConnection
         * @exception  IOException  if an I/O error occurs while opening the
         *               connection.
         * @see java.net.URLConnection#connected */
        public void connect() throws IOException {
        }

        /**
         * Returns an input stream that reads from this open connection.
         *
         * @return     an input stream that reads from this open connection.
         * @exception  IOException              if an I/O error occurs while
         *               creating the input stream.
         * @exception  UnknownServiceException  if the protocol does not support
         *               input.
         */
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(byteCode);
        }
    }


    /**
     * This method should not need to be overridden by subclasses.
     * It is done here because of a bug in the VM.  This method was
     * overriden using the algorithm of it's superclass with one
     * workaround that reliably calls the findClass method.
     * No other changes should be made in this method.
     *
     * @param name
     * @param resolve
     * @return
     * @exception ClassNotFoundException
     */
    protected synchronized Class loadClass(String name, boolean resolve) throws ClassNotFoundException {
    // First, check if the class has already been loaded
    Class c = findLoadedClass(name);
    if (c == null) {
        try {
                ClassLoader parent = getParent();
        if (parent != null) {
                    c = parent.loadClass(name);
        } else {
            c = findSystemClass(name);
        }

        } catch (ClassNotFoundException e) {
            // If still not found, then call findClass in order
            // to find the class.
                c = findClass(name);
            } catch (NoClassDefFoundError bug) {
                /* [DMB] Problem:
                 *       The VM does not consistently throw ClassNotFoundException
                 *       when classes are not found.  The sublcass' findClass method
                 *       is not called in these situations.  This behavior is not in
                 *       accordance with the ClassLoader delegation model.
                 *       Workaround:
                 *       Catch the NoClassDefFoundError that is actually thrown and
                 *       call the findClass method according to the delegation model.
                 */
                c = findClass(name);
        }
    }
    if (resolve) {
            resolveClass(c);
    }
    return c;
    }

    //=========================================================
    // Protected support for getting/setting cached byte-code
    //

    /**
     * Find the byte-code for the specified class.
     *
     * The proxy classes are generated at runtime and are created from a byte
     * array and not a .class file.  The byte array containing the byte-code
     * is discarded by the VM after a Class is created using
     * <CODE>ClassLoader.defineClass(...)</CODE>. Calling
     * Class.getResourceAsStream(proxyClassName) will return null because
     * there is no .class definition in the file system.  In this situation,
     * it is the responsibility of the class loader (ProxyClassLoader) that
     * loaded the class definition to implement ClassLoader.findResource(...)
     * and return a URL that can be used to retreive the proxy class byte-code.
     *
     * @param className
     * @return
     * @see java.lang.ClassLoader.defineClass
     * @see java.lang.ClassLoader.getSystemResourceAsStream()
     * @see org.openejb.util.proxy.ProxyClassLoader
     * @see org.openejb.util.proxy.ProxyClassLoader.findResource
     */
    protected static byte[] getCachedByteCode(String className) {

        try {
            return getCachedByteCode( DynamicProxyFactory.loader.loadClass(className) );
        } catch ( ClassNotFoundException cnfe ) {
            /* If this is a problem, it will be discovered and
             * hanled appropriately by callers of ProxyFactory.newProxyInstance()
             * or  ProxyManager.newProxyInstance()
             */
        }
        return null;
    }

    /*
     * Find the byte-code for the specified class.
     */
    protected static byte[] getCachedByteCode(Class clazz) {
        /* Synchronize on the hashtable so no two threads will do
         * this at the same time.
         */
        synchronized (byteCodeFor) {
            /* Find the matching byte-code if it already known */
            ClassByteCode classByteCode = findByteCodeFor(clazz);
            if ( classByteCode != null ) {
                return classByteCode.getByteCode();
            }

        }
        return null;
    }

    protected static void cacheByteCode(Class clazz, byte[] byteCode) {
        insertClassByteCodeFor(clazz, byteCode);
    }

    //
    // Protected support for getting/setting cached byte-code
    //=========================================================


    //=========================================
    // Private byte-code cache implementation
    //

    /*
     * findByteCodeFor a Class.  This looks in the cache for a
     * mapping from Class -> byte-code mappings.  The hashCode
     * of the Class is used for the lookup since the Class is the key.
     * The entries are extended from java.lang.ref.SoftReference so the
     * gc will be able to free them if needed.
     */
    private static ClassByteCode findByteCodeFor(Class clazz) {

        int hash = clazz.hashCode();
        int index = (hash & 0x7FFFFFFF) % byteCodeFor.length;
        ClassByteCodeEntry e;
        ClassByteCodeEntry prev;

        /* Free any initial entries whose refs have been cleared */
        while ( (e = byteCodeFor[index]) != null && e.get() == null ) {
            byteCodeFor[index] = e.next;
        }

        /* Traverse the chain looking for a byte-code with forClass == clazz.
         * unlink entries that are unresolved.
         */
        prev = e;
        while ( e != null ) {
            ClassByteCode classByteCode = (ClassByteCode)(e.get());
            if ( classByteCode == null ) {
                // This entry has been cleared,  unlink it
                prev.next = e.next;
            } else {
                if ( classByteCode.forClass() == clazz )
                    return classByteCode;
                prev = e;
            }
            e = e.next;
        }
        return null;
    }


    /*
     * Creates and inserts a new ClassByteCodeEntry for the
     * Class -> byte-code mapping.
     * The hashCode of the Class is used to index the entry.
     * The entries are extended from java.lang.ref.SoftReference so the
     * gc will be able to free them if needed.
     */
    private static void insertClassByteCodeFor(Class clazz, byte[] byteCode) {

        // Make sure not already present
        if ( findByteCodeFor(clazz) != null ) {
            return;
        }

        int hash = clazz.hashCode();
        int index = (hash & 0x7FFFFFFF) % byteCodeFor.length;
        ClassByteCodeEntry e = new ClassByteCodeEntry(new ClassByteCode(clazz, byteCode));
        e.next = byteCodeFor[index];
        byteCodeFor[index] = e;
    }

    /*
     * Entries held in the Cache of known ClassByteCode objects.
     * Entries are chained together with the same hash value (modulo array size).
     * The entries are extended from java.lang.ref.SoftReference so the
     * gc will be able to free them if needed.
     */
    private static class ClassByteCodeEntry extends java.lang.ref.SoftReference {
        ClassByteCodeEntry next;

        ClassByteCodeEntry(ClassByteCode bc) {
            super(bc);
        }
    }

    /**
     * The chache that holds byte-code for previously generated class definitions.
     * Entries are chained together with the same hash value (modulo array size).
     */
    private static ClassByteCodeEntry[] byteCodeFor = new ClassByteCodeEntry[61];


    /**
     * Wrapper for class to byte-code mappings.
     */
    static class ClassByteCode {
        Class clazz;
        byte[] byteCode;

        public ClassByteCode(Class clazz, byte[] byteCode) {
            this.clazz = clazz;
            this.byteCode = byteCode;
        }

        public Class forClass() {
            return clazz;
        }

        public byte[] getByteCode() {
            return byteCode;
        }
    }

    //
    // Private byte-code cache implementation
    //=========================================
}

