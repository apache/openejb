package com.titan.ship;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Vector;

import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;
import javax.ejb.ObjectNotFoundException;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class ShipBean implements javax.ejb.EntityBean
{
   
   public Integer id;
   public String name;
   public int capacity;
   public double tonnage;
   
   public EntityContext context;
   
   public Integer ejbCreate (Integer id, String name, 
                             int capacity, double tonnage)
   throws CreateException
   {      
      System.out.println ("ejbCreate() pk="+id+" name="+name);
      
      if ((id.intValue () < 1) || (name == null))
         throw new CreateException ("Invalid Parameters");
      
      this.id = id;
      this.name = name;
      this.capacity = capacity;
      this.tonnage = tonnage;
      
      Connection con = null;
      PreparedStatement ps = null;
      try
      {
         con = this.getConnection ();
         ps = con.prepareStatement ( "insert into Ship (id, name, capacity, tonnage) values (?,?,?,?)" );
         
         ps.setInt (1, id.intValue ());
         ps.setString (2, name);
         ps.setInt (3, capacity);
         ps.setDouble (4, tonnage);
         
         if (ps.executeUpdate () != 1)
         {
            throw new CreateException ("Failed to add Ship to database");
         }
         
         return id;
      }
      catch (SQLException se)
      {
         throw new EJBException (se);
      }
      finally
      {
         try { ps.close (); } catch (Exception e) {}
         try { con.close (); } catch (Exception e) {}
      }
   }
   
   public void ejbPostCreate (Integer id, String name,
   int capacity, double tonnage)
   {
      // Do something useful with the primary key.
   }
   
   public Integer ejbCreate (Integer id, String name )
   throws CreateException
   {
      return ejbCreate (id,name,0,0);
   }
   
   public void ejbPostCreate (Integer id, String name)
   {
      // Do something useful with the EJBObject reference.
   }
   
   public Integer ejbFindByPrimaryKey (Integer primaryKey)
   throws FinderException
   {
      
      System.out.println ("ejbFindByPrimaryKey() primaryKey="+primaryKey);

      Connection con = null;
      PreparedStatement ps = null;
      ResultSet result = null;

      try
      {
         con = this.getConnection ();
         ps = con.prepareStatement ("select id from Ship where id = ?");

         ps.setInt (1, primaryKey.intValue ());
         
         result = ps.executeQuery ();
         
         // Does ship id exist in database?
         if (!result.next ())
         {
            throw new ObjectNotFoundException ("Cannot find Ship with id = "+id);
         }
      } catch (SQLException se)
      {
         throw new EJBException (se);
      }
      finally
      {
         try { result.close (); } catch (Exception e) {}
         try { ps.close (); } catch (Exception e) {}
         try { con.close (); } catch (Exception e) {}
      }
      return primaryKey;
   }
   
   public Collection ejbFindByCapacity (int capacity)
   throws FinderException
   {
      
      System.out.println ("ejbFindByCapacity() capacity="+capacity);
      
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet result = null;
      
      try
      {
         con = this.getConnection ();
         ps = con.prepareStatement ("select id from Ship where capacity = ?");
         
         ps.setInt (1,capacity);
         
         result = ps.executeQuery ();
         Vector keys = new Vector ();
         while(result.next ())
         {
            keys.addElement (result.getObject ("id"));
         }
         return keys;
         
      }
      catch (SQLException se)
      {
         throw new EJBException (se);
      }
      finally
      {
         try { result.close (); } catch (Exception e) {}
         try { ps.close (); } catch (Exception e) {}
         try { con.close (); } catch (Exception e) {}
      }
   }
   
   public void setEntityContext (EntityContext ctx)
   {
      context = ctx;
   }
   public void unsetEntityContext ()
   {
      context = null;
   }
   public void ejbActivate ()
   {}
   public void ejbPassivate ()
   {}
   
   public void ejbLoad ()
   {
      
      Integer primaryKey = (Integer)context.getPrimaryKey ();
      System.out.println ("ejbLoad() pk="+primaryKey);
      
      Connection con = null;
      PreparedStatement ps = null;
      ResultSet result = null;
      try
      {
         con = this.getConnection ();
         ps = con.prepareStatement ("select name, capacity, tonnage from Ship where id = ?");
         
         ps.setInt (1, primaryKey.intValue ());
         result = ps.executeQuery ();
         
         if (result.next ())
         {
            id = primaryKey;
            name = result.getString ("name");
            capacity = result.getInt ("capacity");
            tonnage = result.getDouble ("tonnage");
         } else
         {
            throw new EJBException ();
         }
      } catch (SQLException se)
      {
         throw new EJBException (se);
      }
      finally
      {
         try { result.close (); } catch (Exception e) {}
         try { ps.close (); } catch (Exception e) {}
         try { con.close (); } catch (Exception e) {}
      }
   }
   
   public void ejbStore ()
   {
      
      System.out.println ("ejbStore() pk="+id);

      Connection con = null;
      PreparedStatement ps = null;
      try
      {
         con = this.getConnection ();
         ps = con.prepareStatement ("update Ship set name = ?, capacity = ?, tonnage = ? where id = ?");
         
         ps.setString (1,name);
         ps.setInt (2,capacity);
         ps.setDouble (3,tonnage);
         ps.setInt (4,id.intValue ());
         
         if (ps.executeUpdate () != 1)
         {
            throw new EJBException ("ejbStore unable to update table");
         }
      }
      catch (SQLException se)
      {
         throw new EJBException (se);
      }
      finally
      {
         try { ps.close (); } catch (Exception e) {}
         try { con.close (); } catch (Exception e) {}
      }
   }
   
   public void ejbRemove ()
   {
      
      System.out.println ("ejbRemove() pk="+id);

      Connection con = null;
      PreparedStatement ps = null;
      try
      {
         con = this.getConnection ();
         ps = con.prepareStatement ("delete from Ship where id = ?");
         
         ps.setInt (1, id.intValue ());
         
         if (ps.executeUpdate () != 1)
         {
            throw new EJBException ("ejbRemove unable to remove bean");
         }
      }
      catch (SQLException se)
      {
         throw new EJBException (se);
      }
      finally
      {
         try { ps.close (); } catch (Exception e) {}
         try { con.close (); } catch (Exception e) {}
      }
   }
   
   public String getName ()
   {
      System.out.println ("getName()");
      return name;
   }
   public void setName (String n)
   {
      System.out.println ("setName()");
      name = n;
   }
   public void setCapacity (int cap)
   {
      System.out.println ("setCapacity()");
      capacity = cap;
   }
   public int getCapacity ()
   {
      System.out.println ("getCapacity()");
      return capacity;
   }
   public double getTonnage ()
   {
      System.out.println ("getTonnage()");
      return tonnage;
   }
   public void setTonnage (double tons)
   {
      System.out.println ("setTonnage()");
      tonnage = tons;
   }
   
   private Connection getConnection () throws SQLException
   {
      try
      {
         Context jndiCntx = new InitialContext ();
         DataSource ds =
         (DataSource)jndiCntx.lookup ("java:comp/env/jdbc/titanDB");         
         return ds.getConnection ();
      }
      catch (NamingException ne)
      {
         throw new EJBException (ne);
      }
   }
   
}
