//
//  BMTMethodsImpl.java
//  TransactionBeans
//
//  Created by Stefan Reich on Tue Nov 20 2001.
//  Copyright (c) 2001 OpenEJB Group. All rights reserved.
//
package transactiontests;
import javax.transaction.UserTransaction;
import javax.ejb.*;

public abstract class BMTMethodsImpl implements BMTMethods{
    SessionContext _ctx;

    public void startTa() {
        try{
            UserTransaction ta = _ctx.getUserTransaction();
            ta.begin();
        }catch(Exception e) {
            throw new EJBException(e);
        }
    }

    public void commitTa(boolean commit) throws javax.transaction.HeuristicMixedException, javax.transaction.HeuristicRollbackException, javax.transaction.RollbackException, javax.transaction.SystemException{
        UserTransaction ta = _ctx.getUserTransaction();
        if(commit) {
            ta.commit();
        }else {
            ta.rollback();
        }
    }
}
