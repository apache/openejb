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
package org.openejb.util.io;


import java.io.Externalizable;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.NotSerializableException;
import java.io.ObjectOutput;
import java.io.ObjectStreamConstants;
import java.io.OutputStream;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import org.openejb.util.ArrayStack;

/**
 * This is a faster ObjectOutputStream for high volume object serialization. <BR><BR>
 * 
 * This ObjectOutputStream's strength is that it can be reused unlike the Sun ObjectOutputStream
 * which needs to be discarded and re-instantiated.  This ObjectOutputStream also has the main
 * algorithm inlined.  This of coarse looks terrible but is faster then delegating everything
 * to reusable methods.  This is implementation is not finished yet as it does not use the writeObject
 * callback method and does not serialize exceptions to the stream as it should.<BR><BR>
 * 
 * We chose not to implement the formula to generate the serialVersionID for classes that do not specify
 * one explicitly as this adds a lot of overhead the first time a new class type is introduced into the stream.
 * This will most likely be added as an optional function.<BR><BR>
 * 
 * This ObjectOutputStream is not faster in all situations.  When doing only a few dozen serializations during
 * the life of the VM you will want to use the java.io.ObjectOutputStream.  You'll notice however that this
 * ObjectOutputStream performs considerably faster with a high number of serializations.  This makes this implementation
 * ideal for handling the heavy load of typical a server.<BR><BR>
 * 
 * Run the SerializationPerformanceTest to get a better idea on how this OutputPerforms on your machine.<BR><BR>
 * 
 * <PRE>
 * example:
 * 
 * $java org.openejb.test.SerializationPerformanceTest 20 100 10
 * </PRE>
 * <BR>
 * Running the test with the above parameters will typically give results indicating
 * this ObjectOutputStream running in 64% of the time it take the java.io.ObjectOutputSteam
 * to complete, i.e. about 36% faster.
 * 
 * @author David Blevins
 * @version 0.50, 01/11/2000
 * @since OpenEJB 1.0
 */
public class ObjectOutputStream extends OutputStream implements ObjectOutput, ObjectStreamConstants {

    /**
     * Creates an ObjectOutputStream that writes to the specified OutputStream.
     * The stream header is written to the stream. The caller may want to call
     * flush immediately so that the corresponding ObjectInputStream can read
     * the header immediately.
     *
     * @exception IOException Any exception thrown by the underlying OutputStream.

     */
//    private Handles handles;
    private OutputStream out;
    private ArrayStack classDescStack;
    private byte buf[] = new byte[5000];
    /**
     * The number of valid bytes in the buffer.
     */
    private int count;

    public ObjectOutputStream(OutputStream out) throws IOException {
        this.out = out;

        classDescStack = new ArrayStack();
    }

    public void reset() throws IOException{
        resetStream();
        count = 0;
    }

    public void serializeObject(Object obj, OutputStream out) throws NotSerializableException, IOException{
        this.out = out;
        serializeObject(obj);
    }

    public void serializeObject(Object obj) throws NotSerializableException, IOException{

        if ( !Serializable.class.isAssignableFrom(obj.getClass()) && !Externalizable.class.isAssignableFrom(obj.getClass()) ) {
            throw new NotSerializableException(obj.getClass().getName());
        }

        reset();

        writeShort(STREAM_MAGIC);
        writeShort(STREAM_VERSION);
        writeObject(obj);

    }


    public void writeObject(Object obj) throws IOException{
        try {
            if ( obj == null ) {
                write(TC_NULL);
                return;
            }
            Class clazz = obj.getClass();
            ClassDescriptor classDesc = null;

            if ( clazz == ClassDescriptor.class ) classDesc = (ClassDescriptor)obj;
            else classDesc = ClassDescriptor.lookupInternal(clazz);

            if ( classDesc == null ) {
                write(TC_NULL);
                return;
            }

            int tmpInt = findWireOffset(obj);
            if ( tmpInt >= 0 ) {
                write(TC_REFERENCE);
                tmpInt += baseWireHandle;
                //writeInt(int tmpInt){
                write((tmpInt >>> 24) & 0xFF);
                write((tmpInt >>> 16) & 0xFF);
                write((tmpInt >>>  8) & 0xFF);
                write((tmpInt >>>  0) & 0xFF);
                //}
                return;
            }

            if ( obj instanceof Class ) {
                write(TC_CLASS);
                write(TC_CLASSDESC);
                writeUTF(classDesc.getName());
                long value = classDesc.getSerialVersionUID();
                //writeLong(long value){
                write((int)(value >>> 56) & 0xFF);
                write((int)(value >>> 48) & 0xFF);
                write((int)(value >>> 40) & 0xFF);
                write((int)(value >>> 32) & 0xFF);
                write((int)(value >>> 24) & 0xFF);
                write((int)(value >>> 16) & 0xFF);
                write((int)(value >>>  8) & 0xFF);
                write((int)(value >>>  0) & 0xFF);
                //}
                assignWireOffset(classDesc);
                classDesc.writeClassInfo(this);
                write(TC_ENDBLOCKDATA);
                writeObject(classDesc.getSuperclass());
                assignWireOffset(clazz);
                return;
            }

            if ( obj instanceof ClassDescriptor ) {
                write(TC_CLASSDESC);
                writeUTF(classDesc.getName());
                long value = classDesc.getSerialVersionUID();
                //writeLong(long value){
                write((int)(value >>> 56) & 0xFF);
                write((int)(value >>> 48) & 0xFF);
                write((int)(value >>> 40) & 0xFF);
                write((int)(value >>> 32) & 0xFF);
                write((int)(value >>> 24) & 0xFF);
                write((int)(value >>> 16) & 0xFF);
                write((int)(value >>>  8) & 0xFF);
                write((int)(value >>>  0) & 0xFF);
                //}
                assignWireOffset(classDesc);
                write(classDesc.flags);
                tmpInt = classDesc.fields.length;
                write((tmpInt >>>  8) & 0xFF);
                write((tmpInt >>>  0) & 0xFF);
                FieldDescriptor field;
                for ( int i=0; i < classDesc.fields.length; i++ ) {
                    field = classDesc.fields[i];
                    write((int)field.typeCode);
                    writeUTF(field.name);
                    if ( !field.type.isPrimitive() ) writeObject(field.typeString);
                }
                write(TC_ENDBLOCKDATA);
                writeObject(classDesc.getSuperclass());
                return;
            }
            if ( obj instanceof String ) {
                write(TC_STRING);
                String s = ((String)obj).intern();
                assignWireOffset(s);
                writeUTF(s);
                return;
            }
            if ( clazz.isArray() ) {
                write(TC_ARRAY);
                writeObject(classDesc);
                assignWireOffset(obj);

                Class type = clazz.getComponentType();
                if ( type.isPrimitive() ) {
                    if ( type == Integer.TYPE ) {
                        int[] array = (int[])obj;
                        tmpInt = array.length;
                        //writeInt(int tmpInt){
                        write((tmpInt >>> 24) & 0xFF);
                        write((tmpInt >>> 16) & 0xFF);
                        write((tmpInt >>>  8) & 0xFF);
                        write((tmpInt >>>  0) & 0xFF);
                        //}
                        int value;
                        for ( int i = 0; i < tmpInt; i++ ) {
                            value = array[i];
                            //writeInt(int tmpInt){
                            write((value >>> 24) & 0xFF);
                            write((value >>> 16) & 0xFF);
                            write((value >>>  8) & 0xFF);
                            write((value >>>  0) & 0xFF);
                            //}
                        }
                        return;
                    } else if ( type == Byte.TYPE ) {
                        byte[] array = (byte[])obj;
                        tmpInt = array.length;
                        //writeInt(int tmpInt){
                        write((tmpInt >>> 24) & 0xFF);
                        write((tmpInt >>> 16) & 0xFF);
                        write((tmpInt >>>  8) & 0xFF);
                        write((tmpInt >>>  0) & 0xFF);
                        //}
                        write(array, 0, tmpInt);
                        return;
                    } else if ( type == Long.TYPE ) {
                        long[] array = (long[])obj;
                        tmpInt = array.length;
                        //writeInt(int tmpInt){
                        write((tmpInt >>> 24) & 0xFF);
                        write((tmpInt >>> 16) & 0xFF);
                        write((tmpInt >>>  8) & 0xFF);
                        write((tmpInt >>>  0) & 0xFF);
                        //}
                        long value;
                        for ( int i = 0; i < tmpInt; i++ ) {
                            value = array[i];
                            //writeLong(long value){
                            write((int)(value >>> 56) & 0xFF);
                            write((int)(value >>> 48) & 0xFF);
                            write((int)(value >>> 40) & 0xFF);
                            write((int)(value >>> 32) & 0xFF);
                            write((int)(value >>> 24) & 0xFF);
                            write((int)(value >>> 16) & 0xFF);
                            write((int)(value >>>  8) & 0xFF);
                            write((int)(value >>>  0) & 0xFF);
                            //}
                        }
                        return;
                    } else if ( type == Float.TYPE ) {
                        float[] array = (float[])obj;
                        tmpInt = array.length;
                        //writeInt(int tmpInt){
                        write((tmpInt >>> 24) & 0xFF);
                        write((tmpInt >>> 16) & 0xFF);
                        write((tmpInt >>>  8) & 0xFF);
                        write((tmpInt >>>  0) & 0xFF);
                        //}
                        int value;
                        for ( int i = 0; i < tmpInt; i++ ) {
                            value = Float.floatToIntBits(array[i]);
                            //writeInt(int value){
                            write((value >>> 24) & 0xFF);
                            write((value >>> 16) & 0xFF);
                            write((value >>>  8) & 0xFF);
                            write((value >>>  0) & 0xFF);
                            //}
                        }
                        return;
                    } else if ( type == Double.TYPE ) {
                        double[] array = (double[])obj;
                        tmpInt = array.length;
                        //writeInt(int tmpInt){
                        write((tmpInt >>> 24) & 0xFF);
                        write((tmpInt >>> 16) & 0xFF);
                        write((tmpInt >>>  8) & 0xFF);
                        write((tmpInt >>>  0) & 0xFF);
                        //}
                        long value;
                        for ( int i = 0; i < tmpInt; i++ ) {
                            value = Double.doubleToLongBits(array[i]);
                            //writeLong(long value){
                            write((int)(value >>> 56) & 0xFF);
                            write((int)(value >>> 48) & 0xFF);
                            write((int)(value >>> 40) & 0xFF);
                            write((int)(value >>> 32) & 0xFF);
                            write((int)(value >>> 24) & 0xFF);
                            write((int)(value >>> 16) & 0xFF);
                            write((int)(value >>>  8) & 0xFF);
                            write((int)(value >>>  0) & 0xFF);
                            //}
                        }
                        return;
                    } else if ( type == Short.TYPE ) {
                        short[] array = (short[])obj;
                        tmpInt = array.length;
                        //writeInt(int tmpInt){
                        write((tmpInt >>> 24) & 0xFF);
                        write((tmpInt >>> 16) & 0xFF);
                        write((tmpInt >>>  8) & 0xFF);
                        write((tmpInt >>>  0) & 0xFF);
                        //}
                        short value;
                        for ( int i = 0; i < tmpInt; i++ ) {
                            value = array[i];
                            //writeShort(short value){
                            write((value >>>  8) & 0xFF);
                            write((value >>>  0) & 0xFF);
                            //}
                        }
                        return;
                    } else if ( type == Character.TYPE ) {
                        char[] array = (char[])obj;
                        tmpInt = array.length;
                        //writeInt(int tmpInt){
                        write((tmpInt >>> 24) & 0xFF);
                        write((tmpInt >>> 16) & 0xFF);
                        write((tmpInt >>>  8) & 0xFF);
                        write((tmpInt >>>  0) & 0xFF);
                        //}
                        char value;
                        for ( int i = 0; i < tmpInt; i++ ) {
                            value = array[i];
                            //writeChar(char value){
                            write((value >>>  8) & 0xFF);
                            write((value >>>  0) & 0xFF);
                            //}
                        }
                        return;
                    } else if ( type == Boolean.TYPE ) {
                        boolean[] array = (boolean[])obj;
                        tmpInt = array.length;
                        //writeInt(int tmpInt){
                        write((tmpInt >>> 24) & 0xFF);
                        write((tmpInt >>> 16) & 0xFF);
                        write((tmpInt >>>  8) & 0xFF);
                        write((tmpInt >>>  0) & 0xFF);
                        //}
                        for ( int i = 0; i < tmpInt; i++ ) {
                            write(array[i] ? 1 : 0);
                        }
                        return;
                    } else {
                        throw new InvalidClassException(clazz.getName());
                    }
                } else {
                    Object[] array = (Object[])obj;
                    int length = array.length;
                    //writeInt(int length){
                    write((length >>> 24) & 0xFF);
                    write((length >>> 16) & 0xFF);
                    write((length >>>  8) & 0xFF);
                    write((length >>>  0) & 0xFF);
                    //}
                    for ( int i = 0; i < length; i++ ) writeObject(array[i]);
                }
                return;
            }
            write(TC_OBJECT);
            writeObject(classDesc);
            assignWireOffset(obj);
            //writeObjectData(obj, classDesc);
            if ( classDesc.isExternalizable() ) {
                writeExternal((Externalizable)obj);
                return;
            }

            int stackMark = classDescStack.size();
            try {

                ClassDescriptor superClassDesc;
                while ( (superClassDesc = classDesc.getSuperclass()) != null ) {
                    classDescStack.push(classDesc);
                    classDesc = superClassDesc;
                }

                //classDesc is now the highest non-null superclass.
                do {
                    if ( classDesc.hasWriteObjectMethod() ) {
                        /* DMB:  NOT COMPLETE - Should start writing in block data format 
                         * and state the size of the data to come. 
                         */
                        //setBlockData(true);
                        /* DMB:  NOT COMPLETE - Should Invoke the writeObject
                         * mehtod on the object.
                         * Invoking the write object method requires a 
                         * sublcass of java.io.ObjectOutputStream to be 
                         * passed in.  This implementation is not a subclass
                         * of java.io.ObjectOutputStream.
                         */
                        //invokeObjectWriter(obj);
                        /* DMB:  NOT COMPLETE - Should stop writing in block data format. 
                         * Denote the end of this mode by writing a terminator to the stream.
                         */
                        //setBlockData(false);
                        //writeCode(TC_ENDBLOCKDATA);
                    } else {
                        FieldDescriptor[] fields = classDesc.getFields();
                        Field field;
                        if ( fields.length > 0 ) {
                            for ( int i=0; i< fields.length; i++ ) {
                                field = fields[i].getField();
                                if ( field == null ) throw new InvalidClassException(clazz.getName(), "Nonexistent field " + fields[i].getName());
                                try {
                                    switch ( fields[i].getTypeCode() ) {
                                        case 'B':
                                            write(field.getByte(obj));
                                            break;
                                        case 'C':
                                            char charvalue = field.getChar(obj);
                                            write((charvalue >>>  8) & 0xFF);
                                            write((charvalue >>>  0) & 0xFF);
                                            break;
                                        case 'I':
                                            int intvalue = field.getInt(obj);
                                            write((intvalue >>> 24) & 0xFF);
                                            write((intvalue >>> 16) & 0xFF);
                                            write((intvalue >>>  8) & 0xFF);
                                            write((intvalue >>>  0) & 0xFF);
                                            break;
                                        case 'Z':
                                            write((field.getBoolean(obj)?1:0) );
                                            break;
                                        case 'J':
                                            long longvalue = field.getLong(obj);
                                            write((int)(longvalue >>> 56) & 0xFF);
                                            write((int)(longvalue >>> 48) & 0xFF);
                                            write((int)(longvalue >>> 40) & 0xFF);
                                            write((int)(longvalue >>> 32) & 0xFF);
                                            write((int)(longvalue >>> 24) & 0xFF);
                                            write((int)(longvalue >>> 16) & 0xFF);
                                            write((int)(longvalue >>>  8) & 0xFF);
                                            write((int)(longvalue >>>  0) & 0xFF);
                                            break;
                                        case 'F':
                                            int floatvalue = Float.floatToIntBits(field.getFloat(obj));
                                            write((floatvalue >>> 24) & 0xFF);
                                            write((floatvalue >>> 16) & 0xFF);
                                            write((floatvalue >>>  8) & 0xFF);
                                            write((floatvalue >>>  0) & 0xFF);
                                            break;
                                        case 'D':
                                            long doublevalue = Double.doubleToLongBits(field.getDouble(obj));
                                            write((int)(doublevalue >>> 56) & 0xFF);
                                            write((int)(doublevalue >>> 48) & 0xFF);
                                            write((int)(doublevalue >>> 40) & 0xFF);
                                            write((int)(doublevalue >>> 32) & 0xFF);
                                            write((int)(doublevalue >>> 24) & 0xFF);
                                            write((int)(doublevalue >>> 16) & 0xFF);
                                            write((int)(doublevalue >>>  8) & 0xFF);
                                            write((int)(doublevalue >>>  0) & 0xFF);
                                            break;
                                        case 'S':
                                            short shortvalue = field.getShort(obj);
                                            write((shortvalue >>>  8) & 0xFF);
                                            write((shortvalue >>>  0) & 0xFF);
                                            break;
                                        case '[':
                                        case 'L':
                                            writeObject(field.get(obj));
                                            break;
                                        default: throw new InvalidClassException(clazz.getName());
                                    }
                                } catch ( IllegalAccessException e ) {
                                    throw new InvalidClassException(clazz.getName(), e.getMessage());
                                } finally {
                                }
                            }
                        }
                    }
                }while ( classDescStack.size() > stackMark &&  (classDesc = (ClassDescriptor)classDescStack.pop()) != null );

            } finally {
                /* If an error occcured, make sure we set the stack back
                 * the way it was before we started.
                 */
                //classDescStack.setSize(stackMark);
            }

        } finally {
        }
    }

    public void writeString(String s) throws IOException{
        writeObject(s);
    }

    private void writeExternal(Externalizable ext) throws IOException{
//	    if (useDeprecatedExternalizableFormat) {
        if ( false ) {
            /* JDK 1.1 external data format.
             * Don't write in block data mode and no terminator tag.
             */
            /* This method accepts a java.io.OutputStream as a parameter */
            ext.writeExternal(this);
        } else {
            /* JDK 1.2 Externalizable data format writes in block data mode
             * and terminates externalizable data with TAG_ENDBLOCKDATA.
             */
            /* DMB:  NOT COMPLETE - Should start writing in block data format. 
             * This states the size of the data to come
             */
            //setBlockData(true);
            try {
                /* This method accepts a java.io.ObjectOutputStream as a parameter */
                ext.writeExternal(this);
            } finally {
                /* DMB:  NOT COMPLETE - Should stop writing in block data format. 
                 * Denote the end of this mode by writing a terminator to the stream.
                 */
                //setBlockData(false);
                //writeCode(TC_ENDBLOCKDATA);
            }
        }
    }




    public void writeException(Throwable th) throws IOException{
        /* DMB:  NOT COMPLETE - Must write exceptions that occur during serialization 
         * to the stream.
         */

    }

    public void writeReset() throws IOException{
        /* DMB:  NOT COMPLETE - Must write the reset byte when the reset() method
         * is called.
         */

    }



    /**
     * Writes the specified byte (the low eight bits of the argument
     * <code>b</code>) to the underlying output stream.
     * <p>
     * Implements the <code>write</code> method of <code>OutputStream</code>.
     *
     * @param      b   the <code>byte</code> to be written.
     * @exception  IOException  if an I/O error occurs.
     */
/*    public void write(int b) throws IOException {
        out.write(b);
    }
*/
    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to the underlying output stream.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs.
     */
/*    public void write(byte b[], int off, int len) throws IOException {
        for (int i = 0 ; i < len ; i++) {
            write(b[off + i]);
        }
    }
*/
    /**
     * Writes the specified byte to this byte array output stream.
     *
     * @param   b   the byte to be written.
     */
    public void write(int b) {
        try {
            buf[count++] = (byte)b;
        } catch ( ArrayIndexOutOfBoundsException e ) {
            byte newbuf[] = new byte[Math.max(buf.length << 1, count)];
            System.arraycopy(buf, 0, newbuf, 0, count-1);
            buf = newbuf;
        }
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this byte array output stream.
     *
     * @param   b     the data.
     * @param   off   the start offset in the data.
     * @param   len   the number of bytes to write.
     */
    public synchronized void write(byte b[], int off, int len) {
        if ( len == 0 ) return;

        int newcount = count + len;
        if ( newcount > buf.length ) {
            byte newbuf[] = new byte[Math.max(buf.length << 1, newcount)];
            System.arraycopy(buf, 0, newbuf, 0, count);
            buf = newbuf;
        }
        System.arraycopy(b, off, buf, count, len);
        count = newcount;
    }


    /**
     * Flushes this data output stream. This forces any buffered output
     * bytes to be written out to the stream.
     * <p>
     * The <code>flush</code> method of <code>DataOuputStream</code>
     * calls the <code>flush</code> method of its underlying output stream.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void flush() throws IOException {
//    	out.flush();
    }

    /**
     * Creates a newly allocated byte array. Its size is the current
     * size of this output stream and the valid contents of the buffer
     * have been copied into it.
     *
     * @return  the current contents of this output stream, as a byte array.
     * @see     java.io.ByteArrayOutputStream#size()
     */
    public byte[] toByteArray() {
        byte newbuf[] = new byte[count];
        System.arraycopy(buf, 0, newbuf, 0, count);
        return newbuf;
    }

    /**
     * Returns the current size of the buffer.
     *
     * @return  the value of the <code>count</code> field, which is the number
     *          of valid bytes in this output stream.
     */
    public int size() {
        return count;
    }


    /**
     * Writes a <code>boolean</code> to the underlying output stream as
     * a 1-byte value. The value <code>true</code> is written out as the
     * value <code>(byte)1</code>; the value <code>false</code> is
     * written out as the value <code>(byte)0</code>.
     *
     * @param      v   a <code>boolean</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public final void writeBoolean(boolean v) throws IOException {
        write(v ? 1 : 0);
    }

    /**
     * Writes out a <code>byte</code> to the underlying output stream as
     * a 1-byte value.
     *
     * @param      v   a <code>byte</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public final void writeByte(int v) throws IOException {
        write(v);
    }

    /**
     * Writes a <code>short</code> to the underlying output stream as two
     * bytes, high byte first.
     *
     * @param      v   a <code>short</code> to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public final void writeShort(int v) throws IOException {
        write((v >>> 8) & 0xFF);
        write((v >>> 0) & 0xFF);
    }

    /**
     * Writes a <code>char</code> to the underlying output stream as a
     * 2-byte value, high byte first.
     *
     * @param      v   a <code>char</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public final void writeChar(int v) throws IOException {
        write((v >>> 8) & 0xFF);
        write((v >>> 0) & 0xFF);
    }

    /**
     * Writes an <code>int</code> to the underlying output stream as four
     * bytes, high byte first.
     *
     * @param      v   an <code>int</code> to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public final void writeInt(int v) throws IOException {
        write((v >>> 24) & 0xFF);
        write((v >>> 16) & 0xFF);
        write((v >>>  8) & 0xFF);
        write((v >>>  0) & 0xFF);
    }

    /**
     * Writes a <code>long</code> to the underlying output stream as eight
     * bytes, high byte first.
     *
     * @param      v   a <code>long</code> to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public final void writeLong(long v) throws IOException {
        write((int)(v >>> 56) & 0xFF);
        write((int)(v >>> 48) & 0xFF);
        write((int)(v >>> 40) & 0xFF);
        write((int)(v >>> 32) & 0xFF);
        write((int)(v >>> 24) & 0xFF);
        write((int)(v >>> 16) & 0xFF);
        write((int)(v >>>  8) & 0xFF);
        write((int)(v >>>  0) & 0xFF);
    }

    /**
     * Converts the float argument to an <code>int</code> using the
     * <code>floatToIntBits</code> method in class <code>Float</code>,
     * and then writes that <code>int</code> value to the underlying
     * output stream as a 4-byte quantity, high byte first.
     *
     * @param      v   a <code>float</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public final void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    /**
     * Converts the double argument to a <code>long</code> using the
     * <code>doubleToLongBits</code> method in class <code>Double</code>,
     * and then writes that <code>long</code> value to the underlying
     * output stream as an 8-byte quantity, high byte first.      *
     * @param      v   a <code>double</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.lang.Double#doubleToLongBits(double)
     */
    public final void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    /**
     * Writes out the string to the underlying output stream as a
     * sequence of bytes. Each character in the string is written out, in
     * sequence, by discarding its high eight bits.
     *
     * @param      s   a string of bytes to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public final void writeBytes(String s) throws IOException {
        int tmpLen = s.length();
        for ( int i = 0 ; i < tmpLen ; i++ ) {
            write((byte)s.charAt(i));
        }
    }

    /**
     * Writes a string to the underlying output stream as a sequence of
     * characters.
     * @param      s   a <code>String</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public final void writeChars(String s) throws IOException {
        int tmpLen = s.length();
        for ( int i = 0 ; i < tmpLen ; i++ ) {
            int v = s.charAt(i);
            write((v >>> 8) & 0xFF);
            write((v >>> 0) & 0xFF);
        }
    }

    /*  These are to speed up the writing of strings.
     *  This method is called frequently and placing these here
     *  prevents constant allocation and garbage collection.
     */

    private char[] utfCharBuf = new char[32];
    public final void writeUTF(String str) throws IOException {

        int len = str.length();

        if ( utfCharBuf.length < len ) utfCharBuf = new char[len];

        str.getChars(0,len,utfCharBuf,0);

        int mark = count;
        write(0); // We will write over these two bytes with the
        write(0); // UTF string length later.
        for ( int i = 0 ; i < len ; i++ ) {
            int c = utfCharBuf[i];
            if ( (c >= 0x0001) && (c <= 0x007F) ) {
                write(c);
            } else if ( c > 0x07FF ) {
                write(0xE0 | ((c >> 12) & 0x0F));
                write(0x80 | ((c >>  6) & 0x3F));
                write(0x80 | ((c >>  0) & 0x3F));
            } else {
                write(0xC0 | ((c >>  6) & 0x1F));
                write(0x80 | ((c >>  0) & 0x3F));
            }
        }
        //  With the new algorythm this check is really pointless.
//       	if (tmpUtflen > 65535)throw new UTFDataFormatException();

        len = count-mark-2;
        buf[mark] = (byte)((len >>> 8) & 0xFF);
        buf[mark+1] = (byte)((len >>> 0) & 0xFF);


    }

    /* Object references are mapped to the wire handles through a hashtable
     * WireHandles are integers generated by the ObjectOutputStream,
     * they need only be unique within a stream.
     * Objects are assigned sequential handles and stored in wireHandle2Object.
     * The handle for an object is its index in wireHandle2Object.
     * Object with the "same" hashcode are chained using wireHash2Handle.
     * The hashcode of objects is used to index through the wireHash2Handle.
     * -1 is the marker for unused cells in wireNextHandle
     */
    private ArrayList wireHandle2Object;
    private int nextWireOffset;

    /* the next five members implement an inline hashtable. */
    private int[] wireHash2Handle;        // hash spine
    private int[] wireNextHandle;         // next hash bucket entry
    private int   wireHashSizePower = 2;  // current power of 2 hash table size - 1
    private int   wireHashLoadFactor = 7; // avg number of elements per bucket
    private int   wireHashCapacity = (1 << wireHashSizePower) * wireHashLoadFactor;

    /*
     * Insert the specified object into the hash array and link if
     * necessary. Put the new object into the hash table and link the
     * previous to it. Newer objects occur earlier in the list.
     */
    private void hashInsert(Object obj, int offset) {
        int hash = System.identityHashCode(obj);
        int index = (hash & 0x7FFFFFFF) % wireHash2Handle.length;
        wireNextHandle[offset] = wireHash2Handle[index];
        wireHash2Handle[index] = offset;
    }
    /*
     * Locate and return if found the handle for the specified object.
     * -1 is returned if the object does not occur in the array of
     * known objects.
     */
    private int findWireOffset(Object obj) {
        int hash = System.identityHashCode(obj);
        int index = (hash & 0x7FFFFFFF) % wireHash2Handle.length;

        for ( int handle = wireHash2Handle[index];
            handle >= 0;
            handle = wireNextHandle[handle] ) {

            if ( wireHandle2Object.get(handle) == obj )
                return handle;
        }
        return -1;
    }

    /* Allocate a handle for an object.
     * The Vector is indexed by the wireHandleOffset
     * and contains the object.
     * Allow caller to specify the hash method for the object.
     */
    private void assignWireOffset(Object obj)
    throws IOException
    {
        if ( nextWireOffset == wireNextHandle.length ) {
            int[] oldnexthandles = wireNextHandle;
            wireNextHandle = new int[nextWireOffset*2];
            System.arraycopy(oldnexthandles, 0,
                             wireNextHandle, 0,
                             nextWireOffset);
        }
        if ( nextWireOffset >= wireHashCapacity ) {
            growWireHash2Handle();
        }
        wireHandle2Object.add(obj);
        hashInsert(obj, nextWireOffset);
        nextWireOffset++;
        return;
    }

    private void growWireHash2Handle() {
        // double hash table spine.
        wireHashSizePower++;
        wireHash2Handle = new int[(1 << wireHashSizePower) - 1];
        Arrays.fill(wireHash2Handle, -1);

        for ( int i = 0; i < nextWireOffset; i++ ) {
            wireNextHandle[i] = 0;
        }

        // refill hash table.
        for ( int i = 0; i < wireHandle2Object.size(); i++ ) {
            hashInsert(wireHandle2Object.get(i), i);
        }

        wireHashCapacity = (1 << wireHashSizePower) * wireHashLoadFactor;
    }

    /*
     * Internal reset function to reinitialize the state of the stream.
     * Reset state of things changed by using the stream.
     */
    private void resetStream() throws IOException {
        if ( wireHandle2Object == null ) {
            wireHandle2Object = new ArrayList();
            wireNextHandle = new int[4];
            wireHash2Handle = new int[ (1 << wireHashSizePower) - 1];
        } else {

            // Storage Optimization for frequent calls to reset method.
            // Do not reallocate, only reinitialize.
            wireHandle2Object.clear();
            for ( int i = 0; i < nextWireOffset; i++ ) {
                wireNextHandle[i] = 0;
            }
        }
        nextWireOffset = 0;
        Arrays.fill(wireHash2Handle, -1);

        if ( classDescStack == null )
            classDescStack = new ArrayStack();
        else classDescStack.setSize(0);


    }
}