package transactiontests;

import javax.ejb.*;

public class BMTStatelessBean extends BMTMethodsImpl implements SessionBean  {


    //
    // Creation methods
    //

    public BMTStatelessBean() {
    }

    public void ejbCreate() throws CreateException {
        /* Stateless session bean create methods never have parameters */
    }


    //
    // SessionBean interface implementation
    //

    public void setSessionContext(SessionContext ctx) {
        _ctx=ctx;
        /* does not apply to stateless session beans */
    }

    public void ejbPassivate() {

        /* does not apply to stateless session beans */
    }

    public void ejbActivate() {

        /* does not apply to stateless session beans */
    }

    public void ejbRemove() {

        /* does not apply to stateless session beans */
    }

}