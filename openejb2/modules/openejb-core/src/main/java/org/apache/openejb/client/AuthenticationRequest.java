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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id: AuthenticationRequest.java 444624 2004-03-01 07:17:26Z dblevins $
 */
package org.apache.openejb.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * 
 * @since 11/25/2001
 */
public class AuthenticationRequest implements Request {
    
    /**
     * The principle of the client.  Can be a user name or some other id.
     */
    private transient Object principle;
    /**
     * The client's credentials proves they are who they say they are.  Can be
     * a password, passphrase, certificate, etc.
     */
    private transient Object credentials;

    /**
     * Constructs a new AuthenticationRequest to send to the server.
     */
    public AuthenticationRequest() {
    }
    
    /**
     * Constructs a new AuthenticationRequest to send to the server.
     * 
     * @param principle
     * @param credentials
     */
    public AuthenticationRequest(Object principle, Object credentials) {
        this.principle   = principle;
        this.credentials = credentials;
    }
    
    public byte getRequestType(){
        return AUTH_REQUEST;
    }
    

    /**
     * Returns the client's principle.
     * 
     * @return 
     */
    public Object getPrinciple(){
        return principle;
    }
    
    /**
     * Returns the client's credentials.
     * 
     * @return 
     */
    public Object getCredentials(){
        return credentials;
    }
    
    /**
     * Sets the client's principle.
     * 
     * @param principle
     */
    public void setPrinciple(Object principle){
        this.principle = principle;
    }
    
    /**
     * Sets the clients credentials.
     * 
     * @param credentials
     */
    public void setCredentials(Object credentials){
        this.credentials = credentials;
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
    public void readExternal(ObjectInput in) throws IOException,ClassNotFoundException {
        principle   = in.readObject(); 
        credentials = in.readObject(); 
    }
    /**
     * The object implements the writeExternal method to save its contents
     * by calling the methods of DataOutput for its primitive values or
     * calling the writeObject method of ObjectOutput for objects, strings,
     * and arrays.
     * @serial Data Overriding methods should use this tag to describe
     *             the data layout of this Externalizable object.
     *             List the sequence of element types and, if possible,
     *             relate the element to a public/protected field and/or
     *             method of this Externalizable class.
     * @param out    the stream to write the object to
     * @exception IOException
     *                   Includes any I/O exceptions that may occur
     */
    public void writeExternal(ObjectOutput out) throws IOException {
        out.writeObject(principle  );
        out.writeObject(credentials);
    }
}


