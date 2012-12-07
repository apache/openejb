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
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.openejb.config;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.jee.ActivationConfig;
import org.apache.openejb.jee.ActivationConfigProperty;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.MessageDrivenBean;
import org.apache.openejb.jee.oejb3.EjbDeployment;
import org.apache.openejb.jee.oejb3.OpenejbJar;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;

/**
 * @version $Rev$ $Date$
 */
public class ActivationConfigPropertyOverride implements DynamicDeployer {

    private static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP_CONFIG, ActivationConfigPropertyOverride.class);

    @Override
    public AppModule deploy(AppModule appModule) throws OpenEJBException {

        final Properties system = new Properties();
        system.putAll(SystemInstance.get().getProperties());
        system.putAll(appModule.getProperties());
        system.putAll(System.getProperties());

        for (EjbModule ejbModule : appModule.getEjbModules()) {
            EjbJar ejbJar = ejbModule.getEjbJar();
            OpenejbJar openejbJar = ejbModule.getOpenejbJar();

            final Properties module = new Properties();
            module.putAll(openejbJar.getProperties());
            module.putAll(system);

            Map<String, EjbDeployment> deployments = openejbJar.getDeploymentsByEjbName();

            for (EnterpriseBean bean : ejbJar.getEnterpriseBeans()) {

                final String ejbName = bean.getEjbName();
                final EjbDeployment ejbDeployment = deployments.get(ejbName);

                if (!(bean instanceof MessageDrivenBean))
                    continue;

                final Properties properties = new Properties();
                properties.putAll(module);
                properties.putAll(ejbDeployment.getProperties());

                final MessageDrivenBean mdb = (MessageDrivenBean) bean;

                final Properties overrides = ConfigurationFactory.getOverrides(properties, "mdb.activation", "EnterpriseBean");
                overrides.putAll(ConfigurationFactory.getOverrides(properties, mdb.getMessagingType() + ".activation", "EnterpriseBean"));
                overrides.putAll(ConfigurationFactory.getOverrides(properties, ejbName + ".activation", "EnterpriseBean"));
                overrides.putAll(ConfigurationFactory.getOverrides(properties, ejbDeployment.getDeploymentId() + ".activation", "EnterpriseBean"));

                // If we don't have any overrides, skip to the next
                if (overrides.size() == 0) {
                    continue;
                }

                if (mdb.getActivationConfig() == null) {
                    mdb.setActivationConfig(new ActivationConfig());
                }
                List<ActivationConfigProperty> activationConfigList = mdb.getActivationConfig().getActivationConfigProperty();

                for (Map.Entry<Object, Object> entry : overrides.entrySet()) {

                    final Object property = entry.getKey() + "";
                    final Object value = entry.getValue() + "";

                    ActivationConfigProperty activationConfigProperty = this.findActivationProperty(activationConfigList, property.toString());

                    if (activationConfigProperty != null) {
                        logger.info(String.format("Found %s bean with activation-config property %s=%s to override", ejbName, activationConfigProperty.getActivationConfigPropertyName(), activationConfigProperty.getActivationConfigPropertyValue()));
                        logger.info(String.format("Overriding %s bean activation-config property.%s=%s", ejbName, property, value));
                        activationConfigProperty.setActivationConfigPropertyValue(entry.getValue().toString());

                    } else {
                        logger.info(String.format("Adding %s bean activation-config property %s=%s", ejbName, property, value));
                        activationConfigProperty = new ActivationConfigProperty();
                        activationConfigProperty.setActivationConfigPropertyName(property.toString());
                        activationConfigProperty.setActivationConfigPropertyValue(value.toString());
                        activationConfigList.add(activationConfigProperty);
                    }

                }
            }
        }

        return appModule;
    }

    private ActivationConfigProperty findActivationProperty(List<ActivationConfigProperty> activationConfigList, String nameOfProperty) {
        for (ActivationConfigProperty activationProp : activationConfigList) {
            if (activationProp.getActivationConfigPropertyName().equals(nameOfProperty)) {
                return activationProp;
            }
        }

        return null;
    }

}
