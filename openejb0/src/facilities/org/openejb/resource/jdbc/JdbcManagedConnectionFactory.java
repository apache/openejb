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

import java.sql.DriverManager;

import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.EISSystemException;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ResourceAdapterInternalException;

import org.openejb.core.EnvProps;
import org.openejb.util.Logger;


public class JdbcManagedConnectionFactory 
implements javax.resource.spi.ManagedConnectionFactory, java.io.Serializable {
    
    protected Logger logger = Logger.getInstance("OpenEJB.connector", "org.openejb.alt.util.resources");

    protected String jdbcDriver;
    protected String jdbcUrl;
    protected String defaultUserName;
    protected String defaultPassword;
    protected java.io.PrintWriter logWriter;
    private int hashCode = 0;// assumes that this class is immutable
    
    public void init(java.util.Properties props)throws javax.resource.spi.ResourceAdapterInternalException{
        setDefaultUserName(props.getProperty(EnvProps.USER_NAME));   
        setDefaultPassword(props.getProperty(EnvProps.PASSWORD));   
        setJdbcUrl(props.getProperty(EnvProps.JDBC_URL));   
        setJdbcDriver(props.getProperty(EnvProps.JDBC_DRIVER));   

        String userDir = System.getProperty("user.dir");
        try{
            System.setProperty("user.dir",System.getProperty("openejb.base"));
            // Test the connection out, problems are logged
            testDriver();
        } finally {
            System.setProperty("user.dir",userDir);
        }
    }

    protected void testDriver() {
        java.sql.Connection physicalConn = null;
        try{
            physicalConn = DriverManager.getConnection(jdbcUrl, defaultUserName, defaultPassword);        
        }catch(Throwable e){
            logger.error("Testing driver failed.  "+
                         "["+jdbcUrl+"]  "+
                         "Could not obtain a physical JDBC connection from the DriverManager.  "+
                         e.getMessage());
        } finally {
            try{
                physicalConn.close();
            } catch (Exception dontCare){}
        }
    }
   
    public void setDefaultUserName(String dun){
        defaultUserName = dun;
    }
    public void setDefaultPassword(String dp){
        defaultPassword = dp;
    }
    public void setJdbcUrl(String url){
        jdbcUrl = url;
    }
    public void setJdbcDriver(String driver) throws javax.resource.spi.ResourceAdapterInternalException{
        jdbcDriver = driver;
        try{
            ClassLoader cl = org.openejb.util.ClasspathUtils.getContextClassLoader();
            Class.forName( jdbcDriver, true, cl);
        }catch(ClassNotFoundException cnf){
            //BUG: If this situtuation occurs, only the words:
            // java.lang.reflect.InvocationTargetException: javax.resource.spi.ResourceAdapterInternalException
            // are outputted to the screen.
           //cnf.printStackTrace(System.out);
           ResourceAdapterInternalException raie =  new ResourceAdapterInternalException("JDBC Driver class \""+jdbcDriver+"\" not found by class loader", ErrorCode.JDBC_0002);
           //raie.setLinkedException(cnf);
           throw raie;
        }
    }
    public String getDefaultUserName(){
        return defaultUserName;
    }
    public String getDefaultPassword(){
        return defaultPassword;
    }
    public String getJdbcDriver(){
        return jdbcDriver;
    }
    public String getJdbcUrl(){
        return jdbcUrl;
    }
    
    public java.lang.Object createConnectionFactory()  throws javax.resource.ResourceException{

        throw new javax.resource.NotSupportedException("This connector must be used with an application server connection manager");
    }
    public java.lang.Object createConnectionFactory(ConnectionManager cxManager)  throws javax.resource.ResourceException{
        // return the DataSource
        return new JdbcConnectionFactory(this, cxManager);
    } 
    
    public ManagedConnection createManagedConnection(javax.security.auth.Subject subject,ConnectionRequestInfo cxRequestInfo)  throws javax.resource.ResourceException{
        JdbcConnectionRequestInfo rxInfo = (JdbcConnectionRequestInfo)cxRequestInfo;
        java.sql.Connection physicalConn;
        String userDir = System.getProperty("user.dir");
        try{
            System.setProperty("user.dir",System.getProperty("openejb.home"));
            physicalConn = DriverManager.getConnection(jdbcUrl, rxInfo.getUserName(), rxInfo.getPassword());        
        }catch(java.sql.SQLException sqlE){
            EISSystemException eisse =  new EISSystemException("Could not obtain a physical JDBC connection from the DriverManager");
            eisse.setLinkedException(sqlE);
            throw eisse;
        } finally {
            System.setProperty("user.dir",userDir);
        }
        return new JdbcManagedConnection(this, physicalConn, rxInfo);
    } 
    public boolean equals(Object other){
        if(other instanceof JdbcManagedConnectionFactory){
            JdbcManagedConnectionFactory otherMCF = (JdbcManagedConnectionFactory)other;
            if(jdbcDriver.equals(otherMCF.jdbcDriver) && jdbcUrl.equals(otherMCF.jdbcUrl) &&
               defaultUserName.equals(otherMCF.defaultUserName) && defaultPassword.equals(otherMCF.defaultPassword) )  {
                return true;
            }
        }
        return false;
    }
    public java.io.PrintWriter getLogWriter(){
        return logWriter;
    } 
    public int hashCode(){
        if(hashCode != 0) return hashCode;
        hashCode = jdbcDriver.hashCode()^jdbcUrl.hashCode()^defaultUserName.hashCode()^defaultPassword.hashCode();
        return hashCode;
    }
    public ManagedConnection matchManagedConnections(java.util.Set connectionSet,javax.security.auth.Subject subject, ConnectionRequestInfo cxRequestInfo)  throws javax.resource.ResourceException{
        if(cxRequestInfo instanceof JdbcConnectionRequestInfo){
            Object [] connections = connectionSet.toArray();
            for(int i = 0; i < connections.length; i++){
                JdbcManagedConnection managedConn = (JdbcManagedConnection)connections[i];
                if(managedConn.getRequestInfo().equals(cxRequestInfo))
                    return managedConn;
            }
        }
        return null;
    } 
    public void setLogWriter(java.io.PrintWriter out) {
        logWriter = out;
    }

}