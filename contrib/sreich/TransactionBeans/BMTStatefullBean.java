package transactiontests;

import javax.ejb.*;

public class BMTStatefullBean extends BMTMethodsImpl implements SessionBean  {


    //
    // Creation methods
    //

    /* Stateful session beans may have  multiple create methods taking
    * different parameters. They must all be reflected in identically
    * named methods in the home interface without the 'ejb' prefix
    * and initial cap.
    *
    */

    public BMTStatefullBean() {
    }

    public void ejbCreate() throws CreateException {
    }


    //
    // SessionBean interface implementation
    //


    public void setSessionContext(SessionContext ctx) {
        _ctx = ctx;
    }

    public void ejbPassivate() {

        /* the bean will be serialized and destroyed
        * release any resources your bean may be holding
        */
    }

    public void ejbActivate() {

        /* the bean has been deserialized
        * acquire any resources the bean needs
        */
    }

    public void ejbRemove() {

        /* the bean is on the path to destruction
        */
    }

    
}