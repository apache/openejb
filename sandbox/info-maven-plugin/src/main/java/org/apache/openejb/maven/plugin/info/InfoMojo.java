package org.apache.openejb.maven.plugin.info;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.PreconfiguredFactory;
import org.apache.openejb.loader.IO;
import org.apache.openejb.maven.util.MavenLogStreamFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.Arrays;

/**
 * @goal add-info
 * @phase compile
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
        final String path = module.getPath();
        getLog().info("creating module for " + path);

        System.setProperty("openejb.log.factory", "org.apache.openejb.maven.util.MavenLogStreamFactory");
        MavenLogStreamFactory.setLogger(getLog());

        final ConfigurationFactory configurationFactory = new ConfigurationFactory();
        FileWriter outputWriter = null;
        try {
            OpenEJB.init(System.getProperties());

            final AppModule appModule = configurationFactory.loadApplication(Thread.currentThread().getContextClassLoader(), id, Arrays.asList(module));

            AppInfo info = configurationFactory.configureApplication(appModule);
            info = replaceContainers(info);

            final StringWriter writer = new StringWriter();
            PreconfiguredFactory.dump(writer, info);
            final String xml = replacePath(path, writer.toString());

            final File output = new File(module, PreconfiguredFactory.APP_INFO_XML);
            final File parent = output.getParentFile();
            if (!parent.exists() && !parent.mkdirs()) {
                throw new OpenEJBException("can't create directory " + output.getParent());
            }
            outputWriter = new FileWriter(output);
            outputWriter.write(xml);

            getLog().info("dumped configuration in " + output.getPath());
        } catch (Exception e) {
            throw new MojoFailureException("can't get the configuration", e);
        } finally {
            IO.close(outputWriter);
        }
    }

    private String replacePath(final String path, final String s) {
        return s.replace(path, PreconfiguredFactory.APP_INFO_XML_PATH);
    }

    // TODO?
    private AppInfo replaceContainers(AppInfo appInfo) {
        return appInfo;
    }
}
