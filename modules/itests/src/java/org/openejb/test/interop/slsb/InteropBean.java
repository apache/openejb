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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.test.interop.slsb;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.ejb.SessionContext;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.Util;

import com.sun.corba.se.internal.util.PackagePrefixChecker;
import com.sun.corba.se.internal.util.RepositoryId;
import com.sun.corba.se.internal.util.Utility;
import org.omg.CORBA.portable.Delegate;
import org.omg.CORBA.portable.ObjectImpl;

import org.openejb.test.security.slsb.BasicStatelessHome;


/**
 * @version $Revision$ $Date$
 */
public class InteropBean implements SessionBean {

    private SessionContext sessionContext;
    public static final String STUB_PACKAGE_PREFIX = "org.omg.stub.";

    public String callRemote(String argument1) {
        try {
            InitialContext ic = new InitialContext();
            Object ref = ic.lookup("java:comp/env/ejb/interop/InteropBean");

            org.omg.CORBA.portable.ObjectImpl narrowObj = (org.omg.CORBA.portable.ObjectImpl) ref;
            String id = RepositoryId.createForAnyType(BasicStatelessHome.class);

            if (narrowObj._is_a(id)) {
                Stub result = null;

                try {

                    // Get the codebase from the delegate to use when loading
                    // the new stub, if possible...

                    String codebase = null;
                    try {
                        // We can't assume that narrowFrom is a CORBA_2_3 ObjectImpl, yet
                        // it may have a 2_3 Delegate that provides a codebase.  Swallow
                        // the ClassCastException otherwise.
                        Delegate delegate = narrowObj._get_delegate();
                        codebase = ((org.omg.CORBA_2_3.portable.Delegate) delegate).get_codebase(narrowObj);

                    } catch (ClassCastException e) {
                    }

                    String stubName = Utility.stubName(BasicStatelessHome.class.getName());

                    // _REVISIT_ Should the narrowFrom or narrowTo class be used as the
                    // loadingContext in the following call?  The spec says narrowFrom,
                    // but this does not seem correct...
                    Class resultClass = null;
                    try {
                        resultClass = loadClassOfType(stubName,
                                                      codebase,
                                                      narrowObj.getClass().getClassLoader(),
                                                      BasicStatelessHome.class,
                                                      BasicStatelessHome.class.getClassLoader());
                        if (true) return "FIRST" + resultClass.getName();
                    } catch (ClassNotFoundException cnfe) {
                        resultClass = loadClassOfType(STUB_PACKAGE_PREFIX + stubName,
                                                      codebase,
                                                      narrowObj.getClass().getClassLoader(),
                                                      BasicStatelessHome.class,
                                                      BasicStatelessHome.class.getClassLoader());
                        if (true) return "SECOND" + resultClass.getName();
                    }
                    // Create a stub instance and set the delegate...
                    result = (Stub) resultClass.newInstance();
                    ((ObjectImpl) result)._set_delegate(narrowObj._get_delegate());

                } catch (Exception err) {
                    if (true) return "Exception " + err;
                }

            }
            return id;

//            BasicStatelessHome home = (BasicStatelessHome) PortableRemoteObject.narrow(ref, BasicStatelessHome.class);
//            BasicStateless bean = home.create();
//
//            return bean.allAccessMethod(argument1);
        } catch (NamingException e) {
            e.printStackTrace();  //TODO: change body of catch statement use File | Settings | File Templates.
//        } catch (RemoteException e) {
//            e.printStackTrace();  //TODO: change body of catch statement use File | Settings | File Templates.
//        } catch (CreateException e) {
//            e.printStackTrace();  //TODO: change body of catch statement use File | Settings | File Templates.
        }

        return argument1;
    }

    public Object narrow(Object narrowFrom, Class narrowTo) throws ClassCastException {
        Object result = null;

        if (narrowFrom == null) {
            return null;
        }

        if (narrowTo == null) {
            throw new NullPointerException("invalid argument");
        }

        Class narrowFromClass = narrowFrom.getClass();

        try {

            // Will a cast work?

            if (narrowTo.isAssignableFrom(narrowFromClass)) {

                // Yep, so we're done...

                result = narrowFrom;

            } else {

                // No. Is narrowTo an interface that might be
                // implemented by a servant running on iiop?

                if (narrowTo.isInterface() &&
// What was this test supposed to achieve?
//                  narrowTo != java.rmi.Remote.class &&
                    narrowTo != java.io.Serializable.class &&
                    narrowTo != java.io.Externalizable.class) {

                    // Yes. Ok, so assume the current stub (narrowFrom) is an
                    // ObjectImpl (it should be a _"xxx"_Stub). If it is not,
                    // we'll catch it below and end up failing with a
                    // ClassCastException...

                    org.omg.CORBA.portable.ObjectImpl narrowObj
                            = (org.omg.CORBA.portable.ObjectImpl) narrowFrom;

                    // Create an id from the narrowTo type...

                    String id = RepositoryId.createForAnyType(narrowTo);

                    // Can the server act as the narrowTo type?

                    if (narrowObj._is_a(id)) {

                        // Yes, so try to load a stub for it...

                        result = Utility.loadStub(narrowObj, narrowTo);
                    }
                }
            }
        } catch (Exception error) {
            result = null;
        }

        if (result == null) {
            throw new ClassCastException();
        }

        return result;
    }

    public static Stub loadStub(ObjectImpl narrowFrom,
                                Class narrowTo) {
        Stub result = null;

        try {

// Get the codebase from the delegate to use when loading
// the new stub, if possible...

            String codebase = null;
            try {
// We can't assume that narrowFrom is a CORBA_2_3 ObjectImpl, yet
// it may have a 2_3 Delegate that provides a codebase.  Swallow
// the ClassCastException otherwise.
                Delegate delegate = narrowFrom._get_delegate();
                codebase = ((org.omg.CORBA_2_3.portable.Delegate) delegate).get_codebase(narrowFrom);

            } catch (ClassCastException e) {
            }

            String stubName = Utility.stubName(narrowTo.getName());

            // _REVISIT_ Should the narrowFrom or narrowTo class be used as the
            // loadingContext in the following call?  The spec says narrowFrom,
            // but this does not seem correct...
            Class resultClass = null;
            try {
                resultClass = loadClassOfType(stubName,
                                              codebase,
                                              narrowFrom.getClass().getClassLoader(),
                                              narrowTo,
                                              narrowTo.getClassLoader());
            } catch (ClassNotFoundException cnfe) {
                resultClass = loadClassOfType(STUB_PACKAGE_PREFIX + stubName,
                                              codebase,
                                              narrowFrom.getClass().getClassLoader(),
                                              narrowTo,
                                              narrowTo.getClassLoader());
            }
            // Create a stub instance and set the delegate...
            result = (Stub) resultClass.newInstance();
            ((ObjectImpl) result)._set_delegate(narrowFrom._get_delegate());

        } catch (Exception err) {
        }

        return result;
    }

    static Class loadClassOfType(String className,
                                 String remoteCodebase,
                                 ClassLoader loader,
                                 Class expectedType,
                                 ClassLoader expectedTypeClassLoader)
            throws ClassNotFoundException {

        Class loadedClass = null;

        try {
            //Sequence finding of the stubs according to spec
            try {
                //If-else is put here for speed up of J2EE.
                //According to the OMG spec, the if clause is not dead code.
                //It can occur if some compiler has allowed generation
                //into org.omg.stub hierarchy for non-offending
                //classes. This will encourage people to
                //produce non-offending class stubs in their own hierarchy.
                if (!PackagePrefixChecker.hasOffendingPrefix(PackagePrefixChecker.withoutPackagePrefix(className))) {
                    loadedClass = Util.loadClass(PackagePrefixChecker.withoutPackagePrefix(className),
                                                 remoteCodebase,
                                                 loader);
                } else {
                    loadedClass = Util.loadClass(className,
                                                 remoteCodebase,
                                                 loader);
                }
            } catch (ClassNotFoundException cnfe) {
                loadedClass = Util.loadClass(className,
                                             remoteCodebase,
                                             loader);
            }
            if (expectedType == null)
                return loadedClass;
        } catch (ClassNotFoundException cnfe) {
            if (expectedType == null)
                throw cnfe;
        }

        // If no class was not loaded, or if the loaded class is not of the
        // correct type, make a further attempt to load the correct class
        // using the classloader of the expected type.
        // _REVISIT_ Is this step necessary, or should the Util,loadClass
        // algorithm always produce a valid class if the setup is correct?
        // Does the OMG standard algorithm need to be changed to include
        // this step?
        if (loadedClass == null || !expectedType.isAssignableFrom(loadedClass)) {
            if (expectedType.getClassLoader() != expectedTypeClassLoader)
                throw new IllegalArgumentException("expectedTypeClassLoader not class loader of " +
                                                   "expected Type.");

            if (expectedTypeClassLoader != null)
                loadedClass = expectedTypeClassLoader.loadClass(className);
            else {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                if (cl == null)
                    cl = ClassLoader.getSystemClassLoader();

                loadedClass = cl.loadClass(className);
            }
        }

        return loadedClass;
    }

    public boolean isInRole(String roleName) {
        return sessionContext.isCallerInRole(roleName);
    }

    public void ejbCreate() throws CreateException {
    }

    public void ejbActivate() throws EJBException {
    }

    public void ejbPassivate() throws EJBException {
    }

    public void ejbRemove() throws EJBException {
    }

    public void setSessionContext(SessionContext sessionContext) throws EJBException {
        this.sessionContext = sessionContext;
    }
}
