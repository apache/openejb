package org.openejb.test;

import javax.naming.InitialContext;

import org.openejb.test.beans.Database;

/**
 *
 */
public class AxionTestDatabase extends AbstractTestDatabase {

    protected Database database;
    protected InitialContext initialContext;

    private static final String CREATE_ACCOUNT = "CREATE TABLE account ( ssn string, first_name string, last_name string, balance integer)";
    private static final String DROP_ACCOUNT = "DROP TABLE account";

    private static final String CREATE_ENTITY = "CREATE TABLE entity ( id integer default entity_seq.nextval, first_name string, last_name string )";
    private static final String DROP_ENTITY = "DROP TABLE entity";

    private static final String CREATE_ENTITY_EXPLICIT_PK = "CREATE TABLE entity_explicit_pk ( id integer, first_name string, last_name string )";
    private static final String DROP_ENTITY_EXPLICIT_PK = "DROP TABLE entity_explicit_pk";

    private static final String CREATE_ENTITY_SEQ = "CREATE SEQUENCE entity_seq";
    private static final String DROP_ENTITY_SEQ = "DROP SEQUENCE entity_seq";

    static {
        System.setProperty("noBanner", "true");
    }

    public void createEntityTable() throws java.sql.SQLException {
        executeStatementIgnoreErrors(DROP_ENTITY_SEQ);
        super.createEntityTable();
        executeStatement(CREATE_ENTITY_SEQ);
    }

    public void dropEntityTable() throws java.sql.SQLException {
        super.dropEntityTable();
        executeStatement(DROP_ENTITY_SEQ);
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



