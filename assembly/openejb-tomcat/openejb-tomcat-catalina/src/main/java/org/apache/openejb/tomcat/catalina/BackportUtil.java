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
package org.apache.openejb.tomcat.catalina;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletRequest;

import org.apache.catalina.Service;
import org.apache.catalina.Valve;
import org.apache.catalina.Wrapper;
import org.apache.catalina.core.NamingContextListener;
import org.apache.catalina.core.StandardContext;
import org.apache.catalina.core.StandardWrapper;
import org.apache.catalina.deploy.ContextService;
import org.apache.catalina.deploy.NamingResources;
import org.apache.catalina.deploy.ResourceBase;
import org.apache.naming.ContextAccessController;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.SecurityService;
import org.apache.openejb.tomcat.common.TomcatVersion;
import org.apache.openejb.util.OpenEjbVersion;

/**
 * @version $Rev$ $Date$
 */
public class BackportUtil {

    private static final API api;

    static {
        if (TomcatVersion.v6.isTheVersion()){
            api = new Tomcat6();
        } else if (TomcatVersion.v55.isTheVersion()){
            api = new Tomcat55();
        } else {
            api = new Tomcat50();
        }
    }

    private static API getAPI() {
        return api;
    }

    public static Servlet getServlet(Wrapper wrapper) {
        return getAPI().getServlet(wrapper);
    }

    public static NamingContextListener getNamingContextListener(StandardContext standardContext) {
        return getAPI().getNamingContextListener(standardContext);
    }

    public static String findServiceName(NamingResources naming, String refName) {
        return getAPI().findServiceName(naming, refName);
    }

    public static void removeService(NamingContextListener namingContextListener, String serviceName) {
        getAPI().removeService(namingContextListener, serviceName);
    }

    public static List<Connector> findConnectors(Service service) {
        return getAPI().findConnectors(service);
    }

    public static ContextResourceEnvRef createResourceEnvRef(String name, String type) {
        return getAPI().createResourceEnvRef(name, type);
    }

    public static ContextResourceEnvRef findResourceEnvRef(NamingResources namingResources, String name) {
        return getAPI().findResourceEnvRef(namingResources, name);
    }

    public static List<ContextResourceEnvRef> findResourceEnvRefs(NamingResources namingResources) {
        return getAPI().findResourceEnvRefs(namingResources);
    }

    public static void addResourceEnvRef(NamingResources naming, ContextResourceEnvRef ref) {
        getAPI().addResourceEnvRef(naming, ref);
    }

    public static void replaceResourceEnvRefInListener(NamingContextListener namingContextListener, StandardContext standardContext, boolean addEntry, ContextResourceEnvRef ref) {
        getAPI().replaceResourceEnvRefInListener(namingContextListener, standardContext, addEntry, ref);
    }

    public static Valve newOpenEJBValve() {
        return getAPI().newOpenEJBValve();
    }

    public static void setRefProperty(Object ref, String refName, NamingResources naming, String propertyName, String propertyValue) {
        getAPI().setRefProperty(ref, refName, naming, propertyName, propertyValue);
    }

    public static interface API {
        Servlet getServlet(Wrapper wrapper);
        String findServiceName(NamingResources naming, String refName);
        NamingContextListener getNamingContextListener(StandardContext standardContext);
        void removeService(NamingContextListener namingContextListener, String serviceName);
        List<Connector> findConnectors(Service service);
        ContextResourceEnvRef findResourceEnvRef(NamingResources naming, String name);
        List<ContextResourceEnvRef> findResourceEnvRefs(NamingResources naming);
        void addResourceEnvRef(NamingResources naming, ContextResourceEnvRef ref);
        void replaceResourceEnvRefInListener(NamingContextListener namingContextListener, StandardContext standardContext, boolean addEntry, ContextResourceEnvRef ref);
        Valve newOpenEJBValve();
        void setRefProperty(Object ref, String refName, NamingResources naming, String propertyName, String propertyValue);
        ContextResourceEnvRef createResourceEnvRef(String name, String type);
    }

    public static class Connector {
        private final String scheme;
        private final int port;

        public Connector(String scheme, int port) {
            this.scheme = scheme;
            this.port = port;
        }

        public String getScheme() {
            return scheme;
        }

        public int getPort() {
            return port;
        }
    }

    public interface ContextResourceEnvRef {
        String getName();

        String getType();

        void setType(String type);
    }

    public static class Tomcat50ResourceEnvRef implements ContextResourceEnvRef {
        private final String name;
        private String type;

        public Tomcat50ResourceEnvRef(String name, String type) {
            this.name = name;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

    public static class Tomcat6ResourceEnvRef implements ContextResourceEnvRef {
        private final org.apache.catalina.deploy.ContextResourceEnvRef ref;

        public Tomcat6ResourceEnvRef(org.apache.catalina.deploy.ContextResourceEnvRef ref) {
            this.ref = ref;
        }

        public String getName() {
            return ref.getName();
        }

        public String getType() {
            return ref.getType();
        }

        public void setType(String s) {
            ref.setType(s);
        }

        public void setProperty(String s, Object o) {
            ref.setProperty(s, o);
        }
    }

    public static class Tomcat6 implements API {
        public Servlet getServlet(Wrapper wrapper) {
            return wrapper.getServlet();
        }

        public NamingContextListener getNamingContextListener(StandardContext standardContext) {
            return standardContext.getNamingContextListener();
        }

        public String findServiceName(NamingResources naming, String referenceName) {
            ContextService service = naming.findService(referenceName);
            return (service != null)?service.getName():null;
        }

        public void removeService(NamingContextListener namingContextListener, String serviceName) {
            namingContextListener.removeService(serviceName);
        }

        public List<Connector> findConnectors(Service service) {
            List<Connector> connectors = new ArrayList<Connector>();
            for (org.apache.catalina.connector.Connector connector : service.findConnectors()) {
                connectors.add(new Connector(connector.getScheme(), connector.getPort()));
            }
            return connectors;
        }

        public ContextResourceEnvRef createResourceEnvRef(String name, String type) {
            org.apache.catalina.deploy.ContextResourceEnvRef ref = new org.apache.catalina.deploy.ContextResourceEnvRef();
            ref.setName(name);
            ref.setType(type);
            return new Tomcat6ResourceEnvRef(ref);
        }

        public ContextResourceEnvRef findResourceEnvRef(NamingResources namingResources, String name) {
            org.apache.catalina.deploy.ContextResourceEnvRef ref = namingResources.findResourceEnvRef(name);
            if (ref == null) return null;
            return new Tomcat6ResourceEnvRef(ref);
        }

        public List<ContextResourceEnvRef> findResourceEnvRefs(NamingResources namingResources) {
            List<ContextResourceEnvRef> resourceEnvRefs = new ArrayList<ContextResourceEnvRef>();
            for (org.apache.catalina.deploy.ContextResourceEnvRef ref : namingResources.findResourceEnvRefs()) {
                resourceEnvRefs.add(new Tomcat6ResourceEnvRef(ref));
            }
            return resourceEnvRefs;
        }

        public void addResourceEnvRef(NamingResources naming, ContextResourceEnvRef ref) {
            naming.addResourceEnvRef(((Tomcat6ResourceEnvRef) ref).ref);
        }

        public void replaceResourceEnvRefInListener(NamingContextListener namingContextListener, StandardContext standardContext, boolean addEntry, ContextResourceEnvRef ref) {
            ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
            if (!addEntry) namingContextListener.removeResourceEnvRef(ref.getName());
            namingContextListener.addResourceEnvRef(((Tomcat6ResourceEnvRef) ref).ref);
            ContextAccessController.setReadOnly(namingContextListener.getName());
        }

        public Valve newOpenEJBValve() {
            return new OpenEJBValve();
        }

        public void setRefProperty(Object ref, String refName, NamingResources naming, String propertyName, String propertyValue) {
            if (ref instanceof ResourceBase) {
                ResourceBase resourceBase = (ResourceBase) ref;
                resourceBase.setProperty(propertyName, propertyValue);
            }
            if (ref instanceof Tomcat6ResourceEnvRef) {
                Tomcat6ResourceEnvRef contextResourceEnvRef = (Tomcat6ResourceEnvRef) ref;
                contextResourceEnvRef.ref.setProperty(propertyName, propertyValue);
            }
        }
    }

    public static class Tomcat55 implements API {
        private final Field namingContextListener;
        private final Field instance;

        public Tomcat55() {
            namingContextListener = getField(StandardContext.class, "namingContextListener");
            instance = getField(StandardWrapper.class, "instance");
        }


        public Servlet getServlet(Wrapper wrapper) {
            try {
                return (Servlet) instance.get(wrapper);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        public NamingContextListener getNamingContextListener(StandardContext standardContext) {
            try {
                return (NamingContextListener) namingContextListener.get(standardContext);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
        }

        public String findServiceName(NamingResources naming, String refName) {
            return null;
        }

        public void removeService(NamingContextListener namingContextListener, String serviceName) {
        }


        public List<Connector> findConnectors(Service service) {
            List<Connector> connectors = new ArrayList<Connector>();
            for (org.apache.catalina.connector.Connector connector : service.findConnectors()) {
                connectors.add(new Connector(connector.getScheme(), connector.getPort()));
            }
            return connectors;
        }

        public ContextResourceEnvRef createResourceEnvRef(String name, String type) {
            org.apache.catalina.deploy.ContextResourceEnvRef ref = new org.apache.catalina.deploy.ContextResourceEnvRef();
            ref.setName(name);
            ref.setType(type);
            return new Tomcat6ResourceEnvRef(ref);
        }

        public ContextResourceEnvRef findResourceEnvRef(NamingResources namingResources, String name) {
            org.apache.catalina.deploy.ContextResourceEnvRef ref = namingResources.findResourceEnvRef(name);
            if (ref == null) return null;
            return new Tomcat6ResourceEnvRef(ref);
        }

        public List<ContextResourceEnvRef> findResourceEnvRefs(NamingResources namingResources) {
            List<ContextResourceEnvRef> resourceEnvRefs = new ArrayList<ContextResourceEnvRef>();
            for (org.apache.catalina.deploy.ContextResourceEnvRef ref : namingResources.findResourceEnvRefs()) {
                resourceEnvRefs.add(new Tomcat6ResourceEnvRef(ref));
            }
            return resourceEnvRefs;
        }

        public void addResourceEnvRef(NamingResources naming, ContextResourceEnvRef ref) {
            naming.addResourceEnvRef(((Tomcat6ResourceEnvRef) ref).ref);
        }

        public void replaceResourceEnvRefInListener(NamingContextListener namingContextListener, StandardContext standardContext, boolean addEntry, ContextResourceEnvRef ref) {
            ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
            if (!addEntry) namingContextListener.removeResourceEnvRef(ref.getName());
            namingContextListener.addResourceEnvRef(((Tomcat6ResourceEnvRef) ref).ref);
            ContextAccessController.setReadOnly(namingContextListener.getName());
        }

        public Valve newOpenEJBValve() {
            return new OpenEJBValve();
        }

        public void setRefProperty(Object ref, String refName, NamingResources naming, String propertyName, String propertyValue) {
            if (ref instanceof ResourceBase) {
                ResourceBase resourceBase = (ResourceBase) ref;
                resourceBase.setProperty(propertyName, propertyValue);
            }
            if (ref instanceof Tomcat6ResourceEnvRef) {
                Tomcat6ResourceEnvRef contextResourceEnvRef = (Tomcat6ResourceEnvRef) ref;
                contextResourceEnvRef.ref.setProperty(propertyName, propertyValue);
            }
        }

        private Field getField(final Class clazz, final String name) {
            return AccessController.doPrivileged(new PrivilegedAction<Field>() {
                public Field run() {
                    try {
                        Field field = clazz.getDeclaredField(name);
                        field.setAccessible(true);
                        return field;
                    } catch (Exception e2) {
                        throw (IllegalStateException) new IllegalStateException("Unable to find or access the '"+name+"' field in "+clazz.getName()).initCause(e2);
                    }
                }
            });
        }
    }

    public static class Tomcat50 extends Tomcat55 {
        private final Method findConnectors;
        private final Method getScheme;
        private final Method findResourceEnvRef;
        private final Method findResourceEnvRefs;
        private final Method addResourceEnvRef;
        private final Method findResourceParams;
        private final Method addResourceParams;

        private final Method listenerAddResourceEnvRef;
        private final Method listenerRemoveResourceEnvRef;

        protected Class<?> resourceParamsClass;
        private final Method setResourceParamsName;
        private final Method addParameter;

        public Tomcat50() {
            try {
                findConnectors = Service.class.getMethod("findConnectors");
                Class<?> connectorClass = Service.class.getClassLoader().loadClass("org.apache.catalina.Connector");
                getScheme = connectorClass.getMethod("getScheme");

                resourceParamsClass = Service.class.getClassLoader().loadClass("org.apache.catalina.deploy.ResourceParams");
                addParameter = resourceParamsClass.getMethod("addParameter", String.class, String.class);
                setResourceParamsName = resourceParamsClass.getMethod("setName", String.class);

                findResourceEnvRef = NamingResources.class.getMethod("findResourceEnvRef", String.class);
                findResourceEnvRefs = NamingResources.class.getMethod("findResourceEnvRefs");
                addResourceEnvRef = NamingResources.class.getMethod("addResourceEnvRef", String.class, String.class);
                findResourceParams = NamingResources.class.getMethod("findResourceParams", String.class);
                addResourceParams = NamingResources.class.getMethod("addResourceParams", resourceParamsClass);

                listenerAddResourceEnvRef = NamingContextListener.class.getMethod("addResourceEnvRef", String.class, String.class);
                listenerRemoveResourceEnvRef = NamingContextListener.class.getMethod("removeResourceEnvRef", String.class);
            } catch (Exception e) {
                throw (IllegalStateException) new IllegalStateException("Unable to initialize Tomcat 5.0 API translation layer").initCause(e);
            }
        }

        public List<Connector> findConnectors(Service service) {
            try {
                List<Connector> connectors = new ArrayList<Connector>();
                for (Object connector : (Object[])findConnectors.invoke(service)) {
                    int port;
                    try {
                        // getPort is not a method on the connector interface but the implementation classes
                        Method getPort = connector.getClass().getMethod("getPort");
                        port = (Integer) getPort.invoke(connector);
                    } catch (NoSuchMethodException e) {
                        // ignore wacky connector without a port
                        continue;
                    }

                    String scheme = (String) getScheme.invoke(connector);

                    connectors.add(new Connector(scheme, port));
                }
                return connectors;
            } catch (Exception e) {
                throw (IllegalStateException) new IllegalStateException("Error finding Tomcat 5.0 connectors").initCause(e);
            }
        }

        public ContextResourceEnvRef createResourceEnvRef(String name, String type) {
            return new Tomcat50ResourceEnvRef(name, type);
        }

        public ContextResourceEnvRef findResourceEnvRef(NamingResources namingResources, String name) {
            try {
                String type = (String) findResourceEnvRef.invoke(namingResources, name);
                if (type == null) return null;
                return new Tomcat50ResourceEnvRef(name, type);
            } catch (Exception e) {
                throw (IllegalStateException) new IllegalStateException("Error finding Tomcat 5.0 resource env refs").initCause(e);
            }
        }

        public List<ContextResourceEnvRef> findResourceEnvRefs(NamingResources namingResources) {
            try {
                List<ContextResourceEnvRef> resourceEnvRefs = new ArrayList<ContextResourceEnvRef>();
                for (String name : (String[])findResourceEnvRefs.invoke(namingResources)) {
                    String type = (String) findResourceEnvRef.invoke(namingResources, name);
                    resourceEnvRefs.add(new Tomcat50ResourceEnvRef(name, type));
                }
                return resourceEnvRefs;
            } catch (Exception e) {
                throw (IllegalStateException) new IllegalStateException("Error finding Tomcat 5.0 resource env refs").initCause(e);
            }
        }

        public void addResourceEnvRef(NamingResources naming, ContextResourceEnvRef ref) {
            try {
                addResourceEnvRef.invoke(naming, ref.getName(), ref.getType());
            } catch (Exception e) {
                throw (IllegalStateException) new IllegalStateException("Error finding Tomcat 5.0 resource env refs").initCause(e);
            }
        }

        public void replaceResourceEnvRefInListener(NamingContextListener namingContextListener, StandardContext standardContext, boolean addEntry, ContextResourceEnvRef ref) {
            try {
                ContextAccessController.setWritable(namingContextListener.getName(), standardContext);
                if (!addEntry) {
                    listenerRemoveResourceEnvRef.invoke(namingContextListener, ref.getName());
                }
                listenerAddResourceEnvRef.invoke(namingContextListener, ref.getName(), ref.getType());
                ContextAccessController.setReadOnly(namingContextListener.getName());
            } catch (Exception e) {
                throw (IllegalStateException) new IllegalStateException("Error finding Tomcat 5.0 resource env refs").initCause(e);
            }
        }

        public void setRefProperty(Object ref, String refName, NamingResources naming, String propertyName, String propertyValue) {
            try {
                Object resourceParams = findResourceParams.invoke(naming, refName);
                if (resourceParams == null) {
                    resourceParams = resourceParamsClass.newInstance();
                    setResourceParamsName.invoke(resourceParams, refName);
                    addResourceParams.invoke(naming, resourceParams);
                }
                addParameter.invoke(resourceParams, propertyName, propertyValue);
            } catch (Exception e) {
                throw (IllegalStateException) new IllegalStateException("Error finding Tomcat 5.0 resource env refs").initCause(e);
            }
        }

        public Valve newOpenEJBValve() {
            Valve valve = (Valve) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[] {Valve.class}, new ValveInvocationHandler());
            return valve;
        }

        private static class ValveInvocationHandler implements InvocationHandler {
            private static final Method getInfo;
            private static final Method invoke;
            private static final Method invokeNext;
            private static final Method getWrapper;
            private static final Method getRequest;

            static {
                try {
                    getInfo = Valve.class.getMethod("getInfo");

                    ClassLoader classLoader = Valve.class.getClassLoader();
                    Class<?> requestClass = classLoader.loadClass("org.apache.catalina.Request");
                    Class<?> responseClass = classLoader.loadClass("org.apache.catalina.Response");
                    Class<?> valveContext = classLoader.loadClass("org.apache.catalina.ValveContext");
                    invoke = Valve.class.getMethod("invoke", requestClass, responseClass, valveContext);

                    invokeNext = valveContext.getMethod("invokeNext", requestClass, responseClass);
                    getWrapper = requestClass.getMethod("getWrapper");
                    getRequest = requestClass.getMethod("getRequest");
                } catch (Exception e) {
                    throw (IllegalStateException) new IllegalStateException("Unable to initialize Tomcat 5.0 API translation layer").initCause(e);
                }
            }

            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if (getInfo.equals(method)) {
                    return "OpenEJBValve/" + OpenEjbVersion.get().getVersion();
                } else if (invoke.equals(method)) {
                    Object request = args[0];
                    Object response = args[1];
                    Object valveContext = args[2];

                    Object oldState = null;

                    TomcatSecurityService securityService = getSecurityService();
                    Wrapper wrapper = (Wrapper) getWrapper.invoke(request);
                    if (securityService != null && wrapper != null) {
                        HttpServletRequest servletRequest = (HttpServletRequest) getRequest.invoke(request);
                        oldState = securityService.enterWebApp(wrapper.getRealm(), servletRequest.getUserPrincipal(), wrapper.getRunAs());
                    }

                    try {
                        invokeNext.invoke(valveContext, request, response);
                    } finally {
                        if (securityService != null) {
                            securityService.exitWebApp(oldState);
                        }
                    }
                }
                return null;
            }

            private TomcatSecurityService getSecurityService() {
                SecurityService securityService =  SystemInstance.get().getComponent(SecurityService.class);
                if (securityService instanceof TomcatSecurityService) {
                    return (TomcatSecurityService) securityService;
                }
                return null;
            }
        }
    }

}
