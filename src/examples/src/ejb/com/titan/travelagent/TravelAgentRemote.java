package com.titan.travelagent;

import java.rmi.RemoteException;

public interface TravelAgentRemote extends javax.ejb.EJBObject {

    // String elements follow the format "id, name, deck level"
    public String [] listCabins(int shipID, int bedCount)
        throws RemoteException;

}
