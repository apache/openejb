//
//  Transactions.java
//  TransactionBeans
//
//  Created by Stefan Reich on Fri Nov 16 2001.
//  Copyright (c) 2001 OpenEJB Group. All rights reserved.
//

package transactiontests;

public interface Transactions {

    public void never(boolean rollback) throws java.rmi.RemoteException;
    public void supports(boolean rollback) throws java.rmi.RemoteException;
    public void notSupported(boolean rollback) throws  java.rmi.RemoteException;
    public void mandatory(boolean rollback) throws  java.rmi.RemoteException;
    public void requiresNew(boolean rollback) throws java.rmi.RemoteException;
    public void required(boolean rollback) throws  java.rmi.RemoteException;

    public void neverSystemException() throws java.rmi.RemoteException;
    public void supportsSystemException() throws java.rmi.RemoteException;
    public void notSupportedSystemException() throws  java.rmi.RemoteException;
    public void mandatorySystemException() throws  java.rmi.RemoteException;
    public void requiresNewSystemException() throws java.rmi.RemoteException;
    public void requiredSystemException() throws  java.rmi.RemoteException;

    public void neverAppException() throws AppException, java.rmi.RemoteException;
    public void supportsAppException() throws AppException, java.rmi.RemoteException;
    public void notSupportedAppException() throws  AppException, java.rmi.RemoteException;
    public void mandatoryAppException() throws  AppException, java.rmi.RemoteException;
    public void requiresNewAppException() throws AppException, java.rmi.RemoteException;
    public void requiredAppException() throws  AppException, java.rmi.RemoteException;
    
}
