package com.titan.clients;

import java.rmi.RemoteException;
import java.util.Properties;

import javax.ejb.CreateException;
import javax.naming.Context;
import javax.rmi.PortableRemoteObject;

import com.titan.cabin.CabinHomeRemote;
import com.titan.cabin.CabinRemote;

public class Client_2 {

    public static void main(String[] args) {
        try {
            Context jndiContext = getInitialContext();

            Object ref = jndiContext.lookup("titan/CabinEJB");
            CabinHomeRemote home = (CabinHomeRemote) PortableRemoteObject.narrow(ref, CabinHomeRemote.class);

            // Add 9 cabins to deck 1 of ship 1.
            makeCabins(home, 2, 10, 1, 1);
            // Add 10 cabins to deck 2 of ship 1.
            makeCabins(home, 11, 20, 2, 1);
            // Add 10 cabins to deck 3 of ship 1.
            makeCabins(home, 21, 30, 3, 1);

            // Add 10 cabins to deck 1 of ship 2.
            makeCabins(home, 31, 40, 1, 2);
            // Add 10 cabins to deck 2 of ship 2.
            makeCabins(home, 41, 50, 2, 2);
            // Add 10 cabins to deck 3 of ship 2.
            makeCabins(home, 51, 60, 3, 2);

            // Add 10 cabins to deck 1 of ship 3.
            makeCabins(home, 61, 70, 1, 3);
            // Add 10 cabins to deck 2 of ship 3.
            makeCabins(home, 71, 80, 2, 3);
            // Add 10 cabins to deck 3 of ship 3.
            makeCabins(home, 81, 90, 3, 3);
            // Add 10 cabins to deck 4 of ship 3.
            makeCabins(home, 91, 100, 4, 3);

            for (int i = 1; i <= 100; i++) {
                Integer pk = new Integer(i);
                CabinRemote cabin = home.findByPrimaryKey(pk);
                System.out.println(
                    "PK="
                        + i
                        + ", Ship="
                        + cabin.getShipId()
                        + ", Deck="
                        + cabin.getDeckLevel()
                        + ", BedCount="
                        + cabin.getBedCount()
                        + ", Name="
                        + cabin.getName());
            }

        } catch (java.rmi.RemoteException re) {
            re.printStackTrace();
        } catch (javax.naming.NamingException ne) {
            ne.printStackTrace();
        } catch (javax.ejb.CreateException ce) {
            ce.printStackTrace();
        } catch (javax.ejb.FinderException fe) {
            fe.printStackTrace();
        }
    }

    public static Context getInitialContext() throws javax.naming.NamingException {
        Properties p = new Properties();

        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.openejb.client.JNDIContext");
        p.put(Context.PROVIDER_URL, "localhost:4201");
        p.put(Context.SECURITY_PRINCIPAL, "fakeuser");
        p.put(Context.SECURITY_CREDENTIALS, "fakepass");

        return new javax.naming.InitialContext(p);
    }

    public static void makeCabins(CabinHomeRemote home, int fromId, int toId, int deckLevel, int shipNumber)
        throws RemoteException, CreateException {

        int bc = 3;
        for (int i = fromId; i <= toId; i++) {
            CabinRemote cabin = home.create(new Integer(i));
            int suiteNumber = deckLevel * 100 + (i - fromId);

            cabin.setName("Suite " + suiteNumber);
            cabin.setDeckLevel(deckLevel);
            bc = (bc == 3) ? 2 : 3;
            cabin.setBedCount(bc);
            cabin.setShipId(shipNumber);
        }
    }
}
