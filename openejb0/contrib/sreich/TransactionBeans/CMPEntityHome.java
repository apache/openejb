package transactiontests;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface CMPEntityHome extends EJBHome {

    //
    // Creation methods
    //

    public CMPEntity create() throws RemoteException, CreateException;

    //
    // Find methods
    //

    public CMPEntity findByPrimaryKey(String key) throws RemoteException, FinderException;
    public java.util.Collection findAll() throws RemoteException, FinderException;
}

