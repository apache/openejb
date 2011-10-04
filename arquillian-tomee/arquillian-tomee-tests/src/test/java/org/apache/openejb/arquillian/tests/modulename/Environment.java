package org.apache.openejb.arquillian.tests.modulename;

/**
 * @version $Rev$ $Date$
 */
public interface Environment {
    String getReturnEmail();

    Integer getConnectionPool();

    Long getStartCount();

    Short getInitSize();

    Byte getTotalQuantity();

    Boolean getEnableEmail();

    Character getOptionDefault();
}