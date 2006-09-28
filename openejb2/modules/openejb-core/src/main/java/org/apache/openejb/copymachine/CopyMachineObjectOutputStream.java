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

public class CopyMachineObjectOutputStream extends java.io.ObjectOutputStream
{
   private CopyMachineStream cms = null;

   public CopyMachineObjectOutputStream() throws IOException
   {
      super();
      cms = new CopyMachineStream();
   }

   public CopyMachineStream getCopyMachineStream()
   {
      return cms;
   }

   //BEGIN Write Methods ##################################################################################################### 

   public void writeInt(int val)
   {      
      cms.writeInt(val);
   }

   public void writeShort(int val)
   {
      cms.writeShort(val);
   }

   public void writeFloat(float val)
   {
      cms.writeFloat(val);
   }

   public void writeDouble(double val)
   {
      cms.writeDouble(val);
   }

   public void writeChar(int val)
   {
      cms.writeChar(val);
   }

   public void writeChars(String val)
   {
      cms.writeChars(val);
   }

   public void writeUTF(String val)
   {
      cms.writeUTF(val);      
   }

   public void writeLong(long val)
   {
      cms.writeLong(val);
   }

   public void write(int val) //writes a byte
   {
      cms.write(val);
   }

   public void writeByte(int val)
   {
      cms.writeByte(val);
   }

   public void write(byte[] val)
   {
      cms.write(val);
   }

   public void write(byte[] val, int off, int len)
   {
      cms.write(val,off,len);
   }

   public void writeBytes(String val)
   {
      cms.writeBytes(val);
   }

   public void writeBoolean(boolean val)
   {
      cms.writeBoolean(val);
   }

   //This is a final method so we cannot reimplement.  We instead call the no arg contructor in our contructor so as to make the writeObject call our 
   //implementation of writeObjectOverride.
   /*
   public void writeObject(Object val)
   {
      cms.writeObject(val);
   }
   */

   protected final void writeObjectOverride(Object obj) 
   {
      cms.writeObjectOverride(obj);
   }

   public void writeUnshared(Object val)
   {
      cms.writeUnshared(val);
   }

   //other methods from ObjectOutputStream ######################################################################################
   public void close() 
   {
      cms.close();
   }

   public void flush() 
   {
      cms.flush();
   }

   public void reset() 
   {
      cms.reset();
   }

   public void useProtocolVersion(int version)
   {
      cms.useProtocolVersion(version);
   }

   public void defaultWriteObject()
   {
      cms.defaultWriteObject();
   }

   public ObjectOutputStream.PutField putFields()
   {
      return cms.putFields();
   }

   public void writeFields()
   {
      cms.writeFields();
   }

   //protected methods from ObjectOutputStream #####################################################################################
   //just making sure that we never call down to the base class

   protected void annotateClass(Class cl)
   {
      cms.annotateClass(cl);
   }

   protected void annotateProxyClass(Class cl) 
   {
      cms.annotateProxyClass(cl);
   }

   protected void drain()
   {
      cms.drain();
   }

   protected boolean enableReplaceObject(boolean enable)
   {     
      return cms.enableReplaceObject(enable);
   }

   protected Object replaceObject(Object obj) 
   {
      return cms.replaceObject(obj);
   }

   protected void writeClassDescriptor(ObjectStreamClass desc) 
   {
      cms.writeClassDescriptor(desc);
   }

   protected void writeStreamHeader() 
   {
      cms.writeStreamHeader();
   }

} //end class
