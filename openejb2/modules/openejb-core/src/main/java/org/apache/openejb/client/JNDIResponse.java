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
 * $Id: JNDIResponse.java 445853 2005-12-21 14:21:56Z gdamour $
 */
package org.apache.openejb.client;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.naming.Binding;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.apache.openejb.proxy.EJBProxyReference;

/**
 *
 * @since 11/25/2001
 */
public class JNDIResponse implements Response {
    private transient int responseCode = -1;
    private transient Object result;
    private static final int CONTEXT = 1;
    private static final int EJBHOME = 2;
    private static final int OBJECT = 3;
    private static final int END = 99;

    public JNDIResponse(){
    }

    public JNDIResponse(int code, Object obj){
        responseCode = code;
        result = obj;
    }

    public int getResponseCode(){
        return responseCode;
    }

    public Object getResult(){
        return result;
    }

    public void setResponseCode(int responseCode){
        this.responseCode = responseCode;
    }

    public void setResult(Object result){
        this.result = result;
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
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        responseCode = in.readByte();

        switch (responseCode) {
            case JNDI_OK:
            case JNDI_NAMING_EXCEPTION:
            case JNDI_RUNTIME_EXCEPTION:
            case JNDI_ERROR:
                result = in.readObject();
                break;
            case JNDI_CONTEXT:
            case JNDI_NOT_FOUND:
                break;
            case JNDI_EJBHOME:
                EJBMetaDataImpl m = new EJBMetaDataImpl();
                m.readExternal(in);
                result = m;
                break;
            case JNDI_CONTEXT_TREE:
                result = readContextTree(in);
                break;
            default: throw new IOException("Invalid response code: "+responseCode);
        }
    }

    private Context readContextTree(ObjectInput in) throws IOException, ClassNotFoundException {

        ContextImpl context = new ContextImpl();

        CONTEXT_LOOP: while (true) {
            byte type = in.readByte();
            String name = null;
            Object obj = null;
            switch (type) {
                case CONTEXT:
                    name = in.readUTF();
                    System.out.println("name "+name);
                    obj = readContextTree(in);
                    break;
                case END:
                    break CONTEXT_LOOP;
                default:
                    name = in.readUTF();
                    System.out.println("name "+name);
                    obj = in.readObject();
            }

            try {
                context.internalBind(name,obj);
            } catch (NamingException e) {

            }
        }


        return context;
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
        out.writeByte((byte)responseCode);

        switch (responseCode) {
            case JNDI_OK:
            case JNDI_NAMING_EXCEPTION:
            case JNDI_RUNTIME_EXCEPTION:
            case JNDI_ERROR:
                out.writeObject(result);
                break;
            case JNDI_CONTEXT:
            case JNDI_NOT_FOUND:
                break;
            case JNDI_EJBHOME:
                EJBMetaDataImpl m = (EJBMetaDataImpl)result;
                m.writeExternal(out);
                break;
            case JNDI_CONTEXT_TREE:
                writeContextTree(out, (Context)result);
                break;

        }
    }



    private void writeContextTree(ObjectOutput out, Context context)  throws IOException {
        String name = null;
        try {
            NamingEnumeration namingEnum = context.listBindings( "" );
            while (namingEnum.hasMoreElements()){
                Binding pair = (Binding)namingEnum.next();
                name = pair.getName();

                Object obj = pair.getObject();

                if ( obj instanceof Context ){
                    out.write(CONTEXT);
                    out.writeUTF(name);
                    writeContextTree(out, (Context)obj);
                } else if ( obj instanceof EJBProxyReference ){
                    EJBProxyReference reference = (EJBProxyReference) obj;
                    obj = reference.getContent();
                    out.write(OBJECT);
                    out.writeUTF(name);
                    out.writeObject(obj);
                } else {
                    out.write(OBJECT);
                    out.writeUTF(name);
                    out.writeObject(obj);
                }
            }
            out.write(END);
        } catch (NamingException e) {
            IOException ioException = new IOException("Unable to pull data from JNDI: "+name);
            ioException.initCause(e);
            throw ioException;
        } catch (Exception e) {
            IOException ioException = new IOException("Unable to resolve proxy object instance: "+name);
            ioException.initCause(e);
            throw ioException;
        }
    }

}
