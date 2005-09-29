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
 * $Id$
 */
package org.openejb.client;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * 
 * @since 11/25/2001
 */
public class ServerMetaData implements Externalizable{

    transient int port;

    /**
     * Stores the server's IP as an InetAddress instead of a String. Creating a 
     * socket with a string creates a new InetAddress object anyway.  Storing it
     * here save us from having to do it more than once
     */
    transient InetAddress address;

    public ServerMetaData(){

    }
    
    public ServerMetaData(String host, int port) throws UnknownHostException{
        this.address = InetAddress.getByName( host );
        this.port = port;
    }

    public int getPort(){
        return port;
    }

    public InetAddress getAddress(){
        return address;
    }

    public void setPort(int port){
        this.port = port;
    }

    public void setAddress(InetAddress address){
        this.address = address;
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
        // byte[] IP = new byte[4];
        
        // IP[0] = in.readByte();
        // IP[1] = in.readByte();
        // IP[2] = in.readByte();
        // IP[3] = in.readByte();
        StringBuffer IP = new StringBuffer(15);

        IP.append( in.readByte() ).append('.');
        IP.append( in.readByte() ).append('.');
        IP.append( in.readByte() ).append('.');
        IP.append( in.readByte() );
     ///IP.append( in.readUnsignedByte() ).append('.');
     ///IP.append( in.readUnsignedByte() ).append('.');
     ///IP.append( in.readUnsignedByte() ).append('.');
     ///IP.append( in.readUnsignedByte() );
        
     ///IP += in.readUnsignedByte() + '.';
     ///IP += in.readUnsignedByte() + '.';
     ///IP += in.readUnsignedByte() + '.';
     ///IP += in.readUnsignedByte();
//        System.out.println(IP.toString());        
        try{
            address = InetAddress.getByName( IP.toString() );
        } catch (java.net.UnknownHostException e){
            throw new IOException("Cannot read in the host address "+IP+": The host is unknown");
        }
        
        port    = in.readInt();
        
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
        byte[] addr = address.getAddress();
        
        out.writeByte(addr[0]);
        out.writeByte(addr[1]);
        out.writeByte(addr[2]);
        out.writeByte(addr[3]);
        
        out.writeInt(port);
    }

}


