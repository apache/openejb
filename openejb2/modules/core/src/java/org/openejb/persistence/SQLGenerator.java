/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
 *    (http://openejb.org/).
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */

/**
 *
 * Originally o.o.n.deployment.RDBMS2CMPMappingHelper
 *
 * Second pass at code, based on FeedBack.
 * Moved class to o.o.n.persistence.SQLGenerator
 *
 * Simplified this class to ONLY generate SQL String
 * Moved binding generation to o.o.n.persistence.BindingFactory
 *
 * Cleaned up imports, exceptions and StringBuffer cacophony
 *
 * I'll integrate this, or something derived from this codebase into
 * GeronimoEjbJarLoader at a later date.
 *
 */
package org.openejb.persistence;

import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


// TODO: Reduce code duplication mess in the multiple methods

public class SQLGenerator {
    private static final Log log = LogFactory.getLog(SQLGenerator.class);

    private final String dbTable;

    /**
     * Map from field name to their slot number. This is used in SQL Generation
     * @todo this is not used so should be deleted
     */
    private final Map fieldMap;

    /**
     * Map from CMP Field name to RDBMS column name
     */
    private final Map columnMap;

    /**
     * @param fieldMap map from field name to slot number
     * @param dbTable The database table on which we are operating - used to create statements
     */
    public SQLGenerator(Map fieldMap, Map columnMap, String dbTable) {
        this.fieldMap = fieldMap;
        this.columnMap = columnMap;
        this.dbTable = dbTable;
        log.trace("Instantiated a " + this.getClass().getName() + " to assist on table " + this.dbTable);
    }

    /**
     * Utility method for automatically generating Querys into the persistenceFactory
     *
     * I'm generating what should be standard SQL; hopefully doesn't cause any vendor specific problems
     *
     * TODO: Evaluate whether anything extra needs to be done to handle CMR
     *
     * @param selectFields  An array of strings of the fields for the select clause
     * @param whereFields  An array of strings of the fields for the where clause
     *
     */
    public String createQuery(String[] selectFields, String[] whereFields) throws Exception {
        /* the SQL we will be generating  */
        StringBuffer sql = new StringBuffer();

        /* Begin SQL Statement; queries all start with SELECT ... */
        sql.append("SELECT ");
        /* Iterate over selectFields and selectTypes and process data */
        for (int i = 0; i < selectFields.length; i++) {
            String tField = selectFields[i];
            String tCol = (String) columnMap.get(tField);
            log.trace("SELECT field '" + tField + "' has been mapped to column '" + tCol + "'");

            /* Stupid loop to determine if we need a comma.  Can be optimised */
            if (i == 0) { // first column, just slap it in ... merge in SELECT here for simplicity/drop of extra StringBuffer call?
                sql.append(tCol);
            } else { // do comma delimitation for previous column
                sql.append(", ").append(tCol);
            }

        }

        //log.debug("After looping select fields, sql is now: " + sql);

        /* Append a FROM & WHERE clause (including our table!)... */
        sql.append(" FROM " + dbTable + " WHERE ");
        /* Iterate over the whereFields and process them */
        for (int n = 0; n < whereFields.length; n++) {
            String tField = whereFields[n];
            String tCol = (String) columnMap.get(tField);
            log.trace("WHERE field '" + tField + "' has been mapped to column '" + tCol + "'");

            /* Stupid loop to determine if we need a comma.  Can be optimised */
            if (n == 0) { // first column, just slap it in ... merge in FROM/WHERE, etc here for simplicity/drop of extra StringBuffer call?
                sql.append(tCol).append(" = ?");
            } else { // do comma delimitation for previous column
                sql.append(", ").append(tCol).append(" = ?");
            }

        }

        //log.debug("After looping where fields, sql is now: " + sql);

        log.debug("Final SQL for createQuery: " + sql);

        return sql.toString();

    }

    /**
     * Utility method for automatically generating Update statements via UPDATE into the persistenceFactory
     *
     * I'm generating what should be standard SQL; hopefully doesn't cause any vendor specific problems
     *
     * TODO: Verify CMPQuery objects aren't for Updates
     * TODO: Clean up the way inputParams are handled; it's sloppy at the moment
     *
     * @param setFields  An array of strings of the fields we will be updating/setting
     * @param whereFields  An array of strings of the fields for the where clause
     */

    public String createUpdate(String[] setFields, String[] whereFields) throws Exception {
        // the SQL we will be generating
        StringBuffer sql = new StringBuffer();


        // Begin SQL Statement; update dbTable blah blah blah
        sql.append("UPDATE ").append(dbTable).append(" SET ");
        // Iterate over selectFields and returnTypes and process data
        for (int i = 0; i < setFields.length; i++) {
            String tField = setFields[i];
            String tCol = (String) columnMap.get(tField);
            log.trace("SET field '" + tField + "' has been mapped to column '" + tCol + "'");

            /* Stupid loop to determine if we need a comma.  Can be optimised */
            if (i == 0) { // first column, just slap it in ... merge in SELECT here for simplicity/drop of extra StringBuffer call?
                sql.append(tCol).append(" = ?");
            } else { // do comma delimitation for previous column
                sql.append(" = ?, ").append(tCol);
            }

        }

        //log.debug("After looping SET fields, sql is now: " + sql);

        // Append a WHERE clause ...
        sql.append(" WHERE ");
        // Iterate over the whereFields and process them
        for (int n = 0; n < whereFields.length; n++) {
            String tField = whereFields[n];
            String tCol = (String) columnMap.get(tField);
            log.trace("WHERE field '" + tField + "' has been mapped to column '" + tCol + "'");

            // Stupid loop to determine if we need a comma.  Can be optimised
            if (n == 0) { // first column, just slap it in ... merge in FROM/WHERE, etc here for simplicity/drop of extra StringBuffer call?
                sql.append(tCol).append(" = ?");
            } else { // do comma delimitation for previous column
                sql.append(", ").append(tCol).append(" = ?");
            }

        }

        log.debug("Final SQL for createUpdate: " + sql);

        return sql.toString();
    }

    /**
     * Utility method for automatically generating Update statements via INSERT into the persistenceFactory
     *
     * I'm generating what should be standard SQL; hopefully doesn't cause any vendor specific problems
     *
     * TODO: Verify CMPQuery objects aren't for Updates
     * TODO: Clean up the way inputParams are handled; it's sloppy at the moment
     *
     * @param setFields  An array of strings of the fields we will be updating/setting
     */

    public String createInsert(String[] setFields) throws Exception {
        // the SQL we will be generating
        StringBuffer sql = new StringBuffer();

        // Create a second quick StringBuffer for the values column to prevent a double loop
        StringBuffer values = new StringBuffer();

        // Begin SQL Statement; INSERT INTO dbTable blah blah blah
        sql.append("INSERT INTO ").append(dbTable).append(" (");

        // Init the values buffer
        values.append(") VALUES (");
        // Iterate over selectFields and returnTypes and process data
        for (int i = 0; i < setFields.length; i++) {
            String tField = setFields[i];
            String tCol = (String) columnMap.get(tField);
            log.trace("SET field '" + tField + "' has been mapped to column '" + tCol + "'");

            // Stupid loop to determine if we need a comma.  Can be optimised
            if (i == 0) { // first column, just slap it in ... merge in SELECT here for simplicity/drop of extra StringBuffer call?
                sql.append(tCol);
                values.append("?");
            } else { // do comma delimitation for previous column
                sql.append(", ").append(tCol);
                values.append(", ?");
            }


        }

        values.append(")");
        sql.append(values);

        log.debug("Final SQL For createInsert: " + sql);

        return sql.toString();
    }


    /**
     * Utility method for automatically generating Update statements via DELETE into the persistenceFactory
     *
     * I'm generating what should be standard SQL; hopefully doesn't cause any vendor specific problems
     *
     * TODO: Verify CMPQuery objects aren't for Updates
     * TODO: Clean up the way inputParams are handled; it's sloppy at the moment
     *
     * @param whereFields  An array of strings of the fields we will be updating/setting
     */
    public String createDelete(String[] whereFields) throws Exception {
        // the SQL we will be generating
        StringBuffer sql = new StringBuffer();



        // Create a second quick StringBuffer for the values column to prevent a double loop
        StringBuffer values = new StringBuffer();

        // Begin SQL Statement; INSERT INTO dbTable blah blah blah
        sql.append("DELETE FROM " + dbTable + " WHERE ");
        // Iterate over selectFields and returnTypes and process data
        for (int i = 0; i < whereFields.length; i++) {
            String tField = whereFields[i];
            String tCol = (String) columnMap.get(tField);
            log.trace("WHERE field '" + tField + "' has been mapped to column '" + tCol + "'");

            // Stupid loop to determine if we need a comma.  Can be optimised
            if (i == 0) { // first column, just slap it in ... merge in SELECT here for simplicity/drop of extra StringBuffer call?
                sql.append(tCol).append(" = ?");
            } else { // do comma delimitation for previous column
                sql.append(", ").append(tCol).append(" = ?");
            }
        }

        sql.append(values);
        log.debug("Final SQL for createDelete: " + sql);

        return sql.toString();
    }
}
