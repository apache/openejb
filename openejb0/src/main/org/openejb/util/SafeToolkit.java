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

import java.util.HashMap;
import java.util.Properties;
import org.openejb.OpenEJBException;

public class SafeToolkit{

    private String systemLocation;
    protected static HashMap codebases = new HashMap();

    /**
     * Creates a new SafeToolkit dedicated to the specified system location.
     */
    protected SafeToolkit(String systemLocation) {
        this.systemLocation = systemLocation;
    }

    /**
     * Returns an instance of a SafeToolkit dedicated to the specified system location.
     */
    public static SafeToolkit getToolkit(String systemLocation){
        return new SafeToolkit(systemLocation);
    }

    /**
     * Attempts to find and load the specified class.
     *
     * @param className the name of the class to be loaded.
     * @returns the specified class.
     * @throws OpenEJBExcption if the class cannot be found.
     */
    public Class forName(String className) throws OpenEJBException{
        Class clazz = null;
        try{
            clazz = Class.forName(className);
        }
        catch(ClassNotFoundException cnfe){
            OpenEJBErrorHandler.classNotFound(systemLocation, className);
        }
        return clazz;
    }

    /**
     * Attempts to find and load the specified class, using the 
     * specified codebase.
     * If the codebase is null, the bootstrap classloader is used.
     *
     * @param className the name of the class to be loaded.
     * @param codebase the codebase to load the class from.
     * @returns the specified class.
     * @throws OpenEJBExcption if the class cannot be found.
     */
    public Class forName(String className, String codebase) throws OpenEJBException{
        ClassLoader cl = Class.class.getClassLoader();

        // If the codebase is present, then the classloader variable cl
        // is replaced by a URLClassLoader that can load the class
        // from a jar or url.
        if (codebase != null) {
            try{
                java.net.URL[] urlCodebase = new java.net.URL[1];
                urlCodebase[0] = new java.net.URL(codebase);
                cl = new java.net.URLClassLoader(urlCodebase, cl);
            } catch (java.net.MalformedURLException mue){
                OpenEJBErrorHandler.classCodebaseNotFound(systemLocation, className, codebase, mue);
            } catch (SecurityException se){
                OpenEJBErrorHandler.classCodebaseNotFound(systemLocation, className, codebase, se);
            }
        }

        Class clazz = null;
        try{
            clazz = Class.forName(className, true, cl);
        }
        catch(ClassNotFoundException cnfe){
            OpenEJBErrorHandler.classNotFound(systemLocation, className);
        }
        return clazz;
    }

    /**
     * Attempts to find and load the specified class then instaniate it.
     *
     * @param className the name of the class to be instantiated.
     * @returns an instance of the specified class.
     * @throws OpenEJBException if the class cannot be found or is not accessible .
     */
    public Object newInstance(String className) throws OpenEJBException{
        return newInstance(forName(className));
    }
    
    /**
     * Attempts to find and load the specified class then instaniate it.
     *
     * @param className the name of the class to be instantiated.
     * @returns an instance of the specified class.
     * @throws OpenEJBException if the class cannot be found or is not accessible .
     */
    public Object newInstance(String className, String codebase) throws OpenEJBException{
        return newInstance(forName(className, codebase));
    }

    /**
     * Attempts to instaniate the specified class.
     *
     * @param className the name of the class to be instantiated.
     * @returns an instance of the specified class.
     * @throws OpenEJBException if the class is not accessible .
     */
    public Object newInstance(Class clazz) throws OpenEJBException{
        Object instance = null;
        try{
            instance = clazz.newInstance();
        }
        catch(InstantiationException ie){
            OpenEJBErrorHandler.classNotIntantiateable(systemLocation, clazz.getName());
        }
        catch(IllegalAccessException iae){
            OpenEJBErrorHandler.classNotAccessible(systemLocation, clazz.getName());
        }
	// mjb - Exceptions thrown here can lead to some hard to find bugs, so I've added some rigorous error handling.
        catch(Throwable exception) {
	    exception.printStackTrace();
	    ClassLoader classLoader = clazz.getClassLoader();
	    if( classLoader instanceof java.net.URLClassLoader) {
		OpenEJBErrorHandler.classNotIntantiateableFromCodebaseForUnknownReason(systemLocation, clazz.getName(), getCodebase( (java.net.URLClassLoader)classLoader), 
										       exception.getClass().getName(), exception.getMessage());
	    }
	    else {
		OpenEJBErrorHandler.classNotIntantiateableForUnknownReason(systemLocation, clazz.getName(), exception.getClass().getName(), exception.getMessage());
	    }
        }
        return instance;

    }

    /**
     * Returns a new SafeProperties instance dedicated to this toolkit.
     *
     * @param className the name of the class to be instantiated.
     * @returns a new SafeProperties instance.
     * @throws OpenEJBException the properties object passed in is null.
     */
    public SafeProperties getSafeProperties(Properties props) throws OpenEJBException{
        return new SafeProperties(props, systemLocation);
    }

    /**
     * Loads the class using the class loader for the specific
     * codebase.  If the codebase is null, the bootstrap classloader
     * is used.
     * 
     * @param className
     * @param codebase
     * @return 
     * @exception ClassNotFoundException
     * @exception OpenEJBException
     */
    public static Class loadClass(String className, String codebase) throws OpenEJBException {
        
        ClassLoader cl = getCodebaseClassLoader(codebase);
        Class clazz = null;
        try{
            clazz = cl.loadClass(className);
        } 
	catch (ClassNotFoundException cnfe){
            Object[] details = { className, codebase };
            throw new OpenEJBException("cl0007", details);
        } 
	return clazz;
    }

    /**
     * Ensures that a class loader for each code base used in the
     * system is created at most one time.  The default bootsrap
     * classloader is used if codebase is null.
     * 
     * @param codebase
     * @return 
     * @exception OpenEJBException
     */
    protected static ClassLoader getCodebaseClassLoader(String codebase) throws OpenEJBException{
        if (codebase == null) codebase = "CLASSPATH";

        ClassLoader cl = (ClassLoader) codebases.get(codebase);
        if (cl == null) {
	    synchronized (codebases) {
		cl = (ClassLoader) codebases.get(codebase);
		if (cl == null) {
		    try {
			java.net.URL[] urlCodebase = new java.net.URL[1];
			urlCodebase[0] = new java.net.URL("file",null,codebase);
			cl = new java.net.URLClassLoader(urlCodebase, ClassLoader.getSystemClassLoader() );
			codebases.put(codebase, cl);
		    } catch (java.net.MalformedURLException mue) {
			Object[] details = {codebase, mue.getMessage()};
			throw new OpenEJBException("cl0001", details);
		    } catch (SecurityException se) {
			Object[] details = {codebase, se.getMessage()};
			throw new OpenEJBException("cl0002", details);
		    }
		}
	    }
        }
        return cl;
    }

    /**
     * Returns the search path used by the given URLClassLoader as a ';' delimited list of URLs.
     */
    private static String getCodebase( java.net.URLClassLoader urlClassLoader) {
	StringBuffer codebase = new StringBuffer();
	java.net.URL urlList[] = urlClassLoader.getURLs();
	codebase.append( urlList[0].toString());
	for( int i = 1; i < urlList.length; ++i) {
	    codebase.append(';');
	    codebase.append( urlList[i].toString());
	}
	return codebase.toString();
    }
}
