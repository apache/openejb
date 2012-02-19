package org.apache.openejb.maven.plugin.dd.merger;

import org.apache.maven.plugin.logging.Log;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.jee.bval.PropertyType;
import org.apache.openejb.jee.bval.ValidationConfigType;
import org.apache.openejb.maven.plugin.dd.Merger;

import javax.xml.bind.JAXBElement;
import java.io.BufferedInputStream;
import java.net.URL;

public class ValidationxmlMerger implements Merger<ValidationConfigType> {
    private final Log log;

    public ValidationxmlMerger(final Log logger) {
        log = logger;
    }

    @Override
    public ValidationConfigType merge(final ValidationConfigType reference, final ValidationConfigType toMerge) {
        for (PropertyType property : toMerge.getProperty()) {
            if (reference.getProperty().contains(property)) {
                log.warn("property " + property.getName() + " already present");
            } else {
                reference.getProperty().add(property);
            }
        }

        for (JAXBElement<String> elt : toMerge.getConstraintMapping()) {
            reference.getConstraintMapping().add(elt);
        }

        return reference;
    }

    @Override
    public ValidationConfigType createEmpty() {
        return new ValidationConfigType();
    }

    @Override
    public ValidationConfigType read(URL url) {
        try {
            return JaxbOpenejb.unmarshal(ValidationConfigType.class, new BufferedInputStream(url.openStream()));
        } catch (Exception e) {
            return createEmpty();
        }
    }
}
