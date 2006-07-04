package org.openejb.test.entity.cmp2;

import java.rmi.RemoteException;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.openejb.test.NamedTestCase;
import org.openejb.test.TestFailureException;
import org.openejb.test.TestManager;
import org.openejb.test.entity.cmp2.cmrmapping.CMRMappingFacadeHome;
import org.openejb.test.entity.cmp2.cmrmapping.CMRMappingFacadeRemote;


/**
 * @version $Revision$ $Date$
 */
public class CMRMappingTests extends NamedTestCase {
    private InitialContext initialContext;
    private CMRMappingFacadeHome ejbHome;
    private CMRMappingFacadeRemote facade;

    public CMRMappingTests() {
        super("CMRMappingTests.");
    }

    protected void setUp() throws Exception {
        Properties properties = TestManager.getServer().getContextEnvironment();
        properties.put(Context.SECURITY_PRINCIPAL, "ENTITY_TEST_CLIENT");
        properties.put(Context.SECURITY_CREDENTIALS, "ENTITY_TEST_CLIENT");

        initialContext = new InitialContext(properties);

        ejbHome = (CMRMappingFacadeHome) javax.rmi.PortableRemoteObject.narrow(initialContext.lookup("cmp2/CMRMappingFacade"), CMRMappingFacadeHome.class);
        facade = ejbHome.create();
    }

    public void testOneToManyDoNotSetCMR() throws RemoteException, TestFailureException {
        facade.testOneToManyDoNotSetCMR();
    }

    public void testOneToManySetCMROnInverseSide() throws RemoteException, TestFailureException {
        facade.testOneToManySetCMROnInverseSide();
    }

    public void testOneToManySetCMROnInverseSideResetPK() throws RemoteException, TestFailureException {
        facade.testOneToManySetCMROnInverseSideResetPK();
    }

    public void testOneToManySetCMROnOwningSide() throws RemoteException, TestFailureException {
        facade.testOneToManySetCMROnOwningSide();
    }

    public void testOneToManySetCMROnOwningSideResetPK() throws RemoteException, TestFailureException {
        facade.testOneToManySetCMROnOwningSideResetPK();
    }

    public void testOneToOneDoNotSetCMR() throws RemoteException, TestFailureException {
        facade.testOneToOneDoNotSetCMR();
    }

    public void testOneToOneSetCMROnInverseSide() throws RemoteException, TestFailureException {
        facade.testOneToOneSetCMROnInverseSide();
    }

    public void testOneToOneSetCMROnInverseSideResetPK() throws RemoteException, TestFailureException {
        facade.testOneToOneSetCMROnInverseSideResetPK();
    }

    public void testOneToOneSetCMROnOwningSide() throws RemoteException, TestFailureException {
        facade.testOneToOneSetCMROnOwningSide();
    }

    public void testOneToOneSetCMROnOwningSideResetPK() throws RemoteException, TestFailureException {
        facade.testOneToOneSetCMROnOwningSideResetPK();
    }
}