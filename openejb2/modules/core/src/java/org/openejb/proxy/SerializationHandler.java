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
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
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
package org.openejb.proxy;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.rmi.MarshalledObject;

import org.apache.geronimo.kernel.ObjectInputStreamExt;
import org.omg.CORBA.ORB;


public class SerializationHandler {
    private static InheritableThreadLocal serializationState = new InheritableThreadLocal();

    /**
     * This method is public so it can be called by other parts of the
     * container during their serialization operations, namely session
     * passivation
     */
    public static void setStrategy(ReplacementStrategy strategy) {
        serializationState.set(strategy);
    }

    private static ReplacementStrategy getStrategy() {
        ReplacementStrategy replacementStrategy = (ReplacementStrategy) serializationState.get();
        if (replacementStrategy == null) {
            return ReplacementStrategy.REPLACE;
        }
        return replacementStrategy;
    }

    public static void copyArgs(Object[] objects) throws IOException, ClassNotFoundException {
        for (int i = 0; i < objects.length; i++) {
            Object originalObject = objects[i];
            Object copy = copyObj(originalObject);
            if (copy instanceof javax.rmi.CORBA.Stub) {
                // connect a coppied stub to the same orb as the original stub
                ORB orb = ((javax.rmi.CORBA.Stub)originalObject)._orb();
                if (orb != null) {
                    ((javax.rmi.CORBA.Stub)copy).connect(orb);
                }
            }
            objects[i] = copy;
        }
    }

    public static Object copyObj(Object object) throws IOException, ClassNotFoundException {
        Object quickCopyObject = quickCopy(object);
        if (quickCopyObject != null) return quickCopyObject;

        MarshalledObject obj = new MarshalledObject(object);
        return obj.get();
    }

    public static Object writeReplace(Object object, ProxyInfo proxyInfo) throws ObjectStreamException {
        return getStrategy().writeReplace(object, proxyInfo);
    }

    public static void copyArgs(ClassLoader classLoader, Object[] objects) throws IOException, ClassNotFoundException {
        for (int i = 0; i < objects.length; i++) {
            objects[i] = copyObj(classLoader, objects[i]);
        }
    }

    public static Object copyObj(ClassLoader classLoader, Object object) throws IOException, ClassNotFoundException {
        Object quickCopyObject = quickCopy(object);
        if (quickCopyObject != null) return quickCopyObject;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(object);
        oos.flush();
        oos.close();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStreamExt ois = new ObjectInputStreamExt(bais, classLoader);
        return ois.readObject();
    }

    /**
     * quickCopy does a quick analysis of the object to be copied.  If it is a primitive then the object is simply
     * returned since the object is immutable.  In the case of a primitive array a clone operation is performed to
     * create a quick copy rather than the more expensive serialization.
     *
     * @param object
     * @return null if object should undergo normal serialization.  Otherwise the object reference to return.
     */
    private static Object quickCopy(Object object) {
        try {
            // Get the object's class once for efficiency.
            Class ooc = object.getClass();

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
                (ooc == Short.class)) {
              return object;
            }

            Class oct = ooc.getComponentType();

            /*
            **  Is the passed object an array?  If so it will either be an array of primitives
            **  or an array of some type of object.  If its a primitive we'll clone it and call it a day.
            */
            if ( ooc.isArray() ) {
                /*
                ** We've gotten to this point because the object we've been passed is an array of primitives.
                ** As such we will invoke clone to make the copy and call it a day.  No need to do a recurisive
                ** invocation so once the copy is made we're outta here.
                */
                if (oct != null && oct.isPrimitive()) {
                    Object copiedObject = null;
                    while (true) {
                        if (oct == int.class)     { int[]     t = (int[])    ( (int[])     object).clone() ;  copiedObject = (Object) t;  break; }
                        if (oct == long.class)    { long[]    t = (long[])   ( (long[])    object).clone() ;  copiedObject = (Object) t;  break; }
                        if (oct == float.class)   { float[]   t = (float[])  ( (float[])   object).clone() ;  copiedObject = (Object) t;  break; }
                        if (oct == double.class)  { double[]  t = (double[]) ( (double[])  object).clone() ;  copiedObject = (Object) t;  break; }
                        if (oct == boolean.class) { boolean[] t = (boolean[])( (boolean[]) object).clone() ;  copiedObject = (Object) t;  break; }
                        if (oct == byte.class)    { byte[]    t = (byte[])   ( (byte[])    object).clone() ;  copiedObject = (Object) t;  break; }
                        if (oct == short.class)   { short[]   t = (short[])  ( (short[])   object).clone() ;  copiedObject = (Object) t;  break; }
                        if (oct == char.class)    { char[]    t = (char[])   ( (char[])    object).clone() ;  copiedObject = (Object) t;  break; }

                        return null;  // We should never get to this point.  If we do there is a new primitive type that we don't know about.
                    }
                    return copiedObject;
                }
            }
        } catch (Exception e) {
            // Something bad happened...let normal copying occur
        }
        return null;
    }
}

