
package com.titan.clients;


import com.titan.ship.*;

import java.rmi.RemoteException;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.rmi.PortableRemoteObject;


public class Client_4
{
   
    public static void main ( String [] args ) {
      
	try {
	    Context jndiContext = getInitialContext();
         
	    Object ref = jndiContext.lookup( "titan/ShipEJB" );
	    ShipHomeRemote home = (ShipHomeRemote)PortableRemoteObject.narrow( ref, ShipHomeRemote.class );
         
	    // We check if we have to build the database schema...
	    //
	    if ( (args.length > 0) && args[0].equalsIgnoreCase( "CreateDB" ) ) {
		System.out.println ("Creating database table...");
		home.makeDbTable ();
	    }
	    // ... or if we have to drop it...
	    //
	    else if ( (args.length > 0) && args[0].equalsIgnoreCase( "DropDB" ) )
	    {
		System.out.println("Dropping database table...");
		home.deleteDbTable();
	    }
	    else
	    {
		// ... standard behaviour
		//
		System.out.println( "Creating Ship 101.." );
		ShipRemote ship1 = home.create( new Integer (101), "Edmund Fitzgerald" );
		
		ship1.setTonnage( 50000.0 );
		ship1.setCapacity( 300 );
		
		Integer pk = new Integer( 101 );
		
		System.out.println( "Finding Ship 101 again.." );
		ShipRemote ship2 = home.findByPrimaryKey( pk );
		
		System.out.println ( ship2.getName() );
		System.out.println ( ship2.getTonnage() );
		System.out.println ( ship2.getCapacity() );
		
		System.out.println ( "Removing Ship 101.." );
		ship2.remove ();
	    }
         
	} catch( java.rmi.RemoteException re )
	{
	    re.printStackTrace();
	}
	catch( javax.naming.NamingException ne )
	{
	    ne.printStackTrace();
	}
	catch( javax.ejb.CreateException ce )
	{
	    ce.printStackTrace();
	}
	catch( javax.ejb.FinderException fe )
	{
	    fe.printStackTrace();
	}
	catch( javax.ejb.RemoveException re )
	{
	    re.printStackTrace();
	}
    }
   
    public static Context getInitialContext() throws javax.naming.NamingException
    {
	return new javax.naming.InitialContext();
    }
}
