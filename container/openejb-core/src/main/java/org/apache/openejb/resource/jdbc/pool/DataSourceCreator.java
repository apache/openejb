package org.apache.openejb.resource.jdbc.pool;

import org.apache.openejb.resource.XAResourceWrapper;

import javax.sql.DataSource;
import java.util.Properties;

// Look org.apache.openejb.resource.jdbc.pool.PoolDataSourceCreator
// it is the class to extend
// this interface is mainly so complicated
// to be able to use DBCP "as before"
// in fact all managed method are done through the previous abstraction
public interface DataSourceCreator {
    DataSource managed(String name, DataSource ds);
    DataSource poolManaged(String name, DataSource ds);
    DataSource pool(String name, DataSource ds);
    DataSource poolManagedWithRecovery(String name, XAResourceWrapper xaResourceWrapper, String driver, Properties properties);
    DataSource poolManaged(String name, String driver, Properties properties);
    DataSource pool(String name, String driver, Properties properties);

    void destroy(Object object) throws Throwable;
}
