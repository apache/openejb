/**
 *
 *  Copyright 2006 Matt Hogstrom
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.apache.openejb.copymachine;

import java.io.*;

public class CopyMachineObjectInputStream extends java.io.ObjectInputStream
{
   private CopyMachineStream cms = null;

   public CopyMachineObjectInputStream() throws IOException
   {
      super();
   }

   public void setCopyMachineStream(CopyMachineStream cms)
   {
      this.cms = cms;
   }

   //BEGIN Read Methods ######################################################################################################
   public int read() //reads a byte
   {
      return cms.read();
   }

   public int read(byte[] b)
   {
      return cms.read(b);
   }

   public int read(byte[] buf, int off, int len)
   {
      return cms.read(buf,off,len);
   }

   public void readFully(byte[] buf)
   {
      cms.readFully(buf);
   }

   public void readFully(byte[] buf, int off, int len)
   {
      cms.readFully(buf,off,len);
   }


   public boolean readBoolean()
   {
      return cms.readBoolean();
   }

   public byte readByte()
   {
      return cms.readByte();
   }

   public char readChar()
   {
      return cms.readChar();
   }

   public double readDouble()
   {
      return cms.readDouble();
   }

   public float readFloat()
   {
      return cms.readFloat();
   }

   public int readInt()
   {
      return cms.readInt();
   }

   public long readLong()
   {
      return cms.readLong();
   }

   //This is final so we cannot reimplement.  Instead, we call the no arg contructor in our contructor to force ObjectInputStream to call our 
   //readObjectOverride implementation
   /*
   public Object readObject()
   {
      return cms.readObject();
   }
   */

   public Object readUnshared()
   {
      return cms.readUnshared();
   }

   public short readShort()
   {
      return cms.readShort();
   }

   public int readUnsignedByte() throws EOFException
   {
      return cms.readUnsignedByte();
   }

   public int readUnsignedShort()
   {
      return cms.readUnsignedShort();
   }

   public String readUTF()
   {
      return cms.readUTF();
   }

   //other methods from ObjectInputStream ################################################################################################
   public int available()
   {
      return cms.available();
   }

   public void close()
   {
      cms.close();
   }

   public void defaultReadObject()
   {
      cms.defaultReadObject();
   }

   public ObjectInputStream.GetField readFields()
   {
      return cms.readFields();
   }

   public String readLine() //deprecated
   {
      return cms.readLine();
   }

   public void registerValidation(ObjectInputValidation obj, int prio)
   {
      cms.registerValidation(obj,prio);  
   }

   public int skipBytes(int len)
   {
      return cms.skipBytes(len);
   }


   //inherited from input stream ########################################################################################################
   public void mark(int readLimit)
   {
      cms.mark(readLimit);
   }

   public boolean markSupported()
   {
      return cms.markSupported();
   }

   public void reset()
   {
      cms.reset();
   }

   public long skip(long n)
   {
      return cms.skip(n);
   }

   //protected methods from ObjectInputStream #####################################################################################
   //Just making sure that we never call down to the base class... if we end up calling down to these, the customer is somehow extending ObjectInputStream.
   //In this case, we'll just fall back to the original way to copy and object.
   protected boolean enableResolveObject(boolean enable) 
   {
      return cms.enableResolveObject(enable);
   }

   protected ObjectStreamClass readClassDescriptor() 
   {
      return cms.readClassDescriptor();
   }

   protected final Object readObjectOverride() 
   {
      return cms.readObjectOverride();
   }

   protected void readStreamHeader()
   {
      cms.readStreamHeader();
   }

   protected Class resolveClass(ObjectStreamClass desc) 
   {
      return cms.resolveClass(desc);
   }

   protected Object resolveObject(Object obj) 
   {
      return cms.resolveObject(obj);
   }

   protected Class resolveProxyClass(String[] interfaces) 
   {
      return cms.resolveProxyClass(interfaces);
   }


} //end class
