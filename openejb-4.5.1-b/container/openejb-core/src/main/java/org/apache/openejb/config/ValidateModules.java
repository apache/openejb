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

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.rules.CheckClassLoading;
import org.apache.openejb.loader.SystemInstance;

/**
 * @version $Rev$ $Date$
 */
// START SNIPPET : code
public class ValidateModules implements DynamicDeployer {
    public static final String OPENEJB_CHECK_CLASSLOADER = "openejb.check.classloader";

    public AppModule deploy(AppModule appModule) throws OpenEJBException {
        final AppValidator validator;
        if (!SystemInstance.get().getOptions().get(OPENEJB_CHECK_CLASSLOADER, false)) {
            validator = new AppValidator();
        } else {
            validator = new AppValidator(new CheckClassLoading());
        }
        return validator.validate(appModule);
    }

}
// END SNIPPET : code