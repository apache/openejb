//
//  BMTMethods.java
//  TransactionBeans
//
//  Created by Stefan Reich on Mon Nov 19 2001.
//  Copyright (c) 2001 OpenEJB Group. All rights reserved.
//
package transactiontests;

public interface BMTMethods {
  public void startTa() throws java.rmi.RemoteException;
  public void commitTa(boolean commit) throws java.rmi.RemoteException, javax.transaction.HeuristicMixedException, javax.transaction.HeuristicRollbackException, javax.transaction.RollbackException, javax.transaction.SystemException;
}
