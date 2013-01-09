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
package org.apache.openejb.persistence;


import org.apache.openejb.util.URLs;
import org.apache.openejb.util.classloader.URLClassLoaderFirst;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {
    /**
     * External handler which handles adding a runtime ClassTransformer to the classloader.
     */
    private final PersistenceClassLoaderHandler persistenceClassLoaderHandler;

    /**
     * The unique id of this persistence unit.
     */
    private String id;

    /**
     * Name of this persistence unit.  The JPA specification has restrictions on the
     * uniqueness of this name.
     */
    private String persistenceUnitName;

    /**
     * Name of the persistence provider implementation class.
     */
    private String persistenceProviderClassName;

    /**
     * Does this persistence unti participate in JTA transactions or does it manage
     * resource local tranactions using the JDBC APIs.
     */
    private PersistenceUnitTransactionType transactionType = PersistenceUnitTransactionType.JTA;

    /**
     * Data source used by jta persistence units for accessing transactional data.
     */
    private DataSource jtaDataSource;

    /**
     * Data source used by non-jta persistence units and by jta persistence units for
     * non-transactional operations such as accessing a primary key table or sequence.
     */
    private DataSource nonJtaDataSource;

    /**
     * Names if the entity-mapping.xml files relative to to the persistenceUnitRootUrl.
     */
    private final List<String> mappingFileNames = new ArrayList<String>();

    /**
     * The jar file locations that make up this persistence unit.
     */
    private final List<URL> jarFileUrls = new ArrayList<URL>();

    /**
     * Location of the root of the persistent unit.  The directory in which
     * META-INF/persistence.xml is located.
     */
    private URL persistenceUnitRootUrl;

    /**
     * List of the managed entity classes.
     */
    private final List<String> managedClassNames = new ArrayList<String>();

    /**
     * Should class not listed in the persistence unit be managed by the EntityManager?
     */
    private boolean excludeUnlistedClasses;

    /**
     * JPA provider properties for this persistence unit.
     */
    private Properties properties;

    /**
     * Class loader used by JPA to load Entity classes.
     */
    private ClassLoader classLoader;

    // JPA 2.0
    /** Schema version of the persistence.xml file */
    private String persistenceXMLSchemaVersion;

    /** Second-level cache mode for the persistence unit */
    private SharedCacheMode sharedCacheMode;

    /** The validation mode to be used for the persistence unit */
    private ValidationMode validationMode;

    /** just to be able to dump this PU at runtime */
    private String jtaDataSourceName;
    /** just to be able to dump this PU at runtime */
    private String nonJtaDataSourceName;

    /** does it need to be created lazily (not in constructor) */
    private boolean lazilyInitialized;

    public PersistenceUnitInfoImpl() {
        this.persistenceClassLoaderHandler = null;
    }

    public PersistenceUnitInfoImpl(PersistenceClassLoaderHandler persistenceClassLoaderHandler) {
        this.persistenceClassLoaderHandler = persistenceClassLoaderHandler;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPersistenceUnitName() {
        return persistenceUnitName;
    }

    public void setPersistenceUnitName(String persistenceUnitName) {
        this.persistenceUnitName = persistenceUnitName;
    }

    public String getPersistenceProviderClassName() {
        return persistenceProviderClassName;
    }

    public void setPersistenceProviderClassName(String persistenceProviderClassName) {
        this.persistenceProviderClassName = persistenceProviderClassName;
    }

    public PersistenceUnitTransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(PersistenceUnitTransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public DataSource getJtaDataSource() {
        return jtaDataSource;
    }

    public void setJtaDataSource(DataSource jtaDataSource) {
        this.jtaDataSource = jtaDataSource;
    }

    public DataSource getNonJtaDataSource() {
        return nonJtaDataSource;
    }

    public void setNonJtaDataSource(DataSource nonJtaDataSource) {
        this.nonJtaDataSource = nonJtaDataSource;
    }

    public List<String> getMappingFileNames() {
        return mappingFileNames;
    }

    public void setMappingFileNames(List<String> mappingFileNames) {
        if (mappingFileNames == null) {
            throw new NullPointerException("mappingFileNames is null");
        }
        this.mappingFileNames.clear();
        this.mappingFileNames.addAll(mappingFileNames);
    }

    public void addMappingFileName(String mappingFileName) {
        if (mappingFileName == null) {
            throw new NullPointerException("mappingFileName is null");
        }
        mappingFileNames.add(mappingFileName);
    }

    public List<URL> getJarFileUrls() {
        return jarFileUrls;
    }

    public URL getPersistenceUnitRootUrl() {
        return persistenceUnitRootUrl;
    }

    public void setRootUrlAndJarUrls(String persistenceUnitRootUrl, List<String> jarFiles) throws MalformedURLException {
        File root;
        try{
            final URI rootUri = URLs.uri(persistenceUnitRootUrl);
            root = new File(rootUri);
        } catch (IllegalArgumentException e) {
            root = new File(persistenceUnitRootUrl);
        }

        this.persistenceUnitRootUrl = toUrl(root);
        try {

            for (String path : jarFiles) {
                File file = new File(root, path);
                file = file.getCanonicalFile();
                jarFileUrls.add(toUrl(file));
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private URL toUrl(File root) throws MalformedURLException {
        return root.toURI().toURL();
    }

    public List<String> getManagedClassNames() {
        return managedClassNames;
    }

    public void setManagedClassNames(List<String> managedClassNames) {
        if (managedClassNames == null) {
            throw new NullPointerException("managedClassNames is null");
        }
        this.managedClassNames.clear();
        this.managedClassNames.addAll(managedClassNames);
    }

    public void addManagedClassName(String className) {
        managedClassNames.add(className);
    }

    public boolean excludeUnlistedClasses() {
        return excludeUnlistedClasses;
    }

    public void setExcludeUnlistedClasses(boolean excludeUnlistedClasses) {
        this.excludeUnlistedClasses = excludeUnlistedClasses;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public void addTransformer(ClassTransformer classTransformer) {
        if (persistenceClassLoaderHandler != null) {
            PersistenceClassFileTransformer classFileTransformer = new PersistenceClassFileTransformer(classTransformer);
            persistenceClassLoaderHandler.addTransformer(id, classLoader, classFileTransformer);
        }
    }

    public ClassLoader getNewTempClassLoader() {
        if (persistenceClassLoaderHandler != null) {
            return persistenceClassLoaderHandler.getNewTempClassLoader(classLoader);
        } else {
            return null;
        }
    }

    // for emf in webapp of ears
    public boolean isLazilyInitialized() {
        return lazilyInitialized;
    }

    public void setLazilyInitialized(final boolean lazilyInitialized) {
        this.lazilyInitialized = lazilyInitialized;
    }

    public static class PersistenceClassFileTransformer implements ClassFileTransformer {
        private final ClassTransformer classTransformer;

        public PersistenceClassFileTransformer(ClassTransformer classTransformer) {
            this.classTransformer = classTransformer;
        }

        public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
            // Example code to easily debug transformation of a specific class
            // if ("org/apache/openejb/test/entity/cmp/BasicCmpBean".equals(className) ||
            //        "org/apache/openejb/test/entity/cmp/BasicCmp2Bean_BasicCmp2Bean".equals(className)) {
            //    System.err.println("Loading " + className);
            // }
            String replace = className.replace('/', '.');
            if (isServerClass(replace)) {
                return classfileBuffer;
            }
            return classTransformer.transform(classLoader, replace, classBeingRedefined, protectionDomain, classfileBuffer);
        }
    }

    // not the shouldSkip() method from UrlClassLoaderFirst since we skip more here
    // we just need JPA stuff so all the tricks we have for the server part are useless
    public static boolean isServerClass(final String name) {
        if (name == null) {
            return false;
        }

        for (String prefix : URLClassLoaderFirst.FORCED_SKIP) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        for (String prefix : URLClassLoaderFirst.FORCED_LOAD) {
            if (name.startsWith(prefix)) {
                return false;
            }
        }

        if (name.startsWith("java.")) return true;
        if (name.startsWith("javax.")) return true;
        if (name.startsWith("sun.")) return true;

        if (name.startsWith("org.")) {
            final String org = name.substring("org.".length());

            if (org.startsWith("apache.")) {
                final String apache = org.substring("apache.".length());

                if (apache.startsWith("bval.")) return true;
                if (apache.startsWith("openjpa.")) return true;
                if (apache.startsWith("derby.")) return true;
                if (apache.startsWith("xbean.")) return true;
                if (apache.startsWith("geronimo.")) return true;
                if (apache.startsWith("coyote")) return true;
                if (apache.startsWith("webbeans.")) return true;
                if (apache.startsWith("log4j")) return true;
                if (apache.startsWith("catalina")) return true;
                if (apache.startsWith("jasper.")) return true;
                if (apache.startsWith("tomcat.")) return true;
                if (apache.startsWith("el.")) return true;
                if (apache.startsWith("jsp")) return true;
                if (apache.startsWith("naming")) return true;
                if (apache.startsWith("taglibs.")) return true;
                if (apache.startsWith("openejb.")) return true;
                if (apache.startsWith("openjpa.")) return true;
                if (apache.startsWith("myfaces.")) return true;
                if (apache.startsWith("juli.")) return true;
                if (apache.startsWith("webbeans.")) return true;
                if (apache.startsWith("cxf.")) return true;
                if (apache.startsWith("activemq.")) return true;

                if (apache.startsWith("commons.")) {
                    final String commons = apache.substring("commons.".length());

                    // don't stop on commons package since we don't bring all commons
                    if (commons.startsWith("beanutils")) return true;
                    if (commons.startsWith("cli")) return true;
                    if (commons.startsWith("codec")) return true;
                    if (commons.startsWith("collections")) return true;
                    if (commons.startsWith("dbcp")) return true;
                    if (commons.startsWith("digester")) return true;
                    if (commons.startsWith("jocl")) return true;
                    if (commons.startsWith("lang")) return true;
                    if (commons.startsWith("logging")) return false;
                    if (commons.startsWith("pool")) return true;
                    if (commons.startsWith("net")) return true;

                    return false;
                }

                return false;
            }

            // other org packages
            if (org.startsWith("codehaus.swizzle")) return true;
            if (org.startsWith("w3c.dom")) return true;
            if (org.startsWith("quartz")) return true;
            if (org.startsWith("eclipse.jdt.")) return true;
            if (org.startsWith("slf4j")) return true;
            if (org.startsWith("openejb")) return true; // old packages
            if (org.startsWith("hsqldb")) return true; // old packages
            if (org.startsWith("hibernate")) return true; // old packages

            return false;
        }

        // other packages
        if (name.startsWith("com.sun.org.apache.")) return true;
        if (name.startsWith("javassist")) return true;
        if (name.startsWith("serp.")) return true;

        return false;
    }

    // JPA 2.0
    /* (non-Javadoc)
     * @see javax.persistence.spi.PersistenceUnitInfo#getPersistenceXMLSchemaVersion()
     */
    public String getPersistenceXMLSchemaVersion() {
        return this.persistenceXMLSchemaVersion;
    }

    /**
     * @param persistenceXMLSchemaVersion the persistenceXMLSchemaVersion to set
     */
    public void setPersistenceXMLSchemaVersion(String persistenceXMLSchemaVersion) {
        this.persistenceXMLSchemaVersion = persistenceXMLSchemaVersion;
    }

    /* (non-Javadoc)
     * @see javax.persistence.spi.PersistenceUnitInfo#getSharedCacheMode()
     */
    public SharedCacheMode getSharedCacheMode() {
        return this.sharedCacheMode;
    }

    /**
     * @param sharedCacheMode the sharedCacheMode to set
     */
    public void setSharedCacheMode(SharedCacheMode sharedCacheMode) {
        this.sharedCacheMode = sharedCacheMode;
    }

    /* (non-Javadoc)
     * @see javax.persistence.spi.PersistenceUnitInfo#getValidationMode()
     */
    public ValidationMode getValidationMode() {
        return this.validationMode;
    }

    /**
     * @param validationMode the validationMode to set
     */
    public void setValidationMode(ValidationMode validationMode) {
        this.validationMode = validationMode;
    }

    public String getJtaDataSourceName() {
        return jtaDataSourceName;
    }

    public void setJtaDataSourceName(String jtaDataSourceName) {
        this.jtaDataSourceName = jtaDataSourceName;
    }

    public String getNonJtaDataSourceName() {
        return nonJtaDataSourceName;
    }

    public void setNonJtaDataSourceName(String nonJtaDataSourceName) {
        this.nonJtaDataSourceName = nonJtaDataSourceName;
    }
}
