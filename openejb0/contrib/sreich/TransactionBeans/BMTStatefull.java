package transactiontests;

import javax.ejb.*;
import java.rmi.RemoteException;
import java.rmi.Remote;

public interface BMTStatefull extends EJBObject, BMTMethods {

    //
    // Business Logic Interfaces
    //

    // Example:
    // public String hello() throws java.rmi.RemoteException;
}