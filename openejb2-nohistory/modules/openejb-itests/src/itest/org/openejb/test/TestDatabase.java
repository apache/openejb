package org.openejb.test;

import java.sql.SQLException;
import java.util.Properties;

/**
 * 
 */
public interface TestDatabase {

    public void createEntityTable() throws java.sql.SQLException;

    public void dropEntityTable() throws java.sql.SQLException;

    public void createEntityExplicitePKTable() throws java.sql.SQLException;
    
    public void dropEntityExplicitePKTable() throws java.sql.SQLException;

    public void createAccountTable() throws java.sql.SQLException;

    public void dropAccountTable() throws java.sql.SQLException;

    public void createCMP2Model() throws SQLException;
    
    public void dropCMP2Model() throws SQLException;
    
    public void start() throws IllegalStateException;

    public void stop() throws IllegalStateException;

    public void init(Properties props) throws IllegalStateException;

}
