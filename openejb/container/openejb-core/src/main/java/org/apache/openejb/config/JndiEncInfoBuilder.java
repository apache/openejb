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
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.ContextReferenceInfo;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EjbLocalReferenceInfo;
import org.apache.openejb.assembler.classic.EjbReferenceInfo;
import org.apache.openejb.assembler.classic.EjbResolver;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.EnvEntryInfo;
import org.apache.openejb.assembler.classic.InjectableInfo;
import org.apache.openejb.assembler.classic.InjectionInfo;
import org.apache.openejb.assembler.classic.JndiEncInfo;
import org.apache.openejb.assembler.classic.PersistenceContextReferenceInfo;
import org.apache.openejb.assembler.classic.PersistenceUnitReferenceInfo;
import org.apache.openejb.assembler.classic.PortRefInfo;
import org.apache.openejb.assembler.classic.ReferenceLocationInfo;
import org.apache.openejb.assembler.classic.ResourceEnvReferenceInfo;
import org.apache.openejb.assembler.classic.ResourceReferenceInfo;
import org.apache.openejb.assembler.classic.ServiceReferenceInfo;
import org.apache.openejb.jee.EjbLocalRef;
import org.apache.openejb.jee.EjbReference;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.EnvEntry;
import org.apache.openejb.jee.Injectable;
import org.apache.openejb.jee.InjectionTarget;
import org.apache.openejb.jee.JndiConsumer;
import org.apache.openejb.jee.JndiReference;
import org.apache.openejb.jee.MessageDestinationRef;
import org.apache.openejb.jee.PersistenceContextRef;
import org.apache.openejb.jee.PersistenceContextType;
import org.apache.openejb.jee.PersistenceUnitRef;
import org.apache.openejb.jee.PortComponentRef;
import org.apache.openejb.jee.Property;
import org.apache.openejb.jee.ResAuth;
import org.apache.openejb.jee.ResourceEnvRef;
import org.apache.openejb.jee.ResourceRef;
import org.apache.openejb.jee.ServiceRef;
import org.apache.openejb.jee.SessionBean;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.openejb.util.Messages;
import org.apache.openejb.util.URLs;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.openejb.assembler.classic.EjbResolver.Scope.EAR;
import static org.apache.openejb.assembler.classic.EjbResolver.Scope.EJBJAR;

/**
 * @version $Rev$ $Date$
 */
public class JndiEncInfoBuilder {


    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_STARTUP, JndiEncInfoBuilder.class);
    protected static final Messages messages = new Messages(JndiEncInfoBuilder.class);

    private final EjbResolver earResolver;
    private final Map<String, EjbResolver> ejbJarResolvers = new HashMap<String, EjbResolver>();
    private AppInfo appInfo;

    public JndiEncInfoBuilder(AppInfo appInfo) {
        this.appInfo = appInfo;

        // Global-scoped EJB Resolver

        EjbResolver globalResolver = SystemInstance.get().getComponent(EjbResolver.class);

        // EAR-scoped EJB Resolver

        earResolver = new EjbResolver(globalResolver, EAR, appInfo.ejbJars);

        // EJBJAR-scoped EJB Resolver(s)

        for (EjbJarInfo ejbJarInfo : appInfo.ejbJars) {

            EjbResolver ejbJarResolver = new EjbResolver(earResolver, EJBJAR, ejbJarInfo);

            ejbJarResolvers.put(ejbJarInfo.moduleName, ejbJarResolver);
        }
    }

    private EjbResolver getEjbResolver(String moduleId) {
        EjbResolver resolver = ejbJarResolvers.get(moduleId);
        if (resolver == null){
            resolver = earResolver;
        }
        return resolver;
    }

    public void build(JndiConsumer jndiConsumer, String ejbName, String moduleId, URI moduleUri, JndiEncInfo moduleJndiEnc, JndiEncInfo compJndiEnc) throws OpenEJBException {
        assert moduleJndiEnc != null;
        assert compJndiEnc != null;
        assert jndiConsumer != null;              

        /* Build Environment entries *****************/
        buildEnvEntryInfos(jndiConsumer, moduleJndiEnc, compJndiEnc);

        /* Build Resource References *****************/
        buildResourceRefInfos(jndiConsumer, moduleJndiEnc, compJndiEnc);

        /* Build Resource Environment References *****************/
        buildResourceEnvRefInfos(jndiConsumer, moduleJndiEnc, compJndiEnc);

        buildEjbRefs(jndiConsumer, moduleUri, moduleId, ejbName, moduleJndiEnc, compJndiEnc);

        buildPersistenceUnitRefInfos(jndiConsumer, moduleUri, moduleJndiEnc, compJndiEnc);

        buildPersistenceContextRefInfos(jndiConsumer, moduleUri, moduleJndiEnc, compJndiEnc);

        buildServiceRefInfos(jndiConsumer, moduleJndiEnc, compJndiEnc);
    }

    private void buildEjbRefs(JndiConsumer jndiConsumer, URI moduleUri, String moduleId, String ejbName, JndiEncInfo moduleJndiEnc, JndiEncInfo compJndiEnc) throws OpenEJBException {

        int size = jndiConsumer.getEjbRef().size() + jndiConsumer.getEjbLocalRef().size();

        ArrayList<EjbReference> references = new ArrayList<EjbReference>(size);

        references.addAll(jndiConsumer.getEjbRef());
        references.addAll(jndiConsumer.getEjbLocalRef());

        for (EjbReference ref : references) {

            EjbReferenceInfo info = new EjbReferenceInfo();

            info.homeClassName = ref.getHome();
            info.interfaceClassName = ref.getInterface();
            info.referenceName = ref.getName();
            info.link = ref.getEjbLink();
            info.location = buildLocationInfo(ref);
            info.targets.addAll(buildInjectionInfos(ref));
            info.localbean = isIntefaceLocalBean(moduleId, info.interfaceClassName);

            if (info.location != null) {
                if (ref.getRefType() == EjbReference.Type.LOCAL){
                    insert(toLocal(info), appInfo.globalJndiEnc.ejbLocalReferences, appInfo.appJndiEnc.ejbLocalReferences, moduleJndiEnc.ejbLocalReferences, compJndiEnc.ejbLocalReferences);
                } else {
                    insert(info, appInfo.globalJndiEnc.ejbReferences, appInfo.appJndiEnc.ejbReferences, moduleJndiEnc.ejbReferences, compJndiEnc.ejbReferences);
                }
                continue;
            }

            EjbResolver ejbResolver = getEjbResolver(moduleId);

            String deploymentId = ejbResolver.resolve(new Ref(ref), moduleUri);

            info.ejbDeploymentId = deploymentId;

            if (info.ejbDeploymentId == null) {
                if (info.link != null){
                    logger.warning("config.noBeanFoundEjbLink", ref.getName(), ejbName, ref.getEjbLink());
                } else {
                    logger.warning("config.noBeanFound", ref.getName(), ejbName, ref.getEjbLink());
                }
            } else {

//            info.localbean = isLocalBean(moduleId, deploymentId);

                EjbResolver.Scope scope = ejbResolver.getScope(deploymentId);

                info.externalReference = (scope != EAR && scope != EJBJAR);

                if (ref.getRefType() == EjbReference.Type.UNKNOWN) {
                    EnterpriseBeanInfo otherBean = ejbResolver.getEnterpriseBeanInfo(deploymentId);
                    if (otherBean != null) {
                        if (otherBean.businessLocal.contains(ref.getInterface()) || otherBean.ejbClass.equals(ref.getInterface())) {
                            ref.setRefType(EjbReference.Type.LOCAL);
                            jndiConsumer.getEjbRef().remove(ref);
                            jndiConsumer.getEjbLocalRef().add(new EjbLocalRef(ref));
                        } else {
                            ref.setRefType(EjbReference.Type.REMOTE);
                        }
                    }
                }
            }


            if (ref.getRefType() == EjbReference.Type.LOCAL){
                insert(toLocal(info), appInfo.globalJndiEnc.ejbLocalReferences, appInfo.appJndiEnc.ejbLocalReferences, moduleJndiEnc.ejbLocalReferences, compJndiEnc.ejbLocalReferences);
            } else {
                insert(info, appInfo.globalJndiEnc.ejbReferences, appInfo.appJndiEnc.ejbReferences, moduleJndiEnc.ejbReferences, compJndiEnc.ejbReferences);
            }

        }
    }

    private EjbLocalReferenceInfo toLocal(EjbReferenceInfo r) {
        EjbLocalReferenceInfo l = new EjbLocalReferenceInfo();
        l.ejbDeploymentId = r.ejbDeploymentId;
        l.externalReference = r.externalReference;
        l.homeClassName = r.homeClassName;
        l.interfaceClassName = r.interfaceClassName;
        l.referenceName = r.referenceName;
        l.link = r.link;
        l.location = r.location;
        l.targets.addAll(r.targets);
        l.localbean = r.localbean;
        return l;
    }

    private void buildServiceRefInfos(JndiConsumer jndiConsumer, JndiEncInfo moduleJndiEnc, JndiEncInfo compJndiEnc) {
        for (ServiceRef ref : jndiConsumer.getServiceRef()) {
            ServiceReferenceInfo info = new ServiceReferenceInfo();
            info.referenceName = ref.getName();
            info.location = buildLocationInfo(ref);
            info.targets.addAll(buildInjectionInfos(ref));
            insert(info, appInfo.globalJndiEnc.serviceRefs, appInfo.appJndiEnc.serviceRefs, moduleJndiEnc.serviceRefs, compJndiEnc.serviceRefs);

            if (SystemInstance.get().hasProperty("openejb.geronimo")) continue;

            info.id = ref.getMappedName();
            info.serviceQName = ref.getServiceQname();
            info.serviceType = ref.getServiceInterface();
            info.referenceType = ref.getServiceRefType();
            info.wsdlFile = ref.getWsdlFile();
            info.jaxrpcMappingFile = ref.getJaxrpcMappingFile();
            info.handlerChains.addAll(ConfigurationFactory.toHandlerChainInfo(ref.getAllHandlers()));
            for (PortComponentRef portComponentRef : ref.getPortComponentRef()) {
                PortRefInfo portRefInfo = new PortRefInfo();
                portRefInfo.qname = portComponentRef.getQName();
                portRefInfo.serviceEndpointInterface = portComponentRef.getServiceEndpointInterface();
                portRefInfo.enableMtom = portComponentRef.isEnableMtom();
                portRefInfo.properties.putAll(portComponentRef.getProperties());
                info.portRefs.add(portRefInfo);
            }
        }
    }

    private void buildPersistenceUnitRefInfos(JndiConsumer jndiConsumer, URI moduleId, JndiEncInfo moduleJndiEnc, JndiEncInfo compJndiEnc) {
        for (PersistenceUnitRef puRef : jndiConsumer.getPersistenceUnitRef()) {
            PersistenceUnitReferenceInfo info = new PersistenceUnitReferenceInfo();
            info.referenceName = puRef.getPersistenceUnitRefName();
            info.persistenceUnitName = puRef.getPersistenceUnitName();
            info.unitId = puRef.getMappedName();
            info.location = buildLocationInfo(puRef);
            info.targets.addAll(buildInjectionInfos(puRef));
            insert(info, appInfo.globalJndiEnc.persistenceUnitRefs, appInfo.appJndiEnc.persistenceUnitRefs, moduleJndiEnc.persistenceUnitRefs, compJndiEnc.persistenceUnitRefs);
        }
    }

    private void buildPersistenceContextRefInfos(JndiConsumer jndiConsumer, URI moduleId, JndiEncInfo moduleJndiEnc, JndiEncInfo compJndiEnc) {

        for (PersistenceContextRef contextRef : jndiConsumer.getPersistenceContextRef()) {
            PersistenceContextReferenceInfo info = new PersistenceContextReferenceInfo();
            info.referenceName = contextRef.getPersistenceContextRefName();
            info.persistenceUnitName = contextRef.getPersistenceUnitName();
            info.unitId = contextRef.getMappedName();
            info.location = buildLocationInfo(contextRef);
            info.extended = (contextRef.getPersistenceContextType() == PersistenceContextType.EXTENDED);
            List<Property> persistenceProperty = contextRef.getPersistenceProperty();
            for (Property property : persistenceProperty) {
                String name = property.getName();
                String value = property.getValue();
                info.properties.setProperty(name, value);
            }
            info.targets.addAll(buildInjectionInfos(contextRef));
            insert(info, appInfo.globalJndiEnc.persistenceContextRefs, appInfo.appJndiEnc.persistenceContextRefs, moduleJndiEnc.persistenceContextRefs, compJndiEnc.persistenceContextRefs);
        }
    }

    private void buildResourceRefInfos(JndiConsumer item, JndiEncInfo moduleJndiEnc, JndiEncInfo compJndiEnc) {
        for (ResourceRef res : item.getResourceRef()) {
            final ResourceReferenceInfo info;
            if (res instanceof ContextRef) {
                info = new ContextReferenceInfo();
            } else {
                info = new ResourceReferenceInfo();
            }

            if (res.getResAuth() != null) {
                info.referenceAuth = res.getResAuth().toString();
            } else {
                info.referenceAuth = ResAuth.CONTAINER.toString();
            }
            info.referenceName = res.getResRefName();
            info.referenceType = res.getResType();
            info.resourceID = res.getMappedName();
            info.location = buildLocationInfo(res);
            info.targets.addAll(buildInjectionInfos(res));
            insert(info, appInfo.globalJndiEnc.resourceRefs, appInfo.appJndiEnc.resourceRefs, moduleJndiEnc.resourceRefs, compJndiEnc.resourceRefs);
        }
    }

    private void buildResourceEnvRefInfos(JndiConsumer item, JndiEncInfo moduleJndiEnc, JndiEncInfo compJndiEnc) {
        for (ResourceEnvRef res : item.getResourceEnvRef()) {
            ResourceEnvReferenceInfo info = new ResourceEnvReferenceInfo();
            info.referenceName = res.getResourceEnvRefName();
            info.resourceEnvRefType = res.getResourceEnvRefType();
            info.resourceID = res.getMappedName();
            info.location = buildLocationInfo(res);
            info.targets.addAll(buildInjectionInfos(res));
            insert(info, appInfo.globalJndiEnc.resourceEnvRefs, appInfo.appJndiEnc.resourceEnvRefs, moduleJndiEnc.resourceEnvRefs, compJndiEnc.resourceEnvRefs);
        }
        for (MessageDestinationRef res : item.getMessageDestinationRef()) {
            ResourceEnvReferenceInfo info = new ResourceEnvReferenceInfo();
            info.referenceName = res.getMessageDestinationRefName();
            info.resourceEnvRefType = res.getMessageDestinationType();
            info.resourceID = res.getMappedName();
            info.location = buildLocationInfo(res);
            info.targets.addAll(buildInjectionInfos(res));
            insert(info, appInfo.globalJndiEnc.resourceEnvRefs, appInfo.appJndiEnc.resourceEnvRefs, moduleJndiEnc.resourceEnvRefs, compJndiEnc.resourceEnvRefs);
        }
    }

    private void buildEnvEntryInfos(JndiConsumer item, JndiEncInfo moduleJndiEnc, JndiEncInfo compJndiEnc) {
        for (EnvEntry env : item.getEnvEntry()) {
            // ignore env entries without a value and lookup name
            //If the the reference name of the environment entry is belong to those shareable JNDI name space, it somewhat is a valid one            
            if (env.getEnvEntryValue() == null && env.getLookupName() == null && !isShareableJNDINamespace(env.getEnvEntryName())) {
                continue;
            }

            EnvEntryInfo info = new EnvEntryInfo();

            info.referenceName = env.getEnvEntryName();
            info.type = env.getEnvEntryType();
            info.value = env.getEnvEntryValue();
            info.location = buildLocationInfo(env);
            info.targets.addAll(buildInjectionInfos(env));
            insert(info, appInfo.globalJndiEnc.envEntries, appInfo.appJndiEnc.envEntries, moduleJndiEnc.envEntries, compJndiEnc.envEntries);
        }
    }

    private boolean isShareableJNDINamespace(String jndiName) {
        return jndiName.startsWith("java:global/") || jndiName.startsWith("java:app/") || jndiName.startsWith("java:module/");
    }

    public ReferenceLocationInfo buildLocationInfo(JndiReference reference) {
        String lookupName = reference.getLookupName();
        if (lookupName != null) {
            ReferenceLocationInfo location = new ReferenceLocationInfo();
            location.jndiName = lookupName;
            return location;
        }
        String mappedName = reference.getMappedName();
        if (mappedName != null && mappedName.startsWith("jndi:")) {
            ReferenceLocationInfo location = new ReferenceLocationInfo();

            String name = mappedName.substring(5);

            if(name.startsWith("ext://")) {
                final URI uri = URLs.uri(name);
                location.jndiProviderId = uri.getHost();
                location.jndiName = uri.getPath();
            } else {
                location.jndiName = name;
            }

            return location;
        }
        return null;
    }

    public Collection<? extends InjectionInfo> buildInjectionInfos(Injectable injectable) {
        ArrayList<InjectionInfo> infos = new ArrayList<InjectionInfo>();
        for (InjectionTarget target : injectable.getInjectionTarget()) {
            InjectionInfo info = new InjectionInfo();
            info.className = target.getInjectionTargetClass();
            info.propertyName = target.getInjectionTargetName();
            infos.add(info);
        }
        return infos;
    }

    public void buildDependsOnRefs(EjbModule module, EnterpriseBean enterpriseBean, EnterpriseBeanInfo beanInfo, String moduleId) throws OpenEJBException {
        if (!(enterpriseBean instanceof SessionBean)) return;

        SessionBean sessionBean = (SessionBean) enterpriseBean;

        URI moduleUri = null;
        if (moduleId != null) {
            moduleUri = URLs.uri(moduleId);
        }

        EjbResolver ejbResolver = getEjbResolver(moduleId);

        if (sessionBean.getDependsOn() != null) {
            for (String ejbName : sessionBean.getDependsOn()) {
                String deploymentId = ejbResolver.resolve(new SimpleRef(ejbName), moduleUri);
                if (deploymentId != null) {
                    beanInfo.dependsOn.add(deploymentId);
                }
            }
        }

    }

    private boolean isIntefaceLocalBean(String moduleId, String interfaceClassName) {
        if (interfaceClassName == null) return false;

        EnterpriseBeanInfo beanInfo = getInterfaceBeanInfo(moduleId, interfaceClassName);
        return isLocalBean(beanInfo) && beanInfo.parents.contains(interfaceClassName);
    }

    private EnterpriseBeanInfo getInterfaceBeanInfo(String moduleId, String interfaceClassName) {
        if (interfaceClassName == null) throw new IllegalArgumentException("interfaceClassName cannot be null");

        List<EjbJarInfo> ejbJars = appInfo.ejbJars;
        for (EjbJarInfo ejbJar : ejbJars) {
            // DMB Not sure why we don't allow @LocalBean references in other modules in the EAR
//            if (!ejbJar.moduleId.equals(moduleId) && !(moduleId == null && appInfo.ejbJars.size() == 1)) continue;

            List<EnterpriseBeanInfo> enterpriseBeans = ejbJar.enterpriseBeans;
            for (EnterpriseBeanInfo enterpriseBean : enterpriseBeans) {
                if (interfaceClassName.equals(enterpriseBean.ejbClass)
                        || interfaceClassName.equals(enterpriseBean.local)
                        || interfaceClassName.equals(enterpriseBean.remote)
                        || (enterpriseBean.businessLocal != null && enterpriseBean.businessLocal.contains(interfaceClassName))
                        || (enterpriseBean.businessRemote != null && enterpriseBean.businessRemote.contains(interfaceClassName))) {
                    return enterpriseBean;
                }
            }
        }

        // look if it is an abstract injection (local bean)
        for (EjbJarInfo ejbJar : ejbJars) {
            for (EnterpriseBeanInfo enterpriseBean : ejbJar.enterpriseBeans) {
                if (enterpriseBean.parents.contains(interfaceClassName)) {
                    return enterpriseBean;
                }
            }
        }

        return null;
    }


    private boolean isLocalBean(String moduleId, String deploymentId) {
        EnterpriseBeanInfo beanInfo = getBeanInfo(moduleId, deploymentId);
        return isLocalBean(beanInfo);
    }

    private boolean isLocalBean(EnterpriseBeanInfo beanInfo) {
        return beanInfo != null && beanInfo.localbean;
    }

    private EnterpriseBeanInfo getBeanInfo(String moduleName, String deploymentId) {
        List<EjbJarInfo> ejbJars = appInfo.ejbJars;
        for (EjbJarInfo ejbJar : ejbJars) {
            if (!ejbJar.moduleName.equals(moduleName)) continue;

            List<EnterpriseBeanInfo> enterpriseBeans = ejbJar.enterpriseBeans;
            for (EnterpriseBeanInfo enterpriseBean : enterpriseBeans) {
                if (enterpriseBean.ejbDeploymentId.equals(deploymentId)) {
                    return enterpriseBean;
                }
            }
        }

        return null;
    }

    private static class SimpleRef implements EjbResolver.Reference {
        private final String name;

        public SimpleRef(String name) {
            this.name = name;
        }

        public String getEjbLink() {
            return name;
        }

        public String getHome() {
            return null;
        }

        public String getInterface() {
            return null;
        }

        public String getMappedName() {
            return null;
        }

        public String getName() {
            return name;
        }

        public EjbResolver.Type getRefType() {
            return EjbResolver.Type.UNKNOWN;
        }
    }
    /**
     * The assembler package cannot have a dependency on org.apache.openejb.jee
     * so we simply have a trimmed down copy of the org.apache.openejb.jee.EjbReference interface
     * and we adapt to it here.
     */
    private static class Ref implements EjbResolver.Reference {
        private final EjbReference ref;

        public Ref(EjbReference ref) {
            this.ref = ref;
        }

        public String getName() {
            return ref.getName();
        }

        public String getEjbLink() {
            return ref.getEjbLink();
        }

        public String getHome() {
            return ref.getHome();
        }

        public String getInterface() {
            return ref.getInterface();
        }

        public String getMappedName() {
            return ref.getMappedName();
        }

        public EjbResolver.Type getRefType() {
            // Could have used EjbResolver.Type.valueOf(..)
            // but this protects against an renaming
            switch(ref.getRefType()){
                case LOCAL: return EjbResolver.Type.LOCAL;
                case REMOTE: return EjbResolver.Type.REMOTE;
                case UNKNOWN: return EjbResolver.Type.UNKNOWN;
                default: return EjbResolver.Type.UNKNOWN;
            }
        }
    }

    private <T extends InjectableInfo> void insert(T t, List<T> global, List<T> app, List<T> module, List<T> comp) {
        String name = t.referenceName;
        if (!name.startsWith("java:")) {
            t.referenceName = "comp/env/" + name;
            comp.add(t);
        } else if (name.startsWith("java:global/")) {
            t.referenceName = name.substring(5);
            global.add(t);
        } else if (name.startsWith("java:app/")) {
            t.referenceName = name.substring(5);
            app.add(t);
        } else if (name.startsWith("java:module/")) {
            t.referenceName = name.substring(5);
            module.add(t);
        } else if (name.startsWith("java:comp/")) {
            t.referenceName = name.substring(5);
            comp.add(t);
        } else {
            //warn of error?
        }

    }
}
