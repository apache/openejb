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
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id: ValidationTable.java 444993 2004-10-25 09:55:08Z dblevins $
 */
package org.apache.openejb.config;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.Properties;

import org.apache.openejb.util.FileUtils;
import org.apache.openejb.util.JarUtils;

/**
 * Beans should be validated, but only when:
 * <p/>
 * - the validator version has changed since last validation
 * - the jar has changed since last validation
 * - the jar has never been validated
 * <p/>
 * <p/>
 * This class works, but it causes problems elsewhere.  It seems that
 * using InstantDB just causes us to not be able to shutdown the VM.
 * Obviously, InstantDB is starting user threads.
 * <p/>
 * This probably needs to be rewritten to not use InstantDB.
 *
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class ValidationTable {

    /**
     * Singleton ValidationTable instance
     */
    private static ValidationTable table;

    /**
     * It's possible that we can create/drop this table for each release version
     * or just keep a validator version id in the table. For now, I've added a
     * version number to the table.
     * <p/>
     * It's important that we make the distinction because someone could update
     * their OpenEJB version, which may have more/better validation functionality.
     * <p/>
     * Table looks as such:
     * <p/>
     * CREATE TABLE validation (
     * <p/>
     * jar_path          CHAR(150) PRIMARY KEY,
     * last_validated    CHAR(13),
     * validator_version CHAR(20)
     * <p/>
     * )
     */
    private final String _createTable = "CREATE TABLE validation ( jar_path CHAR(150) PRIMARY KEY, last_validated CHAR(13), validator_version CHAR(20))";
    private final String _selectValidated = "select last_validated, validator_version  from validation where jar_path = ?";
    private final String _updateValidated = "update  validation set last_validated = (?), validator_version = ? where jar_path = ?";
    private final String _insertValidated = "insert into validation (jar_path, last_validated, validator_version) values (?,?,?)";

    private final String jdbcDriver = "org.enhydra.instantdb.jdbc.idbDriver";
    private final String jdbcUrl = "jdbc:idb:conf/registry.properties";
    private final String userName = "system";
    private final String password = "system";

    private Connection conn;
    //physicalConn = DriverManager.getConnection(jdbcUrl, rxInfo.getUserName(), rxInfo.getPassword());        

    private ValidationTable() {
        try {
            // Load the driver
            ClassLoader cl = org.apache.openejb.util.ClasspathUtils.getContextClassLoader();
            Class.forName(jdbcDriver, true, cl);
            // Get a connection
            conn = getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            // try and create the table
            // if it's already there, an exception will 
            // be thrown.  We can ignore it.
            Statement stmt = conn.createStatement();
            stmt.execute(_createTable);
        } catch (Exception e) {
            // We can ignore this.
        } finally {
            try {
                conn.close();
            } catch (Exception e) {
            }
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl, userName, password);
    }

    public static ValidationTable getInstance() {
        if (table == null) {
            table = new ValidationTable();
        }

        return table;
    }

    public boolean isValidated(String jarFile) {
        try {
            File jar = FileUtils.getBase().getFile(jarFile);
            long lastModified = jar.lastModified();
            long lastValidated = getLastValidated(jar);
            //System.out.println("  -- modified  "+lastModified);
            //System.out.println("  -- validated "+lastValidated);
            return (lastValidated > lastModified);
        } catch (Exception e) {
            return false;
        }
    }

    public void setValidated(String jarFile) {
        setLastValidated(jarFile, System.currentTimeMillis());
    }


    public long getLastValidated(File jar) {
        long validated = 0L;
        try {
            conn = getConnection();

            String jarFileURL = jar.toURL().toExternalForm();

            //System.out.println("[] getLastValidated "+jarFileURL);
            PreparedStatement stmt = conn.prepareStatement(_selectValidated);
            stmt.setString(1, jarFileURL);

            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                String version = results.getString(2);
                //System.out.println("[]     version "+version);
                if (version == null || version.equals(getVersion())) {
                    validated = results.getLong(1);
                    //System.out.println("[]     validated "+validated);
                }
            }
        } catch (Exception e) {
            // TODO:1: Log something...
            //e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (Exception e) {
            }
        }
        return validated;
    }


    /**
     * Same as the above getLastValidated, except that this
     * will return the last validated date regardless of
     * the validator version.
     *
     * @param jarFile
     * @return
     */
    private long _getLastValidated(String jarFileURL) {
        long validated = 0L;
        try {
            conn = getConnection();

            PreparedStatement stmt = conn.prepareStatement(_selectValidated);
            stmt.setString(1, jarFileURL);

            ResultSet results = stmt.executeQuery();
            if (results.next()) {
                validated = results.getLong(1);
            }
        } catch (Exception e) {
            // TODO:1: Log something...
            //e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (Exception e) {
            }
        }
        return validated;
    }

    public void setLastValidated(String jarFile, long timeValidated) {
        try {
            conn = getConnection();
            File jar = FileUtils.getBase().getFile(jarFile);
            String jarFileURL = jar.toURL().toExternalForm();
            //System.out.println("[] setLastValidated "+jarFileURL );
            //System.out.println("        -- time "+timeValidated );
            PreparedStatement stmt = null;
            if (_getLastValidated(jarFileURL) != 0L) {
                stmt = conn.prepareStatement(_updateValidated);
                stmt.setLong(1, timeValidated);
                stmt.setString(2, getVersion());
                stmt.setString(3, jarFileURL);
            } else {
                stmt = conn.prepareStatement(_insertValidated);
                stmt.setString(1, jarFileURL);
                stmt.setLong(2, timeValidated);
                stmt.setString(3, getVersion());
            }

            stmt.executeUpdate();
        } catch (Exception e) {
            // TODO:1: Log something...
            //e.printStackTrace();
        } finally {
            try {
                conn.close();
            } catch (Exception e) {
            }
        }
    }

    private String version = null;

    private String getVersion() {
        if (version == null) {
            /*
             * Output startup message
             */
            Properties versionInfo = new Properties();

            try {
                JarUtils.setHandlerSystemProperty();
                versionInfo.load(new URL("resource:/openejb-version.properties").openConnection().getInputStream());
            } catch (java.io.IOException e) {
            }
            version = (String) versionInfo.get("version");
        }
        return version;
    }
}
