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
package org.apache.openejb.copymachine;

import java.util.IdentityHashMap;
import java.util.*;
import java.lang.reflect.*;
import sun.misc.Unsafe;

import java.io.*;

public class CopyMachine
{

   private static Unsafe unsafe = Unsafe.getUnsafe();
   private static Class [] objectOutputStreamClass = new Class[1]; 
   private static IdentityHashMap noReadWriteObjectClasses = new IdentityHashMap(1024);
   private static IdentityHashMap readWriteObjectClasses = new IdentityHashMap(1024);

   private static boolean ignoreReadWriteObject = Boolean.getBoolean("org.apache.openejb.copymachine.CopyObject.ignoreReadWriteObject");

   public CopyMachine()
   {
   }

   /*
   ** This is the main entry point for deep copy operations.  The object to be copied is passed in to
   ** the copyObject method.  
   */
   static public Object copyObject(Object o)
   {
      HashMap objectList = new HashMap(1024);         // 12 is a SWAG at how many objects we'll need to copy

      return copyObject(o, objectList);
   }

   /*
   **  This method is invoked as part of a recursive copy operation on the passed object.  It the HashMap ol
   **  contains the object references of original objects and their copied counterparts.  This allows an 
   ** Object that is referenced in several places to make sure all references point to the same object.
   */
   static protected Object copyObject(Object o, HashMap ol)
   {
      // If the object reference is null there is nothing to do.
      if (o == null) return null;
      Object copyOfOrig = null;

      try
      {
         // Get the object's class once for efficiency.
         Class ooc = o.getClass();

         /*
         ** If this object is a primitive type it's immutable...let's return it.
         */
         if ((ooc == String.class    ) ||
             (ooc == Integer.class   ) ||
             (ooc == Long.class      ) ||
             (ooc == Boolean.class   ) ||
             (ooc == Byte.class      ) ||
             (ooc == Character.class ) ||
             (ooc == Float.class     ) ||
             (ooc == Double.class    ) ||
             (ooc == Short.class))
         {
            return o;
         }

         /*
         ** Okay, not a primitive...let's see if we've worked on this one before.
         */
         Object prevCopy = ol.get(o);
         if (prevCopy != null) return prevCopy;


         Class oct = ooc.getComponentType();


         /*
         **  Test number 1.  Is the passed object an array?  If so it will either be an array of primitives
         **  or an array of some type of object.
         */
         if ( ooc.isArray() )
         {
            /* 
            ** We've gotten to this point because the object we've been passed is an array of primitives.
            ** As such we will invoke clone to make the copy and call it a day.  No need to do a recurisive 
            ** invocation so once the copy is made we're outta here.
            */
            if (oct != null && oct.isPrimitive())
            {

               Object copiedObject = null;
               while (true)
               {
                  if (oct == int.class)
                  {
                     int[]     t = (int[])    ( (int[])     o).clone() ;  copiedObject = (Object) t;  break;
                  }
                  if (oct == long.class)
                  {
                     long[]    t = (long[])   ( (long[])    o).clone() ;  copiedObject = (Object) t;  break;
                  }
                  if (oct == float.class)
                  {
                     float[]   t = (float[])  ( (float[])   o).clone() ;  copiedObject = (Object) t;  break;
                  }
                  if (oct == double.class)
                  {
                     double[]  t = (double[]) ( (double[])  o).clone() ;  copiedObject = (Object) t;  break;
                  }
                  if (oct == boolean.class)
                  {
                     boolean[] t = (boolean[])( (boolean[]) o).clone() ;  copiedObject = (Object) t;  break;
                  }
                  if (oct == byte.class)
                  {
                     byte[]    t = (byte[])   ( (byte[])    o).clone() ;  copiedObject = (Object) t;  break;
                  }
                  if (oct == short.class)
                  {
                     short[]   t = (short[])  ( (short[])   o).clone() ;  copiedObject = (Object) t;  break;
                  }
                  if (oct == char.class)
                  {
                     char[]    t = (char[])   ( (char[])    o).clone() ;  copiedObject = (Object) t;  break;
                  }

                  throw new RuntimeException("Unknown Primitive type in copy.  Class="+ooc.getName());
               }
               ol.put(o, copiedObject); 
               return copiedObject;
            }

            /*
            **  Ok, since we've made it this far the array is an array of Objects.  Let's new up a new array and iterate through
            **  each object in the array.
            */
            int arrayLength = Array.getLength(o);
            Object [] targetArray = (Object []) Array.newInstance(oct, arrayLength);

            /*
            ** Place our newly allocated array into the HashMap.  This will ensure that if we see this object again
            ** we will not do another copy and go into a loop.
            */
            ol.put(o, targetArray);     

            for (int y=0; y<arrayLength; y++)
            {
               targetArray[y] = copyObject( ((Object) (((Object [])o)[y])), ol );  // Copy this object.
            }

            return(Object) targetArray;  // Return the copied object

         }
         else
         {
            // This is an object...let's do it.

            // Get the class from the current class Loader... ignore classloaders for now... will need to enhance the cache of objects
            //that do not implment read/writeObject when we get back to this...


            //ClassLoader cl = Thread.currentThread().getContextClassLoader();
            //Class       tc = cl.loadClass(o.getClass().getName());

            copyOfOrig = unsafe.allocateInstance(ooc);     // New up a copy of the object we are copying.  Remember, only the memory has been allocated.            


            if(!ignoreReadWriteObject) //make sure we execute the read/writeObject methods if told to care about them
            {
               //check the cache
               if(noReadWriteObjectClasses.get(ooc) == null) //this object does either implementes read/writeObject or has not been seen before
               {
                  //check if this implementes read/writeObject... may want to implement a second cache of objects that do implement read/writeObject here 
                  //to avoid the reflection and exception
                  try
                  {
                     ReadWriteMethodWrapper readWriteMethodWrapper = (ReadWriteMethodWrapper) readWriteObjectClasses.get(ooc);
                     if(readWriteMethodWrapper == null)
                     {       
                        //check if this is a new class and put in the appropriate cache... exception will get in the noReadWriteObjectClasses cache

                        Method writeObjectMethod = ooc.getDeclaredMethod("writeObject", new Class[] {ObjectOutputStream.class});
                        writeObjectMethod.setAccessible(true);
                        Method readObjectMethod = ooc.getDeclaredMethod("readObject",new Class[] { ObjectInputStream.class });
                        readObjectMethod.setAccessible(true);
                        readWriteMethodWrapper = new ReadWriteMethodWrapper(writeObjectMethod,readObjectMethod);
                        readWriteObjectClasses.put(ooc,readWriteMethodWrapper);
                     }

                     //Class does implement read/writeObject

                     CopyMachineObjectOutputStream oos = new CopyMachineObjectOutputStream();
                     //writeObjectMethod.setAccessible(true);
                     readWriteMethodWrapper.writeObjectMethod.invoke(o, new Object[] {oos});

		     //Method readObjectMethod = ooc.getDeclaredMethod("readObject",new Class[] { ObjectInputStream.class });
                     //readObjectMethod.setAccessible(true);
                     CopyMachineObjectInputStream ois = new CopyMachineObjectInputStream();
                     ois.setCopyMachineStream(oos.getCopyMachineStream());
                     readWriteMethodWrapper.readObjectMethod.invoke(copyOfOrig,new Object[] {ois});

                     //put the object into the cache to resolve circular references
                     ol.put(o,copyOfOrig);

                     //we are now done, so simply return what we have
                     return copyOfOrig;

                  }
                  catch(NoSuchMethodException e)  
                  {
                     //Class does not implement read/writeObject
                     noReadWriteObjectClasses.put(ooc,ooc);
                     //eat it
                  }
               }
            }

            //this object does not implement read/writeObject or we do not care about if it does, so cary on with the blind deep copy

            while (ooc != null)
            {
               /* 
               **  Hmmm...not a primitive, not an array, must be an object reference.  Let's start the copying.
               */
               Field [] oof = ooc.getDeclaredFields();              // Get the list of fields we are dealing with

               for (int x=0; x< oof.length; x++)
               {
                  // Let's look quickly at the modifiers.  If the field is a static we'll ignore it.
                  // If it's volatile then we'll skip it also.
                  oof[x].setAccessible(true);
                  int modifiers = oof[x].getModifiers();
                  if (Modifier.isStatic(modifiers)) continue;
                  Class dataType = oof[x].getType();

                  // If this field is a primitive (not a wrapper class) we will simply set the value.
                  if (dataType.isPrimitive())
                  {
                     if ( dataType == int.class     )
                     {
                        unsafe.putInt(    copyOfOrig, unsafe.objectFieldOffset(oof[x]), oof[x].getInt(o)     ); continue;
                     }
                     if ( dataType == long.class    )
                     {
                        unsafe.putLong(   copyOfOrig, unsafe.objectFieldOffset(oof[x]), oof[x].getLong(o)    ); continue;
                     }
                     if ( dataType == float.class   )
                     {
                        unsafe.putFloat(  copyOfOrig, unsafe.objectFieldOffset(oof[x]), oof[x].getFloat(o)   ); continue;
                     }
                     if ( dataType == double.class  )
                     {
                        unsafe.putDouble( copyOfOrig, unsafe.objectFieldOffset(oof[x]), oof[x].getDouble(o)  ); continue;
                     }
                     if ( dataType == boolean.class )
                     {
                        unsafe.putBoolean(copyOfOrig, unsafe.objectFieldOffset(oof[x]), oof[x].getBoolean(o) ); continue;
                     }
                     if ( dataType == byte.class    )
                     {
                        unsafe.putByte(   copyOfOrig, unsafe.objectFieldOffset(oof[x]), oof[x].getByte(o)    ); continue;
                     }
                     if ( dataType == short.class   )
                     {
                        unsafe.putShort(  copyOfOrig, unsafe.objectFieldOffset(oof[x]), oof[x].getShort(o)   ); continue;
                     }
                     if ( dataType == char.class    )
                     {
                        unsafe.putChar(   copyOfOrig, unsafe.objectFieldOffset(oof[x]), oof[x].getChar(o)    ); continue;
                     }
                     throw new RuntimeException("Unrecognized primitive type during copy.\nType="+dataType.toString());
                  }

                  // Okay, this isn't a primitive type...copy it
                  unsafe.putObject( copyOfOrig, unsafe.objectFieldOffset(oof[x]), copyObject(oof[x].get(o), ol));
               }
               ooc = ooc.getSuperclass();
            }
         }
         ol.put(o, copyOfOrig);                                  // Add the new object to avoid recursion.
         return copyOfOrig;
      }
      catch (Exception e)
      {
         System.out.println(e.getMessage()); 
         e.printStackTrace();
         e.getCause().printStackTrace();
      }
      return o;
   }


   public static void main(String args[]) {
   CopyMachine c = new CopyMachine();
   Object o = null;

     TestKey tki = new TestKey();
     tki.field1 = "Imbedded String 1";
     tki.field2 = "Imbedded String 2";
     tki.integer1 = new Integer(1);
     tki.testKey1 = tki;

     TestKey tk = new TestKey();
     tk.field1 = "Field 1 String";
     tk.field2 = "Field 2 String";
     tki.testKey1 = tk;
         // Make a reasonable large test object. Note that this doesn't
         // do anything useful -- it is simply intended to be large, have
         // several levels of references, and be somewhat random. We start
         // with a hashtable and add vectors to it, where each element in
         // the vector is a Date object (initialized to the current time),
         // a semi-random string, and a (circular) reference back to the
         // object itself. In this case the resulting object produces
         // a serialized representation that is approximate 700K.
         Hashtable obj = new Hashtable();
         for (int i = 0; i < 100; i++) {
             Vector v = new Vector();
             for (int j = 0; j < 100; j++) {
                 v.addElement(new Object[] { 
                     new Date(), 
                     "A random number: " + Math.random(),
                     obj
                  });
             }
             obj.put(new Integer(i), v);
         } 
         long CDRstartTime = System.currentTimeMillis();

         Object objectToBeCopied = (Object)obj;
         int  iter = 1000;

         if (tki instanceof java.io.Serializable)  System.out.println("True"); else System.out.println("False");

     System.out.println("Starting ObjectStream warmup");
     try {
       for (int y=0; y<iter; y++) {
         ByteArrayOutputStream bstream = new ByteArrayOutputStream();
         ObjectOutputStream objOstream = new ObjectOutputStream(bstream);

         objOstream.writeObject(objectToBeCopied);
         objOstream.flush();
         objOstream.close();

         ByteArrayInputStream bis = new ByteArrayInputStream( bstream.toByteArray());
         ObjectInputStream ois = new ObjectInputStream(bis);
         o = ois.readObject();
         if (tki.field1.equals(tki.field2)) { System.out.println("Something is wrong"); }
       } 
     } catch (Exception e) {System.out.println("IO Error"); System.out.println(e.getMessage()); e.printStackTrace(); }



     System.out.println("         Invoking GC");

     System.gc();
     System.out.println("         Starting timed copy");
     CDRstartTime = System.currentTimeMillis();

     try {
       for (int y=0; y<iter; y++) {
         ByteArrayOutputStream bstream = new ByteArrayOutputStream();
         ObjectOutputStream objOstream = new ObjectOutputStream(bstream);

         objOstream.writeObject(objectToBeCopied);
         objOstream.flush();
         objOstream.close();

         ByteArrayInputStream bis = new ByteArrayInputStream( bstream.toByteArray());
         ObjectInputStream ois = new ObjectInputStream(bis);
         o = ois.readObject();
       } 
     } catch (Exception e) {System.out.println("IO Error"); System.out.println(e.getMessage()); e.printStackTrace(); }

     long CDRendTime = System.currentTimeMillis();

 //    System.loadLibrary("ObjectUtils");

     System.out.println("Starting CopyMachine warmup");

     System.gc();
     for (int y=0; y<iter; y++) {
       o = c.copyObject(objectToBeCopied);    
     }

     System.out.println("         Invoking GC");

     System.gc();
     System.out.println("         Starting timed copy of CopyMachine");
     long MYstartTime = System.currentTimeMillis();
     for (int y=0; y<iter; y++) {
       o = c.copyObject(objectToBeCopied);
     }
     long MYendTime = System.currentTimeMillis();
 //    System.out.println("tki.a="+((TestKey)o).a);

     try {
     FileOutputStream fstream = new FileOutputStream("originalCopy");
     ObjectOutputStream objOstream = new ObjectOutputStream(fstream);

     objOstream.writeObject(o);
     objOstream.flush();
     objOstream.close();
     } catch (Exception e) {System.out.println("Error writing file.  Error = "+e.getMessage());}

     try {
     FileOutputStream fstream = new FileOutputStream("copyMachine");
     ObjectOutputStream objOstream = new ObjectOutputStream(fstream);

     objOstream.writeObject(objectToBeCopied);
     objOstream.flush();
     objOstream.close();
     } catch (Exception e) {System.out.println("Error writing file.  Error = "+e.getMessage()); e.printStackTrace();}


     System.out.println("CDR Output Stream Copy Time = "+(CDRendTime - CDRstartTime));
     System.out.println("               My Copy time = "+(MYendTime  - MYstartTime) );
   } 


}

   class ReadWriteMethodWrapper
   {
      public Method writeObjectMethod;
      public Method readObjectMethod;

      public ReadWriteMethodWrapper(Method w, Method r)
      {
         writeObjectMethod = w;
         readObjectMethod = r;
      }
   }
