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
 * $Id: EJBRequest.java 445791 2005-10-20 22:19:24Z djencks $
 */
package org.apache.openejb.client;


import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.rmi.Remote;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;
import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.Stub;

import org.omg.CORBA.ORB;
import org.apache.openejb.corba.ORBRef;

/**
 * @since 11/25/2001
 */
public class EJBRequest implements Request {
    private transient ORBRef orbRef;

    private transient int requestMethod;
    private transient int deploymentCode = 0;
    private transient Object clientIdentity;
    private transient Method methodInstance;

    private transient Class methodClass;
    private transient String methodName;
    private transient Class[] methodParamTypes;
    private transient Object[] methodParameters;
    private transient String deploymentId;
    private transient Object primaryKey;


    //-------------------------------------------------------//
    // Enertprise Bean Types                                 //
    //-------------------------------------------------------//

    public static final int SESSION_BEAN_STATELESS = 6;
    public static final int SESSION_BEAN_STATEFUL = 7;
    public static final int ENTITY_BM_PERSISTENCE = 8;
    public static final int ENTITY_CM_PERSISTENCE = 9;


    public EJBRequest() {
    }

    public EJBRequest(ORBRef orbRef) {
        this.orbRef = orbRef;
    }

    public EJBRequest(int requestMethod) {
        this.requestMethod = requestMethod;
    }

    public byte getRequestType() {
        return EJB_REQUEST;
    }

    public int getRequestMethod() {
        return requestMethod;
    }

    public Object getClientIdentity() {
        return clientIdentity;
    }

    public Method getMethodInstance() {
        return methodInstance;
    }

    public Object[] getMethodParameters() {
        return methodParameters;
    }

    public String getContainerID() {
        return deploymentId;
    }

    public int getContainerCode() {
        return deploymentCode;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }


    public Class getMethodClass() {
        return methodClass;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class[] getMethodParamTypes() {
        return methodParamTypes;
    }

    //-------------

    public void setRequestMethod(int requestMethod) {
        this.requestMethod = requestMethod;
    }

    public void setClientIdentity(Object clientIdentity) {
        this.clientIdentity = clientIdentity;
    }

    public void setMethodInstance(Method methodInstance) {
        this.methodInstance = methodInstance;
        this.methodClass = methodInstance.getDeclaringClass();
        this.methodName = methodInstance.getName();
        this.methodParamTypes = methodInstance.getParameterTypes();
    }

    public void setMethodParameters(Object[] methodParameters) {
        this.methodParameters = methodParameters;
    }

    public void setContainerID(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public void setContainerCode(int deploymentCode) {
        this.deploymentCode = deploymentCode;
    }

    public void setPrimaryKey(Object primaryKey) {
        this.primaryKey = primaryKey;
    }

    public String toString() {
        StringBuffer s = null;
        switch (requestMethod) {
        case EJB_HOME_GET_EJB_META_DATA:
            s = new StringBuffer("EJB_HOME.GET_EJB_META_DATA");
            break;
        case EJB_HOME_GET_HOME_HANDLE:
            s = new StringBuffer("EJB_HOME.GET_HOME_HANDLE");
            break;
        case EJB_HOME_REMOVE_BY_HANDLE:
            s = new StringBuffer("EJB_HOME.REMOVE_BY_HANDLE");
            break;
        case EJB_HOME_REMOVE_BY_PKEY:
            s = new StringBuffer("EJB_HOME.REMOVE_BY_PKEY");
            break;
        case EJB_HOME_FIND:
            s = new StringBuffer("EJB_HOME.FIND");
            break;
        case EJB_HOME_CREATE:
            s = new StringBuffer("EJB_HOME.CREATE");
            break;
        case EJB_OBJECT_GET_EJB_HOME:
            s = new StringBuffer("EJB_OBJECT.GET_EJB_HOME");
            break;
        case EJB_OBJECT_GET_HANDLE:
            s = new StringBuffer("EJB_OBJECT.GET_HANDLE");
            break;
        case EJB_OBJECT_GET_PRIMARY_KEY:
            s = new StringBuffer("EJB_OBJECT.GET_PRIMARY_KEY");
            break;
        case EJB_OBJECT_IS_IDENTICAL:
            s = new StringBuffer("EJB_OBJECT.IS_IDENTICAL");
            break;
        case EJB_OBJECT_REMOVE:
            s = new StringBuffer("EJB_OBJECT.REMOVE");
            break;
        case EJB_OBJECT_BUSINESS_METHOD:
            s = new StringBuffer("EJB_OBJECT.BUSINESS_METHOD");
            break;
        }
        s.append(':').append(methodName);
        s.append(':').append(deploymentId);
        s.append(':').append(primaryKey);

        return s.toString();
    }

    /*
    When the Request externalizes itself, it will reset
    the appropriate values so that this instance can be used
    again.

    There will be one request instance for each handler
    */

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
        clearState();

        readRequestMethod(in);

        readContainerId(in);

        readClientIdentity(in);

        readPrimaryKey(in);

        readMethod(in);

        readMethodParameters(in);

        loadMethodInstance();
    }

    protected void loadMethodInstance() throws IOException {
        try {
            methodInstance = methodClass.getDeclaredMethod(methodName, methodParamTypes);
        } catch (NoSuchMethodException nsme) {
            throw (IOException)new IOException("Invalid EJB request data.").initCause(nsme);
        }
    }

    protected void readMethod(ObjectInput in) throws ClassNotFoundException, IOException {
        methodClass = (Class) in.readObject();
        methodName = in.readUTF();
    }

    protected void readPrimaryKey(ObjectInput in) throws ClassNotFoundException, IOException {
        primaryKey = in.readObject();
    }

    protected void clearState() {
        requestMethod = -1;
        deploymentId = null;
        deploymentCode = -1;
        clientIdentity = null;
        primaryKey = null;
        methodClass = null;
        methodName = null;
        methodInstance = null;
    }

    protected void readClientIdentity(ObjectInput in) throws ClassNotFoundException, IOException {
        clientIdentity = in.readObject();
    }

    protected void readContainerId(ObjectInput in) throws ClassNotFoundException, IOException {
        deploymentId = (String) in.readObject();
        deploymentCode = in.readShort();
    }

    protected void readRequestMethod(ObjectInput in) throws IOException {
        requestMethod = in.readByte();
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
        out.writeByte(requestMethod);

        if (deploymentCode > 0) {
            out.writeObject(null);
        } else {
            out.writeObject(deploymentId);
        }

        out.writeShort(deploymentCode);
        out.writeObject(clientIdentity);
        out.writeObject(primaryKey);
        // The declaring class should always be the
        // home or remote interface, depending on the
        // request type.

        out.writeObject(methodClass);
        out.writeUTF(methodName);

        writeMethodParameters(out, methodParamTypes, methodParameters);
    }

    protected void writeMethodParameters(ObjectOutput out, Class[] types, Object[] args) throws IOException {


        // TODO Once we index methods properly, we won't need te args to determine the method
        // and can get the arg length from the metadata in the Method instance.
        out.writeByte(types.length);

        for (int i = 0; i < types.length; i++) {
            Class type = types[i];
            Object obj = args[i];

            if (type.isPrimitive()) {
                if (type == Byte.TYPE) {
                    out.write(B);
                    byte bytevalue = ((Byte) obj).byteValue();
                    out.writeByte(bytevalue);

                } else if (type == Character.TYPE) {
                    out.write(C);
                    char charvalue = ((Character) obj).charValue();
                    out.writeChar(charvalue);

                } else if (type == Integer.TYPE) {
                    out.write(I);
                    int intvalue = ((Integer) obj).intValue();
                    out.writeInt(intvalue);

                } else if (type == Boolean.TYPE) {
                    out.write(Z);
                    boolean booleanvalue = ((Boolean) obj).booleanValue();
                    out.writeBoolean(booleanvalue);

                } else if (type == Long.TYPE) {
                    out.write(J);
                    long longvalue = ((Long) obj).longValue();
                    out.writeLong(longvalue);

                } else if (type == Float.TYPE) {
                    out.write(F);
                    float fvalue = ((Float) obj).floatValue();
                    out.writeFloat(fvalue);

                } else if (type == Double.TYPE) {
                    out.write(D);
                    double dvalue = ((Double) obj).doubleValue();
                    out.writeDouble(dvalue);

                } else if (type == Short.TYPE) {
                    out.write(S);
                    short shortvalue = ((Short) obj).shortValue();
                    out.writeShort(shortvalue);

                } else {
                    throw new IOException("unknown primitive type: " + type);
                }
            } else {
                if (obj instanceof PortableRemoteObject && obj instanceof Remote) {
                    Tie tie = javax.rmi.CORBA.Util.getTie((Remote) obj);
                    if (tie == null) {
                        throw new IOException("Unable to serialize PortableRemoteObject; object has not been exported: " + obj);
                    }
                    ORB orb = null;
                    try {
                        Context initialContext = new InitialContext();
                        orb = (ORB) initialContext.lookup("java:comp/ORB");
                    } catch (NamingException e) {
                        throw new IOException("Unable to connect PortableRemoteObject stub to an ORB, no ORB bound to java:comp/ORB");
                    }
                    tie.orb(orb);
                    obj = PortableRemoteObject.toStub((Remote) obj);
                }
                out.write(L);
                out.writeObject(type);
                out.writeObject(obj);
            }
        }
    }

    static final Class[] noArgsC = new Class[0];
    static final Object[] noArgsO = new Object[0];

    protected void readMethodParameters(ObjectInput in) throws IOException, ClassNotFoundException {
        int length = in.read();

        if (length < 1) {
            methodParamTypes = noArgsC;
            methodParameters = noArgsO;
            return;
        }

        Class[] types = new Class[length];
        Object[] args = new Object[length];

        for (int i = 0; i < types.length; i++) {
            Class clazz = null;
            Object obj = null;

            int type = in.read();

            switch (type) {
            case B:
                clazz = Byte.TYPE;
                obj = new Byte(in.readByte());
                break;

            case C:
                clazz = Character.TYPE;
                obj = new Character(in.readChar());
                break;

            case I:
                clazz = Integer.TYPE;
                obj = new Integer(in.readInt());
                break;

            case Z:
                clazz = Boolean.TYPE;
                obj = new Boolean(in.readBoolean());
                break;

            case J:
                clazz = Long.TYPE;
                obj = new Long(in.readLong());
                break;

            case F:
                clazz = Float.TYPE;
                obj = new Float(in.readFloat());
                break;

            case D:
                clazz = Double.TYPE;
                obj = new Double(in.readDouble());
                break;

            case S:
                clazz = Short.TYPE;
                obj = new Short(in.readShort());
                break;

            case L:
                clazz = (Class) in.readObject();
                obj = in.readObject();
                if (obj instanceof Stub && orbRef != null) {
                    Stub stub = (Stub)obj;
                    ORB orb = orbRef.getORB();
                    stub.connect(orb);
                }
                break;
            default:
                throw new IOException("unknown data type: " + type);
            }

            types[i] = clazz;
            args[i] = obj;
        }

        methodParamTypes = types;
        methodParameters = args;
    }

    private static final int I = 0;
    private static final int B = 1;
    private static final int J = 2;
    private static final int F = 3;
    private static final int D = 4;
    private static final int S = 5;
    private static final int C = 6;
    private static final int Z = 7;
    private static final int L = 8;
    private static final int A = 9;


}


