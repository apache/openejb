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
package org.apache.openejb.core.osgi.impl;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.NoSuchApplicationException;
import org.apache.openejb.UndeployException;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.config.AppModule;
import org.apache.openejb.config.ConfigurationFactory;
import org.apache.openejb.config.DeploymentLoader;
import org.apache.openejb.config.UnknownModuleTypeException;
import org.apache.openejb.core.ivm.IntraVmProxy;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.util.proxy.ProxyEJB;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.ServiceRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @version $Rev$ $Date$
 */
public class Deployer implements BundleListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(Deployer.class);
    private static Deployer INSTANCE = null;

    private final Map<Bundle, List<ServiceRegistration>> registrations = new ConcurrentHashMap<Bundle, List<ServiceRegistration>>();
    private final Map<Bundle, String> paths = new ConcurrentHashMap<Bundle, String>();

    private final Activator openejbActivator;

    public Deployer(final Activator activator) {
        openejbActivator = activator;
        INSTANCE = this;
    }

    public static Deployer instance() {
        return INSTANCE;
    }

    @Override
    public void bundleChanged(final BundleEvent event) {
        openejbActivator.checkServiceManager(OpenEJBBundleContextHolder.get());
        switch (event.getType()) {
            case BundleEvent.STARTED:
                final BundleContext context = event.getBundle().getBundleContext();
                if (context != null) {
                    deploy(event.getBundle());
                }
                break;
            case BundleEvent.STOPPED:
            case BundleEvent.UNINSTALLED:
                undeploy(event.getBundle());
                break;
            case BundleEvent.UPDATED:
                try {
                    undeploy(event.getBundle());
                } catch (NullPointerException npe) {
                    // can happen when shutting down an OSGi server
                    // because of all stop events
                    LOGGER.warn("can't undeploy bundle #{0}", event.getBundle().getBundleId());
                }
                deploy(event.getBundle());
                break;
        }
    }

    private void deploy(final Bundle bundle) {
        final ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
        final ClassLoader osgiCl = new OSGIClassLoader(bundle, OpenEJBBundleContextHolder.get().getBundle());
        Thread.currentThread().setContextClassLoader(osgiCl);

        try {
            try {
                try {
                    // equinox? found in aries
                    File bundleDump = bundle.getBundleContext().getDataFile(bundle.getSymbolicName() + "/" + bundle.getVersion() + "/");
                    // TODO: what should happen if there is multiple versions?
                    if (!bundleDump.exists() && bundle.getBundleContext().getDataFile("") != null) { // felix. TODO: maybe find something better
                        bundleDump = findFelixJar(bundle.getBundleContext());
                    }
                    if (bundleDump == null || !bundleDump.exists()) {
                        bundleDump = findEquinoxJar(bundle.getBundleContext());
                    }

                    if (bundleDump == null || !bundleDump.exists()) {
                        LOGGER.warn("can't find bundle {0}", bundle.getBundleId());
                        return;
                    }

                    LOGGER.info("looking bundle {0} in {1}", bundle.getBundleId(), bundleDump);
                    final AppModule appModule = new OSGiDeploymentLoader(bundle).load(bundleDump);
                    LOGGER.info("deploying bundle #" + bundle.getBundleId() + " as an EJBModule");

                    final ConfigurationFactory configurationFactory = new ConfigurationFactory();
                    final AppInfo appInfo = configurationFactory.configureApplication(appModule);
                    appInfo.appId = "bundle_" + bundle.getBundleId();

                    final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
                    final AppContext appContext = assembler.createApplication(appInfo, osgiCl);
                    LOGGER.info("Application deployed: " + appInfo.path);

                    paths.put(bundle, appInfo.path);

                    registrations.put(bundle, new ArrayList<ServiceRegistration>());
                    registerService(bundle, appContext);
                } catch (UnknownModuleTypeException unknowException) {
                    LOGGER.info("bundle #" + bundle.getBundleId() + " is not an EJBModule");
                } catch (Exception ex) {
                    LOGGER.error("can't deploy bundle #" + bundle.getBundleId(), ex);
                }
            } catch (Exception ex1) {
                LOGGER.error("can't deploy bundle #" + bundle.getBundleId(), ex1);
            }
        } finally {
            Thread.currentThread().setContextClassLoader(oldCl);
        }
    }

    private static File findEquinoxJar(final BundleContext bundleContext) {
        final File root = bundleContext.getDataFile("").getParentFile();
        int idx = 0;
        File out;
        File f = null;
        do {
            out = f;
            idx++;
            f = new File(root, idx + "/bundlefile");
        } while (f.exists());
        return out;
    }

    private static File findFelixJar(final BundleContext bundleContext) {
        final File root = bundleContext.getDataFile("").getParentFile();
        int min = 0;
        int max = 0;
        File out;
        File f = null;
        do {
            do {
                out = f;
                f = new File(root, "version" + max + "." + min + "/bundle.jar");
                min++;
            } while (f.exists());
            min = 0;
            max++;
            f = new File(root, "version" + max + "." + min + "/bundle.jar");
        } while (f.exists());
        return out;
    }

    private void undeploy(final Bundle bundle) {
        if (registrations.containsKey(bundle)) {
            for (final ServiceRegistration registration : registrations.get(bundle)) {
                try {
                    registration.unregister();
                } catch (IllegalStateException ise) {
                    // ignored: already unregistered
                }
            }
            registrations.remove(bundle);
        }

        if (paths.containsKey(bundle)) {
            try {
                final Assembler assembler = SystemInstance.get().getComponent(Assembler.class);
                if (assembler != null) { // openejb stopped before bundles when shuttind down the OSGi container
                    assembler.destroyApplication(paths.remove(bundle));
                }
            } catch (IllegalStateException ise) {
                LOGGER.error("Can't undeploy bundle #{0}", bundle.getBundleId());
            } catch (UndeployException e) {
                LOGGER.error("Can't undeploy bundle #{0}", bundle.getBundleId(), e);
            } catch (NoSuchApplicationException e) {
                LOGGER.error("Can't undeploy non existing bundle #{0}", bundle.getBundleId(), e);
            }
        }

        LOGGER.info("[Deployer] Bundle {0} has been stopped", bundle.getSymbolicName());
    }

    /**
     * Register OSGi Service for EJB so calling the service will actually call the EJB
     *
     * @param bundle     the deployed bundle
     * @param appContext the appcontext to search EJBs
     */
    private void registerService(final Bundle bundle, final AppContext appContext) {
        LOGGER.info("Registering remote EJBs as OSGi services");
        final BundleContext context = bundle.getBundleContext();
        for (final BeanContext beanContext : appContext.getBeanContexts()) {
            if (beanContext.getBeanClass().equals(BeanContext.Comp.class) || BeanType.STATEFUL.equals(beanContext.getComponentType())) {
                continue;
            }

            try {
                if (beanContext.getBusinessRemoteInterface() != null) {
                    LOGGER.info("registering remote bean: {0}", beanContext.getEjbName());
                    registerService(beanContext, context, beanContext.getBusinessRemoteInterfaces());
                }
                if (beanContext.getBusinessLocalInterface() != null) {
                    LOGGER.info("registering local bean: {0}", beanContext.getEjbName());
                    registerService(beanContext, context, beanContext.getBusinessLocalInterfaces());
                }
                if (beanContext.isLocalbean()) {
                    LOGGER.info("registering local view bean: {0}", beanContext.getEjbName());
                    registerService(beanContext, context, Arrays.asList(beanContext.getBusinessLocalBeanInterface()));
                }
            } catch (Exception e) {
                LOGGER.error("[Deployer] can't register: {0}", beanContext.getEjbName());
            }
        }
    }

    private void registerService(final BeanContext beanContext, final BundleContext context, final List<Class> rawItf) {
        if (!rawItf.isEmpty()) {
            final List<Class> interfaces = new ArrayList<Class>(rawItf);
            if (interfaces.contains(IntraVmProxy.class)) {
                interfaces.remove(IntraVmProxy.class);
            }

            final Class<?>[] itfs = interfaces.toArray(new Class<?>[interfaces.size()]);
            try {
                final Object service = ProxyEJB.simpleProxy(beanContext, itfs);
                registrations.get(context.getBundle()).add(context.registerService(str(itfs), service, new Properties()));
                LOGGER.info("EJB registered: {0} for interfaces {1}", beanContext.getEjbName(), interfaces);
            } catch (IllegalArgumentException iae) {
                LOGGER.error("can't register: {0} for interfaces {1}", beanContext.getEjbName(), interfaces);
            }
        }
    }

    public Set<Bundle> deployedBundles() {
        return paths.keySet();
    }

    private static String[] str(final Class<?>[] itfs) {
        final String[] itfsStr = new String[itfs.length];
        for (int i = 0; i < itfs.length; i++) {
            itfsStr[i] = itfs[i].getName();
        }
        return itfsStr;
    }

    /**
     * using dynamic imports can be too tricky when this class is often enough.
     * Note: user can still refine the version he needs...but manually.
     */
    private static class OSGIClassLoader extends ClassLoader {
        private final Bundle backingBundle;
        private final Bundle fallbackBundle;

        public OSGIClassLoader(final Bundle bundle, final Bundle openejbClassloader) {
            super(null);
            backingBundle = bundle;
            fallbackBundle = openejbClassloader;
        }

        @Override
        public boolean equals(final Object other) {
            return this == other || backingBundle.equals(other);
        }

        @Override
        protected Class findClass(final String name) throws ClassNotFoundException {
            try {
                return fallbackBundle.loadClass(name);
            } catch (Exception ignored) {
                // no-op
            }

            try {
                return this.backingBundle.loadClass(name);
            } catch (ClassNotFoundException cnfe) {
                throw new ClassNotFoundException(name + " not found from bundle [" + backingBundle.getSymbolicName() + "]", cnfe);
            } catch (NoClassDefFoundError ncdfe) {
                final NoClassDefFoundError e = new NoClassDefFoundError(name + " not found from bundle [" + backingBundle + "]");
                e.initCause(ncdfe);
                throw e;
            }
        }

        @Override
        protected URL findResource(final String name) {
            URL url = fallbackBundle.getResource(name);
            if (url != null) {
                return url;
            }
            url = backingBundle.getResource(name);
            if (url != null) {
                return url;
            }
            return null;
        }

        @Override
        public Enumeration<URL> getResources(final String name) throws IOException {
            return findResources(name);
        }

        @Override
        protected Enumeration<URL> findResources(final String name) throws IOException {
            Enumeration<URL> urls;
            try {
                urls = fallbackBundle.getResources(name);
                if (urls != null && urls.hasMoreElements()) {
                    return urls;
                }
            } catch (IOException ignored) {
                // no-op
            }
            urls = backingBundle.getResources(name);
            if (urls != null && urls.hasMoreElements()) {
                return urls;
            }
            if (urls != null && urls.hasMoreElements()) {
                return urls;
            }
            return new EmptyEnumeration<URL>();
        }

        @Override
        public URL getResource(final String name) {
            return findResource(name);
        }

        @Override
        protected Class loadClass(final String name, final boolean resolve) throws ClassNotFoundException {
            final Class clazz = findClass(name);
            if (resolve) {
                resolveClass(clazz);
            }
            return clazz;
        }

        public String toString() {
            return "OSGIClassLoader for [" + backingBundle + "]";
        }
    }

    private static class EmptyEnumeration<T> implements Enumeration<T> {
        @Override
        public boolean hasMoreElements() {
            return false;
        }

        @Override
        public T nextElement() {
            throw new NoSuchElementException();
        }
    }

    public class OSGiDeploymentLoader extends DeploymentLoader {
        private final Bundle bundle;

        public OSGiDeploymentLoader(final Bundle bdl) {
            bundle = bdl;
        }

        @Override
        protected ClassLoader getOpenEJBClassLoader() {
            return new OSGIClassLoader(bundle, OpenEJBBundleContextHolder.get().getBundle());
        }
    }
}

