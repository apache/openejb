/**
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
package org.apache.openejb.arquillian;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ejb.Stateless;

@Stateless
public class AppStatus implements AppStatusRemote {

    public String[] getDeployedApps() throws AppLookupException {
        // Basically the reflection equivalent of:
        //
        // Assembler assembler =
        // SystemInstance.get().getComponent(Assembler.class);
        // Collection<AppInfo> deployedApplications =
        // assembler.getDeployedApplications();
        // for (AppInfo appInfo : deployedApplications) {
        // result.add(appInfo.path);
        // }

        List<String> result;
        try {
            result = new ArrayList<String>();

            Class<?> systemInstanceCls = Class.forName("org.apache.openejb.loader.SystemInstance");
            Method getMethod = systemInstanceCls.getMethod("get");
            Object systemInstanceObj = getMethod.invoke(null);

            Class<?> assemblerCls = Class.forName("org.apache.openejb.assembler.classic.Assembler");
            Method getComponentMethod = systemInstanceCls.getMethod("getComponent", Class.class);
            Object assemblerObj = getComponentMethod.invoke(systemInstanceObj, assemblerCls);

            Class<?> appInfoCls = Class.forName("org.apache.openejb.assembler.classic.AppInfo");

            Method getDeployedApplicationsMethod = assemblerCls.getMethod("getDeployedApplications");
            Collection<?> deployedApplications = (Collection<?>) getDeployedApplicationsMethod.invoke(assemblerObj);
            for (Object deployedApplication : deployedApplications) {
                String path = (String) appInfoCls.getDeclaredField("path").get(deployedApplication);
                result.add(path);
            }

            return result.toArray(new String[result.size()]);
        } catch (Exception e) {
            throw new AppLookupException("Unable to lookup deployed apps in TomEE. Is this EJB running in TomEE?", e);
        }

    }

}
