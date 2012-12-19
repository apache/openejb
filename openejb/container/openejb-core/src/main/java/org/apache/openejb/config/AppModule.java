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

import org.apache.openejb.config.sys.Resource;
import org.apache.openejb.jee.Application;
import org.apache.openejb.jee.jpa.EntityMappings;
import org.apache.openejb.jee.jpa.unit.Persistence;
import org.apache.openejb.jee.jpa.unit.PersistenceUnit;
import org.apache.openejb.jee.jpa.unit.TransactionType;
import org.apache.openejb.util.SuperProperties;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * @version $Rev$ $Date$
 */
public class AppModule implements DeploymentModule {

    private final Properties properties = new SuperProperties().caseInsensitive(true);
    private final Application application;
    private final ValidationContext validation;
    private final List<URL> additionalLibraries = new ArrayList<URL>();
    private final List<ConnectorModule> connectorModules = new ArrayList<ConnectorModule>();
    private final List<WebModule> webModules = new ArrayList<WebModule>();
    private final List<ClientModule> clientModules = new ArrayList<ClientModule>();
    private final List<EjbModule> ejbModules = new ArrayList<EjbModule>();
    private final List<PersistenceModule> persistenceModules = new ArrayList<PersistenceModule>();
    private final Map<String, TransactionType> txTypeByUnit = new HashMap<String, TransactionType>();
    // TODO We could turn this into the Resources JAXB object and support containers and other things as well
    private final Collection<Resource> resources = new HashSet<Resource>();
    private final Collection<org.apache.openejb.config.sys.Service> services = new HashSet<org.apache.openejb.config.sys.Service>();
    private final ClassLoader classLoader;
    private EntityMappings cmpMappings;
    private final Map<String,Object> altDDs = new HashMap<String,Object>();
    private final Set<String> watchedResources = new TreeSet<String>();
    private final boolean standaloneModule;
    private boolean delegateFirst = true;
    private final Set<String> additionalLibMbeans = new TreeSet<String>();
    private final Collection<String> jaxRsProviders = new TreeSet<String>();
    private final Map<String, PojoConfiguration> pojoConfigurations = new HashMap<String, PojoConfiguration>();

    private ID id;
    private boolean webapp = false;

    public AppModule(ClassLoader classLoader, String jarLocation) {
        this(classLoader, jarLocation, null, false);
    }

    public <T extends DeploymentModule> AppModule(T module) {
        this.standaloneModule = true;
        this.classLoader = module.getClassLoader();
        this.application = new Application(module.getModuleId());

        this.id = new ID(null, application, null, module.getFile(), module.getModuleUri(), this);
        this.validation = new ValidationContext(this);

        final Class<? extends DeploymentModule> type = module.getClass();

        if (type == EjbModule.class) {
            getEjbModules().add((EjbModule) module);
        } else if (type == ClientModule.class) {
            getClientModules().add((ClientModule) module);
        } else if (type == ConnectorModule.class) {
            getConnectorModules().add((ConnectorModule) module);
        } else if (type == WebModule.class) {
            getWebModules().add((WebModule) module);
        } else if (type == PersistenceModule.class) {
            addPersistenceModule((PersistenceModule) module);
        } else {
            throw new IllegalArgumentException("Unknown module type: " + type.getName());
        }
    }

    public boolean isDelegateFirst() {
        return delegateFirst;
    }

    public void setDelegateFirst(final boolean delegateFirst) {
        this.delegateFirst = delegateFirst;
    }

    public Properties getProperties() {
        return properties;
    }

    @Override
    public AppModule appModule() {
        return this;
    }

    public AppModule(ClassLoader classLoader, String jarLocation, Application application, boolean standaloneModule) {
        this.classLoader = classLoader;
        this.application = application;
        
        File file = (jarLocation == null) ? null : new File(jarLocation);
        this.id = new ID(null, application, null, file, null, this);
        this.validation = new ValidationContext(this);
        this.standaloneModule = standaloneModule;
    }

    public Set<String> getAdditionalLibMbeans() {
        return additionalLibMbeans;
    }

    public boolean isStandaloneModule() {
        return standaloneModule;
    }
    
    public void setStandaloneModule(boolean isStandalone) {
       //do nothing
    }    

    public ValidationContext getValidation() {
        return validation;
    }

    public boolean hasWarnings() {
        if (validation.hasWarnings()) return true;
        for (EjbModule module : ejbModules) {
            if (module.getValidation().hasWarnings()) return true;
        }
        for (ClientModule module : clientModules) {
            if (module.getValidation().hasWarnings()) return true;
        }
        for (ConnectorModule module : connectorModules) {
            if (module.getValidation().hasWarnings()) return true;
        }
        for (WebModule module : webModules) {
            if (module.getValidation().hasWarnings()) return true;
        }
        return false;
    }

    public boolean hasFailures() {
        if (validation.hasFailures()) return true;
        for (EjbModule module : ejbModules) {
            if (module.getValidation().hasFailures()) return true;
        }
        for (ClientModule module : clientModules) {
            if (module.getValidation().hasFailures()) return true;
        }
        for (ConnectorModule module : connectorModules) {
            if (module.getValidation().hasFailures()) return true;
        }
        for (WebModule module : webModules) {
            if (module.getValidation().hasFailures()) return true;
        }
        return false;
    }

    public boolean hasErrors() {
        if (validation.hasErrors()) return true;
        for (EjbModule module : ejbModules) {
            if (module.getValidation().hasErrors()) return true;
        }
        for (ClientModule module : clientModules) {
            if (module.getValidation().hasErrors()) return true;
        }
        for (ConnectorModule module : connectorModules) {
            if (module.getValidation().hasErrors()) return true;
        }
        for (WebModule module : webModules) {
            if (module.getValidation().hasErrors()) return true;
        }
        return false;
    }

    public List<ValidationContext> getValidationContexts() {
        List<ValidationContext> contexts = new ArrayList<ValidationContext>();

        contexts.add(getValidation());

        for (EjbModule module : ejbModules) {
            contexts.add(module.getValidation());
        }
        for (ClientModule module : clientModules) {
            contexts.add(module.getValidation());
        }
        for (ConnectorModule module : connectorModules) {
            contexts.add(module.getValidation());
        }
        for (WebModule module : webModules) {
            contexts.add(module.getValidation());
        }
        return contexts;
    }

    public String getJarLocation() {
        return (id.getLocation() != null) ? id.getLocation().getAbsolutePath() : null;
    }
    
    public void setModuleId(String moduleId) {
        
        this.id = new ID(null, application, moduleId, id.getLocation(), id.getUri(), this);
    }    

    public String getModuleId() {
        return id.getName();
    }

    public File getFile() {
        return id.getLocation();
    }

    public URI getModuleUri() {
        return id.getUri();
    }

    public Map<String, Object> getAltDDs() {
        return altDDs;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Application getApplication() {
        return application;
    }

    public List<ClientModule> getClientModules() {
        return clientModules;
    }

    public List<EjbModule> getEjbModules() {
        return ejbModules;
    }

    public List<PersistenceModule> getPersistenceModules() {
        return persistenceModules;
    }

    public List<URL> getAdditionalLibraries() {
        return additionalLibraries;
    }

    public EntityMappings getCmpMappings() {
        return cmpMappings;
    }

    public void setCmpMappings(EntityMappings cmpMappings) {
        this.cmpMappings = cmpMappings;
    }

    public List<ConnectorModule> getConnectorModules() {
        return connectorModules;
    }

    public List<WebModule> getWebModules() {
        return webModules;
    }

    public Set<String> getWatchedResources() {
        return watchedResources;
    }

    public Collection<Resource> getResources() {
        return resources;
    }

    public Collection<org.apache.openejb.config.sys.Service> getServices() {
        return services;
    }

    public Collection<DeploymentModule> getDeploymentModule() {
        ArrayList<DeploymentModule> modules = new ArrayList<DeploymentModule>();
        modules.addAll(ejbModules);
        modules.addAll(webModules);
        modules.addAll(connectorModules);
        modules.addAll(clientModules);
        return modules;
    }


    @Override
    public String toString() {
        return "AppModule{" +
                "moduleId='" + id.getName() + '\'' +
                '}';
    }

    public void setStandloneWebModule() {
        webapp = true;
    }

    public boolean isWebapp() {
        return webapp;
    }

    public Collection<String> getJaxRsProviders() {
        return jaxRsProviders;
    }

    public void addPersistenceModule(final PersistenceModule root) {
        persistenceModules.add(root);

        final Persistence persistence = root.getPersistence();
        for (PersistenceUnit unit : persistence.getPersistenceUnit()) {
            txTypeByUnit.put(unit.getName(), unit.getTransactionType());
        }
    }

    public void addPersistenceModules(final Collection<PersistenceModule> roots) {
        for (PersistenceModule root : roots) {
            addPersistenceModule(root);
        }
    }

    public TransactionType getTransactionType(final String unit) {
        if (unit == null || unit.isEmpty()) {
            if (txTypeByUnit.size() == 1) {
                return txTypeByUnit.values().iterator().next();
            }
        }

        TransactionType type = txTypeByUnit.get(unit);
        if (type == null) { // default, shouldn't occur
            type = TransactionType.JTA;
        }
        return type;
    }

    public Map<String, PojoConfiguration> getPojoConfigurations() {
        return pojoConfigurations;
    }
}
