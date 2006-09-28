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
package org.apache.openejb.deployment.entity.cmp.ejbql;


import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.openejb.deployment.entity.cmp.AbstractCmpTest;

/**
 * @version $Revision$ $Date$
 */
public class EJBQLTest extends AbstractCmpTest {

    private AHome aHome;
    private ALocalHome aLocalHome;

    public void testHomeFindTest() throws Exception {
        ARemote a = aHome.findTest("test");
        assertEquals(new Integer(1), a.getField1());
    }

    public void testLocalHomeFindTest() throws Exception {
        ALocal a = aLocalHome.findTest("test");
        assertEquals(new Integer(1), a.getField1());
    }

    public void testSelectTest() throws Exception {
        ALocal a = aLocalHome.selectTest("test");
        assertEquals(new Integer(1), a.getField1());
    }

    protected void buildDBSchema(Connection c) throws Exception {
        Statement s = c.createStatement();
        try {
            s.execute("DROP TABLE A");
        } catch (SQLException e) {
            // ignore
        }

        s.execute("CREATE TABLE A(A1 INTEGER, A2 VARCHAR(50))");

        s.execute("INSERT INTO A(A1, A2) VALUES(1, 'test')");
        s.close();
        c.close();
    }

    protected String getEjbJarDD() {
        return "src/test-cmp/ejb-ql/ejb-jar.xml";
    }

    protected String getOpenEjbJarDD() {
        return "src/test-cmp/ejb-ql/openejb-jar.xml";
    }

    protected void setUp() throws Exception {
        super.setUp();

        initCmpModule();
        
        AbstractName deploymentName = naming.createChildName(moduleName, "A", NameFactory.ENTITY_BEAN);
        addCmpEjb("A", ABean.class, AHome.class, ARemote.class, ALocalHome.class, ALocal.class, Integer.class, deploymentName);

        startConfiguration();

        aLocalHome = (ALocalHome) kernel.getAttribute(deploymentName, "ejbLocalHome");
        aHome = (AHome) kernel.getAttribute(deploymentName, "ejbHome");
    }
}