package org.apache.tomee.dbcp;

import org.apache.openejb.resource.jdbc.pool.PoolDataSourceCreator;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.apache.tomcat.jdbc.pool.DataSourceFactory;

import javax.sql.DataSource;
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
        return new org.apache.tomcat.jdbc.pool.DataSource(DataSourceFactory.parsePoolProperties(converted));
    }

    @Override
    public boolean hasCreated(final Object object) {
        return object instanceof org.apache.tomcat.jdbc.pool.DataSource;
    }

    @Override
    public void destroy(final Object object) throws Throwable {
        ((org.apache.tomcat.jdbc.pool.DataSource) object).close(true);
    }

    private static class PropertiesReader extends DataSourceFactory {
        public static final String[] KEYS = ALL_PROPERTIES;
    }
}
