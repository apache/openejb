package com.titan.clients;

import java.util.Properties;

import javax.naming.Context;
import javax.rmi.PortableRemoteObject;

import com.titan.cabin.CabinHomeRemote;
import com.titan.cabin.CabinRemote;

public class Client_1 {
    public static void main(String[] args) {
        try {
            Context jndiContext = getInitialContext();
            Object ref = jndiContext.lookup("titan/CabinEJB");
            CabinHomeRemote home = (CabinHomeRemote) PortableRemoteObject.narrow(ref, CabinHomeRemote.class);
            CabinRemote cabin_1 = home.create(new Integer(1));

            cabin_1.setName("Master Suite");
            cabin_1.setDeckLevel(1);
            cabin_1.setShipId(1);
            cabin_1.setBedCount(3);

            Integer pk = new Integer(1);

            CabinRemote cabin_2 = home.findByPrimaryKey(pk);
            System.out.println(cabin_2.getName());
            System.out.println(cabin_2.getDeckLevel());
            System.out.println(cabin_2.getShipId());
            System.out.println(cabin_2.getBedCount());
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

        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.openejb.client.RemoteInitialContextFactory");
        p.put(Context.PROVIDER_URL, "localhost:4201");
        p.put(Context.SECURITY_PRINCIPAL, "fakeuser");
        p.put(Context.SECURITY_CREDENTIALS, "fakepass");

        return new javax.naming.InitialContext(p);
    }
}
