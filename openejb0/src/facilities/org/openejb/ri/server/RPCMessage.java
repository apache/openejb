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

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;
import java.lang.reflect.Method;

import org.openejb.util.Messages;


public class RPCMessage implements java.io.Externalizable {

    static protected Messages _messages = new Messages( "org.openejb.alt.util.resources" );

    public Class[] paramTypes;
    public Serializable[] args;
    public Serializable deploymentID;
    public Serializable primaryKey;
    public String interfaceName;
    public String methodName;
    public String securityToken;

    /** Public no-arg constructor required by Externalizable API */
    public RPCMessage() {}

    public RPCMessage(Object pk, Object depID, Method method, Object [] args, String securityToken) {
        this.securityToken = securityToken;
        this.methodName = method.getName();
        this.interfaceName = method.getDeclaringClass().getName();
        this.args = new Serializable[args.length];
        paramTypes = method.getParameterTypes();

        try{
            this.primaryKey = (Serializable)pk;
        } catch (ClassCastException e){
            throw new IllegalArgumentException( _messages.format( "rpcMessage.illegalPrimaryKey", pk.getClass() ) );
        }
        
        try{
            this.deploymentID = (Serializable)depID;
        } catch (ClassCastException e){
            throw new IllegalArgumentException( _messages.format( "rpcMessage.illegalDeploymentID", depID.getClass() ) );
        }
        
        for ( int i = 0; i < paramTypes.length; i++ ) {
            if ( paramTypes[i].isPrimitive()  ) {
                this.args[i] = new PrimitiveArg(args[i]);
                paramTypes[i] = null;
            } else {
                try{
                    this.args[i] = (Serializable)args[i];
                } catch (ClassCastException e){
                    throw new IllegalArgumentException( _messages.format( "rpcMessage.illegalArgument", methodName, depID.getClass(), interfaceName, method, new Integer(i) ) );
                }
            }
        }
    }


    public String toString(){
        StringBuffer buf = new StringBuffer();
        buf.append( "interfaceName=" );
        buf.append( interfaceName );
        buf.append( ",methodName=" );
        buf.append( methodName );
        buf.append( ",deploymentID=" );
        buf.append( deploymentID );
        buf.append( ",securityToken=" );
        buf.append( securityToken );
        buf.append( ",primaryKey=" );
        buf.append( primaryKey );
        return buf.toString();
    }
    
    //========================================
    // Externalizable object implementation
    //
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeUTF( interfaceName );
        out.writeUTF( methodName );
        out.writeUTF( securityToken );
        out.writeObject( deploymentID );
        out.writeObject( primaryKey );
        out.writeInt( paramTypes.length );
        for (int i=0; i < paramTypes.length; i++){
            out.writeObject(paramTypes[i]);
        }
        out.writeInt( args.length );
        for (int i=0; i < args.length; i++){
            out.writeObject(args[i]);
        }
    }

    boolean failed = false;
    public void readExternal(ObjectInput in) throws IOException,ClassNotFoundException {
        /* <WORKAROUND> */
        int badMember = -1;
        int badIndex = -1;
        Exception caughtException = null;
        /* </WORKAROUND> */

        /* Exceptions thrown from these catch blocks 
         * are completely ignored, only the exception
         * caught in the try block reaches the caller
         * of objectInputStream.readObject().
         * This has to be a VM bug and will hopefully 
         * go away in the future. When it does, uncomment
         * the handleUnreadableMessage methods and remove
         * the code before them in the catch blocks.
         */
        try{
        interfaceName = in.readUTF();
        }catch (Exception e){
            /* <WORKAROUND> */
            if (!failed) {
                failed = true;
                badMember = INTERFACE;
                caughtException = e;
            }
            /* </WORKAROUND> */
            // handleUnreadableMessage(INTERFACE, -1, e);
        }
        
        try{
        methodName = in.readUTF();
        }catch (Exception e){
            /* <WORKAROUND> */
            if (!failed) {
                failed = true;
                badMember = METHOD;
                caughtException = e;
            }
            /* </WORKAROUND> */
            //handleUnreadableMessage(METHOD, -1, e);
        }
        
        try{
        securityToken = in.readUTF();
        }catch (Exception e){
            /* <WORKAROUND> */
            if (!failed) {
                failed = true;
                badMember = SECURITY_TOKEN;
                caughtException = e;
            }
            /* </WORKAROUND> */
            //handleUnreadableMessage(SECURITY_TOKEN, -1, e);
        }
        
        try{
        deploymentID = (Serializable) in.readObject();
        }catch (Exception e){
            /* <WORKAROUND> */
            if (!failed) {
                failed = true;
                badMember = DEPLOYMENT_ID;
                caughtException = e;
            }
            /* </WORKAROUND> */
            //handleUnreadableMessage(DEPLOYMENT_ID, -1, e);
        }
        
        try{
        primaryKey = (Serializable) in.readObject();
        }catch (Exception e){
            /* <WORKAROUND> */
            if (!failed) {
                failed = true;
                badMember = PRIMARYKEY;
                caughtException = e;
            }
            /* </WORKAROUND> */
            //handleUnreadableMessage(PRIMARYKEY, -1, e);
        }
        
        paramTypes = new Class[in.readInt()];
        for (int i=0; i < paramTypes.length; i++){
            try{
            paramTypes[i] = (Class)in.readObject();
            } catch (Exception e){
                /* <WORKAROUND> */
                if (!failed) {
                    failed = true;
                    badMember = ARG_TYPE;
                    badIndex = i;
                    caughtException = e;
                }
                /* </WORKAROUND> */
                //handleUnreadableMessage(ARG_TYPE, i, e);
            }
        }
        
        args = new Serializable[in.readInt()];
        for (int i = 0; i < args.length; i++){
            try{
            args[i] = (Serializable)in.readObject();
            } catch (Exception e){
                /* <WORKAROUND> */
                if (!failed) {
                    failed = true;
                    badMember = ARG_VALUE;
                    badIndex = i;
                    caughtException = e;
                }
                /* </WORKAROUND> */
                //handleUnreadableMessage(ARG_VALUE, i, e);
            }
        }
        /* <WORKAROUND> */
        if (failed) handleUnreadableMessage(badMember, badIndex, caughtException);
        /* </WORKAROUND> */
    }
    
    private static final int INTERFACE      = 0;
    private static final int METHOD         = 1;
    private static final int SECURITY_TOKEN = 2;
    private static final int DEPLOYMENT_ID  = 3;
    private static final int PRIMARYKEY     = 4;
    private static final int ARG_TYPE       = 5;
    private static final int ARG_VALUE      = 6;

    public void handleUnreadableMessage(int notRead, int lastIndexRead, Exception e) throws IOException, ClassNotFoundException{
        StringBuffer buf = new StringBuffer("NOT READABLE");
        switch (notRead) {
        case ARG_VALUE:
            buf.insert(0,"\n\t  ["+lastIndexRead+"] = ");
            for (int i = lastIndexRead-1; i > -1; i--){
                buf.insert(0, args[i]);
                buf.insert(0, "\n\t  ["+i+"] = ");
            }
            buf.insert(0, "\n\targ values:");
            lastIndexRead = paramTypes.length-1;
            buf.insert(0, paramTypes[lastIndexRead].getName());
        case ARG_TYPE:      
            buf.insert(0, "\n\t  ["+lastIndexRead+"] = ");
            for (int i = lastIndexRead-1; i > -1; i--){
                buf.insert(0, paramTypes[i].getName() );
                buf.insert(0, "\n\t  ["+i+"] = ");
            }
            buf.insert(0, "\n\targ types:");
            buf.insert(0, primaryKey);
        case PRIMARYKEY:
            buf.insert(0, "\n\tprimaryKey = ");
            buf.insert(0, deploymentID);
        case DEPLOYMENT_ID:  
            buf.insert(0, "\n\tdeployment id = ");
            buf.insert(0, securityToken);
        case SECURITY_TOKEN:
            buf.insert(0, "\n\tsecurity token = ");
            buf.insert(0, methodName);
        case METHOD:         
            buf.insert(0, "\n\tmethod = ");
            buf.insert(0, interfaceName);
        case INTERFACE:      
            buf.insert(0, "\n\tinterface = ");
            break;
        }

        if (e instanceof ClassNotFoundException) {
            throw new ClassNotFoundException( _messages.format( "rpcMessage.unableToDeserializeRpcMessage", e.getMessage(), buf.toString() ) );
        } else throw new IOException( _messages.format( "rpcMessage.unableToDeserializeRpcMessage", e.getMessage(), buf.toString() ) );
        
    }

    public class PrimitiveArg implements java.io.Externalizable {
        public Serializable argument;

        public PrimitiveArg(Object arg) {
            this.argument = (Serializable)arg;
        }
        
        public Class getPrimitiveClass() throws ClassNotFoundException{

            if ( argument instanceof java.lang.Integer ) {
                return Integer.TYPE;
            } else if ( argument instanceof java.lang.Double ) {
                return Double.TYPE;
            } else if ( argument instanceof java.lang.Long ) {
                return Long.TYPE;
            } else if ( argument instanceof java.lang.Boolean ) {
                return Boolean.TYPE;
            } else if ( argument instanceof java.lang.Float ) {
                return Float.TYPE;
            } else if ( argument instanceof java.lang.Character ) {
                return Character.TYPE;
            } else if ( argument instanceof java.lang.Byte ) {
                return Byte.TYPE;
            } else if ( argument instanceof java.lang.Short ) {
                return Short.TYPE;
            } else {
                throw new ClassNotFoundException();
            }
        }     
    
        //========================================
        // Externalizable object implementation
        //
        public void writeExternal(ObjectOutput out) throws IOException {
            out.writeObject(argument);
        }
    
        public void readExternal(ObjectInput in) throws IOException,ClassNotFoundException {
            argument = (Serializable) in.readObject();
        }

    }

}