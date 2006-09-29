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
package org.apache.openejb.entity.cmp.pkgenerator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.connector.outbound.ManagedConnectionFactoryWrapper;
import org.apache.geronimo.gbean.GBeanLifecycle;
import org.tranql.cache.CacheRow;
import org.tranql.cache.DuplicateIdentityException;
import org.tranql.cache.InTxCache;
import org.tranql.identity.GlobalIdentity;
import org.tranql.pkgenerator.AutoIncrementTablePrimaryKeyGenerator;
import org.tranql.pkgenerator.PrimaryKeyGenerator;
import org.tranql.pkgenerator.PrimaryKeyGeneratorException;
import org.tranql.ql.QueryBindingImpl;
import org.tranql.sql.jdbc.binding.BindingFactory;

import javax.sql.DataSource;

/**
 * @version $Revision$ $Date$
 */
public class AutoIncrementTablePrimaryKeyGeneratorWrapper implements PrimaryKeyGenerator, GBeanLifecycle {
    private static final Log log = LogFactory.getLog(AutoIncrementTablePrimaryKeyGeneratorWrapper.class);

    private final ManagedConnectionFactoryWrapper connectionFactoryWrapper;
    private final String sql;
    private final Class returnType;
    private PrimaryKeyGenerator delegate;

    public AutoIncrementTablePrimaryKeyGeneratorWrapper(ManagedConnectionFactoryWrapper connectionFactoryWrapper, String sql, Class returnType) {
        this.connectionFactoryWrapper = connectionFactoryWrapper;
        this.sql = sql;
        this.returnType = returnType;
    }

    public void doStart() throws Exception {
        DataSource dataSource = (DataSource) connectionFactoryWrapper.$getResource();
        delegate = new AutoIncrementTablePrimaryKeyGenerator(dataSource, sql, BindingFactory.getResultBinding(1, new QueryBindingImpl(0, returnType)));
    }

    public void doStop() throws Exception {
        delegate = null;
    }

    public void doFail() {
        delegate = null;
    }

    public Object getNextPrimaryKey(CacheRow cacheRow) throws PrimaryKeyGeneratorException {
        return delegate.getNextPrimaryKey(cacheRow);
    }

    public CacheRow updateCache(InTxCache cache, GlobalIdentity id, CacheRow cacheRow) throws DuplicateIdentityException {
        return delegate.updateCache(cache, id, cacheRow);
    }

}
