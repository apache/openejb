package transactiontests;

import javax.ejb.*;
import java.rmi.RemoteException;
import java.rmi.Remote;

public interface StatefullSession extends EJBObject, Transactions {

    //
    // Business Logic Interfaces
    //

    // Example:
    // public String hello() throws java.rmi.RemoteException;
}