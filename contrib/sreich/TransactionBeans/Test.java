package transactiontests;

import javax.ejb.*;
import java.rmi.RemoteException;
import java.rmi.Remote;

public interface Test extends EJBObject {

  public void test() throws java.rmi.RemoteException;
}