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

    private static final String CREATE_ENTITY_SEQ = "CREATE SEQUENCE entity_seq";
    private static final String DROP_ENTITY_SEQ = "DROP SEQUENCE entity_seq";

    static {
        System.setProperty("noBanner", "true");
    }


    public void createEntityTable() throws java.sql.SQLException {
        try {
            getDatabase().execute(DROP_ENTITY);
        } catch (Exception e) {
            // not concerned
        }
        try {
            getDatabase().execute(DROP_ENTITY_SEQ);
        } catch (Exception e) {
            // not concerned
        }
        try {
            getDatabase().execute(CREATE_ENTITY_SEQ);
        } catch (RemoteException re) {
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException) re.detail;
            } else {
                throw new java.sql.SQLException("Cannot create entity sequence: " + re.getMessage(), CREATE_ENTITY_SEQ);
            }
        }
        try {
            getDatabase().execute(CREATE_ENTITY);
        } catch (RemoteException re) {
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException) re.detail;
            } else {
                throw new java.sql.SQLException("Cannot create entity table: " + re.getMessage(), CREATE_ENTITY);
            }
        }
    }

    public void dropEntityTable() throws java.sql.SQLException {
        try {
            getDatabase().execute(DROP_ENTITY);
        } catch (RemoteException re) {
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException) re.detail;
            } else {
                throw new java.sql.SQLException("Unable to drop entity table: " + re.getMessage(), DROP_ENTITY);
            }
        }
        try {
            getDatabase().execute(DROP_ENTITY_SEQ);
        } catch (RemoteException re) {
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException) re.detail;
            } else {
                throw new java.sql.SQLException("Unable to drop entity table: " + re.getMessage(), DROP_ENTITY_SEQ);
            }
        }
    }


    public void createAccountTable() throws java.sql.SQLException {
        try {
            try {
                getDatabase().execute(DROP_ACCOUNT);
            } catch (Exception e) {
                // not concerned
            }
            getDatabase().execute(CREATE_ACCOUNT);
        } catch (RemoteException re) {
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException) re.detail;
            } else {
                throw new java.sql.SQLException("Cannot create account table: " + re.getMessage(), CREATE_ACCOUNT);
            }
        }
    }

    public void dropAccountTable() throws java.sql.SQLException {
        try {
            getDatabase().execute(DROP_ACCOUNT);
        } catch (RemoteException re) {
            if (re.detail != null && re.detail instanceof java.sql.SQLException) {
                throw (java.sql.SQLException) re.detail;
            } else {
                throw new java.sql.SQLException("Cannot drop account table: " + re.getMessage(), DROP_ACCOUNT);
            }
        }
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



