package transactiontests;

import java.sql.*;
import java.util.*;

import javax.ejb.*;
import javax.naming.*;
import javax.sql.*;

public class BMPEntityBean extends CMPEntityBean {

    private DataSource datasource;

    //
    // Creation methods
    //

    public BMPEntityBean() {
    }

    public void setEntityContext(EntityContext ctx) {
        super.setEntityContext(ctx);
        try {
            Context context = new InitialContext();
            datasource = (DataSource) context.lookup("java:comp/env/jdbc/BMPEntityBeanDataSource");
        }
        catch(Exception e) {
            e.printStackTrace();
            throw new EJBException("Could not obtain DataSource: " + e);
        }
    }

    public String ejbCreate() throws CreateException {
        id=null;
        value=null;
        super.ejbCreate();
        PreparedStatement statement = null;
        Connection connection=null;
        try {
            connection = datasource.getConnection();
            statement = connection.prepareStatement
                ("INSERT INTO TransactionTests (ID, VALUE) VALUES (? , ?)");
            // id assigned in super class
            statement.setString(1, id);
            statement.setString(2, value);
            if(statement.executeUpdate() != 1) {
                throw new CreateException("Could not create: " + id);
            }
            return id;
        }
        catch(SQLException e) {
            throw new EJBException("Could not create: " + id+":"+ e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException sqe){
                throw new EJBException("Could not create, close statement: " + sqe);
            }
        }
    }

    //
    // Find methods
    //


    public void ejbRemove() throws RemoveException {
        super.ejbRemove();
        PreparedStatement statement = null;
        Connection connection=null;
        try {
            connection = datasource.getConnection();
            statement = connection.prepareStatement
                ("DELETE FROM TransactionTests WHERE id=?");
            statement.setString(1, id());
            if(statement.executeUpdate() < 1) {
                throw new RemoveException("Could not remove: " + id());
            }
        }
        catch(SQLException e) {
            throw new EJBException("Could not remove " + id() +":" + e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException sqe){
                throw new EJBException("Could not close statement: "+ sqe);
            }
        }
    }


    public String ejbFindByPrimaryKey(String key) throws FinderException {
        if(verbose) {
            System.out.println("findByPrimaryKey on " + this);
        }
        PreparedStatement statement = null;
        Connection connection=null;
        try {
            connection = datasource.getConnection();
            statement = connection.prepareStatement
                ("SELECT id FROM TransactionTests WHERE id = ?");
            statement.setString(1, key);
            ResultSet resultSet = statement.executeQuery();
            if(!resultSet.next()) {
                throw new ObjectNotFoundException("Could not find: " + key);
            }
            resultSet.close();
            return key;
        }
        catch(SQLException e) {
            throw new EJBException("Could not find " + id() +":" + e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
                if (statement != null) {
                    statement.close();
                }
            } catch (SQLException sqe){
                throw new EJBException("Could not close statement: "+ sqe);
            }
        }
    }

    public Enumeration ejbFindAll() throws FinderException {
        if(verbose) {
            System.out.println("findAll on " + this);
        }
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = datasource.getConnection();
            statement = connection.prepareStatement
                ("SELECT id FROM TransactionTests");
            ResultSet resultSet = statement.executeQuery();
            Vector keys = new Vector();
            while(resultSet.next()) {
                String name = resultSet.getString(1);
                keys.addElement(name);
            }
            resultSet.close();
            return keys.elements();
        }
        catch(SQLException e) {
            throw new EJBException("Could not findAll: "+ e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqe){
                throw new EJBException("Could not findAll: "+sqe);
            }
        }
    }

    private String id() {
        String pk = (String) ((EntityContext)_ctx).getPrimaryKey();
        if(!pk.equals(id)) {
            System.err.println("context holds invalid pk");
        }
        return pk;
    }        

    public void ejbLoad()  {
        super.ejbLoad();
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = datasource.getConnection();
            statement = connection.prepareStatement
                ("SELECT value FROM TransactionTests WHERE id = ?");
            id = (String) ((EntityContext)_ctx).getPrimaryKey();
            statement.setString(1, id());
            ResultSet resultSet = statement.executeQuery();
            if(!resultSet.next()) {
                throw new NoSuchEntityException("Row not found: " + id());
            }
            value = resultSet.getString(1);
            resultSet.close();
        }
        catch(SQLException e) {
            throw new EJBException("Could not load: " + id() +":"+e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqe){
            throw new EJBException("Could not delete: " + id() +":"+sqe);
            }
        }

    }
    

    public void ejbStore()  {
        super.ejbStore();
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            connection = datasource.getConnection();
            statement = connection.prepareStatement
                ("UPDATE TransactionTests SET value = ? WHERE id = ?");
            statement.setString(1, value);
            statement.setString(2, id());
            statement.executeUpdate();
        }
        catch(SQLException e) {
            throw new EJBException("Could not store: " + id() + e);
        } finally {
            try {
                if (statement != null) {
                    statement.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (SQLException sqe){
                throw new EJBException("Could not store: " + id() + sqe);
            }
        }

    }
    
}