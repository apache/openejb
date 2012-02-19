package org.apache.openejb.maven.plugin.embedded;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import javax.ejb.embeddable.EJBContainer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

/**
 * @goal run
 * @phase compile
 */
public class OpenEJBEmbeddedMojo extends AbstractMojo {
    /**
     * @parameter expression="${project.artifactId}"
     * @required
     */
    private String id;

    /**
     * @parameter expression="${embedded.provider}" default-value="org.apache.openejb.OpenEjbContainer"
     * @required
     */
    private String provider;

    /**
     * @parameter expression="${embedded.modules}" default-value="${project.build.outputDirectory}"
     * @required
     */
    private String modules;

    /**
     * @parameter expression="${embedded.await}" default-value="true"
     * @required
     */
    private boolean await;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final EJBContainer container = EJBContainer.createEJBContainer(map());
        if (await) {
            final CountDownLatch latch = new CountDownLatch(1);
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    latch.countDown();
                }
            }));
            try {
                latch.await();
            } catch (InterruptedException e) {
                // ignored
            }
        }
        container.close();
    }

    private Map<?, ?> map() {
        final Map<String, Object> map = new HashMap<String, Object>();
        map.put(EJBContainer.APP_NAME, id);
        map.put(EJBContainer.PROVIDER, provider);
        map.put(EJBContainer.MODULES, modules.split(","));
        return map;
    }
}
