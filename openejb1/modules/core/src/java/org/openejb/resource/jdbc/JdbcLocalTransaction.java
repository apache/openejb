/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.resource.jdbc;

import javax.resource.spi.LocalTransaction;

import org.openejb.util.Logger;
import org.openejb.util.Messages;

public class JdbcLocalTransaction implements LocalTransaction {

    protected java.sql.Connection sqlConn;
    protected JdbcManagedConnection managedConn;
    protected boolean isActiveTransaction = false;

    protected static final Messages messages = new Messages( "org.openejb.util.resources" );
    protected static final Logger   logger   = Logger.getInstance( "OpenEJB.resource.jdbc", "org.openejb.util.resources" );

    public JdbcLocalTransaction(JdbcManagedConnection managedConn) {
        this.sqlConn = managedConn.getSQLConnection();
        this.managedConn = managedConn;
    }


    public void begin() throws javax.resource.ResourceException{
        if(isActiveTransaction){
            throw new javax.resource.spi.LocalTransactionException("Invalid transaction context. Transaction already active");
        }
        try{
        sqlConn.setAutoCommit(false);
        isActiveTransaction = true;
        }catch(java.sql.SQLException sqlE){
            isActiveTransaction = false;
            throw new javax.resource.spi.ResourceAdapterInternalException("Can not begin transaction demarcation. Setting auto-commit to false for transaction chaining failed");
        }
        managedConn.localTransactionStarted();
    }

    public void commit() throws javax.resource.ResourceException{
        if(isActiveTransaction){
            isActiveTransaction = false;
            try{
            sqlConn.commit();
            }catch(java.sql.SQLException sqlE){
                String msg = messages.format( "jdbc.commit.failed", formatSqlException( sqlE ));
                logger.error( msg );
                throw new javax.resource.spi.LocalTransactionException( msg );
            }
            managedConn.localTransactionCommitted();
            try{
            sqlConn.setAutoCommit(true);
            }catch(java.sql.SQLException sqlE){
                throw new javax.resource.spi.ResourceAdapterInternalException("Setting auto-commit to true to end transaction chaining failed");
            }
        }else{
            throw new javax.resource.spi.LocalTransactionException("Invalid transaction context. No active transaction");
        }
    }

    public void rollback() throws javax.resource.ResourceException{
        if(isActiveTransaction){
            isActiveTransaction = false;

            try{
            sqlConn.rollback();
            }catch(java.sql.SQLException sqlE){
                String msg = messages.format( "jdbc.rollback.failed", formatSqlException( sqlE ));
                logger.error( msg );
                throw new javax.resource.spi.LocalTransactionException( msg );
            }

            managedConn.localTransactionRolledback();

            try{
            sqlConn.setAutoCommit(true);
            }catch(java.sql.SQLException sqlE){
                throw new javax.resource.spi.ResourceAdapterInternalException("Setting auto-commit to true to end transaction chaining failed");
            }
        }else{
            throw new javax.resource.spi.LocalTransactionException("Invalid transaction context. No active transaction");
        }
    }

    /**
    * This method is called by the JdbcConnectionManager when its own cleanup method is called.
    * It ensures that the JdbcLocalTransaction has been properly committed or rolled back. If the
    * transaction is still active, it's rolled back.
    */
    protected void cleanup() throws javax.resource.ResourceException{
        if(isActiveTransaction){
            rollback();
        }
    }

    protected String formatSqlException(java.sql.SQLException e){
        return messages.format("jdbc.exception", e.getClass().getName(), e.getMessage(), e.getErrorCode()+"", e.getSQLState());
    }
}