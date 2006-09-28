package org.openejb.test;

import javax.naming.InitialContext;

import org.openejb.test.beans.Database;

/**
 * 
 */
public class InstantDbTestDatabase extends AbstractTestDatabase {

    protected Database database;
    protected InitialContext initialContext;

    private static String CREATE_ACCOUNT = "CREATE TABLE account ( ssn CHAR(11) PRIMARY KEY, first_name CHAR(20), last_name CHAR(20), balance INT)";
    //private static String _createAccount = "CREATE TABLE Account ( AcctID INT PRIMARY KEY AUTO INCREMENT,  SSN CHAR(11), first_name CHAR(20), last_name CHAR(20), BALANCE INT)";
    private static String DROP_ACCOUNT = "DROP TABLE account";

    //private static String _createEntity = "CREATE TABLE entity ( id INT PRIMARY KEY, first_name CHAR(20), last_name CHAR(20) )";
    private static String CREATE_ENTITY = "CREATE TABLE entity ( id INT PRIMARY KEY AUTO INCREMENT, first_name CHAR(20), last_name CHAR(20) )";
    private static String DROP_ENTITY = "DROP TABLE entity";

    private static final String CREATE_ENTITY_EXPLICIT_PK = "CREATE TABLE entity_explicit_pk ( id INT, first_name CHAR(20), last_name CHAR(20) )";
    private static final String DROP_ENTITY_EXPLICIT_PK = "DROP TABLE entity_explicit_pk";

    static {
        System.setProperty("noBanner", "true");
    }

    protected String getCreateAccount() {
        return CREATE_ACCOUNT;
    }

    protected String getDropAccount() {
        return DROP_ACCOUNT;
    }

    protected String getCreateEntity() {
        return CREATE_ENTITY;
    }

    protected String getDropEntity() {
        return DROP_ENTITY;
    }

    protected String getCreateEntityExplictitPK() {
        return CREATE_ENTITY_EXPLICIT_PK;
    }

    protected String getDropEntityExplicitPK() {
        return DROP_ENTITY_EXPLICIT_PK;
    }

}

    

