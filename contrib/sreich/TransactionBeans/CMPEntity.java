package transactiontests;

import javax.ejb.*;
import java.rmi.RemoteException;
import java.rmi.Remote;

public interface CMPEntity extends EJBObject, Transactions {

    public void setValue(String d) throws java.rmi.RemoteException;
    public String getValue() throws java.rmi.RemoteException;
    public javax.transaction.UserTransaction getUserTransaction() throws java.rmi.RemoteException;    
}