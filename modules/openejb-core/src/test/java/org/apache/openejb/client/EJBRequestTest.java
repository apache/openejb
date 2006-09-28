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
 * Copyright 2004 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.apache.openejb.client;

import java.io.*;
import java.lang.reflect.Method;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.Handle;

import junit.framework.TestCase;
import org.omg.CORBA.UserException;
import org.apache.openejb.EJBComponentType;

public class EJBRequestTest extends TestCase implements RequestMethods {
    private EJBMetaDataImpl ejb;

    static interface FooHome extends EJBHome {
        FooObject create();
        FooObject findByPrimaryKey(Integer key);
    }
    static interface FooObject extends EJBObject{
        String businessMethod(String param) throws UserException;
    }


    protected void setUp() throws Exception {
        ejb = new EJBMetaDataImpl(FooHome.class,FooObject.class,Integer.class,EJBComponentType.BMP_ENTITY,"FooBeanID");
    }

    public void testEJBHomeCreate() throws Exception {
        int requestMethod = RequestMethods.EJB_HOME_CREATE;
        Method method = FooHome.class.getMethod("create", new Class[]{});
        Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    public void testEJBHomeFind() throws Exception {
        int requestMethod = RequestMethods.EJB_HOME_FIND;
        Method method = FooHome.class.getMethod("findByPrimaryKey", new Class[]{Integer.class});
        Object[] args = new Object[]{new Integer(4)};

        invoke(requestMethod, method, args);
    }

    public void testEJBHomeRemove1() throws Exception {
        int requestMethod = RequestMethods.EJB_HOME_REMOVE_BY_HANDLE;
        Method method = FooHome.class.getMethod("remove", new Class[]{Handle.class});
        Object[] args = new Object[]{null};

        invoke(requestMethod, method, args);
    }

    public void testEJBHomeRemove2() throws Exception {
        int requestMethod = RequestMethods.EJB_HOME_REMOVE_BY_PKEY;
        Method method = FooHome.class.getMethod("remove", new Class[]{Object.class});
        Object[] args = new Object[]{new Integer(4)};

        invoke(requestMethod, method, args);
    }

    public void testGetMetaData() throws Exception {
        int requestMethod = RequestMethods.EJB_HOME_GET_EJB_META_DATA;
        Method method = FooHome.class.getMethod("getEJBMetaData", new Class[]{});
        Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    public void testGetHomeHandle() throws Exception {
        int requestMethod = RequestMethods.EJB_HOME_GET_HOME_HANDLE;
        Method method = FooHome.class.getMethod("getHomeHandle", new Class[]{});
        Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    public void testBusinessMethod() throws Exception {
        int requestMethod = RequestMethods.EJB_OBJECT_BUSINESS_METHOD;
        Method method = FooObject.class.getMethod("businessMethod", new Class[]{String.class});
        Object[] args = new Object[]{"hola mundo"};

        invoke(requestMethod, method, args);
    }

    public void testGetEJBHome() throws Exception {
        int requestMethod = RequestMethods.EJB_OBJECT_GET_EJB_HOME;
        Method method = FooObject.class.getMethod("getEJBHome", new Class[]{});
        Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    public void testGetHandle() throws Exception {
        int requestMethod = RequestMethods.EJB_OBJECT_GET_HANDLE;
        Method method = FooObject.class.getMethod("getHandle", new Class[]{});
        Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    public void testGetPrimaryKey() throws Exception {
        int requestMethod = RequestMethods.EJB_OBJECT_GET_PRIMARY_KEY;
        Method method = FooObject.class.getMethod("getPrimaryKey", new Class[]{});
        Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    public void testIsIdentical() throws Exception {
        int requestMethod = RequestMethods.EJB_OBJECT_IS_IDENTICAL;
        Method method = FooObject.class.getMethod("isIdentical", new Class[]{EJBObject.class});
        Object[] args = new Object[]{null};

        invoke(requestMethod, method, args);
    }

    public void testEJBObjectRemove() throws Exception {
        int requestMethod = RequestMethods.EJB_OBJECT_REMOVE;
        Method method = FooObject.class.getMethod("remove", new Class[]{});
        Object[] args = new Object[]{};

        invoke(requestMethod, method, args);
    }

    private void invoke(int requestMethod, Method method, Object[] args) throws IOException, ClassNotFoundException {
        EJBRequest expected = new EJBRequest(requestMethod);
        expected.setContainerCode(ejb.deploymentCode);
        expected.setContainerID(ejb.deploymentID);
        expected.setMethodInstance(method);
        expected.setMethodParameters(args);

        EJBRequest actual = new EJBRequest();

        externalize(expected, actual);

        assertEquals("RequestMethod", expected.getRequestMethod(), actual.getRequestMethod());
        assertEquals("ContainerID", expected.getContainerID(), actual.getContainerID());
        assertEquals("ContainerCode", expected.getContainerCode(), actual.getContainerCode());
        assertEquals("MethodInstance", expected.getMethodInstance(), actual.getMethodInstance());

        Object[] expectedParams = expected.getMethodParameters();
        Object[] actualParams = actual.getMethodParameters();

        assertNotNull("MethodParameters",actualParams);
        assertEquals("MethodParameters.length", expectedParams.length, actualParams.length);
        for (int i = 0; i < expectedParams.length; i++) {
            assertEquals("MethodParameters."+i, expectedParams[i], actualParams[i]);
        }
    }

    private void externalize(Externalizable original, Externalizable copy) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);

        original.writeExternal(out);
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);

        copy.readExternal(in);
    }






}