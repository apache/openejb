package org.openejb.test;

import java.rmi.RemoteException;
import java.util.Properties;

import javax.naming.InitialContext;

import org.openejb.test.beans.Database;
import org.openejb.test.beans.DatabaseHome;

/**
 * 
 */
public class InstantDbTestDatabase implements TestDatabase{
    
    protected Database database;
    protected InitialContext initialContext;
    
    private static String _createAccount = "CREATE TABLE account ( ssn CHAR(11) PRIMARY KEY, first_name CHAR(20), last_name CHAR(20), balance INT)";
    //private static String _createAccount = "CREATE TABLE Account ( AcctID INT PRIMARY KEY AUTO INCREMENT,  SSN CHAR(11), first_name CHAR(20), last_name CHAR(20), BALANCE INT)";
    private static String _dropAccount   = "DROP TABLE account";
    
    //private static String _createEntity = "CREATE TABLE entity ( id INT PRIMARY KEY, first_name CHAR(20), last_name CHAR(20) )";
    private static String _createEntity = "CREATE TABLE entity ( id INT PRIMARY KEY AUTO INCREMENT, first_name CHAR(20), last_name CHAR(20) )";    
    private static String _dropEntity   = "DROP TABLE entity";

    static{
        System.setProperty("noBanner", "true");
    }
    

    public void createEntityTable() throws java.sql.SQLException {
        try{
            try{
                getDatabase().execute(_dropEntity);
            } catch (Exception e){
                // not concerned
            }
            getDatabase().execute(_createEntity);
        } catch (RemoteException re){
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException)re.detail;
            } else {
                throw new java.sql.SQLException("Cannot create entity table: "+re.getMessage(), _createEntity);
            }
        }
    }
    public void dropEntityTable() throws java.sql.SQLException {
        try {
            getDatabase().execute(_dropEntity);
        } catch (RemoteException re){
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException)re.detail;
            } else {
                throw new java.sql.SQLException("Unable to drop entity table: "+re.getMessage(), _dropEntity);
            }
        }
    }
    
    
    public void createAccountTable() throws java.sql.SQLException {
        try{
            try{
                getDatabase().execute(_dropAccount);
            } catch (Exception e){
                // not concerned
            }
            getDatabase().execute(_createAccount);
        } catch (RemoteException re){
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException)re.detail;
            } else {
                throw new java.sql.SQLException("Cannot create account table: "+re.getMessage(), _createAccount);
            }
        }
    }
    
    public void dropAccountTable() throws java.sql.SQLException {
        try {
            getDatabase().execute(_dropAccount);
        } catch (RemoteException re){
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException)re.detail;
            } else {
                throw new java.sql.SQLException("Cannot drop account table: "+re.getMessage(), _dropAccount);
            }
        }
    }
    
    public void start() throws IllegalStateException {
        try {
            Properties properties = TestManager.getServer().getContextEnvironment();
            initialContext = new InitialContext(properties);
        } catch (Exception e){
            throw new IllegalStateException("Cannot create initial context: "+e.getClass().getName()+" "+e.getMessage());
        }
    }
    

    private Database getDatabase(){
        if (database == null){
            database = createDatabaseObject();
        }
        return database;
    }
    
    private Database createDatabaseObject() {
        Object obj = null;
        DatabaseHome databaseHome = null;
        Database database = null;
        try {
            /* Create database */
            obj = initialContext.lookup("client/tools/DatabaseHome");
            databaseHome = (DatabaseHome) javax.rmi.PortableRemoteObject.narrow(obj, DatabaseHome.class);
        } catch (Exception e) {
            throw new IllegalStateException(
                "Cannot find 'client/tools/DatabaseHome': "
                    + e.getClass().getName()
                    + " "
                    + e.getMessage());
        }
        try {
            database = databaseHome.create();
        } catch (Exception e) {
            throw new IllegalStateException(
                "Cannot start database: "
                    + e.getClass().getName()
                    + " "
                    + e.getMessage());
        }
        return database;
    }
    public void stop() throws IllegalStateException {
    }
    
    public void init(Properties props) throws IllegalStateException {
    }
}

    

