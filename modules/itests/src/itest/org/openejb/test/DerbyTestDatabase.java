package org.openejb.test;


/**
 *
 */
public class DerbyTestDatabase extends AbstractTestDatabase {
    private static final String CREATE_ACCOUNT = "CREATE TABLE account ( ssn VARCHAR(25), first_name VARCHAR(256), last_name VARCHAR(256), balance integer)";
    private static final String DROP_ACCOUNT = "DROP TABLE account";

    private static final String CREATE_ENTITY = "CREATE TABLE entity ( id integer GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1), first_name VARCHAR(256), last_name VARCHAR(256) )";
    private static final String DROP_ENTITY = "DROP TABLE entity";

    private static final String CREATE_ENTITY_EXPLICIT_PK = "CREATE TABLE entity_explicit_pk ( id integer, first_name VARCHAR(256), last_name VARCHAR(256) )";
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



