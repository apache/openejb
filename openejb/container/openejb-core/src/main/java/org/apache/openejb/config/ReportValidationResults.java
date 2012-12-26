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

import static org.apache.openejb.util.Join.join;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.LogCategory;

import java.util.List;
import java.util.Arrays;

/**
 * @version $Rev$ $Date$
 */
public class ReportValidationResults implements DynamicDeployer {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_VALIDATION, "org.apache.openejb.config.rules");

    public static final String VALIDATION_LEVEL = "openejb.validation.output.level";

    private enum Level {
        TERSE,
        MEDIUM,
        VERBOSE
    }

    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        Level level = SystemInstance.get().getOptions().get(VALIDATION_LEVEL, Level.MEDIUM);

        boolean hasErrors = appModule.hasErrors();
        boolean hasFailures = appModule.hasFailures();
        boolean hasWarnings = appModule.hasWarnings();

        if (!hasErrors && !hasFailures && !hasWarnings) return appModule;

        ValidationFailedException validationFailedException = null;

        List<ValidationContext> contexts = appModule.getValidationContexts();

        for (ValidationContext context : contexts) {
            logResults(context, level);
        }

        ValidationContext uberContext = new ValidationContext(appModule);
        for (ValidationContext context : contexts) {
            for (ValidationError error : context.getErrors()) {
                uberContext.addError(error);
            }
            for (ValidationFailure failure : context.getFailures()) {
                uberContext.addFailure(failure);
            }
            for (ValidationWarning warning : context.getWarnings()) {
                uberContext.addWarning(warning);
            }
        }

        if (!hasErrors && !hasFailures) return appModule;

        if (level != Level.VERBOSE){
            List<Level> levels = Arrays.asList(Level.values());
            levels = levels.subList(level.ordinal() + 1, levels.size());

            logger.info("Set the '"+VALIDATION_LEVEL+"' system property to "+ join(" or ", levels) +" for increased validation details.");
        }

        throw  new ValidationFailedException("Module failed validation. " + uberContext.getModuleType() + "(name=" + uberContext.getName() + ")", uberContext, validationFailedException);
    }

    private void logResults(ValidationContext context, Level level) {

        for (ValidationError e : context.getErrors()) {
            logger.error(e.getPrefix() + " ... " + e.getComponentName() + ":\t" + e.getMessage(level.ordinal() + 1));
        }

        for (ValidationFailure e : context.getFailures()) {
            logger.error(e.getPrefix() + " ... " + e.getComponentName() + ":\t" + e.getMessage(level.ordinal() + 1));
        }

        for (ValidationWarning e : context.getWarnings()) {
            logger.warning(e.getPrefix() + " ... " + e.getComponentName() + ":\t" + e.getMessage(level.ordinal() + 1));
        }

        if (context.hasErrors() || context.hasFailures()) {

            DeploymentModule module = context.getModule();
            logger.error(String.format("Invalid %s(name=%s, path=%s)", context.getModuleType(), module.getModuleId(), module.getFile()));
//            logger.error("Validation: "+errors.length + " errors, "+failures.length+ " failures, in "+context.getModuleType()+"(path="+context.getJarPath()+")");
        } else if (context.hasWarnings()) {
            if (context.getWarnings().length == 1) {
                logger.warning(context.getWarnings().length +" warning for "+context.getModuleType()+"(path="+context.getName()+")");
            } else {
                logger.warning(context.getWarnings().length +" warnings for "+context.getModuleType()+"(path="+context.getName()+")");
            }
        }
    }

}
