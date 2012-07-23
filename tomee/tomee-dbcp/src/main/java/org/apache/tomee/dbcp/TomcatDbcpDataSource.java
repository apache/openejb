package org.apache.tomee.dbcp;

import org.apache.openejb.resource.jdbc.DataSourceHelper;
import org.apache.openejb.util.reflection.Reflections;
import org.apache.tomcat.dbcp.dbcp.BasicDataSource;
import org.apache.tomcat.dbcp.dbcp.ConnectionFactory;
import org.apache.tomcat.dbcp.dbcp.DataSourceConnectionFactory;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class TomcatDbcpDataSource extends BasicDataSource {
    private final DataSource dataSource;

    public TomcatDbcpDataSource(final DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    protected ConnectionFactory createConnectionFactory() throws SQLException {
        return new DataSourceConnectionFactory(dataSource, username, password);
    }

    @Override
    public void setUrl(String url) {
        try {
            DataSourceHelper.setUrl(this, url);
        } catch (Throwable e1) {
            super.setUrl(url);
        }
    }

    // @Override JDK7
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return (Logger) Reflections.invokeByReflection(dataSource, "getParentLogger", new Class<?>[0], null);
    }
}
