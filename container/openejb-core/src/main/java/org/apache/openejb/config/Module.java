package org.apache.openejb.config;

import org.apache.openejb.jee.bval.ValidationConfigType;

import java.util.HashMap;
import java.util.Map;

public class Module {
    private static int currentId = 1; // unique id to be able to bind something for each module in the jndi tree

    private ValidationConfigType validationConfig;
    private final Map<String, Object> altDDs = new HashMap<String, Object>();

    private String uniqueId;

        public Module() {
        uniqueId = Integer.toString(currentId++);
    }

    public ValidationConfigType getValidationConfig() {
        return validationConfig;
    }

    public void setValidationConfig(ValidationConfigType v) {
        validationConfig = v;
    }

    public Map<String, Object> getAltDDs() {
        return altDDs;
    }

    public String getUniqueId() {
        return uniqueId;
    }
}
