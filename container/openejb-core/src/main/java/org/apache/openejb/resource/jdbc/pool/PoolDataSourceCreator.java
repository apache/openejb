package org.apache.openejb.resource.jdbc.pool;

import org.apache.openejb.OpenEJB;
import org.apache.openejb.resource.TransactionManagerWrapper;
import org.apache.openejb.resource.XAResourceWrapper;
import org.apache.openejb.resource.jdbc.managed.local.ManagedDataSource;
import org.apache.openejb.resource.jdbc.managed.xa.ManagedXADataSource;

import javax.sql.DataSource;
import javax.sql.XADataSource;
import javax.transaction.TransactionManager;
import java.util.Properties;

public abstract class PoolDataSourceCreator implements DataSourceCreator {
    @Override
    public DataSource managed(final String name, final DataSource ds) {
        final TransactionManager transactionManager = OpenEJB.getTransactionManager();
        if (ds instanceof XADataSource) {
            return new ManagedXADataSource(ds, transactionManager);
        }
        return new ManagedDataSource(ds, transactionManager);
    }

    @Override
    public DataSource poolManagedWithRecovery(final String name, final XAResourceWrapper xaResourceWrapper, final String driver, final Properties properties) {
        final TransactionManager transactionManager = new TransactionManagerWrapper(OpenEJB.getTransactionManager(), name, xaResourceWrapper);
        final DataSource ds = pool(name, driver, properties);
        if (ds instanceof XADataSource) {
            return new ManagedXADataSource(ds, transactionManager);
        }
        return new ManagedDataSource(ds, transactionManager);
    }

    @Override
    public DataSource poolManaged(final String name, final DataSource ds) {
        return managed(name, pool(name, ds));
    }

    @Override
    public DataSource poolManaged(final String name, final String driver, final Properties properties) {
        return managed(name, pool(name, driver, properties));
    }

    @Override
    public void destroy(final Object object) throws Throwable {
        if (object instanceof ManagedDataSource) {
            doDestroy(((ManagedDataSource) object).getDelegate());
        } else {
            doDestroy((DataSource) object);
        }
    }

    protected abstract void doDestroy(DataSource dataSource) throws Throwable;
}
