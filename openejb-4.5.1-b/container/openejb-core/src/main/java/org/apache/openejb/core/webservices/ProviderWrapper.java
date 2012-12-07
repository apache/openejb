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
package org.apache.openejb.core.webservices;

import org.apache.openejb.OpenEJBRuntimeException;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.LogCategory;
import org.apache.openejb.util.Logger;
import org.w3c.dom.Element;

import javax.jws.WebService;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.transform.Source;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Endpoint;
import javax.xml.ws.EndpointReference;
import javax.xml.ws.Service;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.handler.HandlerResolver;
import javax.xml.ws.soap.SOAPBinding;
import javax.xml.ws.spi.Provider;
import javax.xml.ws.spi.ServiceDelegate;
import javax.xml.ws.wsaddressing.W3CEndpointReference;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class ProviderWrapper extends Provider {
    public static final Logger logger = Logger.getInstance(LogCategory.OPENEJB_WS, ProviderWrapper.class);

    //
    // Magic to get our provider wrapper installed with the PortRefData
    //

    private static ThreadLocal<ProviderWrapperData> threadPortRefs = new ThreadLocal<ProviderWrapperData>();

    public static void beforeCreate(List<PortRefData> portRefData) {
        // Axis JAXWS api is non compliant and checks system property before classloader
        // so we replace system property so this wrapper is selected.  The original value
        // is saved into an openejb property so we can load the class in the find method
        String oldProperty = System.getProperty(JAXWSPROVIDER_PROPERTY);
        if (oldProperty != null && !oldProperty.equals(ProviderWrapper.class.getName())) {
            System.setProperty("openejb." + JAXWSPROVIDER_PROPERTY, oldProperty);
            System.setProperty(JAXWSPROVIDER_PROPERTY, ProviderWrapper.class.getName());
        }

        System.setProperty(JAXWSPROVIDER_PROPERTY, ProviderWrapper.class.getName());
        
        ClassLoader oldClassLoader = Thread.currentThread().getContextClassLoader();
        if (oldClassLoader != null) {
            Thread.currentThread().setContextClassLoader(new ProviderClassLoader(oldClassLoader));
        } else {
            Thread.currentThread().setContextClassLoader(new ProviderClassLoader());
        }
        threadPortRefs.set(new ProviderWrapperData(portRefData, oldClassLoader));
    }

    public static void afterCreate() {
        Thread.currentThread().setContextClassLoader(threadPortRefs.get().callerClassLoader);
        threadPortRefs.set(null);
    }

    private static class ProviderWrapperData {
        private final List<PortRefData> portRefData;
        private final ClassLoader callerClassLoader;

        public ProviderWrapperData(List<PortRefData> portRefData, ClassLoader callerClassLoader) {
            this.portRefData = portRefData;
            this.callerClassLoader = callerClassLoader;
        }
    }


    //
    // Provider wapper implementation
    //

    private final Provider delegate;
    private final List<PortRefData> portRefs;

    public ProviderWrapper() {
        delegate = findProvider();
        portRefs = (threadPortRefs.get() == null) ? null : threadPortRefs.get().portRefData;
    }

    public Provider getDelegate() {
        return delegate;
    }

    public ServiceDelegate createServiceDelegate(URL wsdlDocumentLocation, QName serviceName, Class serviceClass) {
        ServiceDelegate serviceDelegate = delegate.createServiceDelegate(wsdlDocumentLocation, serviceName, serviceClass);
        // the PortRef list is bound to this thread when using @WebServiceRef injection
        // When using the JAX-WS API we don't need to wrap the ServiceDelegate
        if (threadPortRefs.get() != null) {
            serviceDelegate = new ServiceDelegateWrapper(serviceDelegate);
            
        }
        return serviceDelegate;
    }

    public Endpoint createEndpoint(String bindingId, Object implementor) {
        return delegate.createEndpoint(bindingId, implementor);
    }

    public Endpoint createAndPublishEndpoint(String address, Object implementor) {
        return delegate.createAndPublishEndpoint(address, implementor);
    }

    public W3CEndpointReference createW3CEndpointReference(String address,
            QName serviceName,
            QName portName,
            List<Element> metadata,
            String wsdlDocumentLocation,
            List<Element> referenceParameters) {

        return (W3CEndpointReference) invoke21Delegate(delegate, createW3CEndpointReference,
                address,
                serviceName,
                portName,
                metadata,
                wsdlDocumentLocation,
                referenceParameters);
    }

    public EndpointReference readEndpointReference(Source source){
        return (EndpointReference) invoke21Delegate(delegate, readEndpointReference, source);
    }

    @SuppressWarnings({"unchecked"})
    public <T> T getPort(EndpointReference endpointReference, Class<T> serviceEndpointInterface, WebServiceFeature... features) {
        return (T) invoke21Delegate(delegate, providerGetPort, endpointReference, serviceEndpointInterface, features);
    }

    private class ServiceDelegateWrapper extends ServiceDelegate {
        private final ServiceDelegate serviceDelegate;

        public ServiceDelegateWrapper(ServiceDelegate serviceDelegate) {
            this.serviceDelegate = serviceDelegate;
        }

        public <T> T getPort(QName portName, Class<T> serviceEndpointInterface) {
            T t = serviceDelegate.getPort(portName, serviceEndpointInterface);
            setProperties((BindingProvider) t, portName);
            return t;
        }

        public <T> T getPort(Class<T> serviceEndpointInterface) {
            T t = serviceDelegate.getPort(serviceEndpointInterface);

            QName qname = null;
            if (serviceEndpointInterface.isAnnotationPresent(WebService.class)) {
                WebService webService = serviceEndpointInterface.getAnnotation(WebService.class);
                String targetNamespace = webService.targetNamespace();
                String name = webService.name();
                if (targetNamespace != null && targetNamespace.length() > 0 && name != null && name.length() > 0) {
                    qname = new QName(targetNamespace, name);
                }
            }

            setProperties((BindingProvider) t, qname);
            return t;
        }

        public void addPort(QName portName, String bindingId, String endpointAddress) {
            serviceDelegate.addPort(portName, bindingId, endpointAddress);
        }

        public <T> Dispatch<T> createDispatch(QName portName, Class<T> type, Service.Mode mode) {
            Dispatch<T> dispatch = serviceDelegate.createDispatch(portName, type, mode);
            setProperties(dispatch, portName);
            return dispatch;
        }

        public Dispatch<Object> createDispatch(QName portName, JAXBContext context, Service.Mode mode) {
            Dispatch<Object> dispatch = serviceDelegate.createDispatch(portName, context, mode);
            setProperties(dispatch, portName);
            return dispatch;
        }

        @SuppressWarnings({"unchecked"})
        public <T> Dispatch<T> createDispatch(QName portName, Class<T> type, Service.Mode mode, WebServiceFeature... features) {
            return (Dispatch<T>) invoke21Delegate(serviceDelegate, createDispatchInterface,
                    portName,
                    type,
                    mode,
                    features);
        }

        @SuppressWarnings({"unchecked"})
        public Dispatch<java.lang.Object> createDispatch(QName portName, JAXBContext context, Service.Mode mode, WebServiceFeature... features) {
            return (Dispatch<Object>) invoke21Delegate(serviceDelegate, createDispatchJaxBContext,
                    portName,
                    context,
                    mode,
                    features);
        }

        @SuppressWarnings({"unchecked"})
        public Dispatch<Object> createDispatch(
                EndpointReference endpointReference,
                JAXBContext context,
                Service.Mode mode,
                WebServiceFeature... features) {
            return (Dispatch<Object>) invoke21Delegate(serviceDelegate, createDispatchReferenceJaxB,
                    endpointReference,
                    context,
                    mode,
                    features);
        }

        @SuppressWarnings({"unchecked"})
        public <T> Dispatch<T> createDispatch(EndpointReference endpointReference,
                                               java.lang.Class<T> type,
                                               Service.Mode mode,
                                               WebServiceFeature... features) {
            return (Dispatch<T>) invoke21Delegate(serviceDelegate, createDispatchReferenceClass,
                    endpointReference,
                    type,
                    mode,
                    features);

        }
        @SuppressWarnings({"unchecked"})
        public <T> T getPort(QName portName, Class<T> serviceEndpointInterface, WebServiceFeature... features) {
            return (T) invoke21Delegate(serviceDelegate, serviceGetPortByQName,
                    portName,
                    serviceEndpointInterface,
                    features);
        }

        @SuppressWarnings({"unchecked"})
        public <T> T getPort(EndpointReference endpointReference, Class<T> serviceEndpointInterface, WebServiceFeature... features) {
            return (T) invoke21Delegate(serviceDelegate, serviceGetPortByEndpointReference,
                    endpointReference,
                    serviceEndpointInterface,
                    features);
        }

        @SuppressWarnings({"unchecked"})
        public <T> T getPort(Class<T> serviceEndpointInterface, WebServiceFeature... features) {
            return (T) invoke21Delegate(serviceDelegate, serviceGetPortByInterface,
                    serviceEndpointInterface,
                    features);
        }

        public QName getServiceName() {
            QName qName = serviceDelegate.getServiceName();
            return qName;
        }

        public Iterator<QName> getPorts() {
            Iterator<QName> ports = serviceDelegate.getPorts();
            return ports;
        }

        public URL getWSDLDocumentLocation() {
            URL documentLocation = serviceDelegate.getWSDLDocumentLocation();
            return documentLocation;
        }

        public HandlerResolver getHandlerResolver() {
            HandlerResolver handlerResolver = serviceDelegate.getHandlerResolver();
            return handlerResolver;
        }

        public void setHandlerResolver(HandlerResolver handlerResolver) {
            serviceDelegate.setHandlerResolver(handlerResolver);
        }

        public Executor getExecutor() {
            Executor executor = serviceDelegate.getExecutor();
            return executor;
        }

        public void setExecutor(Executor executor) {
            serviceDelegate.setExecutor(executor);
        }

        private void setProperties(BindingProvider proxy, QName qname) {
            for (PortRefData portRef : portRefs) {
                Class intf = null;
                if (portRef.getServiceEndpointInterface() != null) {
                    try {
                        intf = proxy.getClass().getClassLoader().loadClass(portRef.getServiceEndpointInterface());
                    } catch (Exception e) {
                    }
                }
                if ((qname != null && qname.equals(portRef.getQName())) || (intf != null && intf.isInstance(proxy))) {
                    // set address
                    if (!portRef.getAddresses().isEmpty()) {
                        proxy.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, portRef.getAddresses().get(0));
                    }

                    // set mtom
                    boolean enableMTOM = portRef.isEnableMtom();
                    if (enableMTOM && proxy.getBinding() instanceof SOAPBinding) {
                        ((SOAPBinding)proxy.getBinding()).setMTOMEnabled(enableMTOM);
                    }

                    // set properties
                    for (Map.Entry<Object, Object> entry : portRef.getProperties().entrySet()) {
                        String name = (String) entry.getKey();
                        String value = (String) entry.getValue();
                        proxy.getRequestContext().put(name, value);
                    }

                    return;
                }
            }
        }
    }

    private static Provider findProvider() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) classLoader = ClassLoader.getSystemClassLoader();

        // 0. System.getProperty("openejb.javax.xml.ws.spi.Provider")
        // This is so those using old axis rules still work as expected
        String providerClass = System.getProperty("openejb." + JAXWSPROVIDER_PROPERTY);
        Provider provider = createProviderInstance(providerClass, classLoader);
        if (provider != null) {
            return provider;
        }

        // 1. META-INF/services/javax.xml.ws.spi.Provider
        try {
            for (URL url : Collections.list(classLoader.getResources("META-INF/services/" + JAXWSPROVIDER_PROPERTY))) {
                BufferedReader in = null;
                try {
                    in = new BufferedReader(new InputStreamReader(url.openStream()));

                    providerClass = in.readLine();
                    provider = createProviderInstance(providerClass, classLoader);
                    if (provider != null) {
                        return provider;
                    }
                } catch (Exception ignored) {
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                        }
                    }
                }
            }
        } catch (Exception ingored) {
        }

        // 2. $java.home/lib/jaxws.properties
        String javaHome = System.getProperty("java.home");
        File jaxrpcPropertiesFile = new File(new File(javaHome, "lib"), "jaxrpc.properties");
        if (jaxrpcPropertiesFile.exists()) {
            try {
                final Properties properties = IO.readProperties(jaxrpcPropertiesFile);

                providerClass = properties.getProperty(JAXWSPROVIDER_PROPERTY);
                provider = createProviderInstance(providerClass, classLoader);
                if (provider != null) {
                    return provider;
                }
            } catch(Exception ignored) {
            }
        }

        // 3. System.getProperty("javax.xml.ws.spi.Provider")
        providerClass = System.getProperty(JAXWSPROVIDER_PROPERTY);
        provider = createProviderInstance(providerClass, classLoader);
        if (provider != null) {
            return provider;
        }


        // 4. Use javax.xml.ws.spi.Provider default
        try {
            System.getProperties().remove(JAXWSPROVIDER_PROPERTY);
            provider = Provider.provider();
            if (provider != null && !provider.getClass().getName().equals(ProviderWrapper.class.getName())) {
                return provider;
            }
        } finally {
            // restore original jax provider property
            System.setProperty(JAXWSPROVIDER_PROPERTY, providerClass);
        }

        throw new WebServiceException("No " + JAXWSPROVIDER_PROPERTY + " implementation found");
    }

    private static Provider createProviderInstance(String providerClass, ClassLoader classLoader) {
        if (providerClass != null && providerClass.length() > 0 && !providerClass.equals(ProviderWrapper.class.getName())) {
            try {
                Class<? extends Provider> clazz = classLoader.loadClass(providerClass).asSubclass(Provider.class);
                return clazz.newInstance();
            } catch (Throwable e) {
                logger.warning("Unable to construct provider implementation " + providerClass, e);
            }
        }
        return null;
    }

    private static class ProviderClassLoader extends ClassLoader {
        private static final String PROVIDER_RESOURCE = "META-INF/services/" + JAXWSPROVIDER_PROPERTY;
        private static final URL PROVIDER_URL;
        static {
            try {
                File tempFile = File.createTempFile("openejb-jaxws-provider", "tmp");
                tempFile.deleteOnExit();
                OutputStream out = IO.write(tempFile);
                out.write(ProviderWrapper.class.getName().getBytes());
                out.close();
                PROVIDER_URL = tempFile.toURI().toURL();
            } catch (IOException e) {
                throw new OpenEJBRuntimeException("Cound not create openejb-jaxws-provider file");
            }
        }

        public ProviderClassLoader() {
        }

        public ProviderClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Enumeration<URL> getResources(String name) throws IOException {
            Enumeration<URL> resources = super.getResources(name);
            if (PROVIDER_RESOURCE.equals(name)) {
                ArrayList<URL> list = new ArrayList<URL>();
                list.add(PROVIDER_URL);
                list.addAll(Collections.list(resources));
                resources = Collections.enumeration(list);
            }
            return resources;
        }


        public URL getResource(String name) {
            if (PROVIDER_RESOURCE.equals(name)) {
                return PROVIDER_URL;
            }
            return super.getResource(name);
        }
    }


    //
    // Delegate methods for JaxWS 2.1
    //

    private static Object invoke21Delegate(Object delegate, Method method, Object... args) {
        if (method == null) {
            throw new UnsupportedOperationException("JaxWS 2.1 APIs are not supported");
        }
        try {
            return method.invoke(delegate,args);
        } catch (IllegalAccessException e) {
            throw new WebServiceException(e);
        } catch (InvocationTargetException e) {
            if (e.getCause() != null) {
                throw new WebServiceException(e.getCause());
            }
            throw new WebServiceException(e);
        }
    }

    // Provider methods
    private static final Method createW3CEndpointReference;
    private static final Method providerGetPort;
    private static final Method readEndpointReference;

    // ServiceDelegate methods
    private static final Method createDispatchReferenceJaxB;
    private static final Method createDispatchReferenceClass;
    private static final Method createDispatchInterface;
    private static final Method createDispatchJaxBContext;
    private static final Method serviceGetPortByEndpointReference;
    private static final Method serviceGetPortByQName;
    private static final Method serviceGetPortByInterface;

    static {
        Method method = null;
        try {
            method = Provider.class.getMethod("createW3CEndpointReference",
                    String.class,
                    QName.class,
                    QName.class,
                    List.class,
                    String.class,
                    List.class);
        } catch (NoSuchMethodException e) {
        }
        createW3CEndpointReference = method;

        method = null;
        try {
            method = Provider.class.getMethod("getPort",
                    EndpointReference.class,
                    Class.class,
                    WebServiceFeature[].class);
        } catch (NoSuchMethodException e) {
        }
        providerGetPort = method;

        method = null;
        try {
            method = Provider.class.getMethod("readEndpointReference", Source.class);
        } catch (NoSuchMethodException e) {
        }
        readEndpointReference = method;


        method = null;
        try {
            method = ServiceDelegate.class.getMethod("createDispatch",
                    EndpointReference.class,
                    JAXBContext.class,
                    Service.Mode.class,
                    WebServiceFeature[].class);
        } catch (NoSuchMethodException e) {
        }
        createDispatchReferenceJaxB = method;

        method = null;
        try {
            method = ServiceDelegate.class.getMethod("createDispatch",
                    EndpointReference.class,
                    Class.class,
                    Service.Mode.class,
                    WebServiceFeature[].class);
        } catch (NoSuchMethodException e) {
        }
        createDispatchReferenceClass = method;

        method = null;
        try {
            method = ServiceDelegate.class.getMethod("createDispatch",
                    QName.class,
                    JAXBContext.class,
                    Service.Mode.class,
                    WebServiceFeature[].class);
        } catch (NoSuchMethodException e) {
        }
        createDispatchJaxBContext = method;

        method = null;
        try {
            method = ServiceDelegate.class.getMethod("createDispatch",
                    QName.class,
                    Class.class,
                    Service.Mode.class,
                    WebServiceFeature[].class);
        } catch (NoSuchMethodException e) {
        }
        createDispatchInterface = method;

        method = null;
        try {
            method = ServiceDelegate.class.getMethod("getPort",
                    EndpointReference.class,
                    Class.class,
                    WebServiceFeature[].class);
        } catch (NoSuchMethodException e) {
        }
        serviceGetPortByEndpointReference = method;

        method = null;
        try {
            method = ServiceDelegate.class.getMethod("getPort",
                    QName.class,
                    Class.class,
                    WebServiceFeature[].class);
        } catch (NoSuchMethodException e) {
        }
        serviceGetPortByQName = method;

        method = null;
        try {
            method = ServiceDelegate.class.getMethod("getPort",
                    Class.class,
                    WebServiceFeature[].class);
        } catch (NoSuchMethodException e) {
        }
        serviceGetPortByInterface = method;

    }
}
