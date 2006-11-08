/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openejb.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.InitialContext;

import org.openejb.test.beans.Database;

/**
 * 
 */
public class PostgreSqlTestDatabase extends AbstractTestDatabase {

    protected Database database;
    protected InitialContext initialContext;


    private static String CREATE_ACCOUNT = "CREATE TABLE account ( ssn CHAR(11), first_name CHAR(20), last_name CHAR(20), balance INT, Constraint \"account_pkey\" Primary Key (\"ssn\"))";
    private static String DROP_ACCOUNT = "DROP TABLE account";
    
    private static String CREATE_ACCOUNT_SEQ = "CREATE SEQUENCE account_id_seq";
    private static String DROP_ACCOUNT_SEQ = "DROP SEQUENCE account_id_seq";
    
    //private static String _createEntity  = "CREATE TABLE entity ( id INT NOT NULL, first_name CHAR(20), last_name CHAR(20), Constraint \"entity_pkey\" Primary Key (\"id\") )";
    private static String CREATE_ENTITY = "CREATE TABLE entity ( id INT DEFAULT nextval('entity_id_seq') , first_name CHAR(20), last_name CHAR(20), Constraint \"entity_pkey\" Primary Key (\"id\") )";
    private static String DROP_ENTITY = "DROP TABLE entity";

    private static final String CREATE_ENTITY_SEQ = "CREATE SEQUENCE entity_id_seq";
    private static final String DROP_ENTITY_SEQ = "DROP SEQUENCE entity_id_seq";

    private static final String CREATE_ENTITY_EXPLICIT_PK = "CREATE TABLE entity_explicit_pk ( id INT, first_name CHAR(20), last_name CHAR(20) )";
    private static final String DROP_ENTITY_EXPLICIT_PK = "DROP TABLE entity_explicit_pk";

    public void createEntityTable() throws java.sql.SQLException {
        executeStatementIgnoreErrors(DROP_ENTITY_SEQ);
        super.createEntityTable();
        executeStatement(CREATE_ENTITY_SEQ);
    }

    public void dropEntityTable() throws java.sql.SQLException {
        super.dropEntityTable();
        executeStatement(DROP_ENTITY_SEQ);
    }

    public void createAccountTable() throws SQLException {
        executeStatement(DROP_ACCOUNT_SEQ);
        super.createAccountTable();
        executeStatement(CREATE_ACCOUNT_SEQ);
    }

    public void dropAccountTable() throws SQLException {
        super.dropAccountTable();
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

    public static void main(String[] args) {
        System.out.println("Checking if driver is registered with DriverManager.");
        try {
            ClassLoader cl = org.apache.openejb.util.ClasspathUtils.getContextClassLoader();
            Class.forName("org.postgresql.Driver", true, cl);
        } catch (ClassNotFoundException e) {
            System.out.println("Couldn't find the driver!");
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Registered the driver, so let's make a connection.");

        Connection conn = null;

        try {
            conn = DriverManager.getConnection("jdbc:postgresql://localhost/openejbtest", "openejbuser", "javaone");
        } catch (SQLException e) {
            System.out.println("Couldn't connect.");
            e.printStackTrace();
            System.exit(1);
        }

        if (conn == null) {
            System.out.println("No connection!");
        }

        Statement stmt = null;

        try {
            stmt = conn.createStatement();
        } catch (SQLException e) {
            System.out.println("Couldn't create a statement.");
            e.printStackTrace();
            System.exit(1);
        }

        ResultSet rs = null;

        try {
            stmt.execute("DROP TABLE entity");
        } catch (SQLException e) {
        }

        System.out.println("Creating entity table.");
        try {
            stmt.execute(CREATE_ENTITY);
        } catch (SQLException e) {
            System.out.println("Couldn't create the entity table");
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Inserting record.");
        try {
            PreparedStatement pstmt = conn.prepareStatement("insert into entity (id, first_name, last_name) values (?,?,?)");
            pstmt.setInt(1, 101);
            pstmt.setString(2, "Bunson");
            pstmt.setString(3, "Honeydew");
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Couldn't create the entity table");
            e.printStackTrace();
            System.exit(1);
        }

        System.out.println("Selecting the record.");
        try {
            PreparedStatement pstmt = conn.prepareStatement("select id from entity where first_name = ? AND last_name = ?");
            pstmt.setString(1, "Bunson");
            pstmt.setString(2, "Honeydew");
            ResultSet set = pstmt.executeQuery();
        } catch (SQLException e) {
            System.out.println("Couldn't select the entry");
            e.printStackTrace();
            System.exit(1);
        }


        System.out.println("Dropping the entity table.");
        try {
            stmt.execute(DROP_ENTITY);
        } catch (SQLException e) {
            System.out.println("Couldn't drop the entity table");
            e.printStackTrace();
            System.exit(1);
        }

        try {
            conn.close();
        } catch (SQLException e) {
            System.out.println("Couldn't create the sequense");
            e.printStackTrace();
            System.exit(1);
        }

    }
}
