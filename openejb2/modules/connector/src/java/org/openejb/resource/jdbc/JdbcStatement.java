/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.openejb.resource.jdbc;

import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Connection;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class JdbcStatement implements Statement {

    private Statement statement;

    public JdbcStatement(Statement statement) {
        this.statement = statement;
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        System.out.println("executing: " + sql);
        return new JdbcResultSet(statement.executeQuery(sql));
    }

    public int executeUpdate(String sql) throws SQLException {
        System.out.println("executing: " + sql);
        return statement.executeUpdate(sql);
    }

    public void close() throws SQLException {
        statement.close();
    }

    public int getMaxFieldSize() throws SQLException {
        return statement.getMaxFieldSize();
    }

    public void setMaxFieldSize(int max) throws SQLException {
        statement.setMaxFieldSize(max);
    }

    public int getMaxRows() throws SQLException {
        return statement.getMaxRows();
    }

    public void setMaxRows(int max) throws SQLException {
        statement.setMaxRows(max);
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        statement.setEscapeProcessing(enable);
    }

    public int getQueryTimeout() throws SQLException {
        return statement.getQueryTimeout();
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        statement.setQueryTimeout(seconds);
    }

    public void cancel() throws SQLException {
        statement.cancel();
    }

    public SQLWarning getWarnings() throws SQLException {
        return statement.getWarnings();
    }

    public void clearWarnings() throws SQLException {
        statement.clearWarnings();
    }

    public void setCursorName(String name) throws SQLException {
        statement.setCursorName(name);
    }

    public boolean execute(String sql) throws SQLException {
        System.out.println("executing: " + sql);
        return statement.execute(sql);
    }

    public ResultSet getResultSet() throws SQLException {
        return new JdbcResultSet(statement.getResultSet());
    }

    public int getUpdateCount() throws SQLException {
        return statement.getUpdateCount();
    }

    public boolean getMoreResults() throws SQLException {
        return statement.getMoreResults();
    }

    public void setFetchDirection(int direction) throws SQLException {
        statement.setFetchDirection(direction);
    }

    public int getFetchDirection() throws SQLException {
        return statement.getFetchDirection();
    }

    public void setFetchSize(int rows) throws SQLException {
        statement.setFetchSize(rows);
    }

    public int getFetchSize() throws SQLException {
        return statement.getFetchSize();
    }

    public int getResultSetConcurrency() throws SQLException {
        return statement.getResultSetConcurrency();
    }

    public int getResultSetType() throws SQLException {
        return statement.getResultSetType();
    }

    public void addBatch(String sql) throws SQLException {
        statement.addBatch(sql);
    }

    public void clearBatch() throws SQLException {
        statement.clearBatch();
    }

    public int[] executeBatch() throws SQLException {
        return statement.executeBatch();
    }

    public Connection getConnection() throws SQLException {
        return statement.getConnection();
    }

    public boolean getMoreResults(int current) throws SQLException {
        return statement.getMoreResults(current);
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return new JdbcResultSet(statement.getGeneratedKeys());
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        System.out.println("executing: " + sql);
        return statement.executeUpdate(sql, autoGeneratedKeys);
    }

    public int executeUpdate(String sql, int columnIndexes[]) throws SQLException {
        System.out.println("executing: " + sql);
        return statement.executeUpdate(sql, columnIndexes);
    }

    public int executeUpdate(String sql, String columnNames[]) throws SQLException {
        System.out.println("executing: " + sql);
        return statement.executeUpdate(sql, columnNames);
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        System.out.println("executing: " + sql);
        return statement.execute(sql, autoGeneratedKeys);
    }

    public boolean execute(String sql, int columnIndexes[]) throws SQLException {
        System.out.println("executing: " + sql);
        return statement.execute(sql, columnIndexes);
    }

    public boolean execute(String sql, String columnNames[]) throws SQLException {
        System.out.println("executing: " + sql);
        return statement.execute(sql, columnNames);
    }

    public int getResultSetHoldability() throws SQLException {
        return statement.getResultSetHoldability();
    }


}
