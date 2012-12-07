/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *     contributor license agreements.  See the NOTICE file distributed with
 *     this work for additional information regarding copyright ownership.
 *     The ASF licenses this file to You under the Apache License, Version 2.0
 *     (the "License"); you may not use this file except in compliance with
 *     the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 */

package org.apache.openejb.server.rest;

import org.apache.openejb.BeanContext;
import org.apache.openejb.BeanType;
import org.apache.openejb.Injection;
import org.apache.openejb.assembler.classic.AppInfo;
import org.apache.openejb.assembler.classic.Assembler;
import org.apache.openejb.assembler.classic.EjbJarInfo;
import org.apache.openejb.assembler.classic.EnterpriseBeanInfo;
import org.apache.openejb.assembler.classic.IdPropertiesInfo;
import org.apache.openejb.assembler.classic.ServiceInfo;
import org.apache.openejb.assembler.classic.WebAppInfo;
import org.apache.openejb.assembler.classic.event.AssemblerAfterApplicationCreated;
import org.apache.openejb.assembler.classic.event.AssemblerBeforeApplicationDestroyed;
import org.apache.openejb.assembler.classic.util.PojoUtil;
import org.apache.openejb.assembler.classic.util.ServiceConfiguration;
import org.apache.openejb.core.CoreContainerSystem;
import org.apache.openejb.core.WebContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.observer.Observes;
import org.apache.openejb.server.SelfManaging;
import org.apache.openejb.server.ServerService;
import org.apache.openejb.server.ServiceException;
import org.apache.openejb.server.ServiceManager;
import org.apache.openejb.server.httpd.HttpListener;
import org.apache.openejb.server.httpd.HttpListenerRegistry;
import org.apache.openejb.spi.ContainerSystem;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.xbean.finder.MetaAnnotatedClass;

import javax.naming.Context;
import javax.ws.rs.ApplicationPath;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public abstract class RESTService implements ServerService, SelfManaging {
    public static final Logger LOGGER = Logger.getInstance(LogCategory.OPENEJB_RS, RESTService.class);
    private static final boolean OLD_WEBSERVICE_DEPLOYMENT = SystemInstance.get().getOptions().get("openejb.webservice.old-deployment", false);
    public static final String OPENEJB_JAXRS_PROVIDERS_AUTO_PROP = "openejb.jaxrs.providers.auto";

    private static final String IP = "n/a";
    private static final int PORT = -1;
    public static final String NOPATH_PREFIX = "http://nopath/";

    private final Set<AppInfo> deployedApplications = new HashSet<AppInfo>();
    private final Set<WebAppInfo> deployedWebApps = new HashSet<WebAppInfo>();
    private Assembler assembler;
    private CoreContainerSystem containerSystem;
    private RsRegistry rsRegistry;
    private List<DeployedService> services = new ArrayList<DeployedService>();
    private String virtualHost;
    private boolean enabled = true;
    private String wildcard = SystemInstance.get().getProperty("openejb.rest.wildcard", ".*");

    public void afterApplicationCreated(final AppInfo appInfo, final WebAppInfo webApp) {
        final Map<String, EJBRestServiceInfo> restEjbs = getRestEjbs(appInfo);

        final WebContext webContext = containerSystem.getWebContext(webApp.moduleId);
        if (webContext == null) {
            return;
        }

        if (!deployedWebApps.add(webApp)) {
            return;
        }

        final ClassLoader classLoader = getClassLoader(webContext.getClassLoader());
        final Collection<Injection> injections = webContext.getInjections();
        final WebBeansContext owbCtx;
        if (webContext.getWebbeansContext() != null) {
            owbCtx = webContext.getWebbeansContext();
        } else {
            owbCtx = webContext.getAppContext().getWebBeansContext();
        }

        Context context = webContext.getJndiEnc();
        if (context == null) { // usually true since it is set in org.apache.tomee.catalina.TomcatWebAppBuilder.afterStart() and lookup(comp) fails
            context = webContext.getAppContext().getAppJndiContext();
        }

        final Collection<Object> additionalProviders = new HashSet<Object>();
        if (useDiscoveredProviders()) {
            for (String name : webApp.jaxRsProviders) {
                try {
                    additionalProviders.add(classLoader.loadClass(name));
                } catch (ClassNotFoundException e) {
                    LOGGER.warning("can't load '" + name + "'", e);
                }
            }
            additionalProviders.addAll(appProviders(appInfo, classLoader));
        }

        final ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(classLoader);

        try {
            // The spec says:
            //
            // "The resources and providers that make up a JAX-RS application are configured via an application-supplied
            // subclass of Application. An implementation MAY provide alternate mechanisms for locating resource
            // classes and providers (e.g. runtime class scanning) but use of Application is the only portable means of
            //  configuration."
            //
            //  The choice here is to deploy using the Application if it exists or to use the scanned classes
            //  if there is no Application.
            //
            //  Like this providing an Application subclass user can totally control deployed services.

            boolean useApp = false;
            String appPrefix = webApp.contextRoot;
            Collection<IdPropertiesInfo> pojoConfigurations = null; // done lazily
            for (String app : webApp.restApplications) { // normally a unique one but we support more
                appPrefix = webApp.contextRoot; // if multiple application classes reinit it
                if (!appPrefix.endsWith("/")) {
                    appPrefix += "/";
                }

                Application appInstance;
                Class<?> appClazz;
                try {
                    appClazz = classLoader.loadClass(app);
                    appInstance = Application.class.cast(appClazz.newInstance());
                } catch (Exception e) {
                    throw new OpenEJBRestRuntimeException("can't create class " + app, e);
                }

                ApplicationPath path = appClazz.getAnnotation(ApplicationPath.class);
                if (path != null) {
                    String appPath = path.value();
                    if (appPath.startsWith("/")) {
                        appPrefix += appPath.substring(1);
                    } else {
                        appPrefix += appPath;
                    }
                }

                Set<Class<?>> classes = appInstance.getClasses();
                Set<Object> singletons = appInstance.getSingletons();

                // look for providers
                for (Class<?> clazz : classes) {
                    if (isProvider(clazz)) {
                        additionalProviders.add(clazz);
                    }
                }
                for (Object obj : singletons) {
                    if (obj != null && isProvider(obj.getClass())) {
                        additionalProviders.add(obj);
                    }
                }

                for (Object o : singletons) {
                    if (o == null) {
                        continue;
                    }

                    if (hasEjbAndIsNotAManagedBean(restEjbs, o.getClass().getName())) {
                        // no more a singleton if the ejb is not a singleton...but it is a weird case
                        deployEJB(appPrefix, restEjbs.get(o.getClass().getName()).context, additionalProviders, appInfo.services);
                    } else {
                        pojoConfigurations = PojoUtil.findPojoConfig(pojoConfigurations, appInfo, webApp);
                        deploySingleton(appPrefix, o, appInstance, classLoader, additionalProviders,
                                new ServiceConfiguration(PojoUtil.findConfiguration(pojoConfigurations, o.getClass().getName()), appInfo.services));
                    }
                }

                for (Class<?> clazz : classes) {
                    if (additionalProviders.contains(clazz)) {
                        continue;
                    }

                    if (hasEjbAndIsNotAManagedBean(restEjbs, clazz.getName())) {
                        deployEJB(appPrefix, restEjbs.get(clazz.getName()).context, additionalProviders, appInfo.services);
                    } else {
                        pojoConfigurations = PojoUtil.findPojoConfig(pojoConfigurations, appInfo, webApp);
                        deployPojo(appPrefix, clazz, appInstance, classLoader, injections, context, owbCtx, additionalProviders,
                                new ServiceConfiguration(PojoUtil.findConfiguration(pojoConfigurations, clazz.getName()), appInfo.services));
                    }
                }

                useApp = useApp || classes.size() + singletons.size() > 0;
                LOGGER.info("REST application deployed: " + app);
            }

            if (!useApp) {
                final Set<String> restClasses = new HashSet<String>(webApp.restClass);
                restClasses.addAll(webApp.ejbRestServices);

                for (String clazz : restClasses) {
                    if (restEjbs.containsKey(clazz)) {
                        final BeanContext ctx = restEjbs.get(clazz).context;
                        if (hasEjbAndIsNotAManagedBean(restEjbs, clazz)) {
                            deployEJB(appPrefix, restEjbs.get(clazz).context, additionalProviders, appInfo.services);
                        } else {
                            deployPojo(appPrefix, ctx.getBeanClass(), null, ctx.getClassLoader(), ctx.getInjections(), context,
                                    owbCtx, additionalProviders, new ServiceConfiguration(ctx.getProperties(), appInfo.services));
                        }
                    } else {
                        try {
                            Class<?> loadedClazz = classLoader.loadClass(clazz);
                            pojoConfigurations = PojoUtil.findPojoConfig(pojoConfigurations, appInfo, webApp);
                            deployPojo(appPrefix, loadedClazz, null, classLoader, injections, context, owbCtx,
                                    additionalProviders,
                                    new ServiceConfiguration(PojoUtil.findConfiguration(pojoConfigurations, loadedClazz.getName()), appInfo.services));
                        } catch (ClassNotFoundException e) {
                            throw new OpenEJBRestRuntimeException("can't find class " + clazz, e);
                        }
                    }
                }
            }

            restEjbs.clear();
        } finally {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
    }

    private static <T> boolean isProvider(final Class<T> clazz) {
        return new MetaAnnotatedClass<T>(clazz).isAnnotationPresent(Provider.class);
    }

    private boolean hasEjbAndIsNotAManagedBean(final Map<String, EJBRestServiceInfo> restEjbs, final String clazz) {
        return restEjbs.containsKey(clazz) && !BeanType.MANAGED.equals(restEjbs.get(clazz).context.getComponentType());
    }

    private boolean useDiscoveredProviders() {
        return SystemInstance.get().getOptions().get(OPENEJB_JAXRS_PROVIDERS_AUTO_PROP, false);
    }

    private Collection<Object> appProviders(final AppInfo appInfo, final ClassLoader classLoader) {
        final Collection<Object> additionalProviders = new HashSet<Object>();
        for (String name : appInfo.jaxRsProviders) {
            try {
                additionalProviders.add(classLoader.loadClass(name));
            } catch (ClassNotFoundException e) {
                LOGGER.warning("can't load '" + name + "'", e);
            }
        }
        return additionalProviders;
    }

    public void afterApplicationCreated(@Observes final AssemblerAfterApplicationCreated event) {
        if (!enabled) return;

        final AppInfo appInfo = event.getApp();
        if (deployedApplications.add(appInfo)) {
            if (appInfo.webApps.size() == 0) {
                final Map<String, EJBRestServiceInfo> restEjbs = getRestEjbs(appInfo);
                final Collection<Object> providers;
                if (useDiscoveredProviders()) {
                    providers = appProviders(appInfo, containerSystem.getAppContext(appInfo.appId).getClassLoader());
                } else {
                    providers = new ArrayList<Object>();
                }

                for (Map.Entry<String, EJBRestServiceInfo> ejb : restEjbs.entrySet()) {
                    final BeanContext ctx = ejb.getValue().context;
                    if (BeanType.MANAGED.equals(ctx.getComponentType())) {
                        deployPojo(ejb.getValue().path, ctx.getBeanClass(), null, ctx.getClassLoader(), ctx.getInjections(),
                                ctx.getJndiContext(),
                                containerSystem.getAppContext(appInfo.appId).getWebBeansContext(),
                                providers, new ServiceConfiguration(ctx.getProperties(), appInfo.services));
                    } else {
                        deployEJB(ejb.getValue().path, ctx, providers, appInfo.services);
                    }
                }
                restEjbs.clear();
            } else {
                for (final WebAppInfo webApp : appInfo.webApps) {
                    afterApplicationCreated(appInfo, webApp);
                }
            }
        }
    }

    protected Map<String,EJBRestServiceInfo> getRestEjbs(AppInfo appInfo) {
        Map<String, BeanContext> beanContexts = new HashMap<String, BeanContext>();
        for (EjbJarInfo ejbJar : appInfo.ejbJars) {
            for (EnterpriseBeanInfo bean : ejbJar.enterpriseBeans) {
                if (bean.restService) {
                    BeanContext beanContext = containerSystem.getBeanContext(bean.ejbDeploymentId);
                    if (beanContext == null) {
                        continue;
                    }

                    beanContexts.put(bean.ejbClass, beanContext);
                }
            }
        }

        Map<String, EJBRestServiceInfo> restEjbs = new HashMap<String, EJBRestServiceInfo>();
        for (WebAppInfo webApp : appInfo.webApps) {
            for (String ejb : webApp.ejbRestServices) {
                restEjbs.put(ejb, new EJBRestServiceInfo(webApp.contextRoot, beanContexts.get(ejb)));
            }
        }
        for (Map.Entry<String, BeanContext> ejbs : beanContexts.entrySet()) {
            final String clazz = ejbs.getKey();
            if (!restEjbs.containsKey(clazz)) {
                // null is important, it means there is no webroot path in standalone
                String context = null;
                if (!OLD_WEBSERVICE_DEPLOYMENT) {
                    if (appInfo.appId != null && !appInfo.appId.isEmpty()) {
                        context = appInfo.appId;
                    } else {
                        context = ejbs.getValue().getModuleName();
                    }
                }
                restEjbs.put(clazz, new EJBRestServiceInfo(context, beanContexts.get(clazz)));
            }
        }
        beanContexts.clear();

        return restEjbs;
    }

    private void deploySingleton(String contextRoot, Object o, Application appInstance, ClassLoader classLoader,
                                 Collection<Object> additionalProviders, ServiceConfiguration configuration) {
        final String nopath = getAddress(contextRoot, o.getClass());
        final RsHttpListener listener = createHttpListener();
        final RsRegistry.AddressInfo address = rsRegistry.createRsHttpListener(contextRoot, listener, classLoader, nopath.substring(NOPATH_PREFIX.length() - 1), virtualHost);

        services.add(new DeployedService(address.complete, contextRoot, o.getClass().getName()));
        listener.deploySingleton(getFullContext(address.base, contextRoot), o, appInstance, additionalProviders, configuration);

        LOGGER.info("deployed REST singleton: " + o);
    }

    private void deployPojo(String contextRoot, Class<?> loadedClazz, Application app, ClassLoader classLoader, Collection<Injection> injections,
                            Context context, WebBeansContext owbCtx, Collection<Object> additionalProviders, ServiceConfiguration config) {
        if (loadedClazz.isInterface()) {
            return;
        }

        final String nopath = getAddress(contextRoot, loadedClazz);
        final RsHttpListener listener = createHttpListener();
        final RsRegistry.AddressInfo address = rsRegistry.createRsHttpListener(contextRoot, listener, classLoader, nopath.substring(NOPATH_PREFIX.length() - 1), virtualHost);

        services.add(new DeployedService(address.complete, contextRoot, loadedClazz.getName()));
        listener.deployPojo(getFullContext(address.base, contextRoot), loadedClazz, app, injections, context, owbCtx,
                additionalProviders, config);

        LOGGER.info("REST Service: " + address.complete + "  -> Pojo " + loadedClazz.getName());
    }

    private void deployEJB(String context, BeanContext beanContext, Collection<Object> additionalProviders, Collection<ServiceInfo> serviceInfos) {
        final String nopath = getAddress(context, beanContext.getBeanClass());
        final RsHttpListener listener = createHttpListener();
        final RsRegistry.AddressInfo address = rsRegistry.createRsHttpListener(context, listener, beanContext.getClassLoader(), nopath.substring(NOPATH_PREFIX.length() - 1), virtualHost);

        services.add(new DeployedService(address.complete, context, beanContext.getBeanClass().getName()));
        listener.deployEJB(getFullContext(address.base, context), beanContext,
                additionalProviders, new ServiceConfiguration(beanContext.getProperties(), serviceInfos));

        LOGGER.info("REST Service: " + address.complete + "  -> EJB " + beanContext.getEjbName());
    }

    /**
     * It creates the service container (http listener).
     *
     * @return the service container
     */
    protected abstract RsHttpListener createHttpListener();

    private static String getFullContext(String address, String context) {
        if (context == null) {
            return address;
        }
        if (context.isEmpty() && address.contains("/")) {
            return address.substring(0, address.lastIndexOf("/"));
        }

        // context can get the app path too
        // so keep only web context without /
        String webCtx = context;
        while (webCtx.startsWith("/")) {
            webCtx = webCtx.substring(1);
        }

        // get root path ending with /
        try {
            final URL url = new URL(address);
            final int port = url.getPort();
            if (port > 0) {
                return url.getProtocol() + "://" + url.getHost() + ":" + port + "/" + webCtx;
            } else {
                return url.getProtocol() + "://" + url.getHost() + "/" + webCtx;
            }
        } catch (MalformedURLException e) {
            throw new OpenEJBRestRuntimeException("bad url: " + address, e);
        }
    }

    private Class<?> findPath(final Class<?> clazz) {
        Class<?> usedClass = clazz;
        while (usedClass.getAnnotation(Path.class) == null && usedClass.getSuperclass() != null) {
            usedClass = usedClass.getSuperclass();
        }
        return usedClass;
    }

    private String getAddress(String context, Class<?> clazz) {
        String root = NOPATH_PREFIX;
        if (context != null) {
            if (context.startsWith("/")) {
                root += context.substring(1);
            } else {
                root += context;
            }
        }

        Class<?> usedClass = findPath(clazz);
        if (usedClass == null || Object.class.equals(usedClass)) { // try interfaces
            final Class<?>[] itfs = clazz.getInterfaces();
            if (itfs != null) {
                for (Class<?> c : itfs) {
                    usedClass = findPath(c);
                    if (usedClass.getAnnotation(Path.class) != null ) {
                        break;
                    }
                }
            }

        }
        if (usedClass == null || usedClass.getAnnotation(Path.class) == null) {
            throw new IllegalArgumentException("no @Path annotation on " + clazz.getName());
        }

        String builtUrl = null;
        try {
            builtUrl = UriBuilder.fromUri(new URI(root)).path(usedClass).build().toURL().toString();
            return replaceParams(builtUrl); // pathparam at class level
        } catch (IllegalArgumentException iae) {
            if (builtUrl != null) {
                return builtUrl;
            }

            // try to do it manually with @Path on the class
            Class<?> current = usedClass;
            while (current != null && !Object.class.equals(current)) {
                Path path = current.getAnnotation(Path.class);
                if (path != null) {
                    String classPath = path.value();
                    if (classPath.startsWith("/")) {
                        classPath = classPath.substring(1);
                    }
                    if (!root.endsWith("/")) {
                        root = root + "/";
                    }
                    return replaceParams(root + classPath);
                }
                current = current.getSuperclass();
            }

            throw new OpenEJBRestRuntimeException("can't built the service mapping for service '" + usedClass.getName() + "'", iae);
        } catch (MalformedURLException e) {
            throw new OpenEJBRestRuntimeException("url is malformed", e);
        } catch (URISyntaxException e) {
            throw new OpenEJBRestRuntimeException("uri syntax is not correct", e);
        }
    }

    // this mean not really conflicting mappings (rest/servlet and so on) can be conflicting
    // a good solution is to handle a unique rest servlet managing the routing
    private String replaceParams(final String url) {
        final String managedUrl = url.replaceAll("\\{[^}]*\\}.*", wildcard);
        if (managedUrl.endsWith(wildcard)) {
            return managedUrl;
        }
        return managedUrl + "/" + wildcard;
    }

    private void undeployRestObject(String context) {
        HttpListener listener = rsRegistry.removeListener(context);
        if (listener != null) {
            RsHttpListener.class.cast(listener).undeploy();
        }
    }

    private static ClassLoader getClassLoader(ClassLoader classLoader) {
        ClassLoader cl = classLoader;
        if (cl == null) {
            cl = Thread.currentThread().getContextClassLoader();
        }
        if (cl == null) {
            cl = RESTService.class.getClassLoader();
        }
        return cl;
    }

    public void beforeApplicationDestroyed(@Observes AssemblerBeforeApplicationDestroyed event) {
        final AppInfo app = event.getApp();
        if (deployedApplications.contains(app)) {
            for (WebAppInfo webApp : app.webApps) {
                final List<DeployedService> toRemove = new ArrayList<DeployedService>();
                for (DeployedService service : services) {
                    if (service.isInWebApp(webApp)) {
                        undeployRestObject(service.address);
                        toRemove.add(service);
                    }
                }
                services.removeAll(toRemove);
                deployedWebApps.remove(webApp);
            }
        }
    }

    @Override public void start() throws ServiceException {
        SystemInstance.get().setComponent(RESTService.class, this);

        beforeStart();

        containerSystem = (CoreContainerSystem) SystemInstance.get().getComponent(ContainerSystem.class);
        assembler = SystemInstance.get().getComponent(Assembler.class);
        if (assembler != null) {
            SystemInstance.get().addObserver(this);
            for (AppInfo appInfo : assembler.getDeployedApplications()) {
                afterApplicationCreated(new AssemblerAfterApplicationCreated(appInfo));
            }
        }
    }

    protected void beforeStart() {
        rsRegistry = SystemInstance.get().getComponent(RsRegistry.class);
        if (rsRegistry == null && SystemInstance.get().getComponent(HttpListenerRegistry.class) != null) {
            rsRegistry = new RsRegistryImpl();
        }
    }

    @Override public void stop() throws ServiceException {
        if (assembler != null) {
            SystemInstance.get().removeObserver(this);
            for (AppInfo appInfo : new ArrayList<AppInfo>(deployedApplications)) {
                beforeApplicationDestroyed(new AssemblerBeforeApplicationDestroyed(appInfo));
            }
        }

        for (DeployedService service : services) {
            undeployRestObject(service.address);
        }
    }

    @Override public void service(InputStream in, OutputStream out) throws ServiceException, IOException {
        throw new UnsupportedOperationException(getClass().getName() + " cannot be invoked directly");
    }

    @Override public void service(Socket socket) throws ServiceException, IOException {
        throw new UnsupportedOperationException(getClass().getName() + " cannot be invoked directly");
    }

    @Override public String getIP() {
        return IP;
    }

    @Override public int getPort() {
        return PORT;
    }

    @Override public void init(Properties props) throws Exception {
        virtualHost = props.getProperty("virtualHost");
        enabled = ServiceManager.isEnabled(props);
    }

    public String getVirtualHost() {
        return virtualHost;
    }

    public void setVirtualHost(String virtualHost) {
        this.virtualHost = virtualHost;
    }

    public static class EJBRestServiceInfo {
        public String path;
        public BeanContext context;

        public EJBRestServiceInfo(String path, BeanContext context) {
            if (context == null) {
                throw new OpenEJBRestRuntimeException("can't find context");
            }

            this.path = path;
            this.context = context;
        }
    }

    // look WebServiceHelperImpl before updating it
    public static class DeployedService {
        public String address;
        public String webapp;
        public String origin;

        public DeployedService(final String address, final String webapp, final String origin) {
            this.address = address;
            this.webapp = webapp;
            this.origin = origin;
        }

        public boolean isInWebApp(final WebAppInfo webApp) {
            return webApp.contextRoot == webapp || (webapp != null && webapp.startsWith(webApp.contextRoot));
        }
    }

    public List<DeployedService> getServices() {
        return services;
    }
}
