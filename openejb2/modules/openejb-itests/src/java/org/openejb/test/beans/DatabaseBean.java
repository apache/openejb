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
package org.openejb.test.beans;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import javax.ejb.CreateException;
import javax.ejb.EJBException;
import javax.ejb.SessionBean;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class DatabaseBean implements SessionBean {
    private InitialContext jndiContext;

    public void ejbCreate() throws CreateException {
        try {
            jndiContext = new InitialContext();
        } catch (Exception e) {
            throw new EJBException(e.getMessage());
        }
    }

    public void executeQuery(String statement) throws SQLException {
        Connection con = null;
        PreparedStatement stmt = null;
        try {
            DataSource ds = (DataSource) jndiContext.lookup("java:comp/env/database");
            con = ds.getConnection();

            stmt = con.prepareStatement(statement);
            stmt.executeQuery();
        } catch (NamingException e) {
            throw new EJBException("Cannot lookup the Database bean."+e.getMessage());
        } catch (Exception e) {
            throw new EJBException("Cannot execute the statement: " + statement + e.getMessage());
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean execute(String statement) throws SQLException {
        Connection con = null;
        Statement stmt = null;
        try {
            DataSource ds = (DataSource) jndiContext.lookup("java:comp/env/database");
            con = ds.getConnection();

            stmt = con.createStatement();
            return stmt.execute(statement);
        } catch (NamingException e) {
            throw new EJBException("Cannot lookup the Database bean."+e.getMessage(), e);
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    //e.printStackTrace();
                }
            }
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    //e.printStackTrace();
                }
            }
        }
    }

    public void ejbPassivate() {
        // never called
    }

    public void ejbActivate() {
        // never called
    }

    public void ejbRemove() {
    }

    public void setSessionContext(javax.ejb.SessionContext cntx) {
    }
}
