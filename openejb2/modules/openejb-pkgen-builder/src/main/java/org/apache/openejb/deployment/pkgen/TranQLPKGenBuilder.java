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
package org.apache.openejb.deployment.pkgen;

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
import org.apache.openejb.entity.cmp.PrimaryKeyGeneratorWrapperGBean;
import org.apache.openejb.xbeans.pkgen.EjbAutoIncrementTableType;
import org.apache.openejb.xbeans.pkgen.EjbCustomGeneratorType;
import org.apache.openejb.xbeans.pkgen.EjbKeyGeneratorType;
import org.apache.openejb.xbeans.pkgen.EjbSequenceTableType;
import org.apache.openejb.xbeans.pkgen.EjbSqlGeneratorType;
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
