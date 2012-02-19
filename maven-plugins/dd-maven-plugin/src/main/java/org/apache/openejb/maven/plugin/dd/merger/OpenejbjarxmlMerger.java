package org.apache.openejb.maven.plugin.dd.merger;

import org.apache.maven.plugin.logging.Log;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.JaxbOpenejbJar3;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.maven.plugin.dd.Merger;

import java.io.BufferedInputStream;
import java.net.URL;

public class OpenejbjarxmlMerger implements Merger<OpenejbJar> {
    private final Log log;

    public OpenejbjarxmlMerger(final Log logger) {
        log = logger;
    }

    @Override
    public OpenejbJar merge(OpenejbJar reference, OpenejbJar toMerge) {
        new EnventriespropertiesMerger(log).merge(reference.getProperties(), toMerge.getProperties());

        for (EjbDeployment deployment : toMerge.getEjbDeployment()) {
            if (reference.getDeploymentsByEjbName().containsKey(deployment.getEjbName())) {
                log.warn("ejb deployement " + deployment.getEjbName() + " already present");
            } else {
                reference.addEjbDeployment(deployment);
            }
        }

        return reference;
    }

    @Override
    public OpenejbJar createEmpty() {
        return new OpenejbJar();
    }

    @Override
    public OpenejbJar read(URL url) {
        try {
            return JaxbOpenejbJar3.unmarshal(OpenejbJar.class, new BufferedInputStream(url.openStream()));
        } catch (Exception e) {
            return createEmpty();
        }
    }
}
