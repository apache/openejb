package org.openejb.test;

import java.rmi.RemoteException;
import java.util.Properties;
import javax.naming.InitialContext;

import org.openejb.test.beans.Database;
import org.openejb.test.beans.DatabaseHome;

/**
 *
 */
public class AxionTestDatabase implements TestDatabase {

    protected Database database;
    protected InitialContext initialContext;

    private static final String CREATE_ACCOUNT = "CREATE TABLE account ( ssn string, first_name string, last_name string, balance integer)";
    private static final String DROP_ACCOUNT = "DROP TABLE account";

    private static final String CREATE_ENTITY = "CREATE TABLE entity ( id integer default entity_seq.nextval, first_name string, last_name string )";
    private static final String DROP_ENTITY = "DROP TABLE entity";

    private static final String CREATE_BASICCMP = "CREATE TABLE BASICCMP ( id integer default entity_seq.nextval, firstname string, lastname string )";
    private static final String DROP_BASICCMP = "DROP TABLE BASICCMP";

    private static final String CREATE_AOBASICCMP = "CREATE TABLE AOBASICCMP ( id integer default entity_seq.nextval, firstname string, lastname string )";
    private static final String DROP_AOBASICCMP = "DROP TABLE AOBASICCMP";

    private static final String CREATE_ENCCMP = "CREATE TABLE ENCCMP ( id integer default entity_seq.nextval, firstname string, lastname string )";
    private static final String DROP_ENCCMP = "DROP TABLE ENCCMP";

    private static final String CREATE_CMPRMIIIOP = "CREATE TABLE CMPRMIIIOP ( id integer default entity_seq.nextval, firstname string, lastname string )";
    private static final String DROP_CMPRMIIIOP = "DROP TABLE CMPRMIIIOP";

    private static final String CREATE_ENTITY_SEQ = "CREATE SEQUENCE entity_seq";
    private static final String DROP_ENTITY_SEQ = "DROP SEQUENCE entity_seq";

    static {
        System.setProperty("noBanner", "true");
    }


    public void createEntityTable() throws java.sql.SQLException {
        executeStatementIgnoreErrors(DROP_CMPRMIIIOP);
        executeStatementIgnoreErrors(DROP_ENCCMP);
        executeStatementIgnoreErrors(DROP_BASICCMP);
        executeStatementIgnoreErrors(DROP_AOBASICCMP);
        executeStatementIgnoreErrors(DROP_ENTITY);
        executeStatementIgnoreErrors(DROP_ENTITY_SEQ);
        executeStatement(CREATE_ENTITY_SEQ);
        executeStatement(CREATE_ENTITY);
        executeStatement(CREATE_BASICCMP);
        executeStatement(CREATE_AOBASICCMP);
        executeStatement(CREATE_ENCCMP);
        executeStatement(CREATE_CMPRMIIIOP);
    }

    public void dropEntityTable() throws java.sql.SQLException {
        executeStatement(DROP_CMPRMIIIOP);
        executeStatement(DROP_ENCCMP);
        executeStatement(DROP_BASICCMP);
        executeStatement(DROP_AOBASICCMP);
        executeStatement(DROP_ENTITY);
        executeStatement(DROP_ENTITY_SEQ);
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
                throw (java.sql.SQLException) re.detail;
            } else {
                throw new java.sql.SQLException("Cannot exectute statement: " + re.getMessage(), command);
            }
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
            Properties properties = TestManager.getServer().getContextEnvironment();
            initialContext = new InitialContext(properties);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot create initial context: " + e.getClass().getName() + " " + e.getMessage());
        }
    }


    private Database getDatabase() {
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



