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

public class CopyMachineStream
{
   int[]     ints       = null;  int  intsCount       = 0;    int intsPointer=0;        static final int INTS        = 0;
   short[]   shorts     = null;  int  shortsCount     = 0;    int shortsPointer=0;      static final int SHORTS      = 1;
   float[]   floats     = null;  int  floatsCount     = 0;    int floatsPointer=0;      static final int FLOATS      = 2;
   double[]  doubles    = null;  int  doublesCount    = 0;    int doublesPointer=0;     static final int DOUBLES     = 3;
   char[]    chars      = null;  int  charsCount      = 0;    int charsPointer=0;       static final int CHARS       = 4;
   Object[]  charArrays = null;  int  charArraysCount = 0;    int charArraysPointer=0;  static final int CHARARRAYS  = 5;
   long[]    longs      = null;  int  longsCount      = 0;    int longsPointer=0;       static final int LONGS       = 6;
   byte[]    bytes      = null;  int  bytesCount      = 0;    int bytesPointer=0;       static final int BYTES       = 7;
   Object[]  byteArrays = null;  int  byteArraysCount = 0;    int byteArraysPointer=0;  static final int BYTEARRAYS  = 8;
   boolean[] booleans   = null;  int  booleansCount   = 0;    int booleansPointer=0;    static final int BOOLEANS    = 9;
   Object[]  objects    = null;  int  objectsCount    = 0;    int objectsPointer=0;     static final int OBJECTS     = 10;
   String[]  strings    = null;  int  stringsCount    = 0;    int stringsPointer=0;     static final int STRINGS     = 11;

   ///Object[] objs = new Object[11]; //Each position corresponds to a type above
   //we really should probably break this into the various arrays it was before to simplify the object graph for the GC.  This structure is still around
   //from an aborted design attempt

   int totalObjCount = 0;
   int typeOrderPointer = 0;
   int[] typeOrder = new int[25];  //Keeps an absolute order to make sure we write and read in the same order...
                                   //If we do something crazy such that we dont match the typed read and write methods or call one of the methods 
                                   //to determine how much data is still on the stream, we'll fall back to using the default output stream

   int protocolVersion = 1;

   int byteArrayLeftOverPointer = 0;

   public CopyMachineStream()
   {
   }

   //BEGIN Write Methods ##################################################################################################### 
   private void setNextType(int type)
   {
      //expand the array if necessary
      if(totalObjCount == typeOrder.length )
      {
         int[] temp = new int[typeOrder.length + 25];
         for(int i=0; i<typeOrder.length; i++)
            temp[i] = typeOrder[i];

         typeOrder = temp;
      }

      typeOrder[totalObjCount] = type;
      totalObjCount++;
   }

   public void writeInt(int val)
   {      
      //make sure the int[] is initialized
      if( ints == null )
         ints = new int[10];

      //store the type in order
      setNextType(INTS);

      //expand the array if necessary
      if( intsCount ==  ints.length )
      {
         //expand the array
         int[] temp = new int[ints.length + 10];
         for(int i=0; i<ints.length; i++)
            temp[i] = ints[i];

         ints = temp;
      }

      //put the value into the array
      ints[intsCount] = val;
      intsCount++;
   }

   public void writeShort(int val)
   {
      //make sure the short[] is initialized
      if( shorts == null )
         shorts = new short[10];

      //store the type in order
      setNextType(SHORTS);
      
      //expand the array if necessary
      if( shortsCount ==  shorts.length )
      {
         //expand the array
         short[] temp = new short[shorts.length + 10];
         for(int i=0; i<shorts.length; i++)
            temp[i] = shorts[i];

         shorts = temp;
      }

      //put the value into the array
      shorts[shortsCount] = (short)val;
      shortsCount++;
   }

   public void writeFloat(float val)
   {
      //make sure the float[] is initialized
      if( floats == null )
         floats = new float[10];

      //store the type in order
      setNextType(FLOATS);

      //expand the array if necessary
      if( floatsCount ==  floats.length )
      {
         //expand the array
         float[] temp = new float[floats.length + 10];
         for(int i=0; i<floats.length; i++)
            temp[i] = floats[i];

         floats = temp;
      }

      //put the value floato the array
      floats[floatsCount] = val;
      floatsCount++;
   }

   public void writeDouble(double val)
   {
      //make sure the double[] is initialized
      if( doubles == null )
         doubles = new double[10];

      //store the type in order
      setNextType(DOUBLES);
      
      //expand the array if necessary
      if( doublesCount ==  doubles.length )
      {
         //expand the array
         double[] temp = new double[doubles.length + 10];
         for(int i=0; i<doubles.length; i++)
            temp[i] = doubles[i];

         doubles = temp;
      }

      //put the value doubleo the array
      doubles[doublesCount] = val;
      doublesCount++;
   }

   public void writeChar(int val)
   {
      //make sure the char[] is initialized
      if( chars == null )
         chars = new char[10];

      //store the type in order
      setNextType(CHARS);

      //expand the array if necessary
      if( charsCount ==  chars.length )
      {
         //expand the array
         char[] temp = new char[chars.length + 10];
         for(int i=0; i<chars.length; i++)
            temp[i] = chars[i];

         chars = temp;
      }

      //put the value charo the array
      chars[charsCount] = (char)val;
      charsCount++;
   }

   public void writeChars(String val)
   {
      //make sure the charArray[] is initialized
      if( charArrays == null )
         charArrays = new Object[10];

      //store the type in order
      setNextType(CHARARRAYS);

      //expand the array if necessary
      if( charArraysCount ==  charArrays.length )
      {
         //expand the array
         Object[] temp = new Object[charArrays.length + 10];
         for(int i=0; i<charArrays.length; i++)
            temp[i] = charArrays[i];

         charArrays = temp;
      }

      //put the value into the array
      charArrays[charArraysCount] = val.toCharArray();;
      charArraysCount++;
   }

   public void writeUTF(String val)
   {
      //treating this as a simple writeString... may not be completley kosher to do so...

      //make sure the long[] is initialized
      if( strings == null )
         strings = new String[10];

      //store the type in order
      setNextType(STRINGS);

      //expand the array if necessary
      if( stringsCount ==  strings.length )
      {
         //expand the array
         String[] temp = new String[strings.length + 10];
         for(int i=0; i<strings.length; i++)
            temp[i] = strings[i];

         strings = temp;
      }

      //put the value longo the array
      strings[stringsCount] = val;
      stringsCount++;      
   }

   public void writeLong(long val)
   {
      //make sure the long[] is initialized
      if( longs == null )
         longs = new long[10];

      //store the type in order
      setNextType(LONGS);

      //expand the array if necessary
      if( longsCount ==  longs.length )
      {
         //expand the array
         long[] temp = new long[longs.length + 10];
         for(int i=0; i<longs.length; i++)
            temp[i] = longs[i];

         longs = temp;
      }

      //put the value longo the array
      longs[longsCount] = val;
      longsCount++;
   }

   public void write(int val) //writes a byte
   {
      writeByte(val);
   }

   public void writeByte(int val)
   {
      //make sure the byte[] is initialized
      if( bytes == null )
         bytes = new byte[10];

      //store the type in order
      setNextType(BYTES);

      //expand the array if necessary
      if( bytesCount ==  bytes.length )
      {
         //expand the array
         byte[] temp = new byte[bytes.length + 10];
         for(int i=0; i<bytes.length; i++)
            temp[i] = bytes[i];

         bytes = temp;
      }

      //put the value byteo the array
      bytes[bytesCount] = (byte)val;
      bytesCount++;
   }

   public void write(byte[] val)
   {
      write(val, 0, val.length);
   }

   public void write(byte[] val, int off, int len)
   {
      //make sure the byteArray[] is initialized
      if( byteArrays == null )
         byteArrays = new Object[10];

      //store the type in order
      setNextType(BYTEARRAYS);

      //expand the array if necessary
      if( byteArraysCount ==  byteArrays.length )
      {
         //expand the array
         Object[] temp = new Object[byteArrays.length + 10];
         for(int i=0; i<byteArrays.length; i++)
            temp[i] = byteArrays[i];

         byteArrays = temp;
      }

      //put the value into the array
      byteArrays[byteArraysCount] = val;
      byteArraysCount++;

      //this is not implemented correctly...
      throw new RuntimeException("Not implemented correctly... ignoring off and len");
   }

   public void writeBytes(String val)
   {
      //need to figure out how this should work
      throw new RuntimeException("NotImplemented");
   }

   public void writeBoolean(boolean val)
   {
      //make sure the boolean[] is initialized
      if( booleans == null )
         booleans = new boolean[10];

      //store the type in order
      setNextType(BOOLEANS);

      //expand the array if necessary
      if( booleansCount ==  booleans.length )
      {
         //expand the array
         boolean[] temp = new boolean[booleans.length + 10];
         for(int i=0; i<booleans.length; i++)
            temp[i] = booleans[i];

         booleans = temp;
      }

      //put the value booleano the array
      booleans[booleansCount] = val;
      booleansCount++;
   }

   public void writeObject(Object val)
   {
      //make sure the Object[] is initialized
      if( objects == null )
         objects = new Object[10];

      //store the type in order
      setNextType(OBJECTS);

      //expand the array if necessary
      if( objectsCount ==  objects.length )
      {
         //expand the array
         Object[] temp = new Object[objects.length + 10];
         for(int i=0; i<objects.length; i++)
            temp[i] = objects[i];

         objects = temp;
      }

      //put the value Objecto the array
      objects[objectsCount] = val;
      objectsCount++;
   }

   protected void writeObjectOverride(Object obj) 
   {
      writeObject(obj);
   }


   public void writeUnshared(Object val)
   {
      //We do not have back pointers in this implementation, so treat this just as writeObject...
      writeObject(val);
   }

   //BEGIN Read Methods ######################################################################################################
   private void checkNextType(int type)
   {
      if(typeOrder[typeOrderPointer] == type)
      {
         typeOrderPointer++;
      }
      else
      {
         //Throw an exception back to the calling code to let it know to use a real object stream
         //This will only happen when implementers of read/writeObject do not read/write their types in the same order
         throw new FallbackException();
      }
   }

   public int read() //reads a byte
   {
      return readByte();
   }

   public int read(byte[] b)
   {
      return read(b,0,b.length);
   }

   public int read(byte[] buff, int off, int len)
   {
      //not doing any validation of off and len as valid parameters

      //check that we are attempting to read the correct type
      checkNextType(BYTEARRAYS);

      byte[] byteArray = (byte[])byteArrays[byteArraysPointer];

      int dataLeft = byteArray.length - byteArrayLeftOverPointer;
      if(len >= dataLeft )
      {
         //data will fit into the array

         //copy this data over
         System.arraycopy(byteArray,byteArrayLeftOverPointer,buff,off,byteArray.length);

         //Assuming here that we never want to attempt to read the next object in the stream even if it was a byte array... 
         //The Java API spec (in ObjectOutputStream) says we cannot read a type in as bytes if it was written as another type, but 
         //it does not prohibit us from reading in two byte arrays that were written back to back in a single read call.  The Java
         //code for ObjectInputStream would indicate that we would read back to back byte arrays into this single byte array buff, so we
         //should follow that some day probably.  For now, we'll make the customer call down to us again if they want the second byte array
         //in one buffer.  I think this should be a small percentage, so we can avoid some extra processing here to check the next type for
         //being a byte array and such.

         //reset byteArrayLeftOverPointer
         byteArrayLeftOverPointer = 0;

         return dataLeft;
      }
      else
      {
         //data will not fit... must be calling with multiple buffers somehow

         //copy the data that will fit
         System.arraycopy(byteArray,byteArrayLeftOverPointer,buff,off,off+len);

         //decrement the type counter to still point at this object as having some pieces left to read
         typeOrderPointer--;

         //indicate where we've left off
         byteArrayLeftOverPointer += len;

         return len;
      }

   }

   public void readFully(byte[] buf)
   {
      readFully(buf, 0, buf.length);
   }

   public void readFully(byte[] buf, int off, int len)
   {
      //check that we are attempting to read the correct type
      checkNextType(BYTEARRAYS);
      
      byte[] byteArray = (byte[])byteArrays[byteArraysPointer];

      int dataLeft = byteArray.length - byteArrayLeftOverPointer;
      if(len == dataLeft)
      {
         //great, we're a perfect match
         System.arraycopy(byteArray,byteArrayLeftOverPointer,buf,off,len);

         //reset byteArrayLeftOverPointer
         byteArrayLeftOverPointer = 0;
      }
      else if(len < dataLeft)
      {
         //we have more data than is being asked for
         System.arraycopy(byteArray,byteArrayLeftOverPointer,buf,off,len);

         //decrement typeOrderPointer to reflect we still have data left.... customer must be calling down with multiple buffers
         typeOrderPointer--;

         //set the left over pointer
         byteArrayLeftOverPointer += len;
      }
      else
      {
         //We do not have enough data left in this byte array to match what is being asked for...
         //We'll attempt to continue reading from subsequent byte arrays, but if we hit a non byte array type, we're hosed since
         //we'd then have to violate the Java API spec that says in ObjectOutputStream that objects must be read in in the same 
         //order and type that they were written out in.  The Java code does not prevent this checking, but it would be very
         //hard to implement here as we'd have to convert the non byte array types to bytes and such. 

         System.arraycopy(byteArray,byteArrayLeftOverPointer,buf,off,dataLeft);

         //reset byteArrayLeftOverPointer
         byteArrayLeftOverPointer = 0;

         try
         {
            //for simplicity, just recursively call readFully shifting the amount to be read and the offset
            readFully(buf,off+dataLeft,len-dataLeft);
         }
         catch(FallbackException e)
         {
            //Well crap, we hit the case we didnt want to... Technically, we should never end up here unless the customer is misusing
            //the Java API and taking advantage of its lack of checking for types.

            //Java would have converted the typed objects we ran up against into bytes and then just read them in despite it being
            //against what the Java API spec says in ObjectOutputStream regarding objects should be read in in the same order they 
            //were written.

            //Since java does not actually check that types are written for correct read order, we should technically convert the non byte array
            //objects to bytes and copy them into the byte array, but since I'm lazy right now, i'll just throw our fallback exception.  Eventually,
            //we should remove the checking for checkNextType at the begining, do that manually, and convert non byte array types as we find them.

            throw e;
         }
      }
   }


   public boolean readBoolean()
   {
      //check that we are attempting to read the correct type
      checkNextType(BOOLEANS);
    
      if(booleansPointer < booleans.length)
      {
         return booleans[booleansPointer++];
      }
      else
      {
         //just fallback to the original
         throw new FallbackException();
      }
   }

   public byte readByte()
   {
      //check that we are attempting to read the correct type
      checkNextType(BYTES);
     
      if(bytesPointer < bytes.length)
      {
         return bytes[bytesPointer++];
      }
      else
      {
         //just fallback to the original
         throw new FallbackException();
      }
   }

   public char readChar()
   {
      //check that we are attempting to read the correct type
      checkNextType(CHARS);
   
      if(charsPointer < chars.length)
      {
         return chars[charsPointer++];
      }
      else
      {
         //just fallback to the original
         throw new FallbackException();
      }
   }

   public double readDouble()
   {
      //check that we are attempting to read the correct type
      checkNextType(DOUBLES);
     
      if(doublesPointer < doubles.length)
      {
         return doubles[doublesPointer++];
      }
      else
      {
         //just fallback to the original
         throw new FallbackException();
      }
   }

   public float readFloat()
   {
      //check that we are attempting to read the correct type
      checkNextType(FLOATS);
    
      if(floatsPointer < floats.length)
      {
         return floats[floatsPointer++];
      }
      else
      {
         //just fallback to the original
         throw new FallbackException();
      }
   }

   public int readInt()
   {
      //check that we are attempting to read the correct type
      checkNextType(INTS);
    
      if(intsPointer < ints.length)
      {
         return ints[intsPointer++];
      }
      else
      {
         //just fallback to the original
         throw new FallbackException();
      }
   }

   public long readLong()
   {
      //check that we are attempting to read the correct type
      checkNextType(LONGS);
     
      if(longsPointer < longs.length)
      {
         return longs[longsPointer++];
      }
      else
      {
         //just fallback to the original
         throw new FallbackException();
      }
   }

   public Object readObject()
   {
      //check that we are attempting to read the correct type
      checkNextType(OBJECTS);
     
      if(objectsPointer < objects.length)
      {
         return CopyMachine.copyObject(objects[objectsPointer++]);
      }
      else
      {
         //just fallback to the original
         throw new FallbackException();
      }
   }

   protected Object readObjectOverride() 
   {
      return readObject();
   }

   public Object readUnshared()
   {
      //we do not have back pointers in this implementation, so treat just like readObject
      return readObject();
   }

   public short readShort()
   {
      //check that we are attempting to read the correct type
      checkNextType(SHORTS);
    
      if(shortsPointer < shorts.length)
      {
         return shorts[shortsPointer++];
      }
      else
      {
         //just fallback to the original
         throw new FallbackException();
      }
   }

   public int readUnsignedByte() throws EOFException
   {
      //assuming the only way to write an unsigned byte is through write or writeByte... there may be other possible ways

      int b = read();
      if(b < 0)
         throw new EOFException();
      return b;

   }

   public int readUnsignedShort()
   {
      //assuming the only way to write an unsigned short is through writeShort, though there are other possible ways
      return readShort();
   }

   public String readUTF()
   {
      //treating this basically as a readString... may not be totally correct

      //check that we are attempting to read the correct type
      checkNextType(STRINGS);
    
      if(stringsPointer < strings.length)
      {
         return (String)CopyMachine.copyObject(strings[stringsPointer++]);
      }
      else
      {
         //just fallback to the original
         throw new FallbackException();
      }
   }

                              
   //other methods from ObjectOutputStream ######################################################################################
   public void close() {} //not applicable
   public void flush() {} //not applicable

   public void reset() 
   {
      //need to figure this out
      throw new RuntimeException("NotImplemented");
   }

   public void useProtocolVersion(int version)
   {
      protocolVersion = version;
   }

   public void defaultWriteObject()
   {
      //We do not have to account for classloader issues in this layer, so simply ignore this
   }

   public ObjectOutputStream.PutField putFields()
   {
      //need to figure this out
      throw new RuntimeException("NotImplemented");
   }

   public void writeFields()
   {
      //need to figure this out
      throw new RuntimeException("NotImplemented");
   }

   //protected methods from ObjectOutputStream #####################################################################################
   //Just making sure that we never call down to the base class... if we end up calling down to these, the customer is somehow extending ObjectOutputStream.
   //In this case, we'll just fall back to the original way to copy and object.

   protected void annotateClass(Class cl)
   {
      throw new FallbackException();
   }

   protected void annotateProxyClass(Class cl) 
   {
      throw new FallbackException();
   }

   protected void drain()
   {
      throw new FallbackException();
   }

   protected boolean enableReplaceObject(boolean enable)
   {
      throw new FallbackException();
   }

   protected Object replaceObject(Object obj) 
   {
      throw new FallbackException();
   }

   protected void writeClassDescriptor(ObjectStreamClass desc) 
   {
      throw new FallbackException();
   }

   protected void writeStreamHeader() 
   {
      throw new FallbackException();
   }


   //other methods from ObjectInputStream ################################################################################################
   public int available()
   {
      //need to figure this out
      throw new RuntimeException("NotImplemented");
   }

   //already covered above with the method from ObjectOutputStream
   //public void close(){}

   public void defaultReadObject()
   {
      //We do not have to account for cross classloader issues in this layser, so simply ignore this
   }

   public ObjectInputStream.GetField readFields()
   {
      //need to figure this out
      throw new RuntimeException("NotImplemented");
   }

   public String readLine() //deprecated
   {
      //need to figure this out
      throw new RuntimeException("NotImplemented");
   }

   public void registerValidation(ObjectInputValidation obj, int prio)
   {
      //need to figure this out
      throw new RuntimeException("NotImplemented");
   }

   public int skipBytes(int len)
   {
      //need to figure this out
      throw new RuntimeException("NotImplemented");
   }


   //inherited from input stream ########################################################################################################
   public void mark(int readLimit) 
   {
   }

   public boolean markSupported()
   {
      return false;
   }

   //already defined in ObjectOutputStreamMethods
   //public void reset()

   public long skip(long n)
   {
      //need to figure this out... ObjectInputStream implements this, but will be difficult to do here.
      throw new RuntimeException("NotImplemented");
   }

   //protected methods from ObjectInputStream #####################################################################################
   //Just making sure that we never call down to the base class... if we end up calling down to these, the customer is somehow extending ObjectInputStream.
   //In this case, we'll just fall back to the original way to copy and object.
   protected boolean enableResolveObject(boolean enable) 
   {
      throw new FallbackException();
   }

   protected ObjectStreamClass readClassDescriptor() 
   {
      throw new FallbackException();
   }

   protected void readStreamHeader()
   {
      throw new FallbackException();
   }

   protected Class resolveClass(ObjectStreamClass desc) 
   {
      throw new FallbackException();
   }

   protected Object resolveObject(Object obj) 
   {
      throw new FallbackException();
   }

   protected Class resolveProxyClass(String[] interfaces) 
   {
      throw new FallbackException();
   }


} //end class
