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
package org.openejb.entity;

import junit.framework.TestCase;

/**
 * @version $Revision$ $Date$
 */
public class SQLGeneratorCMPEntityContainerTest extends TestCase {
    public void testNothing() {
    }
/*
    private static final ObjectName CONTAINER_NAME = JMXUtil.getObjectName("geronimo.test:ejb=Mock");
    private static final ObjectName TM_NAME = JMXUtil.getObjectName("geronimo.test:role=TransactionManager");
    private static final ObjectName TCA_NAME = JMXUtil.getObjectName("geronimo.test:role=TrackedConnectionAssociator");
    private org.openejb.EJBContainerConfiguration config;
    private Kernel kernel;
    private GBeanMBean container;
    private MBeanServer mbServer;

    static {
        new org.hsqldb.jdbcDriver();
    }

    private final jdbcDataSource ds = new jdbcDataSource();

    public void testLocalInvoke() throws Exception {
        //
        // Note: this connection must be held open for the entire test or hsqldb will drop the in memory datatbase
        //
        Connection c = initDatabase();

        MockLocalHome home = (MockLocalHome) mbServer.invoke(CONTAINER_NAME, "getEJBLocalHome", null, null);
        assertEquals(2, home.intMethod(1));

        MockLocal local = home.findByPrimaryKey(new Integer(1));

        System.out.println("The Int field: " + local.getIntField() + " and the value: " + local.getValue());

        assertEquals(3, local.intMethod(1));
        assertEquals(1, local.getIntField());
        c.close();
    }

    public void testFields() throws Exception {
        //
        // Note: this connection must be held open for the entire test or hsqldb will drop the in memory datatbase
        //
        Connection c = initDatabase();
        Statement s = c.createStatement();
        MockLocalHome home = (MockLocalHome) mbServer.invoke(CONTAINER_NAME, "getEJBLocalHome", null, null);
        MockLocal local = home.findByPrimaryKey(new Integer(1));
        assertEquals("Hello", local.getValue());
        local.setValue("World");
        ResultSet rs = s.executeQuery("SELECT VALUE FROM MOCK WHERE ID=1");
        assertTrue(rs.next());
        assertEquals("World", rs.getString(1));

        assertEquals("World", local.getValue());

        s.close();
        c.close();
    }

    public void testLifeCycle() throws Exception {
        //
        // Note: this connection must be held open for the entire test or hsqldb will drop the in memory datatbase
        //
        Connection c = initDatabase();
        MockLocalHome home = (MockLocalHome) mbServer.invoke(CONTAINER_NAME, "getEJBLocalHome", null, null);


        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT ID FROM MOCK WHERE ID=2");
        assertFalse(rs.next());
        rs.close();

        MockLocal local = home.create(new Integer(2), "Hello");
        rs = s.executeQuery("SELECT VALUE FROM MOCK WHERE ID=2");
        assertTrue(rs.next());
        assertEquals("Hello", rs.getString(1));
        rs.close();

        local = home.findByPrimaryKey(new Integer(2));
        assertEquals("Hello", local.getValue());

        local.remove();
        rs = s.executeQuery("SELECT ID FROM MOCK WHERE ID=2");
        assertFalse(rs.next());
        rs.close();
        s.close();
        c.close();
    }

    public void testSelect() throws Exception {
        //
        // Note: this connection must be held open for the entire test or hsqldb will drop the in memory datatbase
        //
        Connection c = initDatabase();
        MockLocalHome home = (MockLocalHome) mbServer.invoke(CONTAINER_NAME, "getEJBLocalHome", null, null);


        assertEquals("Hello", home.singleSelect(new Integer(1)));
        try {
            home.singleSelect(new Integer(2));
            fail("did not get ObjectNotFoundException");
        } catch (ObjectNotFoundException e) {
            // ok
        }

        System.err.println("alright, singleSelect now works");
        Collection result = home.multiSelect(new Integer(1));
        assertEquals(1, result.size());
        assertEquals("Hello", result.iterator().next());


        result = home.multiSelect(new Integer(0));
        assertEquals(0, result.size());

        System.err.println("Got past multiSelect too!");
        result = home.multiObject(new Integer(1));
        assertEquals(1, result.size());
        MockLocal local = (MockLocal) result.iterator().next();
        assertEquals(new Integer(1), local.getPrimaryKey());

        c.close();
    }


    public void testACID() throws Exception {
        //
        // Note: this connection must be held open for the entire test or hsqldb will drop the in memory datatbase
        //
        Connection c = initDatabase();
        final String DENT = "Huh? What? Where's the tea?";

        MockLocalHome home = (MockLocalHome) mbServer.invoke(CONTAINER_NAME, "getEJBLocalHome", null, null);


        MockLocal tBean = home.create(new Integer(42), "Life, the universe, and everything");

        assertNotNull(tBean);

        tBean = null;

        tBean = home.findByPrimaryKey(new Integer(42));

        tBean.setValue(DENT);

        tBean = null;
        tBean = home.findByPrimaryKey(new Integer(42));

        assertEquals(DENT, tBean.getValue());

        tBean.remove();


        boolean beanRemoved = false;

        try {
            tBean = home.findByPrimaryKey(new Integer(42));
        } catch (FinderException fe) {
            beanRemoved = true;
        }

        assertTrue(beanRemoved);

        c.close();
    }

    protected void setUp() throws Exception {
        super.setUp();


        String abstractSchemaName = "Mock";
        String dbTable = "MOCK";

        ds.setDatabase(".");
        ds.setUser("sa");
        ds.setPassword("");

        config = new org.openejb.EJBContainerConfiguration();
        //config.uri = new URI("async", null, "localhost", 3434, "/JMX", null, CONTAINER_NAME.toString());
        config.beanClassName = MockCMPEJB.class.getName();
        config.homeInterfaceName = MockHome.class.getName();
        config.localHomeInterfaceName = MockLocalHome.class.getName();
        config.remoteInterfaceName = MockRemote.class.getName();
        config.localInterfaceName = MockLocal.class.getName();
        config.txnDemarcation = TransactionDemarcation.CONTAINER;
        config.pkClassName = Integer.class.getName();
        config.unshareableResources = new HashSet();
        config.transactionPolicySource = new TransactionPolicySource() {
            public TransactionPolicy getTransactionPolicy(String methodIntf, MethodSignature signature) {
                return ContainerPolicy.Required;
            }

            public TransactionPolicy getTransactionPolicy(String methodIntf, String methodName, String[] parameterTypes) {
                return ContainerPolicy.Required;
            }
        };

        SimpleCommandFactory persistenceFactory = new SimpleCommandFactory(ds);
        ArrayList queries = new ArrayList();
        HashMap colMap = new HashMap();
        HashMap typeMap = new HashMap();
        MethodSignature signature;
        String sql;
        Binding[] inputBindings;
        ArrayList bindings;

        CMPConfiguration cmpConfig = new CMPConfiguration();
        cmpConfig.defineFields(new String[]{"id", "value"});


        colMap.put("id", "ID");
        colMap.put("value", "VALUE");
        typeMap.put("id", "java.lang.Integer");
        typeMap.put("value", "java.lang.String");

        SQLGenerator sqlHelper = new SQLGenerator(cmpConfig.cmpFieldMap, colMap, dbTable);
        CMPBindingGenerator binder = new CMPBindingGenerator(cmpConfig.cmpFieldMap, typeMap);

        signature = new MethodSignature("ejbFindByPrimaryKey", new String[]{"java.lang.Object"});
        sql = sqlHelper.createQuery(new String[]{"id"}, new String[]{"id"});
        bindings = binder.bindQuery(new String[]{"id"}, new String[]{"id"});
        persistenceFactory.defineQuery(signature, sql, (Binding[]) bindings.get(1), (Binding[]) bindings.get(0));
        queries.add(new CMPQuery(abstractSchemaName, false, signature, false, null));

        signature = new MethodSignature("ejbLoad", new String[]{});
        sql = sqlHelper.createQuery(new String[]{"id", "value"}, new String[]{"id"});
        bindings = binder.bindQuery(new String[]{"id", "value"}, new String[]{"id"});
        persistenceFactory.defineQuery(signature, sql, (Binding[]) bindings.get(binder.INPUT_BINDINGS), (Binding[]) bindings.get(binder.OUTPUT_BINDINGS));
        queries.add(new CMPQuery(signature, false, null));

        signature = new MethodSignature("ejbSelectSingleValue", new String[]{"java.lang.Integer"});
        sql = sqlHelper.createQuery(new String[]{"value"}, new String[]{"id"});
        bindings = binder.bindQuery(new String[]{"value"}, new String[]{"id"}, true);
        persistenceFactory.defineQuery(signature, sql, (Binding[]) bindings.get(binder.INPUT_BINDINGS), (Binding[]) bindings.get(binder.OUTPUT_BINDINGS));
        queries.add(new CMPQuery(signature, false, null));

        signature = new MethodSignature("ejbSelectMultiValue", new String[]{"java.lang.Integer"});
        sql = sqlHelper.createQuery(new String[]{"value"}, new String[]{"id"});
        bindings = binder.bindQuery(new String[]{"value"}, new String[]{"id"}, true);
        persistenceFactory.defineQuery(signature, sql, (Binding[]) bindings.get(binder.INPUT_BINDINGS), (Binding[]) bindings.get(binder.OUTPUT_BINDINGS));
        queries.add(new CMPQuery(signature, true, null));

        signature = new MethodSignature("ejbSelectMultiObject", new String[]{"java.lang.Integer"});
        sql = sqlHelper.createQuery(new String[]{"id"}, new String[]{"id"});
        bindings = binder.bindQuery(new String[]{"id"}, new String[]{"id"}, true);
        persistenceFactory.defineQuery(signature, sql, (Binding[]) bindings.get(binder.INPUT_BINDINGS), (Binding[]) bindings.get(binder.OUTPUT_BINDINGS));
        queries.add(new CMPQuery("Mock", true, signature, true, null));


        signature = new MethodSignature("ejbCreate", new String[]{"java.lang.Integer", "java.lang.String"});
        sql = sqlHelper.createInsert(new String[]{"id", "value"});
        inputBindings = binder.bindUpdate(new String[]{"id", "value"});
        persistenceFactory.defineUpdate(signature, sql, inputBindings);

        signature = new MethodSignature("ejbRemove", new String[0]);
        sql = sqlHelper.createDelete(new String[]{"id"});
        inputBindings = binder.bindUpdate(new String[]{"id"});
        persistenceFactory.defineUpdate(signature, sql, inputBindings);

        signature = new MethodSignature("ejbStore", new String[0]);
        sql = sqlHelper.createUpdate(new String[]{"value"}, new String[]{"id"});
        inputBindings = binder.bindUpdate(new String[]{"value", "id"}); // TODO: Careful of ordering here - we should cleanup this tricky bit
        persistenceFactory.defineUpdate(signature, sql, inputBindings);


        cmpConfig.persistenceFactory = persistenceFactory;
        cmpConfig.queries = (CMPQuery[]) queries.toArray(new CMPQuery[0]);

        cmpConfig.relations = new CMRelation[]{};
        cmpConfig.schema = "Mock";

        kernel = new Kernel("SQLGeneratorContainerManagedPersistenceTest");
        kernel.boot();
        mbServer = kernel.getMBeanServer();

        GBeanMBean transactionManager = new GBeanMBean(TransactionManagerProxy.GBEAN_INFO);
        transactionManager.setAttribute("Delegate", new MockTransactionManager());
        start(TM_NAME, transactionManager);

        GBeanMBean trackedConnectionAssociator = new GBeanMBean(ConnectionTrackingCoordinator.GBEAN_INFO);
        start(TCA_NAME, trackedConnectionAssociator);

        container = new GBeanMBean(CMPEntityContainer.GBEAN_INFO);
        container.setAttribute("EJBContainerConfiguration", config);
        container.setAttribute("CMPConfiguration", cmpConfig);
        container.setReferencePatterns("TransactionManager", Collections.singleton(TM_NAME));
        container.setReferencePatterns("TrackedConnectionAssociator", Collections.singleton(TCA_NAME));
        start(CONTAINER_NAME, container);

    }

    private void start(ObjectName name, Object instance) throws Exception {
        mbServer.registerMBean(instance, name);
        mbServer.invoke(name, "start", null, null);
    }

    private void stop(ObjectName name) throws Exception {
        mbServer.invoke(name, "stop", null, null);
        mbServer.unregisterMBean(name);
    }


    protected void tearDown() throws Exception {
        stop(CONTAINER_NAME);
        stop(TM_NAME);
        stop(TCA_NAME);
        kernel.shutdown();
    }

    private Connection initDatabase() throws SQLException {
        Connection c = DriverManager.getConnection("jdbc:hsqldb:.", "sa", "");
        Statement s = c.createStatement();
        s.execute("CREATE TABLE MOCK(ID INTEGER, VALUE VARCHAR(50))");
        s.execute("INSERT INTO MOCK(ID, VALUE) VALUES(1, 'Hello')");
        s.close();
        return c;
    }

*/
}
