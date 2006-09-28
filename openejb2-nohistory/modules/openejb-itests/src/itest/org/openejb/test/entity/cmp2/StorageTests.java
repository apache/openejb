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
package org.openejb.test.entity.cmp2;

import java.util.Properties;
import java.util.Arrays;
import javax.naming.Context;
import javax.naming.InitialContext;

import org.openejb.test.NamedTestCase;
import org.openejb.test.TestManager;
import org.openejb.test.entity.cmp2.model.StorageHome;
import org.openejb.test.entity.cmp2.model.StorageRemote;


/**
 * @version $Revision$ $Date$
 */
public class StorageTests extends NamedTestCase {
    private InitialContext initialContext;
    private StorageHome ejbHome;
    private StorageRemote storage;
    private byte[] testdata;

    public StorageTests() {
        super("StorageTests.");
    }

    public void testStorageBlob() throws Exception {

        storage.setBlob(testdata);
        byte[] readBlob = storage.getBlob();
        assertTrue(Arrays.equals(testdata, readBlob));
    }

    public void testReadBlob() throws Exception {
        storage.setBlob(testdata);
        byte[] readbytes = storage.getBytes();
        assertTrue(Arrays.equals(testdata, readbytes));
    }

    public void testWriteBlob() throws Exception {
        storage.setBytes(testdata);
        byte[] readblob = storage.getBlob();
        assertTrue(Arrays.equals(testdata, readblob));
    }

    public void testChar() throws Exception {
        char expectedChar = 'c';
        storage.setChar(expectedChar);
        char readChar = storage.getChar();
        assertEquals(expectedChar, readChar);
    }
    
    protected void setUp() throws Exception {
        Properties properties = TestManager.getServer().getContextEnvironment();
        properties.put(Context.SECURITY_PRINCIPAL, "ENTITY_TEST_CLIENT");
        properties.put(Context.SECURITY_CREDENTIALS, "ENTITY_TEST_CLIENT");

        initialContext = new InitialContext(properties);

        ejbHome = (StorageHome) javax.rmi.PortableRemoteObject.narrow(initialContext.lookup("cmp2/Storage"), StorageHome.class);
        storage = ejbHome.create(new Integer(1));

        testdata = "this is a test".getBytes();
    }

    protected void tearDown() throws Exception {
        storage.remove();
    }
}