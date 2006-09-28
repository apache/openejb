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
package org.openejb.deployment.pkgen;

import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.geronimo.gbean.AbstractNameQuery;
import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.GBeanAlreadyExistsException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.openejb.entity.cmp.PrimaryKeyGeneratorWrapperGBean;
import org.openejb.xbeans.pkgen.EjbAutoIncrementTableType;
import org.openejb.xbeans.pkgen.EjbCustomGeneratorType;
import org.openejb.xbeans.pkgen.EjbKeyGeneratorType;
import org.openejb.xbeans.pkgen.EjbSequenceTableType;
import org.openejb.xbeans.pkgen.EjbSqlGeneratorType;
import org.tranql.pkgenerator.AutoIncrementTablePrimaryKeyGenerator;
import org.tranql.pkgenerator.PrimaryKeyGenerator;
import org.tranql.pkgenerator.PrimaryKeyGeneratorDelegate;
import org.tranql.pkgenerator.SQLPrimaryKeyGenerator;
import org.tranql.pkgenerator.SequenceTablePrimaryKeyGenerator;
import org.tranql.ql.QueryBindingImpl;
import org.tranql.ql.QueryException;
import org.tranql.sql.jdbc.binding.BindingFactory;

/**
 * Process the key-generator element in a deployment descriptor, and create
 * a matching PrimaryKeyGenerator object.  This is a common utility which
 * can be invoked by the builder of any other schema which includes nested
 * key-generator elements.
 *
 * This implementation generates TranQL PrimaryKeyGenerator classes, and
 * likely would not work with a different CMP engine.
 *
 * Note that this class caches certain data, so it's best to reuse one
 * instance instead of creating a separate instance for each EJB that uses
 * a key generator.
 *
 * For the XML format, see the schema openejb-pkgen.xsd
 *
 * @version $Revision$ $Date$
 */
public class TranQLPKGenBuilder implements PKGenBuilder {
    private Map keyGenerators = new HashMap();

    /**
     * Creates a PrimaryKeyGenerator object from the XML and other input data.
     * Note that the PrimaryKeyGenerator is constructed at deployment time, but
     * may be stored (serialized) and later restored (deserialized) and run
     * without an additional deployment operation.  That means the key
     * generator does not hardcode access to a database, for example, but must
     * be prepared to acquire a new data source after deserialization.
     *
     * @param config The XMLBeans object tree representing the key-generator element in the XML document
     * @param tm The TransactionManager that the key generator should use, if needed
     * @param dataSource The JDBC data source that the key generator should use
     * @param pkClass The key generator should return IDs of this type
     * @param earContext Used by the key generator when it needs to register a GBean for any reason
     * @return The configured PrimaryKeyGenerator
     */
    public PrimaryKeyGenerator configurePKGenerator(EjbKeyGeneratorType config, TransactionManager tm, DataSource dataSource, Class pkClass, EARContext earContext) throws DeploymentException, QueryException {
        AbstractName baseName = earContext.getModuleName();
        //todo: Handle a PK Class with multiple fields?
        if(config.isSetCustomGenerator()) {
            EjbCustomGeneratorType custom = config.getCustomGenerator();
            String generatorName = custom.getGeneratorName().trim();
            PrimaryKeyGeneratorDelegate keyGeneratorDelegate = (PrimaryKeyGeneratorDelegate) keyGenerators.get(generatorName);
            if ( null == keyGeneratorDelegate ) {
                keyGeneratorDelegate = new PrimaryKeyGeneratorDelegate();
                GBeanData keyGenerator;
                try {
                    AbstractNameQuery generatorNameQuery = buildAbstractNameQuery(null, null, generatorName, NameFactory.KEY_GENERATOR);
                    AbstractName wrapperGeneratorObjectName = earContext.getNaming().createChildName(baseName, generatorName, "PKGenWrapper");
                    keyGenerator = new GBeanData(wrapperGeneratorObjectName, PrimaryKeyGeneratorWrapperGBean.GBEAN_INFO);
                    keyGenerator.setReferencePattern("PrimaryKeyGenerator", generatorNameQuery);
                    keyGenerator.setAttribute("primaryKeyGeneratorDelegate", keyGeneratorDelegate);
                } catch (Exception e) {
                    throw new DeploymentException("Unable to initialize PrimaryKeyGeneratorWrapper GBean", e);
                }
                try {
                    earContext.addGBean(keyGenerator);
                } catch (GBeanAlreadyExistsException e) {
                    throw new DeploymentException("Could not add pk generator wrapper to context", e);
                }

                keyGenerators.put(generatorName, keyGeneratorDelegate);
            }
            return keyGeneratorDelegate;
        } else if(config.isSetSqlGenerator()) {
            EjbSqlGeneratorType sqlGen = config.getSqlGenerator();
            String sql = sqlGen.getSql();
            return new SQLPrimaryKeyGenerator(dataSource, sql, BindingFactory.getResultBinding(1, new QueryBindingImpl(0, pkClass)));
        } else if(config.isSetSequenceTable()) {
            EjbSequenceTableType seq = config.getSequenceTable();
            String tableName = seq.getTableName();
            String sequenceName = seq.getSequenceName();
            int batchSize = seq.getBatchSize();
            return new SequenceTablePrimaryKeyGenerator(tm, dataSource, tableName, sequenceName, batchSize);
        } else if(config.isSetAutoIncrementTable()) {
            EjbAutoIncrementTableType auto = config.getAutoIncrementTable();
            String sql = auto.getSql();
            return new AutoIncrementTablePrimaryKeyGenerator(dataSource, sql, BindingFactory.getResultBinding(1, new QueryBindingImpl(0, pkClass)));
//        } else if(config.isSetDatabaseGenerated()) {
            //todo: need to somehow implement this behavior in TranQL
        }
        throw new UnsupportedOperationException("Not implemented");
    }

    //copied from  ENCConfigBuilder, which is NOT where it should be
    public static AbstractNameQuery buildAbstractNameQuery(Artifact configId, String module, String name, String type) {
        Map nameMap = new HashMap();
        nameMap.put("name", name);
        if (type != null) {
            nameMap.put("j2eeType", type);
        }
        if (module != null) {
            nameMap.put("module", module);
        }
        return new AbstractNameQuery(configId, nameMap);
    }
}
