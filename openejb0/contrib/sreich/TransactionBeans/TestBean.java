package transactiontests;

import javax.ejb.*;
import javax.transaction.*;
import javax.naming.*;
import java.rmi.RemoteException;
import java.util.Properties;

public class TestBean implements SessionBean {

    SessionContext _ctx;

    //
    // Creation methods
    //

    public TestBean() {
    }

    public void ejbCreate() throws CreateException {
        /* Stateless session bean create methods never have parameters */
    }


    //
    // SessionBean interface implementation
    //

    public void setSessionContext(SessionContext ctx) {
        _ctx =ctx;
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

    private Transactions createNewInstance(Transactions oldbean) {
        try{
            InitialContext context= new InitialContext();
            if(oldbean instanceof StatelessSession) {
                /* We don't need to look up a stateless session again. See 6.8.3.
                6.8.3 Dealing with exceptions
                A RuntimeException thrown from any method of the enterprise bean class (including the business
                                                                                        methods and the callbacks invoked by the Container) results in the transition to the “does not exist”
                state. Exception handling is described in detail in Chapter 12.
                From the client perspective, the session object continues to exist. The client can continue accessing the
                session object because the Container can delegate the client’s requests to another instance.
                */                
                
                return oldbean;
            }
            if(oldbean instanceof StatefullSession) {
                StatefullSessionHome slshome = (StatefullSessionHome) javax.rmi.PortableRemoteObject.narrow(context.lookup("java:comp/env/ejb/Statefull"), StatefullSessionHome.class);
                return slshome.create();
            }
            if(oldbean instanceof BMPEntity) {
                BMPEntityHome slshome = (BMPEntityHome)
                javax.rmi.PortableRemoteObject.narrow(context.lookup("java:comp/env/ejb/BMPEntity"), BMPEntityHome.class);
                return slshome.create();
            }
            if(oldbean instanceof CMPEntity) {
                CMPEntityHome slshome = (CMPEntityHome) javax.rmi.PortableRemoteObject.narrow(context.lookup("java:comp/env/ejb/CMPEntity"), CMPEntityHome.class);
                return slshome.create();
            }
        } catch (javax.naming.NameNotFoundException e) {
            System.err.println("EJB container doesn't follow 11.6.1: can't lookup java:comp/UserTransaction");
        }catch(Exception e2) {
            e2.printStackTrace();
        }
        return null;
    }

    public void test() {
        Transactions session;

        // first some simple tests.
        UserTransaction ta = _ctx.getUserTransaction();

        /** Section 11.6.1, Page 173
            The Container must make the javax.transaction.UserTransaction interface available to
            the enterprise bean’s business method via the javax.ejb.EJBContext interface and under the
            environment entry java:comp/UserTransaction.
            */
        try{
            InitialContext context= new InitialContext();
            ta = (UserTransaction) context.lookup("java:comp/UserTransaction");
        } catch (javax.naming.NameNotFoundException e) {
            System.err.println("EJB container doesn't follow 11.6.1: can't lookup java:comp/UserTransaction");
        }catch(Exception e2) {
            e2.printStackTrace();
        }
        try{
            ta.begin();
            try{
                ta.begin();
                System.err.println("Nested transaction attempt succeeded!?");
            }catch(javax.transaction.NotSupportedException nse) {
                System.out.println("Nested transaction test passed");
            }finally{
                try{
                    ta.rollback();
                }catch(SystemException t) {}
            }
        }catch(Exception e) {
            e.printStackTrace();
        }

        try{
            _ctx.setRollbackOnly();
            System.err.println("_ctx.setRollbackOnly didn't raise");
        }catch(java.lang.IllegalStateException e) {
            System.out.println("setRollbackOnly() passed");
        }catch(Exception e) {
            e.printStackTrace();
        }

        try{
            _ctx.getRollbackOnly();
            System.err.println("_ctx.getRollbackOnly didn't raise");
        }catch(java.lang.IllegalStateException e) {
            System.out.println("getRollbackOnly() passed");
        }catch(Exception e) {
            e.printStackTrace();
        }

        try{
            Properties properties = new Properties();
            InitialContext context= new InitialContext(properties);
            StatelessSessionHome slshome = (StatelessSessionHome)
                javax.rmi.PortableRemoteObject.narrow(context.lookup("java:comp/env/ejb/Stateless"), StatelessSessionHome.class);
            System.out.println("----Testing stateless bean without a transaction context");
            testWithoutTransaction(slshome.create());
            System.out.println("----Testing stateless bean with a transaction context");
            testWithTransaction(slshome.create());

            StatefullSessionHome sfshome = (StatefullSessionHome)javax.rmi.PortableRemoteObject.narrow(context.lookup("java:comp/env/ejb/Statefull"), StatefullSessionHome.class);
            System.out.println("----Testing stateful bean without a transaction context");
            testWithoutTransaction(sfshome.create());
            System.out.println("----Testing stateful bean with a transaction context");
            testWithTransaction(sfshome.create());

            BMPEntityHome bmphome = (BMPEntityHome)javax.rmi.PortableRemoteObject.narrow(context.lookup("java:comp/env/ejb/BMPEntity"), BMPEntityHome.class);
            System.out.println("----Testing BMP entity bean without a transaction context");
            testWithoutTransaction(bmphome.create());
            System.out.println("----Testing BMP entity  bean with a transaction context");
            testWithTransaction(bmphome.create());

            CMPEntityHome cmphome = (CMPEntityHome)javax.rmi.PortableRemoteObject.narrow(context.lookup("java:comp/env/ejb/CMPEntity"), CMPEntityHome.class);
            System.out.println("----Testing CMP entity bean without a transaction context");

            testWithoutTransaction(cmphome.create());
            System.out.println("----Testing CMP entity  bean with a transaction context");
            testWithTransaction(cmphome.create());


            System.out.println("------------");


            testStatelessSession();
            testStatefullSession();
        } catch (Exception e) {
            e.printStackTrace();
            throw new EJBException("Can't do stuff because "+e);
        }
    }

    public void testWithTransaction(Transactions session) {
        UserTransaction ta = _ctx.getUserTransaction();

        /**
        11.6.2.6 Never
         The Container invokes an enterprise Bean method whose transaction attribute is set to Never without
         a transaction context defined by the EJB specification. The client is required to call without a transaction
         context.
         • If the client calls with a transaction context, the Container throws the java.rmi.Remote-
         Exception exception.
         • If the client calls without a transaction context, the Container performs the same steps as
         described in the NotSupported case.
         */
        try{
            ta.begin();
            session.never(false);
            System.err.println("Container doesn't follow section 11.6.2.6: it was supposed to raise java.rmi.RemoteException");
        }catch(java.rmi.RemoteException re) {
            // test succeeded
        }
        catch(Exception e) {
            System.err.println("never() received unexpected exception "+e);
            //e.printStackTrace();
        }finally{
            try{
                ta.rollback();
            }catch(SystemException t) {
                t.printStackTrace();
            }
        }

        /**
            11.6.2.3 Supports
         The Container invokes an enterprise Bean method whose transaction attribute is set to Supports as
         follows.
         • If the client calls with a transaction context, the Container performs the same steps as
         described in the Required case.
         • If the client calls without a transaction context, the Container performs the same steps as
         described in the NotSupported case.
         */

        try{
            ta.begin();
            session.supports(true);
            if(ta.getStatus()==Status.STATUS_MARKED_ROLLBACK) {
                //                System.out.println("supports & ta ok");
            } else {
                System.err.println("Container didn't implement 11.6.2.3: it didn't use the client transaction. Got back status "+ta.getStatus());
            }
        }catch(Exception e) {
            System.err.println("supports() received unexpected exception "+e);
            //e.printStackTrace();
        }finally{
            try{
                ta.commit();
                System.err.println("supports() should have been rolled back!");
            }catch(javax.transaction.RollbackException e) {
                // ok
            }catch(javax.transaction.HeuristicRollbackException e) {
                // ok
            }catch(Exception t) {
                t.printStackTrace();
            }
        }

        /**
            11.6.2.1 NotSupported
         The Container invokes an enterprise Bean method whose transaction attribute is set to NotSupported
         with an unspecified transaction context.
         If a client calls with a transaction context, the container suspends the association of the transaction context
         with the current thread before invoking the enterprise bean’s business method. The container
         resumes the suspended association when the business method has completed. The suspended transaction
         context of the client is not passed to the resource managers or other enterprise Bean objects that are
         invoked from the business method.
         If the business method invokes other enterprise beans, the Container passes no transaction context with
         the invocation.
         */


        try{
            ta.begin();
            session.notSupported(false);
            if(ta.getStatus()==Status.STATUS_ACTIVE) {
                //            System.out.println("NotSupported & ta ok");
            } else {
                System.err.println("NotSupported violated section 11.6.2.1! Received status "+ta.getStatus());
            }
        }catch(Exception e) {
            System.err.println("notSupported() received unexpected exception "+e);
            //e.printStackTrace();
        }finally{
            try{
                ta.commit();
            }catch(javax.transaction.RollbackException e) {
                System.err.println("Container violated section 11.6.2.1 (notSupported)! It used the client transaction context."+e);
            }catch(javax.transaction.HeuristicRollbackException e) {
                System.err.println("Container violated section 11.6.2.1 (notSupported)! It used the client transaction context."+e);
            }catch (Exception t) {t.printStackTrace();}
        }

        /**
            11.6.2.5 Mandatory
         The Container must invoke an enterprise Bean method whose transaction attribute is set to Mandatory
         in a client’s transaction context. The client is required to call with a transaction context.
         • If the client calls with a transaction context, the Container performs the same steps as
         described in the Required case.
         • If the client calls without a transaction context, the Container throws the javax.transaction.
         TransactionRequiredException exception.
         */

        try{
            ta.begin();
            session.mandatory(true);
            if(ta.getStatus()==Status.STATUS_MARKED_ROLLBACK) {
                //                System.out.println("mandatory & ta ok");
            }else {
                System.err.println("Container violated section 11.6.2.5 (mandatory)! It didn't use the client transaction."+ta.getStatus());
            }
        }catch(Exception e) {
            System.err.println("Container violated section 11.6.2.5 (mandatory)! It raised exception "+e);
        }finally{
            try{
                ta.commit();
                System.err.println("commit for mandatory should have failed!");
            }catch(javax.transaction.RollbackException e) {
                // ok
            }catch(javax.transaction.HeuristicRollbackException e) {
                // ok, too
            }catch(Exception t) {
                t.printStackTrace();
            }
        }

        /**
            11.6.2.4 RequiresNew
         The Container must invoke an enterprise Bean method whose transaction attribute is set to
         RequiresNew with a new transaction context.
         If the client invokes the enterprise Bean’s method while the client is not associated with a transaction
         context, the container automatically starts a new transaction before delegating a method call to the
         enterprise Bean business method. The Container automatically enlists all the resource managers
         accessed by the business method with the transaction. If the business method invokes other enterprise
         beans, the Container passes the transaction context with the invocation. The Container attempts to commit
         the transaction when the business method has completed. The container performs the commit protocol
         before the method result is sent to the client.
         If a client calls with a transaction context, the container suspends the association of the transaction context
         with the current thread before starting the new transaction and invoking the business method. The
         container resumes the suspended transaction association after the business method and the new transaction
         have been completed.
         */
        try{
            ta.begin();
            session.requiresNew(true);
            if(ta.getStatus()==Status.STATUS_MARKED_ROLLBACK) {
                System.err.println("Container violated section 11.6.2.4 (requiresNew)! It used the client transaction context."+ta.getStatus());
            }
        }catch(Exception e) {
            System.err.println("requiresNew() received unexpected exception "+e);
            //e.printStackTrace();
        }finally{
            try{
                ta.commit();
            }catch(javax.transaction.RollbackException e) {
                System.err.println("Container violated section 11.6.2.4 (requiresNew)! It used the client transaction context."+e);
            }catch(javax.transaction.HeuristicRollbackException e) {
                System.err.println("Container violated section 11.6.2.4 (requiresNew)! It used the client transaction context."+e);
            }catch (Exception t) {t.printStackTrace();}
        }


        /**
            11.6.2.2 Required
         The Container must invoke an enterprise Bean method whose transaction attribute is set to Required
         with a valid transaction context.
         If a client invokes the enterprise Bean’s method while the client is associated with a transaction context,
         the container invokes the enterprise Bean’s method in the client’s transaction context.

         If the client invokes the enterprise Bean’s method while the client is not associated with a transaction
         context, the container automatically starts a new transaction before delegating a method call to the
         enterprise Bean business method. The Container automatically enlists all the resource managers
         accessed by the business method with the transaction. If the business method invokes other enterprise
         beans, the Container passes the transaction context with the invocation. The Container attempts to commit
         the transaction when the business method has completed. The container performs the commit protocol
         before the method result is sent to the client.
         */

        try{
            ta.begin();
            session.required(true);
            if(ta.getStatus()==Status.STATUS_MARKED_ROLLBACK) {
                //            System.err.println("required ok");
            } else {
                System.err.println("Container violated section 11.6.2.2 (required): expected a roll back status, but have "+ta.getStatus());
            }
        }catch(Exception e) {
            System.err.println("required() received unexpected exception "+e);
            //e.printStackTrace();
        }finally{
            try{
                ta.commit();
                System.err.println("required() shouldn't commit");
            }catch(javax.transaction.RollbackException e) {
                //ok
            }catch (Exception t) {t.printStackTrace();}
        }


        /**
            * let's test app exceptions first. No exception should cause a rollback.
         * See Chapter 12 EJB 1.1 for details.
         */
        try{
            ta.begin();
            session.notSupportedAppException();
            if(ta.getStatus()==Status.STATUS_MARKED_ROLLBACK) {
                System.err.println("Container violated section 12: transaction was rolled back!");
            }
        }catch(AppException e) {
            //ok should check for a bean here
        }catch(Exception e) {
            System.err.println("notSupportedAppException() failed:"+e);
            //            e.printStackTrace();
        }finally{
            try{
                ta.commit();
            }catch(javax.transaction.RollbackException e) {
                System.err.println("notSupportedAppException() should have committed:"+e);
            }catch(javax.transaction.HeuristicRollbackException h) {
                System.err.println("notSupportedAppException() should have committed:"+h);
            }
            catch (Exception t) {
                System.err.println("notSupportedAppException() received:"+t);
                t.printStackTrace();
            }
        }

        try{
            ta.begin();
            session.supportsAppException();
            if(ta.getStatus()==Status.STATUS_MARKED_ROLLBACK) {
                System.err.println("Container violated section 12: transaction was rolled back!");
            }
        }catch(AppException e) {
            //ok should check for a bean here
        }catch(Exception e) {
            System.err.println("supportsAppException() failed:"+e);
            //            e.printStackTrace();
        }finally{
            try{
                ta.commit();
            }catch(javax.transaction.RollbackException e) {
                System.err.println("supportsAppException() should have committed:"+e);
            }catch(javax.transaction.HeuristicRollbackException h) {
                System.err.println("supportsAppException() should have committed:"+h);
            }
            catch (Exception t) {
                System.err.println("supportsAppException() received:"+t);
                t.printStackTrace();
            }
        }

        try{
            ta.begin();
            session.mandatoryAppException();
        }catch(AppException e) {
            //ok should check for a bean here
        }catch(Exception e) {
            System.err.println("mandatoryAppException() failed:"+e);
            //            e.printStackTrace();
        }finally{
            try{
                ta.commit();
            }catch(javax.transaction.RollbackException e) {
                System.err.println("mandatoryAppException() should have committed:"+e);
            }catch(javax.transaction.HeuristicRollbackException h) {
                System.err.println("mandatoryAppException() should have committed:"+h);
            }
            catch (Exception t) {
                System.err.println("mandatoryAppException() received:"+t);
                t.printStackTrace();
            }
        }
        try{
            ta.begin();
            session.requiredAppException();
        }catch(AppException e) {
            //ok should check for a bean here
        }catch(Exception e) {
            System.err.println("requiredAppException() failed:"+e);
            //            e.printStackTrace();
        }finally{
            try{
                ta.commit();
            }catch(javax.transaction.RollbackException e) {
                System.err.println("requiredAppException() should have committed:"+e);
            }catch(javax.transaction.HeuristicRollbackException h) {
                System.err.println("requiredAppException() should have committed:"+h);
            }
            catch (Exception t) {
                System.err.println("requiredAppException() received:"+t);
                t.printStackTrace();
            }
        }

        try{
            ta.begin();
            session.requiresNewAppException();
            if(ta.getStatus()==Status.STATUS_MARKED_ROLLBACK) {
                System.err.println("Container violated section 12: transaction was rolled back!");
            }
        }catch(AppException e) {
            //ok should check for a bean here
        }catch(Exception e) {
            System.err.println("requiresNewAppException() failed:"+e);
            //            e.printStackTrace();
        }finally{
            try{
                ta.commit();
            }catch(javax.transaction.RollbackException e) {
                System.err.println("requiresNewAppException() should have committed:"+e);
            }catch (Exception t) {t.printStackTrace();}
        }



        //Now come the system exceptions

        boolean isStateless=false;
        try{
            isStateless = ((EJBObject)session).getEJBHome().getEJBMetaData().isStatelessSession();
            boolean really=session instanceof StatelessSession;
            if(isStateless != really) {
                System.err.println("This container doesn't implement isStatelessSession() correctly");
            }
        }catch(RemoteException e) {
            System.err.println("Can't get bean type:"+e);
        }

        try{
            ta.begin();
            session.requiresNewSystemException();
            System.err.println("Container didn't rethrow system exception");
        }catch(RemoteException e) {
            //ok should check for a bean here
            if(!isStateless) {
                try{
                    session.supports(false);
                    System.err.println("requiresNewSystemException(): This instance should have been discarded: (12.3.1)");
                }catch(java.rmi.NoSuchObjectException nso){
                    // we are happy
                }catch(Exception e1) {
                    System.err.println("requiresNewSystemException() failed:"+e1);
                    //            e.printStackTrace();
                }
            }
        }catch(Exception e) {
            System.err.println("requiresNewSystemException() failed:"+e);
            //            e.printStackTrace();
        }finally{
            try{
                ta.commit();
            }catch(javax.transaction.RollbackException e) {
                System.err.println("requiresNewSystemException: Container violated section 12: transaction was rolled back!");
            }catch (Exception t) {t.printStackTrace();}
        }
        
        session=createNewInstance(session);
        try{
            ta.begin();
            session.requiredSystemException();
            System.err.println("Container didn't rethrow system exception");
        }catch(RemoteException e) {
            if(! (e instanceof javax.transaction.TransactionRolledbackException)) {
                System.err.println("requiredSystemException: Container didn't throw TransactionRolledbackException, but "+e);
            }
            if(!isStateless) {
                try{
                    session.supports(false);
                    System.err.println("requiredSystemException(): This instance should have been discarded: (12.3.1)");
                }catch(java.rmi.NoSuchObjectException  nso){
                    // we are happy
                }catch(Exception e1) {
                    System.err.println("requiredSystemException() failed:"+e1);
                    //            e.printStackTrace();
                }
            }
        }catch(Exception e) {
            System.err.println("requiredSystemException() failed:"+e);
            //            e.printStackTrace();
        }finally{
            try{
                ta.commit();
                System.err.println("requiredSystemException: transaction wasn't rolled back!");
            }catch(javax.transaction.RollbackException e) {
            }catch (Exception t) {t.printStackTrace();}
        }
        
        session=createNewInstance(session);
        try{
            ta.begin();
            session.notSupportedSystemException();
            System.err.println("Container didn't rethrow system exception");
        }catch(RemoteException e) {
            if(!isStateless) {
                try{
                    session.supports(false);
                    System.err.println("notSupportedSystemException(): This instance should have been discarded: (12.3.1)");
                }catch(java.rmi.NoSuchObjectException  nso){
                    // we are happy
                }catch(Exception e1) {
                    System.err.println("notSupportedSystemException() failed:"+e1);
                    //            e.printStackTrace();
                }
            }
        }catch(Exception e) {
            System.err.println("notSupportedSystemException() failed:"+e);
            //            e.printStackTrace();
        }finally{
            try{
                ta.commit();
            }catch(javax.transaction.RollbackException e) {
                System.err.println("notSupportedSystemException: Container violated section 12: transaction was rolled back!");
            }catch (Exception t) {t.printStackTrace();}
        }
        
        session=createNewInstance(session);
        try{
            ta.begin();
            session.supportsSystemException();
            System.err.println("Container didn't rethrow system exception");
        }catch(RemoteException e) {
            if(! (e instanceof javax.transaction.TransactionRolledbackException)) {
                System.err.println("supportsSystemException: Container didn't throw TransactionRolledbackException, but "+e);
            }
            if(!isStateless) {
                try{
                    session.supports(false);
                    System.err.println("supportsSystemException(): This instance should have been discarded: (12.3.1)");
                }catch(java.rmi.NoSuchObjectException nso){
                    // we are happy
                }catch(Exception e1) {
                    System.err.println("supportsSystemException() failed:"+e1);
                    //            e.printStackTrace();
                }
            }
        }catch(Exception e) {
            System.err.println("supportsSystemException() failed:"+e);
            //            e.printStackTrace();
        }finally{
            try{
                ta.commit();
                System.err.println("Container violated section 12: transaction was supposed to be rolled back!");
            }catch(javax.transaction.RollbackException e) {
            }catch (Exception t) {t.printStackTrace();}
        }
        
    }

    void testWithoutTransaction(Transactions session) {

        // Nothing special here...
        try{
            session.never(false);
        }catch(Exception e) {
            System.err.println("never() received unexpected exception "+e);
            //e.printStackTrace();
        }
        try{
            session.supports(false);
        }catch(Exception e) {
            System.err.println("supports() received unexpected exception "+e);
            //e.printStackTrace();
        }
        try{
            session.notSupported(false);
        }catch(Exception e) {
            System.err.println("notSupported() received unexpected exception "+e);
            //e.printStackTrace();
        }

        try{
            session.mandatory(false);
            System.err.println("Container violated 11.6.2.5 (Mandatory) by not throwing TransactionRequiredException");
        }catch(javax.transaction.TransactionRequiredException e) {
            // test passed
        }catch(Exception e) {
            System.err.println("mandatory() received unexpected exception "+e);
            //e.printStackTrace();
        }


        try{
            session.requiresNew(false);
        }catch(Exception e) {
            System.err.println("requiresNew() received unexpected exception "+e);
            //e.printStackTrace();
        }

        try{
            session.required(false);
        }catch(Exception e) {
            System.err.println("required() received unexpected exception "+e);
            //e.printStackTrace();
        }

        // test app exceptions first
        try{
            session.requiresNewAppException();
            System.err.println("Should not be here");
        }catch(AppException e) {
            //ok should check for a bean here
        }catch(Exception e) {
            System.err.println("requiresNewAppException() failed:"+e);
            //            e.printStackTrace();
        }

        try{
            session.requiredAppException();
            System.err.println("Should not be here");
        }catch(AppException e) {
            //ok should check for a bean here
        }catch(Exception e) {
            System.err.println("requiredAppException() failed:"+e);
            //            e.printStackTrace();
        }

        try{
            session.notSupportedAppException();
            System.err.println("Should not be here");
        }catch(AppException e) {
            //ok should check for a bean here
        }catch(Exception e) {
            System.err.println("notSupportedAppException() failed:"+e);
            //            e.printStackTrace();
        }

        try{
            session.neverAppException();
            System.err.println("Should not be here");
        }catch(AppException e) {
            //ok should check for a bean here
        }catch(Exception e) {
            System.err.println("neverAppException() failed:"+e);
            //            e.printStackTrace();
        }
        try{
            session.supportsAppException();
            System.err.println("Should not be here");
        }catch(AppException e) {
            //ok should check for a bean here
        }catch(Exception e) {
            System.err.println("supportsAppException() failed:"+e);
            //            e.printStackTrace();
        }
        //Now come the system exceptions

        boolean isStateless=false;
        try{
            isStateless = ((EJBObject)session).getEJBHome().getEJBMetaData().isStatelessSession();
            boolean really=session instanceof StatelessSession;
            if(isStateless != really) {
                System.err.println("This container doesn't implement isStatelessSession() correctly");
            }
        }catch(RemoteException e) {
            System.err.println("Can't get bean type:"+e);
        }

        try{
            session.requiresNewSystemException();
            System.err.println("Container didn't rethrow system exception");
        }catch(RemoteException e) {
            //ok should check for a bean here
            if(!isStateless) {
                try{
                    session.supports(false);
                    System.err.println("requiresNewSystemException(): This instance should have been discarded: (12.3.1)");
                }catch(java.rmi.NoSuchObjectException nso){
                    // we are happy
                }catch(Exception e1) {
                    System.err.println("requiresNewSystemException() failed:"+e1);
                    //            e.printStackTrace();
                }
            }
        }catch(Exception e) {
            System.err.println("requiresNewSystemException() failed:"+e);
            //            e.printStackTrace();
        }

        session=createNewInstance(session);
        try{
            session.requiredSystemException();
            System.err.println("Container didn't rethrow system exception");
        }catch(RemoteException e) {
            if(!isStateless) {
                try{
                    session.supports(false);
                    System.err.println("requiredSystemException(): This instance should have been discarded: (12.3.1)");
                }catch(java.rmi.NoSuchObjectException  nso){
                    // we are happy
                }catch(Exception e1) {
                    System.err.println("requiredSystemException() failed:"+e1);
                    //            e.printStackTrace();
                }
            }
        }catch(Exception e) {
            System.err.println("requiredSystemException() failed:"+e);
            //            e.printStackTrace();
        }

        session=createNewInstance(session);
        try{
            session.notSupportedSystemException();
            System.err.println("Container didn't rethrow system exception");
        }catch(RemoteException e) {
            if(!isStateless) {
                try{
                    session.supports(false);
                    System.err.println("notSupportedSystemException(): This instance should have been discarded: (12.3.1)");
                }catch(java.rmi.NoSuchObjectException  nso){
                    // we are happy
                }catch(Exception e1) {
                    System.err.println("notSupportedSystemException() failed:"+e1);
                    //            e.printStackTrace();
                }
            }
        }catch(Exception e) {
            System.err.println("notSupportedSystemException() failed:"+e);
            //            e.printStackTrace();
        }

        session=createNewInstance(session);
        try{
            session.neverSystemException();
            System.err.println("Container didn't rethrow system exception");
        }catch(RemoteException e) {
            if(!isStateless) {
                try{
                    session.supports(false);
                    System.err.println("neverSystemException(): This instance should have been discarded: (12.3.1)");
                }catch(java.rmi.NoSuchObjectException  nso){
                    // we are happy
                }catch(Exception e1) {
                    System.err.println("neverSystemException() failed:"+e1);
                    //            e.printStackTrace();
                }
            }
        }catch(Exception e) {
            System.err.println("neverSystemException() failed:"+e);
            //            e.printStackTrace();
        }

        session=createNewInstance(session);
        try{
            session.supportsSystemException();
            System.err.println("Container didn't rethrow system exception");
        }catch(RemoteException e) {
            if(!isStateless) {
                try{
                    session.supports(false);
                    System.err.println("supportsSystemException(): This instance should have been discarded: (12.3.1)");
                }catch(java.rmi.NoSuchObjectException nso){
                    // we are happy
                }catch(Exception e1) {
                    System.err.println("supportsSystemException() failed:"+e1);
                    //            e.printStackTrace();
                }
            }
            }catch(Exception e) {
            System.err.println("supportsSystemException() failed:"+e);
            //            e.printStackTrace();
        }
    }

    void testStatelessSession() {
        BMTStateless bean;
        try{
            InitialContext context= new InitialContext();
            BMTStatelessHome slshome = (BMTStatelessHome)
                javax.rmi.PortableRemoteObject.narrow(context.lookup("java:comp/env/ejb/BMTStateless"), BMTStatelessHome.class);
            bean = slshome.create();
        }catch(Exception e){
            e.printStackTrace();
            throw new EJBException();
        }
        try{
            bean.startTa();
            System.err.println("Expected RemoteException here. Stateless Session bean didn't finish ta.");
        }catch(java.rmi.RemoteException re) {
            System.out.println("Got expected exception :"+re);
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        try{
            ((EJBObject)bean).isIdentical((EJBObject)bean);
        }catch(Exception e) {
            System.err.println("Second call to bean didn't succeed!");
        }
    }

    void testStatefullSession() {
        BMTStatefull bean;
        try{

            InitialContext context= new InitialContext();
            BMTStatefullHome slshome = (BMTStatefullHome)
                javax.rmi.PortableRemoteObject.narrow(context.lookup("java:comp/env/ejb/BMTStatefull"), BMTStatefullHome.class);
            bean = slshome.create();
        }catch(Exception e){
            e.printStackTrace();
            throw new EJBException();
        }

        try{
            bean.startTa();
        }catch(Exception e) {
            e.printStackTrace();
        }
        try{
            bean.commitTa(true);
        }catch(Exception e) {
            e.printStackTrace();
        }

        try{
            bean.startTa();
        }catch(Exception e) {
            e.printStackTrace();
        }
        try{
            bean.commitTa(false);
        }catch(Exception e) {
            e.printStackTrace();
        }
        
    }

}