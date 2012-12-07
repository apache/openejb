/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.config;

import static org.apache.openejb.util.JarExtractor.delete;

import java.util.Properties;
import java.io.File;
import java.io.IOException;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.Deployer;
import org.apache.openejb.cli.SystemExitException;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.OpenEjbVersion;

/**
 * UnDeploy EJB beans
 */
public class Undeploy {

    private static Messages messages = new Messages(Undeploy.class);

    private static final String defaultServerUrl = "ejbd://localhost:4201";

    public static void main(String[] args) throws SystemExitException {

        CommandLineParser parser = new PosixParser();

        // create the Options
        Options options = new Options();
        options.addOption(Undeploy.option("v", "version", "cmd.deploy.opt.version"));
        options.addOption(Undeploy.option("h", "help", "cmd.undeploy.opt.help")); // TODO this message doesn't exist
        options.addOption(Undeploy.option("s", "server-url", "url", "cmd.deploy.opt.server"));

        CommandLine line = null;
        try {
            // parse the command line arguments
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            Undeploy.help(options);
            throw new SystemExitException(-1);
        }

        if (line.hasOption("help")) {
            Undeploy.help(options);
            return;
        } else if (line.hasOption("version")) {
            OpenEjbVersion.get().print(System.out);
            return;
        }

        if (line.getArgList().size() == 0) {
            System.out.println("Must specify an module id.");
            help(options);
            return;
        }

        Properties p = new Properties();
        p.put(Context.INITIAL_CONTEXT_FACTORY, "org.apache.openejb.client.RemoteInitialContextFactory");

        String serverUrl = line.getOptionValue("server-url", defaultServerUrl);
        p.put(Context.PROVIDER_URL, serverUrl);

        Deployer deployer = null;
        try {
            InitialContext ctx = new InitialContext(p);
            deployer = (Deployer) ctx.lookup("openejb/DeployerBusinessRemote");
        } catch (javax.naming.ServiceUnavailableException e) {
            System.out.println(e.getCause().getMessage());
            System.out.println(Undeploy.messages.format("cmd.deploy.serverOffline"));
            throw new SystemExitException(-1);
        } catch (javax.naming.NamingException e) {
            System.out.println("DeployerEjb does not exist in server '" + serverUrl + "', check the server logs to ensure it exists and has not been removed.");
            throw new SystemExitException(-2);
        }

        int exitCode = 0;
        for (Object obj : line.getArgList()) {
            String moduleId = (String) obj;

            try {
                undeploy(moduleId, deployer);
            } catch (DeploymentTerminatedException e) {
                System.out.println(e.getMessage());
                exitCode++;
            } catch (UndeployException e) {
                System.out.println(messages.format("cmd.undeploy.failed", moduleId));
                e.printStackTrace(System.out);
                exitCode++;
            } catch (NoSuchApplicationException e) {
                // TODO make this message
                System.out.println(messages.format("cmd.undeploy.noSuchModule", moduleId));
                exitCode++;
            }
        }

        if (exitCode != 0){
            throw new SystemExitException(exitCode);
        }
    }

    public static void undeploy(String moduleId, Deployer deployer) throws UndeployException, NoSuchApplicationException, DeploymentTerminatedException {
        // Treat moduleId as a file path, and see if there is a matching app to undeploy
        undeploy(moduleId, new File(moduleId), deployer);
    }

    public static void undeploy(String moduleId, File file, Deployer deployer) throws UndeployException, NoSuchApplicationException, DeploymentTerminatedException {
        try {
            file = file.getCanonicalFile();
        } catch (IOException e) {
        }

        boolean undeployed = false;
        if (file != null) {
            String path = file.getAbsolutePath();
            try {
                deployer.undeploy(path);
                undeployed = true;
                moduleId = path;
                if (!delete(file)){
                    throw new DeploymentTerminatedException(messages.format("cmd.undeploy.cantDelete", file.getAbsolutePath()));
                }
            } catch (NoSuchApplicationException e) {
            }
        }

        // If that didn't work, undeploy using just the moduleId
        if (!undeployed) {
            deployer.undeploy(moduleId);
            System.out.println(messages.format("cmd.undeploy.nothingToDelete", moduleId));
        }

        // TODO make this message
        System.out.println(messages.format("cmd.undeploy.successful", moduleId));
    }

    private static void help(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("undeploy [options] <file> [<file>...]", "\n"+ Undeploy.i18n("cmd.undeploy.description"), options, "\n");
    }

    private static Option option(String shortOpt, String longOpt, String description) {
        return OptionBuilder.withLongOpt(longOpt).withDescription(Undeploy.i18n(description)).create(shortOpt);
    }

    private static Option option(String shortOpt, String longOpt, String argName, String description) {
        return OptionBuilder.withLongOpt(longOpt).withArgName(argName).hasArg().withDescription(Undeploy.i18n(description)).create(shortOpt);
    }

    private static String i18n(String key) {
        return Undeploy.messages.format(key);
    }


    public static class DeploymentTerminatedException extends Deploy.DeploymentTerminatedException {

        private static final long serialVersionUID = 1L;

        public DeploymentTerminatedException(String message) {
            super(message);
        }

        public DeploymentTerminatedException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
