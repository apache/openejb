package com.titan.ship;

import java.rmi.RemoteException;
import java.util.Collection;

import javax.ejb.CreateException;
import javax.ejb.FinderException;

public interface ShipHomeRemote extends javax.ejb.EJBHome {

    public ShipRemote create(Integer id, String name, 
                             int capacity, double tonnage)
        throws RemoteException,CreateException;

    public ShipRemote create(Integer id, String name)
        throws RemoteException,CreateException;

    public ShipRemote findByPrimaryKey(Integer primaryKey)
        throws FinderException, RemoteException;

    public Collection findByCapacity(int capacity)
        throws FinderException, RemoteException;
}
