//
//  TransactionsImpl.java
//  TransactionBeans
//
//  Created by Stefan Reich on Tue Nov 20 2001.
//  Copyright (c) 2001 OpenEJB Group. All rights reserved.
//
package transactiontests;

public abstract class TransactionsImpl implements Transactions{

    javax.ejb.EJBContext _ctx;

    public void never(boolean rollback){
        if(rollback) {
            _ctx.setRollbackOnly();
        }
    }
    public void supports(boolean rollback){
        if(rollback) {
            _ctx.setRollbackOnly();
        }
    }
    public void notSupported(boolean rollback){
        if(rollback) {
            _ctx.setRollbackOnly();
        }
    }
    public void mandatory(boolean rollback){
        if(rollback) {
            _ctx.setRollbackOnly();
        }
    }
    public void requiresNew(boolean rollback){
        if(rollback) {
            _ctx.setRollbackOnly();
        }
    }
    public void required(boolean rollback){
        if(rollback) {
            _ctx.setRollbackOnly();
        }
    }

    public void neverSystemException(){
        throw new NullPointerException();
    }

    public void supportsSystemException(){
        throw new NullPointerException();
    }

    public void notSupportedSystemException() {
        throw new NullPointerException();
    }

    public void mandatorySystemException(){
        throw new NullPointerException();
    }

    public void requiresNewSystemException(){
        throw new NullPointerException();
    }

    public void requiredSystemException(){
        throw new NullPointerException();
    }
    

    public void neverAppException() throws AppException{
        throw new AppException();
    }

    public void supportsAppException() throws AppException{
        throw new AppException();
    }

    public void notSupportedAppException() throws AppException{
        throw new AppException();
    }

    public void mandatoryAppException() throws AppException{
        throw new AppException();
    }

    public void requiresNewAppException() throws AppException{
        throw new AppException();
    }

    public void requiredAppException() throws AppException{
        throw new AppException();
    }
    
    
}
