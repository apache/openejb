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


package org.openejb.ri.server;


import java.lang.reflect.Method;
import java.security.Principal;

import org.openejb.DeploymentInfo;
import org.openejb.util.Messages;

/**
 * This object is used to uniqly identify a method invocation within  
 * on an EJBObject or EJBHome reference is created.  The method invocation
 * represents the interface, method, arguments, and context of the invocation.
 * <p>
 * @author Richard Monson-Haefel
 * @version 0.1, 3/21/2000
 * @since JDK 1.2
 */
public class MethodInvocation implements java.io.Serializable {

    static protected Messages _messages = new Messages( "org.openejb.alt.util.resources" );

    /**
    * The unique identity of the bean servicing the request.
    * In stateless session beans its null. In stateful session beans its an instance of SessionKey 
    * that is unique to the bean's container. In entity beans its a entity's primary key.
    *
    * This value is set permanently by the container system. It is immutable.
    */
    protected Object primaryKey;

    /*
    * The deployment identifier. It is unique within a container system.
    *
    * This value is set permanently by the container system. It is immutable.
    */
    protected DeploymentInfo deploymentInfo;



    Object []   arguments;
    Principal principal;
    transient   Method  method = null;
    transient   Class clazz = null;
    Class []    parameterTypes = null;

    public MethodInvocation(Method mthd, Object [] args, Object primaryKey, DeploymentInfo depInfo, Principal caller) {
        this.method = mthd;
        this.arguments = args;
        this.primaryKey = primaryKey;
        this.deploymentInfo = depInfo;
        this.principal = caller;
    }
    public MethodInvocation(Object primaryKey, DeploymentInfo depInfo) {
        this(null, null, primaryKey, depInfo, null);
    }
    public MethodInvocation(DeploymentInfo depInfo) {
        this(null,null,null, depInfo, null);
    }   
    public MethodInvocation(Method mthd, Object [] args) {
        this(mthd,args,null,null, null);
    }
    public Object getPrimaryKey( ) {
        return primaryKey;
    }
    public void setPrimaryKey(Object key) {
        primaryKey = key;
    }
    public Principal getPrincipal( ) {
        return principal;
    }
    public void setPrincipal(Principal caller) {
        principal = caller;
    }
    public DeploymentInfo getDeploymentInfo( ) {
        return deploymentInfo;
    }    
    public Method getMethod( ) {
        return method;
    }
    public void setMethod(Method mthd ) {
        method = mthd;
    }
    public Object [] getArguments( ) {
        return arguments;
    }
    public void setArguments(Object [] args) {
        arguments = args;
    }
    public boolean equals(Object obj) {
        if ( obj != null && obj instanceof MethodInvocation ) {
            MethodInvocation other = (MethodInvocation)obj;
            if ( other.method.equals(this.method) ) {
                for ( int i=0; i<arguments.length;i++ ) {
                    if ( !other.arguments[i].equals(arguments[i]) ) {
                        return false; 
                    }
                }
                if ( this.deploymentInfo.equals(other.deploymentInfo) )
                    if ( this.primaryKey==null )
                        return other.primaryKey == null;
                    else if ( other.primaryKey !=null )
                        return this.primaryKey.equals(other.primaryKey);
            }
        }
        return false;

    }

    public int hashCode() {
        return deploymentInfo.hashCode() ^ primaryKey.hashCode() ^ method.getDeclaringClass().getName().hashCode() ^ method.getName().hashCode() ^ (arguments.length==0?0:arguments[0].hashCode());
    }

    public String toString() {
        try {
            StringBuffer sb = new StringBuffer(""+deploymentInfo+primaryKey+":");
            sb.append(method.toString());
            sb.append( _messages.message( "methodInvocation.arguments" ) );
            for ( int k = 0; k < arguments.length; k++ ) {
                sb.append(" ("+k+") "+arguments[k].toString());
                if ( k < (arguments.length - 1) )
                    sb.append(",");
            }
            return sb.toString();
        } catch ( Exception e ) {
            return "<" + e + ">";
        }
    }

}
