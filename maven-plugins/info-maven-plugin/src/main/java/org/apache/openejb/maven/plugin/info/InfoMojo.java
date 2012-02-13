package org.apache.openejb.maven.plugin.info;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.loader.IO;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * @goal add-info
 */
public class InfoMojo extends AbstractMojo {
    /**
     * @parameter expression="${project.build.outputDirectory}"
     * @required
     * @readonly
     */
    private File module;

    /**
     * @parameter expression="${project.artifactId}"
     * @required
     */
    private String id;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        getLog().info("creating module for " + module.getPath());
        final ConfigurationFactory configurationFactory = new ConfigurationFactory();
        try {
            OpenEJB.init(System.getProperties());

            final AppModule appModule = configurationFactory.loadApplication(Thread.currentThread().getContextClassLoader(), id, Arrays.asList(module));

            AppInfo info = configurationFactory.configureApplication(appModule);
            info = replacePath(info);
            info = replaceContainer(info);

            // TODO use an openejb constant for the path
            dump(new File(module, "openejb/app-info.xml"), info);
        } catch (OpenEJBException e) {
            throw new MojoFailureException("can't get the configuration", e);
        } catch (IOException e) {
            throw new MojoFailureException("can't write the configuration", e);
        }
    }

    private void dump(final File output, final AppInfo info) throws IOException, MojoFailureException {
        final File parent = output.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new MojoFailureException("can't create directory " + output.getParent());
        }

        // TODO: something else is surely better than java serialization!
        final OutputStream fos = new BufferedOutputStream(new FileOutputStream(output));
        final ObjectOutputStream oos = new ObjectOutputStream(fos);
        try {
            oos.writeObject(info);
        } finally {
            IO.close(fos);
        }
    }

    // TODO?
    private AppInfo replaceContainer(AppInfo appInfo) {
        return appInfo;
    }

    // TODO?
    private AppInfo replacePath(AppInfo info) {
        return info;
    }
}
