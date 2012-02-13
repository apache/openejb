package org.apache.openejb.maven.plugin.info;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.openejb.OpenEJB;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.LogStream;
import org.apache.openejb.util.LogStreamFactory;

import java.io.File;
import java.io.IOException;
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
        getLog().info("creating module for " + module.getPath());

        System.setProperty("openejb.log.factory", MavenLogStreamFactory.class.getName());
        MavenLogStreamFactory.setLogger(getLog());

        final ConfigurationFactory configurationFactory = new ConfigurationFactory();
        try {
            OpenEJB.init(System.getProperties());

            final AppModule appModule = configurationFactory.loadApplication(Thread.currentThread().getContextClassLoader(), id, Arrays.asList(module));

            AppInfo info = configurationFactory.configureApplication(appModule);
            info = replacePath(info);
            info = replaceContainer(info);

            final File output = new File(module, ConfigurationFactory.APP_INFO_XML);
            configurationFactory.dump(output, info);
            getLog().info("dumped configuration in " + output.getPath());
        } catch (OpenEJBException e) {
            throw new MojoFailureException("can't get the configuration", e);
        } catch (IOException e) {
            throw new MojoFailureException("can't write the configuration", e);
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

    public static class MavenLogStreamFactory implements LogStreamFactory {
        private static Log logger;

        @Override
        public LogStream createLogStream(LogCategory logCategory) {
            return new MavenLogStream(logger);
        }

        public static void setLogger(Log logger) {
            MavenLogStreamFactory.logger = logger;
        }

        private static class MavenLogStream implements LogStream {
            private final Log log;

            public MavenLogStream(Log logger) {
                log = logger;
            }

            @Override
            public boolean isFatalEnabled() {
                return log.isErrorEnabled();
            }

            @Override
            public void fatal(String message) {
                log.error(message);
            }

            @Override
            public void fatal(String message, Throwable t) {
                log.error(message, t);
            }

            @Override
            public boolean isErrorEnabled() {
                return log.isErrorEnabled();
            }

            @Override
            public void error(String message) {
                log.error(message);
            }

            @Override
            public void error(String message, Throwable t) {
                log.error(message, t);
            }

            @Override
            public boolean isWarnEnabled() {
                return log.isWarnEnabled();
            }

            @Override
            public void warn(String message) {
                log.warn(message);
            }

            @Override
            public void warn(String message, Throwable t) {
                log.warn(message, t);
            }

            @Override
            public boolean isInfoEnabled() {
                return log.isInfoEnabled();
            }

            @Override
            public void info(String message) {
                log.info(message);
            }

            @Override
            public void info(String message, Throwable t) {
                log.info(message, t);
            }

            @Override
            public boolean isDebugEnabled() {
                return log.isDebugEnabled();
            }

            @Override
            public void debug(String message) {
                log.debug(message);
            }

            @Override
            public void debug(String message, Throwable t) {
                log.debug(message, t);
            }
        }
    }
}
