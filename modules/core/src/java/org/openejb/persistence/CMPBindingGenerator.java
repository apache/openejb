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
 * Originally part of o.o.n.deployment.RDBMS2CMPMappingHelper
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

import java.util.ArrayList;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openejb.persistence.jdbc.Binding;


// TODO: Reduce code duplication mess in the multiple methods
public class CMPBindingGenerator {
    /**
     * Map from field name to their slot number. This is used in SQL Generation
     */
    private final Map fieldMap;

    /**
     * Map from CMP Field name to RDBMS column name
     */
    private final Map typeMap;

    public final int OUTPUT_BINDINGS = 0;
    public final int INPUT_BINDINGS = 1;

    private static final Log log = LogFactory.getLog(CMPBindingGenerator.class);

    /**
     * @param fieldMap a map from field name to slot number
     * @param typeMap a map from field name to java type name (a String)
     */
    public CMPBindingGenerator(Map fieldMap, Map typeMap) {
        this.fieldMap = fieldMap;
        this.typeMap = typeMap;
        log.trace("Instantiated a " + this.getClass().getName());
    }

    /**
     * Utility method for automatically generating binding arrays for persistenceFactory queries
     *
     *
     * TODO: Evaluate whether anything extra needs to be done to handle CMR
     *
     * @param outputFields  An array of strings of the fields for the select clause
     * @param inputFields  An array of strings of the fields for the where clause
     *
     */
    public ArrayList bindQuery(String[] outputFields, String[] inputFields) throws Exception {
        return bindQuery(outputFields, inputFields, false);
    }

    /**
     * Utility method for automatically generating binding arrays for persistenceFactory queries
     *
     *
     * TODO: Evaluate whether anything extra needs to be done to handle CMR
     *
     * @param outputFields  An array of strings of the fields for the select clause
     * @param inputFields  An array of strings of the fields for the where clause
     * @param isEJBSelect   Indicates if this is an EJB Select method; if so do special op
     * @return An arraylist of Binding[] objects - 1 = input 0 = output
     *
     */
    public ArrayList bindQuery(String[] outputFields, String[] inputFields, boolean isEJBSelect) throws Exception {
        ArrayList bindings = new ArrayList();

        // Binding for Input values
        Binding[] inputBindings = new Binding[inputFields.length];

        // Binding for Output values
        Binding[] outputBindings = new Binding[outputFields.length];

        // Iterate over outputFields and setup bindings
        for (int i = 0; i < outputFields.length; i++) {
            String tField = outputFields[i];
            String tReturn = (String) typeMap.get(tField);
            if (tReturn.equals(null)) {
                throw new IllegalArgumentException("Cannot map field " + tField + "; no Java Type Defined in typeMap");
            }

            // get a binding object back; get slot from fieldMap
            if (isEJBSelect) {
                /*
                 * It *APPEARS*, and I could be wrong, that ejbSelect methods handle result sets and tuples differently...
                 * hence, we use the actual selectField binding rather than a slot.
                 * Appears to be since we select a value, and not an EJB....
                 * We haven't CREATED an EJB here - we're working off the home interface grabbing a value...
                 */
                outputBindings[i] = BindingFactory.getBinding(tReturn, i + 1, i);
            } else {
                outputBindings[i] = BindingFactory.getBinding(tReturn, i + 1, ((Integer) fieldMap.get(tField)).intValue());
            }

            log.debug("Created an output binding object of type '" + outputBindings[i].getClass().getName() + "'");
        }

        bindings.add(OUTPUT_BINDINGS, outputBindings);

        // Iterate over the inputFields and setup bindings
        for (int n = 0; n < inputFields.length; n++) {
            String tField = inputFields[n];
            String tReturn = (String) typeMap.get(tField);
            if (tReturn.equals(null)) {
                throw new IllegalArgumentException("Cannot map field " + tField + "; no Java Type Defined in typeMap");
            }

            log.trace("Mapped field '" + tField + "' to type '" + tReturn + "'");
            // get a binding object back; get slot from fieldMap
            if (isEJBSelect) {
                /*
                 * It *APPEARS*, and I could be wrong, that ejbSelect methods handle result sets and tuples differently...
                 * hence, we use the actual selectField binding rather than a slot.
                 * Appears to be since we select a value, and not an EJB....
                 * We haven't CREATED an EJB here - we're working off the home interface grabbing a value...
                 */
                inputBindings[n] = BindingFactory.getBinding(tReturn, n + 1, n);
            } else {
                inputBindings[n] = BindingFactory.getBinding(tReturn, n + 1, ((Integer) fieldMap.get(tField)).intValue());
            }

            log.trace("Created an input binding object of type '" + inputBindings[n].getClass().getName() + "'");
        }

        bindings.add(INPUT_BINDINGS, inputBindings);
        return bindings;
    }

    /**
     * Utility method for automatically generating binding arrays for persistenceFactory queries
     *
     *
     * TODO: Evaluate whether anything extra needs to be done to handle CMR
     *
     * @param inputFields  An array of strings of the fields for the where clause
     *
     */
    public Binding[] bindUpdate(String[] inputFields) throws Exception {
        // Binding for Input values
        Binding[] inputBindings = new Binding[inputFields.length];

        // Iterate over the inputFields and setup bindings
        for (int n = 0; n < inputFields.length; n++) {
            String tField = inputFields[n];
            String tReturn = (String) typeMap.get(tField);
            if (tReturn.equals(null)) {
                throw new IllegalArgumentException("Cannot map field " + tField + "; no Java Type Defined in typeMap");
            }

            log.trace("Mapped field '" + tField + "' to type '" + tReturn + "'");
            inputBindings[n] = BindingFactory.getBinding(tReturn, n + 1, ((Integer) fieldMap.get(tField)).intValue());

            log.trace("Created an input binding object of type '" + inputBindings[n].getClass().getName() + "'");
        }

        return inputBindings;
    }
}