package org.apache.openejb.assembler.classic;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author Romain Manni-Bucau
 */
public class ValidationInfo extends InfoObject {
    public String providerClassName;
    public String messageInterpolatorClass;
    public String traversableResolverClass;
    public String constraintFactoryClass;
    public final Properties propertyTypes = new Properties();
    public final List<String> constraintMappings = new ArrayList<String>();
}
