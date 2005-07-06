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


package org.openejb.core;


import org.openejb.util.FastThreadLocal;
import org.openejb.OpenEJB;

/**
 * TODO: Add comment
 */
public class ThreadContext implements Cloneable {

    /**
     * TODO: Add comment
     */
    protected static final FastThreadLocal threadStorage = new FastThreadLocal();
    /**
     * TODO: Add comment
     */
    protected static Class implClass = ThreadContext.class;

    /**
     * TODO: Add comment
     */
    protected boolean valid = false;
    /**
     * TODO: Add comment
     */
    protected DeploymentInfo deploymentInfo;
    /**
     * TODO: Add comment
     */
    protected Object primaryKey;
    /**
     * TODO: Add comment
     */
    protected byte currentOperation;
    /**
     * TODO: Add comment
     */
    protected Object securityIdentity;
    /**
     * Unspecified is any object that a customer container may want to
     * attach to the current thread context. (e.g. CastorCMP11_EntityContainer
     * attaches a JDO Database object to the thread.
     */
    protected Object unspecified;

    static{
        String className = System.getProperty(EnvProps.THREAD_CONTEXT_IMPL);

        if ( className == null ) {
            className = System.getProperty(EnvProps.THREAD_CONTEXT_IMPL);
        }

        if ( className !=null ) {
            try {
                ClassLoader cl = OpenEJB.getContextClassLoader();
                implClass = Class.forName(className, true, cl);
            } catch ( Exception e ) {
                System.out.println("Can not load ThreadContext class. org.openejb.core.threadcontext_class = "+className);
                e.printStackTrace();
                implClass = null;
            }
        }
    }

    protected static ThreadContext newThreadContext() {
        try {
            return(ThreadContext)implClass.newInstance();
        } catch ( Exception e ) {
            // this error is so serious that the system shouldn't even be running if
            // you can't get a ThreadContext implemented.
            e.printStackTrace();
            throw new RuntimeException("ThreadContext implemenation class could not be instantiated. Class type = "+implClass+" exception message = "+e.getMessage());
        }
    }

    public static boolean isValid() {
        ThreadContext tc = (ThreadContext)threadStorage.get();
        if ( tc!=null )
            return tc.valid;
        else
            return false;
    }

    protected void makeInvalid() {
        valid = false;
        deploymentInfo = null;
        primaryKey = null;
        currentOperation = (byte)0;
        securityIdentity = null;
        unspecified = null;
    }

    public static void invalidate() {
        ThreadContext tc = (ThreadContext)threadStorage.get();
        if ( tc!=null )
            tc.makeInvalid();
    }

    public static void setThreadContext(ThreadContext tc) {
        if ( tc==null ) {
            tc = (ThreadContext)threadStorage.get();
            if ( tc!=null )tc.makeInvalid();
        } else {
            threadStorage.set(tc);
        }
    }

    public static ThreadContext getThreadContext( ) {
        ThreadContext tc = (ThreadContext)threadStorage.get();
        if ( tc==null ) {
            tc = ThreadContext.newThreadContext();
            threadStorage.set(tc);
        }
        return tc;
    }

    public byte getCurrentOperation( ) {
        return currentOperation;
    }

    public Object getPrimaryKey( ) {
        return primaryKey;
    }

    public DeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
    }

    public Object getSecurityIdentity( ) {
        return securityIdentity;
    }

    public Object getUnspecified() {
        return unspecified;
    }

    public void set(DeploymentInfo di, Object primKey, Object securityIdentity) {
        setDeploymentInfo(di);
        setPrimaryKey(primKey);
        setSecurityIdentity(securityIdentity);
        valid = true;
    }

    public void setCurrentOperation(byte op) {
        currentOperation = op;
        valid = true;
    }

    public void setPrimaryKey(Object primKey) {
        primaryKey = primKey;
        valid = true;
    }

    public void setSecurityIdentity(Object identity) {
        securityIdentity = identity;
        valid = true;
    }

    public void setDeploymentInfo(DeploymentInfo info) {
        deploymentInfo = info;
    }

    public void setUnspecified(Object obj) {
        unspecified = obj;
    }

    public boolean valid() {
        return valid;
    }

    public java.lang.Object clone() throws java.lang.CloneNotSupportedException {
        return super.clone();
    }
}
