package transactiontests;

import javax.ejb.*;
import javax.transaction.UserTransaction;

public class StatefullSessionBean extends TransactionsImpl implements SessionBean, Transactions, SessionSynchronization {


    //
    // Creation methods
    //

    /* Stateful session beans may have  multiple create methods taking
    * different parameters. They must all be reflected in identically
    * named methods in the home interface without the 'ejb' prefix
    * and initial cap.
    *
    */

    public StatefullSessionBean() {
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

    /** Section 6.5.3
    The afterBegin notification signals a session bean instance that a new transaction has begun. The
container invokes this method before the first business method within a transaction (which is not necessarily
at the beginning of the transaction). The afterBegin notification is invoked with the transaction
context. The instance may do any database work it requires within the scope of the transaction.
    */
    public void afterBegin() {
        System.out.print("afterBegin.");
    }

    /** Section 6.5.3
    The afterCompletion notification signals that the current transaction has completed. A completion
status of true indicates that the transaction has committed; a status of false indicates that a rollback
has occurred. Since a session bean instance’s conversational state is not transactional, it may need to
manually reset its state if a rollback occurred.
    */
    public void afterCompletion(boolean completed){
        System.out.println(" afterCompletion(). completed="+completed);
    }

    /** Section 6.5.3
The beforeCompletion notification is issued when a session bean instance’s client has completed
work on its current transaction but prior to committing the resource managers used by the instance. At
this time, the instance should write out any database updates it has cached. The instance can cause the
transaction to roll back by invoking the setRollbackOnly method on its session context.
    */
    public void beforeCompletion(){
        System.out.print("beforeCompletion().");
    }


}