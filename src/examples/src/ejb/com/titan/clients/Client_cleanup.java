package com.titan.clients;

import java.rmi.RemoteException;
import java.util.Properties;

import javax.ejb.FinderException;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.rmi.PortableRemoteObject;

import com.titan.cabin.CabinHomeRemote;
import com.titan.cabin.CabinRemote;

public class Client_cleanup {

    public static void main(String[] args) {
        try {
            Context jndiContext = getInitialContext();

            Object ref = jndiContext.lookup("titan/CabinEJB");
            CabinHomeRemote home = (CabinHomeRemote) PortableRemoteObject.narrow(ref, CabinHomeRemote.class);

            // Remove 10 cabins
            removeCabins(home, 1, 10);
            // Remove 10 cabins
            removeCabins(home, 11, 20);
            // Remove 10 cabins
            removeCabins(home, 21, 30);

            // Remove 10 cabins
            removeCabins(home, 31, 40);
            // Remove 10 cabins
            removeCabins(home, 41, 50);
            // Remove 10 cabins
            removeCabins(home, 51, 60);

            // Remove 10 cabins
            removeCabins(home, 61, 70);
            // Remove 10 cabins
            removeCabins(home, 71, 80);
            // Remove 10 cabins
            removeCabins(home, 81, 90);
            // Remove 10 cabins
            removeCabins(home, 91, 100);
        } catch (java.rmi.RemoteException re) {
            re.printStackTrace();
        } catch (javax.naming.NamingException ne) {
            ne.printStackTrace();
        } catch (javax.ejb.FinderException fe) {
            fe.printStackTrace();
        } catch (javax.ejb.RemoveException rmve) {
            rmve.printStackTrace();
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

    public static void removeCabins(CabinHomeRemote home, int fromId, int toId)
        throws RemoteException, FinderException, RemoveException {

        int bc = 3;
        for (int i = fromId; i <= toId; i++) {
            CabinRemote cabin = home.findByPrimaryKey(new Integer(i));

            cabin.remove();
        }
    }
}
