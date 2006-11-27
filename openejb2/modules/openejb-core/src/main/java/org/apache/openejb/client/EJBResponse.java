/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.CORBA.Stub;

import org.omg.CORBA.ORB;

/**
 * 
 * @since 11/25/2001
 */
public class EJBResponse implements ClusteredResponse {
    private transient int responseCode = -1;
    private transient Object result;
    private transient ServerMetaData[] servers;

    public EJBResponse(){
    }

    public EJBResponse(int code, Object obj){
        responseCode = code;
        result = obj;
    }

    public int getResponseCode(){
        return responseCode;
    }
    
    public Object getResult(){
        return result;
    }
    
    public void setResponse(int code, Object result){
        this.responseCode = code;
        this.result       = result;
    }


    public ServerMetaData[] getServers() {
        return servers;
    }

    public void setServers(ServerMetaData[] servers) {
        this.servers = servers;
    }

    public String toString(){
        StringBuffer s = null;
        switch (responseCode) {
            case EJB_APP_EXCEPTION:
                s = new StringBuffer( "EJB_APP_EXCEPTION" );
                break;
            case EJB_ERROR:
                s = new StringBuffer( "EJB_ERROR" );
                break;
            case EJB_OK:
                s = new StringBuffer( "EJB_OK" );
                break;
            case EJB_OK_CREATE:
                s = new StringBuffer( "EJB_OK_CREATE" );
                break;
            case EJB_OK_FOUND:
                s = new StringBuffer( "EJB_OK_FOUND" );
                break;
            case EJB_OK_FOUND_COLLECTION:
                s = new StringBuffer( "EJB_OK_FOUND_COLLECTION" );
                break;
            case EJB_OK_FOUND_ENUMERATION:
                s = new StringBuffer( "EJB_OK_FOUND_ENUMERATION" );
                break;
            case EJB_OK_NOT_FOUND:
                s = new StringBuffer( "EJB_OK_NOT_FOUND" );
                break;
            case EJB_SYS_EXCEPTION:
                s = new StringBuffer( "EJB_SYS_EXCEPTION" );
                break;
            default:
                s = new StringBuffer( "UNKNOWN_RESPONSE" );
        }
        s.append(':').append(result);
    
        return s.toString();
    }

    /**
     * The object implements the readExternal method to restore its
     * contents by calling the methods of DataInput for primitive
     * types and readObject for objects, strings and arrays.  The
     * readExternal method must read the values in the same sequence
     * and with the same types as were written by writeExternal.
     *
     * @param in the stream to read data from in order to restore the object
     * @exception IOException if I/O errors occur
     * @exception ClassNotFoundException If the class for an object being
     *              restored cannot be found.
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        responseCode = in.readByte();
        // TODO: We should try and guess the content type coming in
        // so we can take an active part in reading it in
        // as we do with the other reponse objects
        result = in.readObject();
        if (result instanceof Stub) {
            Stub stub = (Stub)result;
            ORB orb = null;
            try {
                Context initialContext = new InitialContext();
                orb = (ORB) initialContext.lookup("java:comp/ORB");
            } catch (NamingException e) {
                throw new IOException("Unable to connect PortableRemoteObject stub to an ORB, no ORB bound to java:comp/ORB");
            }
            stub.connect(orb);
        }
        
        servers = (ServerMetaData[]) in.readObject();
    }
    
    /**
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     *
     * @serialData Overriding methods should use this tag to describe
     *             the data layout of this Externalizable object.
     *             List the sequence of element types and, if possible,
     *             relate the element to a public/protected field and/or
     *             method of this Externalizable class.
     *
     * @param out the stream to write the object to
     * @exception IOException Includes any I/O exceptions that may occur
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        //out.writeByte((byte)responseCode);
        out.writeByte(responseCode);
        out.writeObject(result);
        out.writeObject(servers);
    }
}
