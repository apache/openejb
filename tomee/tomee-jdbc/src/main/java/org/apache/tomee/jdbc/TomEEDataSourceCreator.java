package org.apache.tomee.jdbc;

import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.resource.jdbc.pool.PoolDataSourceCreator;
import org.apache.tomcat.jdbc.pool.ConnectionPool;
import org.apache.tomcat.jdbc.pool.DataSourceFactory;
import org.apache.tomcat.jdbc.pool.PoolConfiguration;
import org.apache.tomcat.jdbc.pool.PoolProperties;

import javax.management.ObjectName;
import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

public class TomEEDataSourceCreator extends PoolDataSourceCreator {
    @Override
    public DataSource pool(final String name, final DataSource ds, Properties properties) {
        final Properties converted = new Properties();
        updateProperties(properties, converted, null);

        final PoolConfiguration config = build(PoolProperties.class, converted);
        config.setDataSource(ds);
        final ConnectionPool pool;
        try {
            pool = new ConnectionPool(config);
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        return build(TomEEDataSource.class, new TomEEDataSource(pool), properties);
    }

    @Override
    public DataSource pool(final String name, final String driver, final Properties properties) {
        final Properties converted = new Properties();
        converted.setProperty("name", name);
        // some compatibility with old dbcp style
        updateProperties(properties, converted, driver);
        final org.apache.tomcat.jdbc.pool.DataSource ds = new org.apache.tomcat.jdbc.pool.DataSource(DataSourceFactory.parsePoolProperties(converted));
        try { // just to force the pool to be created
            ds.getConnection().close();
        } catch (Throwable ignored) {
            // no-op
        }
        try {
            ds.preRegister(LocalMBeanServer.get(), new ObjectName("openejb", "name", name));
        } catch (Exception ignored) {
            // ignored
        }
        return ds;
    }

    private void updateProperties(final Properties properties, final Properties converted, final String driver) {
        if (driver != null) {
            converted.setProperty("driverClassName", driver);
        }
        if (properties.containsKey("JdbcUrl")) {
            converted.setProperty("url", properties.getProperty("JdbcUrl"));
        }
        if (properties.containsKey("user")) {
            converted.setProperty("username", properties.getProperty("user"));
        }
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            final String value = entry.getValue().toString().trim();
            if (!value.isEmpty()) {
                converted.put(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public void doDestroy(final DataSource object) throws Throwable {
        org.apache.tomcat.jdbc.pool.DataSource ds = (org.apache.tomcat.jdbc.pool.DataSource) object;
        ds.close(true);
        ds.postDeregister();
    }

    public static class TomEEDataSource extends org.apache.tomcat.jdbc.pool.DataSource {
        public TomEEDataSource(final ConnectionPool pool) {
            this.pool = pool;
        }
    }
}
