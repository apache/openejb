package transactiontests;

import javax.ejb.*;
import java.rmi.RemoteException;

public interface BMTStatefullHome extends EJBHome {


    /** Creation methods **/

    /* Stateful session beans may have multiple create methods taking
    * different parameters. They must all be reflected in identically
    * named methods in the home interface without the 'ejb' prefix
    * and initial cap.
    *
    * Stateless session bean create methods never have parameters.
    *
    */

    public BMTStatefull create() throws RemoteException, CreateException;


}

