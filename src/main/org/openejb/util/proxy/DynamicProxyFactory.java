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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Properties;
import java.util.Vector;

import org.openejb.OpenEJBException;

/**
 *  EXAMPLE USAGE
 *
 *
 * ProxyManager.getProxyClass( Customer.class );
 *
 *
 *
 *  CUSTOMER INTERFACE
 *
 * public interface Customer extends javax.ejb.EJBObject {
 *
 *     public boolean setAddress(String street, int x, short y) throws RemoteException;
 *
 *
 *
 *  GENERATED PROXY CLASS
 *
 * public class CustomerProxy extends Proxy implements java.io.Serializable,Customer{
 *
 *     protected static transient java.lang.reflect.Method [] methodMap = new java.lang.reflect.Method[6];
 *
 *     protected CustomerProxy(){}
 *
 *     ...// EJBObject methods
 *
 *     public boolean setAddress( java.lang.String parm0,int parm1,short parm2) throws java.rmi.RemoteException{
 *         // obtain method
 *         java.lang.reflect.Method method = methodMap[5];
 *         if(method == null){
 *             try{
 *                 method=Customer.class.getMethod("setAddress",new Class [] { java.lang.String.class,int.class,short.class});
 *                 methodMap[5] = method;
 *             }catch(NoSuchMethodException nsme){ throw new RuntimeException();}
 *         }
 *         // package arguments
 *         Object [] args = new Object[3];
 *         args[0] = parm0;
 *         args[1] = new java.lang.Integer(parm1);
 *         args[2] = new java.lang.Short(parm2);
 *
 *         try{
 *             java.lang.Boolean retval = (java.lang.Boolean)handler.invoke(this,method,args);
 *             return retval.booleanValue( );
 *         }catch(Throwable t){
 *             // rethrow exceptions
 *             if(t instanceof java.rmi.RemoteException)
 *                 throw (java.rmi.RemoteException)t;
 *             if(t instanceof RuntimeException)
 *                 throw (RuntimeException)t;
 *             else
 *                 throw (Error)t;
 *         }
 *     }
 *
 * @author David Blevins
 * @author Kevin Lewis
 * @author Ray Racine
 * @author Richard Monson-Haefel
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class DynamicProxyFactory implements ProxyFactory {
    public final static ProxyClassLoader loader = new ProxyClassLoader();

    private static String LINE_SEPARATOR = System.getProperty("line.separator");

    public boolean DELETE_DEFINITIONS = true;
    private boolean CREATE_PACKAGE_DIRECTORIES = false;

    private static String PROXY_PREFIX = "org.openejb.proxies.";
    private static String PROXY_SUFFIX = "Proxy";
    private static char   PROXY_PACKAGE_SEPATATOR_CHAR = '_';

    private File PROXY_OUTPUT_DIRECTORY = null;

    public DynamicProxyFactory() {
    }

    //=================================
    // ProxyFactory interface methods
    //

    public void init(Properties props) throws OpenEJBException {

        String propValue = "";

        propValue = props.getProperty("DELETE_DEFINITIONS", "true").trim();
        DELETE_DEFINITIONS = "true".equalsIgnoreCase(propValue);

        PROXY_PREFIX = props.getProperty("PROXY_PREFIX", PROXY_PREFIX).trim();

        PROXY_SUFFIX = props.getProperty("PROXY_SUFFIX", PROXY_SUFFIX).trim();

        propValue = props.getProperty("PROXY_PACKAGE_SEPARATOR_CHAR", PROXY_PACKAGE_SEPATATOR_CHAR + "").trim();
        PROXY_PACKAGE_SEPATATOR_CHAR = (char)propValue.getBytes()[0];

        String outputDir = props.getProperty("PROXY_OUTPUT_DIRECTORY", System.getProperty("user.dir")).trim();

        File nonCanonicalDir = new File(outputDir);
        try{
            PROXY_OUTPUT_DIRECTORY = new File(nonCanonicalDir.getCanonicalPath());
            PROXY_OUTPUT_DIRECTORY.mkdirs();
            loader.addURL(PROXY_OUTPUT_DIRECTORY);
        } catch(java.net.MalformedURLException mue){
            throw new OpenEJBException("DynamicProxyFactory: Cannot add PROXY_OUTPUT_DIRECTORY to the classpath:"+PROXY_OUTPUT_DIRECTORY.getAbsolutePath(),mue);
        } catch(IOException e){
            throw new OpenEJBException("DynamicProxyFactory: Cannot resolve PROXY_OUTPUT_DIRECTORY:"+nonCanonicalDir.getAbsolutePath(),e);
        }

        CREATE_PACKAGE_DIRECTORIES = ( PROXY_PACKAGE_SEPATATOR_CHAR == '.' );

    }

    /**
     * Returns the invocation handler for the specified proxy instance.
     */
    public InvocationHandler getInvocationHandler(Object proxy) throws IllegalArgumentException{
        return ((Proxy)proxy).getInvocationHandler();
    }

    /**
     * Sets the invocation handler for the specified proxy instance and
     * returns the handler that was previously associated with the proxy instance.
     */
    public Object setInvocationHandler(Object proxy, InvocationHandler handler) throws IllegalArgumentException{
        ((Proxy)proxy).setInvocationHandler(handler);
        return proxy;
    }

    /**
     * Returns the java.lang.Class object for a proxy class given a class loader and an array of interfaces.
     */
    public Class getProxyClass(Class interfce) throws IllegalArgumentException {
        Class clazz = null;
        String proxyName = intfcNameToProxyName(interfce.getName());
        try {
            clazz = loader.loadClass( proxyName );
        } catch ( ClassNotFoundException cnfe ){
            try {
                ProxyClassLoader proxyloader = (ProxyClassLoader)loader;
                // Create the class but do not cache the byte-code
                if (!DELETE_DEFINITIONS) {
                    String source = generateSourceCode(proxyName);
                    compileSourceCode(source, proxyName);
                    clazz = proxyloader.loadClass( proxyName );
                // Create the class, define it, and cache the byte-code
                } else {
                    clazz = proxyloader.defineClass( proxyName , generateProxyByteCode( proxyName ) );
                }
	    } catch ( InstantiationException ie ) {
		throw new IllegalArgumentException("Cant instatiate compiler: "+ie.getMessage());
            } catch ( ClassNotFoundException cnfe2 ){
                throw new IllegalArgumentException("Cannot load the proxy from the classpath or PROXY_OUTPUT_DIRECTORY:"+PROXY_OUTPUT_DIRECTORY+"   "+cnfe2.getMessage());
            } catch ( IllegalAccessException iae ){
                throw new IllegalArgumentException(iae.getMessage());
            } catch ( ClassFormatError cfe ){
                throw new IllegalArgumentException(cfe.getMessage());
            }
        }
        return clazz;
    }

    /**
     * Returns the java.lang.Class object for a proxy class given a class loader and an array of interfaces.
     */
    public Class getProxyClass(Class[] interfaces) throws IllegalArgumentException {
        if (interfaces.length == 1) {
            return getProxyClass( interfaces[0] );
        } else if (interfaces.length > 1) {
            throw new IllegalArgumentException("DynamicProxyFactory can only implement one interface at a time.");
        } else throw new IllegalArgumentException("At least one interface must be specified");
    }

    /*
     * Returns true if and only if the specified class was dynamically generated to be a proxy class using the getProxyClass method or the newProxyInstance method.
     */
    public boolean isProxyClass(Class cl){
        return (cl.getSuperclass() == Proxy.class);
    }

     /**
      * Returns a new proxy instance from the specified proxy class.  The
      * interface(s) implemented by the proxy instance are determined by
      * the proxy class.
      * @throws java.lang.IllegalArgumentException
      *     Occurs when the specified class is not a proxy class.
     */
     public Object newProxyInstance(Class proxyClass) throws IllegalArgumentException{
         if(!isProxyClass(proxyClass))
             throw new IllegalArgumentException(proxyClass+" is not a proxy class!");
         try {
             return proxyClass.newInstance();
         } catch ( IllegalAccessException iae ) {
             throw new RuntimeException("Cannot create new proxy instance: " + iae.getMessage());
         } catch ( InstantiationException ie ) {
             throw new RuntimeException("Cannot create new proxy instance: " + ie.getMessage());
         }
     }


    /*
     * Returns an instance of a proxy class for the specified interface that dispatches method invocations to
     * the specified invocation handler.
     */
    public Object newProxyInstance(Class[] interfaces, InvocationHandler h) throws IllegalArgumentException{
        if (interfaces.length == 1) {
            return newProxyInstance( interfaces[0] );
        } else if (interfaces.length > 1) {
            throw new IllegalArgumentException("DynamicProxyFactory can only implement one interface at a time.");
        } else throw new IllegalArgumentException("At least one interface must be specified");
    }

    /*
     * Returns an instance of a proxy class for the specified interface that dispatches method invocations to
     * the specified invocation handler.
     */
    public Object newProxyInstance(Class interfce, InvocationHandler h) throws IllegalArgumentException{
        try {
            Proxy proxy = (Proxy)getProxyClass(interfce).newInstance();
            proxy.setInvocationHandler(h);
            return proxy;
        } catch ( IllegalAccessException iae ) {
            throw new RuntimeException("Cannot create new proxy instance: " + iae.getMessage());
        } catch ( InstantiationException ie ) {
            throw new RuntimeException("Cannot create new proxy instance: " + ie.getMessage());
        }

    }

    //
    // ProxyFactory interface methods
    //=================================




    //===============================
    // Proxy name generation logic
    //

    public String proxyNameToIntfcName(String proxyName) {
        proxyName = proxyName.substring(PROXY_PREFIX.length());
        proxyName = proxyName.substring( 0, proxyName.lastIndexOf(PROXY_SUFFIX) );

        if ( !CREATE_PACKAGE_DIRECTORIES ) proxyName = proxyName.replace(PROXY_PACKAGE_SEPATATOR_CHAR, '.');
        return proxyName;
    }

    public String intfcNameToProxyName(String interfaceName) {
        interfaceName = PROXY_PREFIX + interfaceName + PROXY_SUFFIX;

        if ( !CREATE_PACKAGE_DIRECTORIES ) {
            // Cannot use a package separator character that is already part of the interface name.
            // This would not allow us to parse the interface name again.
            // If this is the case, we must switch to separating packages in the normal way.
            if (interfaceName.indexOf(PROXY_PACKAGE_SEPATATOR_CHAR) < 0 ){
                interfaceName = interfaceName.replace('.', PROXY_PACKAGE_SEPATATOR_CHAR);
            } else {
                CREATE_PACKAGE_DIRECTORIES = true;
                PROXY_PACKAGE_SEPATATOR_CHAR = '.';
            }
        }
        return interfaceName;
    }

    private String parsePackageName(String className) {
        if ( className.indexOf('.') < 1 ) return null;
        return className.substring( 0, className.lastIndexOf('.') );
    }

    private String parsePartialClassName(String className) {
        if ( className.indexOf('.') < 1 ) return className;
        return className.substring( className.lastIndexOf('.')+1 );
    }

    //
    // Proxy name generation logic
    //===============================

    //===============================
    // Source code generation logic
    //
    private String generateSourceCode(String proxyClassName) throws IllegalAccessException{

        Class proxyInterface = null;

        try {
            proxyInterface = loader.loadClass(proxyNameToIntfcName(proxyClassName));
        } catch ( ClassNotFoundException e ) {

        }

        StringBuffer proxyCode = new StringBuffer( 8000 );
        appendClassDeclaration(proxyCode, proxyClassName);
        appendMethodMapDeclaration(proxyCode, proxyInterface);
        int next = appendMethodDefinitions(proxyCode, proxyInterface, 0);
        //next = appendMethodDefinitions(proxyCode, OpenEJBProxy.class, next);

        // delimit class
        proxyCode.append(LINE_SEPARATOR).append('}');

        return proxyCode.toString();
    }


    /**
     * <PRE>
     * example output:
     *
     * package org.openejb.test.beans;
     *
     * public class ShoppingCartProxy extends org.openejb.util.proxy.Proxy implements java.io.Serializable
     *
     * @param sourceCode
     * @param className
     */
    private void appendClassDeclaration(StringBuffer sourceCode, String className) {
        if ( CREATE_PACKAGE_DIRECTORIES ) {
            String packageName = parsePackageName(className);
            sourceCode.append("package ").append(packageName).append(";").append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        }
        sourceCode.append("public class ").append(parsePartialClassName(className)).append(" extends org.openejb.util.proxy.Proxy implements java.io.Serializable");
    }


    /**
     * <PRE>
     * example output:
     *
     * , ShoppingCart{
     *
     * private static transient java.lang.reflect.Method [] methodMap = new java.lang.reflect.Method[5];
     * private static Class interfce = ShoppingCart.class;
     *
     * protected Method _proxyMethod$lookupMethod(int index, String methodName, Class[] argTypes){
     *     return _proxyMethod$lookupMethod(interfce, methodMap, index, methodName, argTypes);
     * }
     *
     * @param sourceCode
     * @param interfce
     */
    private void appendMethodMapDeclaration(StringBuffer sourceCode, Class interfce) {

        // static method map count & interface extensions
        int methodCount = 0;
        sourceCode.append(',').append(' ');
        sourceCode.append(interfce.getName());
        methodCount += interfce.getMethods().length;

        // end class declaration
        sourceCode.append('{');
        // declare static method map
        //methodMap;
        sourceCode.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
        sourceCode.append(TAB).append("private static transient java.lang.reflect.Method [] methodMap = new java.lang.reflect.Method[");
        sourceCode.append(methodCount);
        sourceCode.append(']').append(';').append(LINE_SEPARATOR);
        sourceCode.append(TAB).append("private static Class interfce = "+ interfce.getName() + ".class;").append(LINE_SEPARATOR);
        sourceCode.append(LINE_SEPARATOR).append(TAB);
        sourceCode.append("protected java.lang.reflect.Method _proxyMethod$lookupMethod(int index, String methodName, Class[] argTypes){").append(LINE_SEPARATOR);
        sourceCode.append(TAB).append(TAB);
        sourceCode.append("return _proxyMethod$lookupMethod(interfce, methodMap, index, methodName, argTypes);").append(LINE_SEPARATOR);
        sourceCode.append(TAB).append('}');

    }

    /**
     *
     *
     * @param sourceCode
     * @param interfce
     */
    private int appendMethodDefinitions(StringBuffer sourceCode, Class interfce, int methodCounter) {

        // add method signatures for each interface
        methodCounter = writeMethods(sourceCode, interfce, methodCounter);
        //nextMethodIndex += interfce.getMethods().length;

        return methodCounter;
    }
    
    /**
     * This operation returns the class name
     * either if it's a simple class or an array
     *
     * @param clz the class
     * @return the class name 
     *
     */
    private String getClassName( Class clz ) {
      
      if( clz.isArray() )
        return clz.getComponentType().getName()+"[]";
        
      return clz.getName();   
    }   
    
    /**
     *
     *
     * @param sourceCode
     * @param intrface
     * @param nextMethodIndex
     */
    protected int writeMethods(StringBuffer sourceCode, Class intrface, int nextMethodIndex) {
        Method [] methods = intrface.getMethods();
        // write each method declaration
        for ( int i = 0; i < methods.length; i++ ) {
            // accessor
            sourceCode.append(LINE_SEPARATOR).append(LINE_SEPARATOR);
            sourceCode.append(TAB).append("public ");
            // return type
            
            // sourceCode.append(methods[i].getReturnType().getName()); // Modification by Olivier Modica 4/3/2001
            sourceCode.append( getClassName(methods[i].getReturnType()) ); 
            
            sourceCode.append(' ');
            // name
            sourceCode.append(methods[i].getName());
            // paramters
            sourceCode.append("( ");
            Class [] types = methods[i].getParameterTypes();
            for ( int j = 0; j < types.length; j++ ) {
                //sourceCode.append( types[j].getName()); // Modification by Olivier Modica 4/3/2001
                sourceCode.append( getClassName(types[j]) ); 
                sourceCode.append(" p");
                sourceCode.append(j);
                sourceCode.append(',');
            }
            sourceCode.setCharAt(sourceCode.length()-1,' ');
            sourceCode.append(')');
            // exceptions
            types = methods[i].getExceptionTypes();
            if ( types.length>0 ) {
                sourceCode.append(" throws ");
                for ( int j = 0; j < types.length; j++ ) {
                    sourceCode.append(types[j].getName());
                    sourceCode.append(',');
                }
                sourceCode.setCharAt(sourceCode.length()-1,'{');
            } else
                sourceCode.append('{');

            sourceCode.append( LINE_SEPARATOR );
            /* CODE JUST WRITTEN:
             * public void remove( java.lang.Object p0) throws java.rmi.RemoteException,javax.ejb.RemoveException{
             */

            // add deligation behavior for this method
            writeDeligationLogic(sourceCode, intrface, methods[i], nextMethodIndex+i);

            // method close
            sourceCode.append(TAB).append('}');

        }
        return nextMethodIndex + methods.length;
    }

    private static final String TAB = "    ";
    /**
     *
     *
     * @param sourceCode
     * @param intrface
     * @param method
     * @param nextMethodIndex
     */
    protected void writeDeligationLogic(StringBuffer sourceCode, Class intrface, Method method, int nextMethodIndex) {
        // declare method variable m
        //sourceCode.append(TAB).append(TAB);
        //sourceCode.append(nextMethodIndex);

        /* Write the Class array for the method's parameter types
         * =======================================================
         *
         * Class[] argTypes = new Class [] {java.lang.Object.class};
         *
         */
        Class [] parms = method.getParameterTypes();
        if ( parms.length > 0 ) {
            sourceCode.append(TAB).append(TAB).append("Class[] argTypes = new Class [] {");
            sourceCode.append(getClassName(parms[0])).append(".class");
            for ( int i = 1; i < parms.length; i++ ) {
                sourceCode.append(',');
                // Modification by Olivier Modica 4/3/2001
                  sourceCode.append(getClassName(parms[i]));
                 sourceCode.append(".class");
                
            }
            sourceCode.append('}').append(';').append(LINE_SEPARATOR);
        }

        /* Write the Object array with the method's parameters
         * ====================================================
         *
         * Object[] args = new Object[]{param0};
         *
         */
        if ( parms.length > 0 ) {
            sourceCode.append(TAB).append(TAB).append("Object[] args = new Object[]{");
            for ( int i = 0; i < parms.length; i++ ) {
                if ( parms[i].isPrimitive() ) {
                    sourceCode.append("new ");
                    sourceCode.append(primitiveWrapper(parms[i]));
                    sourceCode.append("(p");
                    sourceCode.append(i);
                    sourceCode.append(')').append(',');
                } else {
                    sourceCode.append("p");
                    sourceCode.append(i);
                    sourceCode.append(',');
                }
            }
            sourceCode.setCharAt(sourceCode.length()-1,'}');
            sourceCode.append(';');
            sourceCode.append(LINE_SEPARATOR);
        }
        boolean throwsAppException = (method.getExceptionTypes().length > 1);

        StringBuffer proxyMethod = new StringBuffer(100);

        /* Write the try statement IF the method defines additional
         * exceptions other than java.rmi.RemoteException
         * ====================================================
         *
         * == IF == defines exceptions other than java.rmi.RemoteException
         * try{
         *
         *
         * Proxy method delegate built at this point
         * ===========================================
         *
         * == IF == defines exceptions other than java.rmi.RemoteException
         * _proxyMethod$throws_AppException
         * == IF == only defines java.rmi.RemoteException
         * _proxyMethod$throws_default
         *
         */
        proxyMethod.append("_proxyMethod$throws_");
        if ( throwsAppException ) {
            sourceCode.append(TAB).append(TAB).append("try {").append(LINE_SEPARATOR);
            sourceCode.append(TAB);
            proxyMethod.append("AppException");
        } else {
            proxyMethod.append("default");
        }


        /* Write the return value
         * ===============================
         *
         * == IF == returns void
         * (write nothing)
         * == IF == returns a primitive
         * return
         * == IF == returns an Object
         * return (org.openejb.test.beans.ShoppingCart)
         *
         *
         * Proxy method delegate built at this point
         * ===========================================
         *
         * == IF == returns void
         * $returns_void
         * == IF == returns a primitive
         * $returns_int
         * == IF == returns an Object
         * $returns_Object
         *
         */
        proxyMethod.append("$returns_");
        if ( method.getReturnType() == void.class ) {
            sourceCode.append(TAB).append(TAB);
            // return type is void. don't return anything
            proxyMethod.append("void");
        } else if ( method.getReturnType().isPrimitive( ) ) {
            sourceCode.append(TAB).append(TAB);
            // if return type is primitive. extract primitive value from wrapper
            sourceCode.append("return ");
            proxyMethod.append( getClassName(method.getReturnType()));
        } else {// return type is an object
            sourceCode.append(TAB).append(TAB);
            sourceCode.append("return ");
            sourceCode.append('(');
            sourceCode.append(getClassName(method.getReturnType()));
            sourceCode.append(')');
            proxyMethod.append("Object");
        }



       /* Append method arguments to the proxyMethod call.
        * Proxy method delegate built at this point
        * ===========================================
        *
        * == IF == has no arguments
        * (0, "getEJBMetaData", NO_ARGS_C, NO_ARGS_O);
        * == IF == has arguments
        * (2, "remove", argTypes, args);
        *
        */
        proxyMethod.append('(');
        // int methodNumber
        proxyMethod.append( nextMethodIndex ).append(',');
        // String methodName
        proxyMethod.append('\"').append( method.getName() ).append('\"').append(',');

        // Class[] argTypes
        if ( method.getParameterTypes().length > 0 ) {
            proxyMethod.append("argTypes, args");
        } else {
            proxyMethod.append("NO_ARGS_C, NO_ARGS_O");
        }
        proxyMethod.append(')').append(';').append(LINE_SEPARATOR);

        /* Append Proxy method delegate to the rest of the source code
         * ============================================================
         */
        sourceCode.append( proxyMethod.toString() );

        /* Write the catch statements IF the method defines additional
         * exceptions other than java.rmi.RemoteException
         * ====================================================
         *
         * == IF == defines exceptions other than java.rmi.RemoteException
         * } catch (org.openejb.ApplicationException ae) {
         *     if (ae.getRootCause() instanceof javax.ejb.RemoveException)
         *         throw (javax.ejb.RemoveException)ae.getRootCause();
         *     else throw _proxyError$(ae);
         * }
         *
         */
        Class [] exceptionTypes = method.getExceptionTypes();
        if ( exceptionTypes.length > 1 ) {
            sourceCode.append(LINE_SEPARATOR).append(TAB).append(TAB);
            sourceCode.append("} catch (org.openejb.ApplicationException ae) {").append(LINE_SEPARATOR);

            for ( int i = 0; i < exceptionTypes.length; i++ ) {
                if ( exceptionTypes[i] == java.rmi.RemoteException.class ) continue;
                sourceCode.append(TAB).append(TAB).append(TAB);
                sourceCode.append("if (ae.getRootCause() instanceof ");
                sourceCode.append(exceptionTypes[i].getName());
                sourceCode.append(')').append(LINE_SEPARATOR);
                sourceCode.append(TAB).append(TAB).append(TAB).append(TAB);
                sourceCode.append("throw (");
                sourceCode.append(exceptionTypes[i].getName());
                sourceCode.append(")ae.getRootCause();").append(LINE_SEPARATOR);
            }
            // handle possible non-runtime system exception being passed to proxy as an ApplicationException.
            sourceCode.append(TAB).append(TAB).append(TAB);
            sourceCode.append("else throw _proxyError$(ae);").append(LINE_SEPARATOR);
            sourceCode.append(TAB).append(TAB).append('}').append(LINE_SEPARATOR);
        }
    }

    /**
     *
     *
     * @param primitiveClass
     * @return
     */
    protected String primitiveWrapper(Class primitiveClass) {

        if ( primitiveClass == Integer.TYPE ) {
            return "java.lang.Integer";
        } else if ( primitiveClass == Double.TYPE ) {
            return "java.lang.Double";
        } else if ( primitiveClass == Long.TYPE ) {
            return "java.lang.Long";
        } else if ( primitiveClass == Boolean.TYPE ) {
            return "java.lang.Boolean";
        } else if ( primitiveClass == Float.TYPE ) {
            return "java.lang.Float";
        } else if ( primitiveClass == Character.TYPE ) {
            return "java.lang.Character";
        } else if ( primitiveClass == Byte.TYPE ) {
            return "java.lang.Byte";
        } else //is(primitiveClass == Short.TYPE){
            return "java.lang.Short";

    }

    //
    // Source code generation logic
    //===============================


    //=================================
    // Dynamic proxy generation logic
    //

    /**
     *
     * @param outputPath
     * @param className
     * @param sourceCode
     * @exception IllegalAccessException
     */
    public byte[] generateProxyByteCode(String proxyClassName) throws IllegalAccessException {
        byte[] byteCode = null;
        // write source code to file
        try {
            String source = generateSourceCode(proxyClassName);
            File classFile = compileSourceCode(source, proxyClassName);

            //=====================================
            // Load the .class and get it's bytes

            FileInputStream fis = new FileInputStream(classFile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream( (int)classFile.length() );

            int b;
            while ( true ) {
                b = fis.read();
                if ( b==-1 ) break;
                baos.write(b);
            }

            byteCode = baos.toByteArray();

            //============================
            // Clean up before returning
            fis.close();
            baos.close();
            if ( DELETE_DEFINITIONS ) classFile.delete();

	} catch ( InstantiationException ie ) {
	    throw new IllegalAccessException("Cant instatiate compiler: "+ie.getMessage());
        } catch ( SecurityException se ) {
            throw new IllegalAccessException("Cant compile. SecurityManager restriction");
        } catch ( IOException io ) {
            throw new IllegalAccessException("Cant write generated proxy");
        }
        return byteCode;
    }

    private File compileSourceCode(String sourceCode, String proxyClassName) throws IllegalAccessException, InstantiationException {
        File classFile = null;

        try {
            File outputDir = null;

            if ( CREATE_PACKAGE_DIRECTORIES ) {
                String packageName = parsePackageName(proxyClassName);
                outputDir = new File(PROXY_OUTPUT_DIRECTORY, packageName.replace('.', File.separatorChar));
            } else outputDir = PROXY_OUTPUT_DIRECTORY;

            String partialClassName = parsePartialClassName( proxyClassName );
            outputDir.mkdirs();
            File javaFile = new File(outputDir, partialClassName + ".java");
            classFile = new File(outputDir, partialClassName + ".class");

            //=======================
            // Write source to file
            try {
                FileOutputStream fos = new FileOutputStream( javaFile );
                fos.write(sourceCode.toString().getBytes());
                fos.flush();
                fos.close();
            } catch ( IOException io ) {
                throw new IllegalAccessException("Can't write generated proxy source code to file:\n" + javaFile.getAbsoluteFile());
            }

            //======================
            // Compile source file
            Vector cargs = new Vector();
            //cargs.addElement("-d");
            //cargs.addElement(getSourcePath());
            cargs.addElement("-classpath");
            cargs.addElement( PROXY_OUTPUT_DIRECTORY.getAbsolutePath() + System.getProperty("path.separator")+ System.getProperty("java.class.path"));
            // cargs.addElement("-g");  debug off by default
            cargs.addElement("-O"); //optimize
            cargs.addElement(""+javaFile.getAbsoluteFile());

            String[] args = new String[cargs.size()];
            cargs.copyInto(args);

	    org.openejb.util.compiler.Compiler compiler = org.openejb.util.compiler.CompilerFactory.newCompilerInstance();
            compiler.compile(args);

            //=====================
            // Delete source file
            if ( DELETE_DEFINITIONS ) javaFile.delete();

	} catch ( InstantiationException ie ) {
	    throw new IllegalAccessException("Cant instatiate compiler: "+ie.getMessage());
        } catch ( SecurityException se ) {
            throw new IllegalAccessException("SecurityManager restriction. Can't compile "+classFile.getAbsoluteFile());
        }
        return classFile;
    }

    //
    // Dynamic proxy generation logic
    //=================================


}

