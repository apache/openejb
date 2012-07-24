/*
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
package org.apache.openejb.resource.jdbc;

import org.apache.openejb.loader.IO;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.resource.XAResourceWrapper;
import org.apache.openejb.resource.jdbc.pool.DataSourceCreator;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.xbean.recipe.ObjectRecipe;
import org.apache.xbean.recipe.Option;

import javax.sql.DataSource;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @version $Rev$ $Date$
 */
public class DataSourceFactory {
    private static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB, DataSourceFactory.class);

    public static final String POOL_PROPERTY = "openejb.datasource.pool";
    public static final String DATA_SOURCE_CREATOR_PROP = "DataSourceCreator";

    private static final Map<DataSource, DataSourceCreator> creatorByDataSource = new HashMap<DataSource, DataSourceCreator>();

    public static DataSource create(final String name, final boolean managed, final Class impl, final String definition) throws IllegalAccessException, InstantiationException, IOException {
        final Properties properties = asProperties(definition);
        final DataSourceCreator creator = creator(properties);


        final DataSource ds;
        if (createDataSourceFromClass(impl)) { // opposed to "by driver"
            trimNotSupportedDataSourceProperties(properties);

            final ObjectRecipe recipe = new ObjectRecipe(impl);
            recipe.allow(Option.CASE_INSENSITIVE_PROPERTIES);
            recipe.allow(Option.IGNORE_MISSING_PROPERTIES);
            recipe.allow(Option.NAMED_PARAMETERS);
            recipe.setAllProperties(properties);

            final DataSource dataSource = (DataSource) recipe.create();

            if (managed) {
                if (useDbcp(properties)) {
                    ds = creator.poolManaged(name, dataSource);
                } else {
                    ds = creator.managed(name, dataSource);
                }
            } else {
                if (useDbcp(properties)) {
                    ds = creator.pool(name, dataSource);
                } else {
                    ds = dataSource;
                }
            }
        } else { // by driver
            if (managed) {
                final XAResourceWrapper xaResourceWrapper = SystemInstance.get().getComponent(XAResourceWrapper.class);
                if (xaResourceWrapper != null) {
                    ds = creator.poolManagedWithRecovery(name, xaResourceWrapper, impl.getName(), properties);
                } else {
                    ds = creator.poolManaged(name, impl.getName(), properties);
                }
            } else {
                ds = creator.pool(name, impl.getName(), properties);
            }
        }

        creatorByDataSource.put(ds, creator);
        return ds;
    }

    private static DataSourceCreator creator(final Properties properties) {
        final DataSourceCreator defaultCreator = SystemInstance.get().getComponent(DataSourceCreator.class);
        Object creatorName = properties.remove(DATA_SOURCE_CREATOR_PROP);
        if (creatorName != null && creatorName instanceof String && !creatorName.equals(defaultCreator.getClass().getName())) {
            try {
                return (DataSourceCreator) Thread.currentThread().getContextClassLoader().loadClass((String) creatorName).newInstance();
            } catch (Throwable e) {
                LOGGER.error("can't create '" + creatorName + "', the default one will be used: " + defaultCreator, e);
            }
        }
        return defaultCreator;
    }

    private static boolean createDataSourceFromClass(final Class<?> impl) {
        return DataSource.class.isAssignableFrom(impl) && !SystemInstance.get().getOptions().get("org.apache.openejb.resource.jdbc.hot.deploy", false);
    }

    private static boolean useDbcp(final Properties properties) {
        return "true".equalsIgnoreCase(properties.getProperty(POOL_PROPERTY, "true"));
    }

    private static Properties asProperties(final String definition) throws IOException {
        return IO.readProperties(IO.read(definition), new Properties());
    }

    public static void trimNotSupportedDataSourceProperties(Properties properties) {
        properties.remove("LoginTimeout");
    }

    public static boolean canBeDestroyed(final Object object) {
        if (!(object instanceof DataSource)) {
            return false;
        }
        return creatorByDataSource.containsKey(object);
    }

    public static void destroy(final Object o) throws Throwable {
        creatorByDataSource.remove(o).destroy(o);
    }
}
