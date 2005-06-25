/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.corba.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.UnexpectedException;
import java.rmi.RemoteException;
import java.lang.reflect.Method;
import javax.ejb.spi.HandleDelegate;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;

import org.bouncycastle.asn1.DERInputStream;
import org.bouncycastle.asn1.DERObjectIdentifier;
import org.bouncycastle.asn1.DEROutputStream;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.X509Name;
import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.UserException;
import org.omg.CORBA.portable.ResponseHandler;
import org.omg.GSSUP.GSSUPMechOID;
import org.omg.GSSUP.InitialContextToken;
import org.omg.GSSUP.InitialContextTokenHelper;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.CORBA_2_3.portable.OutputStream;
import org.omg.CORBA_2_3.portable.InputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openejb.corba.CorbaApplicationServer;
import org.openejb.server.ServerFederation;
import org.openejb.spi.ApplicationServer;

/**
 * Various utility functions.
 * <p/>
 * Note: #getORB() and #getCodec() rely on UtilInitializer to initialze the ORB and codec.
 *
 * @version $Rev: $ $Date$
 * @see UtilInitializer
 */
public final class Util {
    private static final Log log = LogFactory.getLog(Util.class);
    private static final byte ASN_TAG_NT_EXPORTED_NAME1 = 0x04;
    private static final byte ASN_TAG_NT_EXPORTED_NAME2 = 0x01;
    private static final byte ASN_TAG_OID = 0x06;
    private static final byte ASN_TAG_GSS = 0x60;
    private static ORB orb;
    private static Codec codec;
    private static HandleDelegate handleDelegate;
    private static CorbaApplicationServer corbaApplicationServer = new CorbaApplicationServer();

    public static ORB getORB() {
        assert orb != null;
        return orb;
    }

    public static void setORB(ORB orb) throws UserException {
        if (Util.orb == null) {
            Util.orb = orb;
            CodecFactory factory = (CodecFactory) Util.orb.resolve_initial_references("CodecFactory");
            codec = factory.create_codec(new Encoding(ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 2));
        }
    }

    public static Codec getCodec() {
        assert codec != null;
        return codec;
    }

    public static HandleDelegate getHandleDelegate() throws NamingException {
        if (handleDelegate == null) {
            InitialContext ic = new InitialContext();
            handleDelegate = (HandleDelegate) ic.lookup("java:comp/HandleDelegate");
        }
        return handleDelegate;
    }

    public static byte[] encodeOID(String oid) throws IOException {
        oid = (oid.startsWith("oid:") ? oid.substring(4) : oid);
        return encodeOID(new DERObjectIdentifier(oid));
    }

    public static byte[] encodeOID(DERObjectIdentifier oid) throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DEROutputStream dOut = new DEROutputStream(bOut);

        dOut.writeObject(oid);

        return bOut.toByteArray();
    }

    public static String decodeOID(byte[] oid) throws IOException {
        return decodeOIDDERObjectIdentifier(oid).getId();
    }

    public static DERObjectIdentifier decodeOIDDERObjectIdentifier(byte[] oid) throws IOException {
        ByteArrayInputStream bIn = new ByteArrayInputStream(oid);
        DERInputStream dIn = new DERInputStream(bIn);

        return (DERObjectIdentifier) dIn.readObject();
    }

    public static byte[] encodeGeneralName(String name) throws IOException {
        return encodeGeneralName(new X509Name(name));
    }

    public static byte[] encodeGeneralName(X509Name x509Name) throws IOException {
        return encodeGeneralName(new GeneralName(x509Name));
    }

    public static byte[] encodeGeneralName(GeneralName generalName) throws IOException {
        ByteArrayOutputStream bOut = new ByteArrayOutputStream();
        DEROutputStream dOut = new DEROutputStream(bOut);

        dOut.writeObject(generalName);

        return bOut.toByteArray();
    }

    public static String decodeGeneralName(byte[] name) throws IOException {
        throw new java.lang.UnsupportedOperationException();
    }

    /**
     * This method encodes a name as if it was encoded using the GSS-API
     * gss_export_name() function call (see RFC 2743, page 84).
     * The oid to indicate names of this format is:<br/>
     * {1(iso), 3(org), 6(dod), 1(internet), 5(security), 6(nametypes),
     * 4(gss-api-exported-name)}<br/>
     * The token has the following format:
     * <table>
     * <tr><td><b>Offset</b></td><td><b>Meaning</b></td><td><b>Value</b></td></tr>
     * <tr><td>0</td><td>token id</td><td>0x04</td></tr>
     * <tr><td>1</td><td>token id</td><td>0x01</td></tr>
     * <p/>
     * <tr><td>2</td><td>oid length</td><td>hi-byte (len/0xFF)</td></tr>
     * <tr><td>3</td><td>oid length</td><td>lo-byte (len%0xFF)</td></tr>
     * <p/>
     * <tr><td>4</td><td>oid</td><td>oid:1.3.6.1.5.6.4</td></tr>
     * <p/>
     * <tr><td>n+0</td><td>name length</td><td>len/0xFFFFFF</td></tr>
     * <tr><td>n+1</td><td>name length</td><td>(len%0xFFFFFF)/0xFFFF</td></tr>
     * <tr><td>n+2</td><td>name length</td><td>((len%0xFFFFFF)%0xFFFF)/0xFF</td></tr>
     * <tr><td>n+3</td><td>name length</td><td>((len%0xFFFFFF)%0xFFFF)%0xFF</td></tr>
     * <p/>
     * <tr><td>n+4</td><td>name</td><td>foo</td></tr>
     * </table>
     *
     * @param oid  The oid of the mechanism this name is exported from.
     * @param name The name to be exported.
     * @return The byte array representing the exported name object.
     */
    public static byte[] encodeGSSExportName(String oid, String name) {
        try {
            byte[] oid_arr = encodeOID(oid);
            int oid_len = oid_arr.length;
            byte[] name_arr = name.getBytes("UTF-8");
            int name_len = name_arr.length;

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            // token id at 0
            baos.write(ASN_TAG_NT_EXPORTED_NAME1);
            baos.write(ASN_TAG_NT_EXPORTED_NAME2);

            // write the two length bytes
            baos.write((byte) (oid_len & 0xFF00) >> 8);
            baos.write((byte) (oid_len & 0x00FF));

            // oid at 2
            baos.write(oid_arr);

            // name length at n
            baos.write((byte) (name_len & 0xFF000000) >> 24);
            baos.write((byte) (name_len & 0x00FF0000) >> 16);
            baos.write((byte) (name_len & 0x0000FF00) >> 8);
            baos.write((byte) (name_len & 0x000000FF));

            // name at n+4
            baos.write(name_arr);
            return baos.toByteArray();
        } catch (Exception ex) {
            // do nothing, return null
        }
        return null;
    }

    /**
     * This function reads a name from a byte array which was created
     * by the gssExportName() method.
     *
     * @param name_tok The GSS name token.
     * @return The name from the GSS name token.
     */
    public static String decodeGSSExportName(byte[] name_tok) {
        String result = null;
        if (name_tok != null) {
            ByteArrayInputStream bais = new ByteArrayInputStream(name_tok);
            try {
                // GSSToken tag 1 0x04
                int t1 = bais.read();
                if (t1 == ASN_TAG_NT_EXPORTED_NAME1) {
                    // GSSToken tag 2 0x01
                    int t2 = bais.read();
                    if (t2 == ASN_TAG_NT_EXPORTED_NAME2) {
                        // read the two length bytes
                        int l = bais.read() << 8;
                        l += bais.read();

                        // read the oid
                        byte[] oid_arr = new byte[l];
                        bais.read(oid_arr, 0, l);
                        String oid = decodeOID(oid_arr);

                        if (oid.equals(GSSUPMechOID.value.substring(4))) {
                            int l1 = bais.read();
                            int l2 = bais.read();
                            int l3 = bais.read();
                            int l4 = bais.read();

                            int name_len = (l1 << 24) + (l2 << 16) + (l3 << 8) + l4;
                            byte[] name_arr = new byte[name_len];
                            bais.read(name_arr, 0, name_len);
                            result = new String(name_arr);
                        } else {
                            System.err.print("ASN1Utils.gssImportName: Unknown OID: " + oid +
                                    " ('" + Integer.toHexString(oid_arr[0]) + "')");
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                // do nothing, return null
            }
        }
        return result;
    }

    /**
     * Encode a mechanism independent initial context token (GSSToken). Defined
     * in [IETF RFC 2743] Section 3.1, "Mechanism-Independent token Format" pp. 81-82.
     * <table>
     * <tr><td><b>Offset</b></td><td><b>Meaning</b></td></tr>
     * <tr><td>0</td><td>ASN1 tag</td></tr>
     * <tr><td>1</td><td>token length (&lt;128)</td></tr>
     * <tr><td>2</td><td>mechanism oid</td></tr>
     * <tr><td>n</td><td>mechanism specific token (e.g. GSSUP::InitialContextToken)</td></tr>
     * </table>
     * Currently only one mechanism specific token is supported: GSS username password
     * (GSSUP::InitialContextToken).
     *
     * @param orb    The orb to get an Any from.
     * @param codec  The codec to do the encoding of the Any.
     * @param user   The username.
     * @param pwd    The password of the user.
     * @param target The target name.
     * @return The byte array of the ASN1 encoded GSSToken.
     */
    public static byte[] encodeGSSUPToken(ORB orb, Codec codec, String user, String pwd, String target) {
        byte[] result = null;
        try {
            // write the GSS ASN tag
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            baos.write(ASN_TAG_GSS);

            // create and encode a GSSUP initial context token
            InitialContextToken init_token = new InitialContextToken();
            init_token.username = (user + "@" + target).getBytes("UTF-8");

            init_token.password = pwd.getBytes("UTF-8");

            init_token.target_name = encodeGSSExportName(GSSUPMechOID.value.substring(4), target);

            Any a = orb.create_any();
            InitialContextTokenHelper.insert(a, init_token);
            byte[] init_ctx_token = codec.encode_value(a);

            // encode the mechanism oid
            byte[] oid_arr = encodeOID(GSSUPMechOID.value.substring(4));

            // write the length
            baos.write((byte) (oid_arr.length + init_ctx_token.length + 2));

            // write the mechanism oid
            baos.write(oid_arr);

            // write the
            baos.write(init_ctx_token);

            // get the bytes
            result = baos.toByteArray();
        } catch (Exception ex) {
            // do nothing, return null
        }
        return result;
    }

    /**
     * Decode an GSSUP InitialContextToken from a GSSToken.
     *
     * @param codec     The codec to do the encoding of the Any.
     * @param gssup_tok The InitialContextToken struct to fill in the decoded values.
     * @return Return true when decoding was successful, false otherwise.
     */
    public static boolean decodeGSSUPToken(Codec codec, byte[] token_arr,
                                           InitialContextToken gssup_tok) {
        boolean result = false;
        if (gssup_tok != null) {
            ByteArrayInputStream bais = new ByteArrayInputStream(token_arr);
            try {
                // GSSToken tag
                int c = bais.read();
                if (c == ASN_TAG_GSS) {
                    // GSSToken length
                    int token_len = bais.read();
                    // OID tag
                    int oid_tag = bais.read();
                    if (oid_tag == ASN_TAG_OID) {
                        // OID length
                        int oid_len = bais.read();
                        byte[] oid_tmp_arr = new byte[oid_len];
                        bais.read(oid_tmp_arr, 0, oid_len);
                        byte[] oid_arr = new byte[oid_len + 2];
                        oid_arr[0] = (byte) oid_tag;
                        oid_arr[1] = (byte) oid_len;
                        System.arraycopy(oid_tmp_arr, 0, oid_arr, 2, oid_len);
                        String oid = decodeOID(oid_arr);
                        if (oid.equals(GSSUPMechOID.value.substring(4))) {
                            int len = token_len - oid_len;
                            byte[] init_tok_arr = new byte[len];
                            bais.read(init_tok_arr, 0, len);
                            Any a = codec.decode_value(init_tok_arr,
                                    InitialContextTokenHelper.type());
                            InitialContextToken token = InitialContextTokenHelper.extract(a);
                            if (token != null) {
                                gssup_tok.username = token.username;
                                gssup_tok.password = token.password;
                                gssup_tok.target_name = decodeGSSExportName(token.target_name).getBytes("UTF-8");

                                result = true;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                // do nothing, return false
            }
        }
        return result;
    }

    public static String byteToString(byte[] data) {
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < data.length; i++) {
            buffer.append(HEXCHAR[(data[i] >>> 4) & 0x0F]);
            buffer.append(HEXCHAR[(data[i]) & 0x0F]);
        }
        return buffer.toString();

    }

    private static final char[] HEXCHAR = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static void writeObject(Class type, Object object, OutputStream out) {
        if (type == Void.TYPE) {
            // do nothing for a void
        } else if (type == Boolean.TYPE) {
            out.write_boolean(((Boolean) object).booleanValue());
        } else if (type == Byte.TYPE) {
            out.write_octet(((Byte) object).byteValue());
        } else if (type == Character.TYPE) {
            out.write_wchar(((Character) object).charValue());
        } else if (type == Double.TYPE) {
            out.write_double(((Double) object).doubleValue());
        } else if (type == Float.TYPE) {
            out.write_float(((Float) object).floatValue());
        } else if (type == Integer.TYPE) {
            out.write_long(((Integer) object).intValue());
        } else if (type == Long.TYPE) {
            out.write_longlong(((Long) object).longValue());
        } else if (type == Short.TYPE) {
            out.write_short(((Short) object).shortValue());
        }  else {
            // object types must bbe written in the context of the corba application server
            // which properly write replaces our objects for corba
            ApplicationServer oldApplicationServer = ServerFederation.getApplicationServer();
            try {
                ServerFederation.setApplicationServer(corbaApplicationServer);
                if (type == Object.class || type == Serializable.class) {
                    javax.rmi.CORBA.Util.writeAny(out, object);
                } else if (org.omg.CORBA.Object.class.isAssignableFrom(type)) {
                    out.write_Object((org.omg.CORBA.Object) object);
                } else if (Remote.class.isAssignableFrom(type)) {
                    javax.rmi.CORBA.Util.writeRemoteObject(out, object);
                } else if (type.isInterface() && Serializable.class.isAssignableFrom(type)) {
                    javax.rmi.CORBA.Util.writeAbstractObject(out, object);
                } else {
                    out.write_value((Serializable) object, type);
                }
            } finally {
                ServerFederation.setApplicationServer(oldApplicationServer);
            }
        }
    }

    public static Object readObject(Class type, InputStream in) {
        if (type == Void.TYPE) {
            return null;
        } else if (type == Boolean.TYPE) {
            return new Boolean(in.read_boolean());
        } else if (type == Byte.TYPE) {
            return new Byte(in.read_octet());
        } else if (type == Character.TYPE) {
            return new Character(in.read_wchar());
        } else if (type == Double.TYPE) {
            return new Double(in.read_double());
        } else if (type == Float.TYPE) {
            return new Float(in.read_float());
        } else if (type == Integer.TYPE) {
            return new Integer(in.read_long());
        } else if (type == Long.TYPE) {
            return new Long(in.read_longlong());
        } else if (type == Short.TYPE) {
            return new Short(in.read_short());
        } else if (type == Object.class || type == Serializable.class) {
            return javax.rmi.CORBA.Util.readAny(in);
        } else if (org.omg.CORBA.Object.class.isAssignableFrom(type)) {
            return in.read_Object(type);
        } else if (Remote.class.isAssignableFrom(type)) {
            return PortableRemoteObject.narrow(in.read_Object(), type);
        } else if (type.isInterface() && Serializable.class.isAssignableFrom(type)) {
            return in.read_abstract_interface();
        } else {
            return in.read_value(type);
        }
    }

    public static void throwException(Method method, InputStream in) throws Throwable {
        // read the exception id
        final String id = in.read_string();

        // get the class name from the id
        if (!id.startsWith("IDL:")) {
            log.warn("Malformed exception id: " + id);
            return;
        }

        Class[] exceptionTypes = method.getExceptionTypes();
        for (int i = 0; i < exceptionTypes.length; i++) {
            Class exceptionType = exceptionTypes[i];

            String exceptionId = getExceptionId(exceptionType);
            if (id.equals(exceptionId)) {
                throw (Throwable) in.read_value(exceptionType);
            }
        }
        throw new UnexpectedException(id);
    }

    public static OutputStream writeUserException(Method method, ResponseHandler reply, Exception exception) throws Exception {
        if (exception instanceof RuntimeException || exception instanceof RemoteException) {
            throw exception;
        }

        Class[] exceptionTypes = method.getExceptionTypes();
        for (int i = 0; i < exceptionTypes.length; i++) {
            Class exceptionType = exceptionTypes[i];
            if (!exceptionType.isInstance(exception)) {
                continue;
            }

            OutputStream out = (OutputStream) reply.createExceptionReply();
            String exceptionId = getExceptionId(exceptionType);
            out.write_string(exceptionId);
            out.write_value(exception);
            return out;
        }
        throw exception;
    }

    private static String getExceptionId(Class exceptionType) {
        String exceptionName = exceptionType.getName().replace('.', '/');
        if (exceptionName.endsWith("Exception")) {
            exceptionName = exceptionName.substring(0, exceptionName.length() - "Exception".length());
        }
        exceptionName += "Ex";
        String exceptionId = "IDL:" + exceptionName + ":1.0";
        return exceptionId;
    }
}
