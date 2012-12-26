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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.cli.SystemExitException;
import org.apache.openejb.config.rules.CheckAssemblyBindings;
import org.apache.openejb.config.rules.CheckAsynchronous;
import org.apache.openejb.config.rules.CheckCallbacks;
import org.apache.openejb.config.rules.CheckCdiEnabled;
import org.apache.openejb.config.rules.CheckClasses;
import org.apache.openejb.config.rules.CheckDependsOn;
import org.apache.openejb.config.rules.CheckDescriptorLocation;
import org.apache.openejb.config.rules.CheckInjectionPointUsage;
import org.apache.openejb.config.rules.CheckInjectionTargets;
import org.apache.openejb.config.rules.CheckMethods;
import org.apache.openejb.config.rules.CheckPersistenceRefs;
import org.apache.openejb.config.rules.CheckRestMethodArePublic;
import org.apache.openejb.config.rules.CheckUserTransactionRefs;
import org.apache.openejb.config.rules.CheckAnnotations;
import org.apache.openejb.config.rules.CheckIncorrectPropertyNames;
import org.apache.openejb.config.rules.ValidationBase;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.OpenEjbVersion;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @version $Rev$ $Date$
 */
public class AppValidator {

    protected static final Messages _messages = new Messages("org.apache.openejb.config.rules");

    int LEVEL = 2;
    boolean PRINT_XML = false;
    boolean PRINT_WARNINGS = true;
    boolean PRINT_COUNT = false;

    private List<ValidationResults> sets = new ArrayList<ValidationResults>();
    private ValidationBase[] additionalValidators;

    /*------------------------------------------------------*/
    /*    Constructors                                      */
    /*------------------------------------------------------*/
    public AppValidator() throws OpenEJBException {
    }

    public AppValidator(int LEVEL, boolean PRINT_XML, boolean PRINT_WARNINGS, boolean PRINT_COUNT) {
        this.LEVEL = LEVEL;
        this.PRINT_XML = PRINT_XML;
        this.PRINT_WARNINGS = PRINT_WARNINGS;
        this.PRINT_COUNT = PRINT_COUNT;
    }

    public AppValidator(final ValidationBase... additionalValidator) {
        additionalValidators = additionalValidator;
    }

    public void addValidationResults(ValidationResults set) {
        sets.add(set);
    }

    public ValidationResults[] getValidationResultsSets() {
        ValidationResults[] ejbSets = new ValidationResults[sets.size()];
        return sets.toArray(ejbSets);
    }

    // START SNIPPET : code2
    public AppModule validate(final AppModule appModule) {
        final ClassLoader loader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(appModule.getClassLoader()); // be sure to not mix classloaders
        try {
            ValidationRule[] rules = getValidationRules();
            for (int i = 0; i < rules.length; i++) {
                rules[i].validate(appModule);
            }
        } catch (Throwable e) {
            e.printStackTrace(System.out);
            ValidationError err = new ValidationError("cannot.validate");
            err.setCause(e);
            err.setDetails(e.getMessage());
            appModule.getValidation().addError(err);
        } finally {
            Thread.currentThread().setContextClassLoader(loader);
        }
        return appModule;
    }

    // END SNIPPET : code2
// START SNIPPET : code1
    protected ValidationRule[] getValidationRules() {
        // we don't want CheckClassLoading in standalone mode since it doesn't mean anything
        final ValidationRule[] defaultRules = new ValidationRule[]{
                new CheckClasses(),
                new CheckMethods(),
                new CheckCallbacks(),
                new CheckAssemblyBindings(),
                new CheckInjectionTargets(),
                new CheckInjectionPointUsage(),
                new CheckPersistenceRefs(),
                new CheckDependsOn(),
                new CheckUserTransactionRefs(),
                new CheckAsynchronous(),
                new CheckDescriptorLocation(),
                new CheckAnnotations(),
                new CheckIncorrectPropertyNames(),
                new CheckRestMethodArePublic(),
                new CheckCdiEnabled()
        };
        if (additionalValidators == null || additionalValidators.length == 0) {
            return defaultRules;
        }

        final ValidationRule[] rules = new ValidationRule[additionalValidators.length + defaultRules.length];
        System.arraycopy(additionalValidators, 0, rules, 0, additionalValidators.length);
        System.arraycopy(defaultRules, 0, rules, additionalValidators.length, defaultRules.length);
        return rules;
    }

    // END SNIPPET : code1
    public void printResults(ValidationResults set) {
        if (!set.hasErrors() && !set.hasFailures() && (!PRINT_WARNINGS || !set.hasWarnings())) {
            return;
        }
        System.out.println("------------------------------------------");
        System.out.println("JAR " + set.getName());
        System.out.println("                                          ");

        printValidationExceptions(set.getErrors());
        printValidationExceptions(set.getFailures());

        if (PRINT_WARNINGS) {
            printValidationExceptions(set.getWarnings());
        }
    }

    protected void printValidationExceptions(ValidationException[] exceptions) {
        for (int i = 0; i < exceptions.length; i++) {
            System.out.print(" ");
            System.out.print(exceptions[i].getPrefix());
            System.out.print(" ... ");
            if (!(exceptions[i] instanceof ValidationError)) {
                System.out.print(exceptions[i].getComponentName());
                System.out.print(": ");
            }
            if (LEVEL > 2) {
                System.out.println(exceptions[i].getMessage(1));
                System.out.println();
                System.out.print('\t');
                System.out.println(exceptions[i].getMessage(LEVEL));
                System.out.println();
            } else {
                System.out.println(exceptions[i].getMessage(LEVEL));
            }
        }
        if (PRINT_COUNT && exceptions.length > 0) {
            System.out.println();
            System.out.print(" " + exceptions.length + " ");
            System.out.println(exceptions[0].getCategory());
            System.out.println();
        }

    }

    public void printResultsXML(ValidationResults set) {
        if (!set.hasErrors() && !set.hasFailures() && (!PRINT_WARNINGS || !set.hasWarnings())) {
            return;
        }

        System.out.println("<jar>");
        System.out.print("  <path>");
        System.out.print(set.getName());
        System.out.println("</path>");

        printValidationExceptionsXML(set.getErrors());
        printValidationExceptionsXML(set.getFailures());

        if (PRINT_WARNINGS) {
            printValidationExceptionsXML(set.getWarnings());
        }
        System.out.println("</jar>");
    }

    protected void printValidationExceptionsXML(ValidationException[] exceptions) {
        for (int i = 0; i < exceptions.length; i++) {
            System.out.print("    <");
            System.out.print(exceptions[i].getPrefix());
            System.out.println(">");
            if (!(exceptions[i] instanceof ValidationError)) {
                System.out.print("      <ejb-name>");
                System.out.print(exceptions[i].getComponentName());
                System.out.println("</ejb-name>");
            }
            System.out.print("      <summary>");
            System.out.print(exceptions[i].getMessage(1));
            System.out.println("</summary>");
            System.out.println("      <description><![CDATA[");
            System.out.println(exceptions[i].getMessage(3));
            System.out.println("]]></description>");
            System.out.print("    </");
            System.out.print(exceptions[i].getPrefix());
            System.out.println(">");
        }
    }

    public void displayResults(ValidationResults[] sets) {
        if (PRINT_XML) {
            System.out.println("<results>");
            for (int i = 0; i < sets.length; i++) {
                printResultsXML(sets[i]);
            }
            System.out.println("</results>");
        } else {
            for (int i = 0; i < sets.length; i++) {
                printResults(sets[i]);
            }
            for (int i = 0; i < sets.length; i++) {
                if (sets[i].hasErrors() || sets[i].hasFailures()) {
                    if (LEVEL < 3) {
                        System.out.println();
                        System.out.println("For more details, use the -vvv option");
                    }
                    i = sets.length;
                }
            }
        }
    }

    public static void main(String[] args) throws SystemExitException {
        CommandLineParser parser = new PosixParser();

        // create the Options
        Options options = new Options();
        options.addOption(AppValidator.option("v", "version", "cmd.validate.opt.version"));
        options.addOption(AppValidator.option("h", "help", "cmd.validate.opt.help"));

        CommandLine line = null;
        try {
            line = parser.parse(options, args);
        } catch (ParseException exp) {
            AppValidator.help(options);
            throw new SystemExitException(-1);
        }

        if (line.hasOption("help")) {
            AppValidator.help(options);
            return;
        } else if (line.hasOption("version")) {
            OpenEjbVersion.get().print(System.out);
            return;
        }

        if (line.getArgList().size() == 0) {
            System.out.println("Must specify an module id.");
            AppValidator.help(options);
        }

        DeploymentLoader deploymentLoader = new DeploymentLoader();

        try {
            AppValidator validator = new AppValidator();
            for (Object obj : line.getArgList()) {
                String module = (String) obj;
                File file = new File(module);
                AppModule appModule = deploymentLoader.load(file);
                validator.validate(appModule);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void help(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("validate [options] <file> [<file>...]", "\n" + AppValidator.i18n("cmd.validate.description"), options, "\n");
    }

    private static Option option(String shortOpt, String longOpt, String description) {
        return OptionBuilder.withLongOpt(longOpt).withDescription(AppValidator.i18n(description)).create(shortOpt);
    }

    private static Option option(String shortOpt, String longOpt, String argName, String description) {
        return OptionBuilder.withLongOpt(longOpt).withArgName(argName).hasArg().withDescription(AppValidator.i18n(description)).create(shortOpt);
    }

    private static String i18n(String key) {
        return AppValidator._messages.format(key);
    }
}
