package org.apache.tomee.dbcp;

import org.apache.openejb.monitoring.LocalMBeanServer;
import org.apache.openejb.resource.jdbc.pool.PoolDataSourceCreator;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.apache.tomcat.jdbc.pool.DataSourceFactory;

import javax.management.ObjectName;
import javax.sql.DataSource;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

public class TomEEDataSourceCreator extends PoolDataSourceCreator {
    @Override
    public DataSource pool(final String name, final DataSource ds) {
        return new TomcatDbcpDataSource(ds);
    }

    @Override
    public DataSource pool(final String name, final String driver, final Properties properties) {
        final Properties converted = new Properties();
        converted.setProperty("name", name);
        // some compatibility with old dbcp style
        if (properties.containsKey("JdbcDriver")) {
            converted.setProperty("driverClassName", driver);
        }
        if (properties.containsKey("JdbcUrl")) {
            converted.setProperty("url", properties.getProperty("JdbcUrl"));
        }
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            for (String key : PropertiesReader.KEYS) {
                final String thisKey = entry.getKey().toString();
                if (key.equalsIgnoreCase(thisKey)) {
                    converted.put(key, entry.getValue());
                }
            }
        }
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

    @Override
    public boolean hasReallyCreated(final Object object) {
        return object instanceof org.apache.tomcat.jdbc.pool.DataSource
                || object instanceof TomcatDbcpDataSource;
    }

    @Override
    public void doDestroy(final DataSource object) throws Throwable {
        if (object instanceof TomcatDbcpDataSource) {
            ((TomcatDbcpDataSource) object).close();
        } else {
            org.apache.tomcat.jdbc.pool.DataSource ds = (org.apache.tomcat.jdbc.pool.DataSource) object;
            ds.close(true);
            ds.postDeregister();
        }
    }

    private static class PropertiesReader extends DataSourceFactory {
        public static final String[] KEYS = ALL_PROPERTIES;
    }
}
