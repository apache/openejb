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

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.net.InetAddress;
import java.net.UnknownHostException;


/**
 * $Rev$ $Date$
 */
public class ServerMetaData implements Externalizable{
    transient String nodeName;
    transient int port;

    /**
     * Stores the server's IP as an InetAddress instead of a String. Creating a 
     * socket with a string creates a new InetAddress object anyway.  Storing it
     * here save us from having to do it more than once
     */
    transient InetAddress address;

    /**
     * The Java API: InetAddress.getLocalHost() is slow, cache the value to be used
     * later on.
     */
    private transient static InetAddress localHost;

    static {
        try {
            localHost = InetAddress.getLocalHost();
        } catch( UnknownHostException e ) {
            localHost = null;
        }
    }

    public ServerMetaData(){
        nodeName = "";
        address = localHost;
    }
    
    public ServerMetaData(String nodeName, String host, int port) throws UnknownHostException {
        this.nodeName = nodeName;
        this.address = InetAddress.getByName( host );
        this.port = port;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public int getPort(){
        return port;
    }

    public void setPort(int port){
        this.port = port;
    }

    public InetAddress getAddress(){
        return address;
    }

    public void setAddress(InetAddress address){
        this.address = address;
    }

    public boolean equals(Object obj) {
        if (false == obj instanceof ServerMetaData) {
            return false;
        }
        ServerMetaData other = (ServerMetaData) obj;
        
        if (false == nodeName.equals(other.nodeName)) {
            return false;
        } else if (false == address.equals(other.address)) {
            return false;
        } else if (false == (port == other.port)) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return nodeName.hashCode() * address.hashCode() * port;
    }

    public String toString() {
        return nodeName + " " + address + ":" + port;
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
        nodeName = in.readUTF();
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
        out.writeUTF(nodeName);
        
        byte[] addr = address.getAddress();
        
        out.writeByte(addr[0]);
        out.writeByte(addr[1]);
        out.writeByte(addr[2]);
        out.writeByte(addr[3]);
        
        out.writeInt(port);
    }
}

