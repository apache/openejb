package com.titan.clients;

import java.util.Properties;

import javax.naming.Context;
import javax.rmi.PortableRemoteObject;

import com.titan.travelagent.TravelAgentHomeRemote;
import com.titan.travelagent.TravelAgentRemote;

public class Client_3 {
    public static int SHIP_ID = 1;
    public static int BED_COUNT = 3;

    public static void main(String[] args) {
        try {
            Context jndiContext = getInitialContext();

            Object ref = jndiContext.lookup("titan/TravelAgentEJB");

            TravelAgentHomeRemote home =
                (TravelAgentHomeRemote) PortableRemoteObject.narrow(ref, TravelAgentHomeRemote.class);

            TravelAgentRemote travelAgent = home.create();

            // Get a list of all cabins on ship 1 with a bed count of 3.
            String list[] = travelAgent.listCabins(SHIP_ID, BED_COUNT);

            for (int i = 0; i < list.length; i++) {
                System.out.println(list[i]);
            }
        } catch (java.rmi.RemoteException re) {
            re.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
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
}
