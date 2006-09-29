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

import javax.sql.DataSource;
import javax.transaction.TransactionManager;

import org.tranql.pkgenerator.PrimaryKeyGenerator;
import org.tranql.ql.QueryException;
import org.apache.openejb.xbeans.pkgen.EjbKeyGeneratorType;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.common.DeploymentException;

/**
 * Process the key-generator element in a deployment descriptor, and create
 * a matching PrimaryKeyGenerator object.  This is a common utility which
 * can be invoked by the builder of any other schema which includes nested
 * key-generator elements.
 *
 * This is a placeholder interface in case other PK Generator implementations
 * become available.  If so, the return type and exception type would need
 * to be changed to not be a TranQL interface.
 *
 * @version $Revision$ $Date$
 */
public interface PKGenBuilder {
    PrimaryKeyGenerator configurePKGenerator(EjbKeyGeneratorType config, TransactionManager tm, DataSource dataSource, Class pkClass, EARContext earContext) throws DeploymentException, QueryException;
}
