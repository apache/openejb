package transactiontests;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface BMPEntityHome extends EJBHome {

    //
    // Creation methods
    //

    public BMPEntity create() throws RemoteException, CreateException;


    //
    // Find methods
    //

    public BMPEntity findByPrimaryKey(String key) throws RemoteException, FinderException;
    public java.util.Enumeration findAll() throws RemoteException, FinderException;
}

