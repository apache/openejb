package org.openejb.test;

import java.rmi.RemoteException;
import java.util.Properties;
import java.sql.SQLException;
import javax.naming.InitialContext;

import org.openejb.test.beans.Database;
import org.openejb.test.beans.DatabaseHome;

/**
 *
 */
public class DerbyTestDatabase implements TestDatabase {

    protected Database database;
    protected InitialContext initialContext;

    private static final String CREATE_ACCOUNT = "CREATE TABLE account ( ssn VARCHAR(25), first_name VARCHAR(256), last_name VARCHAR(256), balance integer)";
    private static final String DROP_ACCOUNT = "DROP TABLE account";

    private static final String CREATE_ENTITY = "CREATE TABLE entity ( id integer GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), first_name VARCHAR(256), last_name VARCHAR(256) )";
    private static final String DROP_ENTITY = "DROP TABLE entity";

    static {
        System.setProperty("noBanner", "true");
    }


    public void createEntityTable() throws java.sql.SQLException {
        executeStatementIgnoreErrors(DROP_ENTITY);
        executeStatement(CREATE_ENTITY);
    }

    public void dropEntityTable() throws java.sql.SQLException {
        executeStatement(DROP_ENTITY);
    }


    private void executeStatementIgnoreErrors(String command) {
        try {
            getDatabase().execute(command);
        } catch (Exception e) {
            // not concerned
        }
    }

    private void executeStatement(String command) throws java.sql.SQLException {
        try {
            getDatabase().execute(command);
        } catch (RemoteException re) {
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (SQLException)new SQLException("could not execute command: " + command).initCause(re.detail);
            } else {
                throw new SQLException("Cannot execute statement: " + re.getMessage(), command);
            }
        } catch (Throwable t) {
            throw (SQLException)new SQLException("could not execute command: " + command).initCause(t);
        }
    }

    public void createAccountTable() throws java.sql.SQLException {
        executeStatementIgnoreErrors(DROP_ACCOUNT);
        executeStatement(CREATE_ACCOUNT);
    }

    public void dropAccountTable() throws java.sql.SQLException {
        executeStatement(DROP_ACCOUNT);
    }

    public void start() throws IllegalStateException {
        try {
            // @todo this is a hack that limits us to a single server 
//            Properties properties = TestManager.getServer().getContextEnvironment();
            Properties properties = new Properties();
            properties.put("test.server.class", "org.openejb.test.RemoteTestServer");
            properties.put("java.naming.factory.initial", "org.openejb.client.RemoteInitialContextFactory");
            properties.put("java.naming.provider.url", "127.0.0.1:4201");
            properties.put("java.naming.security.principal", "testuser");
            properties.put("java.naming.security.credentials", "testpassword");
            initialContext = new InitialContext(properties);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create initial context: " + e.getClass().getName() + " " + e.getMessage());
        }
    }


    private Database getDatabase() {
        if (initialContext == null) {
            start();
        }
        if (database == null) {
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
            throw (IllegalStateException)new IllegalStateException("Cannot find 'client/tools/DatabaseHome': "
                    + e.getMessage()).initCause(e);
        }
        try {
            database = databaseHome.create();
        } catch (Exception e) {
            throw (IllegalStateException)new IllegalStateException("Cannot start database: "
                    + e.getMessage()).initCause(e);
        }
        return database;
    }

    public void stop() throws IllegalStateException {
    }

    public void init(Properties props) throws IllegalStateException {
    }
}



