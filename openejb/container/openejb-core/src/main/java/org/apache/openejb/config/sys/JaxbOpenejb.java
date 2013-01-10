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
package org.apache.openejb.config.sys;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.ConfigUtils;
import org.apache.openejb.jee.JAXBContextFactory;
import org.apache.openejb.loader.IO;
import org.apache.openejb.util.Join;
import org.apache.openejb.util.Saxs;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLFilterImpl;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.MarshalException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.ValidationException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.transform.sax.SAXSource;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

public abstract class JaxbOpenejb {

    @SuppressWarnings({"unchecked"})
    public static <T> T create(Class<T> type) {
        if (type == null) throw new NullPointerException("type is null");

        if (type == ConnectionManager.class) {
            return (T) createConnectionManager();
        } else if (type == Connector.class) {
            return (T) createConnector();
        } else if (type == Container.class) {
            return (T) createContainer();
        } else if (type == Deployments.class) {
            return (T) createDeployments();
        } else if (type == JndiProvider.class) {
            return (T) createJndiProvider();
        } else if (type == Openejb.class) {
            return (T) createOpenejb();
        } else if (type == ProxyFactory.class) {
            return (T) createProxyFactory();
        } else if (type == Resource.class) {
            return (T) createResource();
        } else if (type == SecurityService.class) {
            return (T) createSecurityService();
        } else if (type == ServiceProvider.class) {
            return (T) createServiceProvider();
        } else if (type == ServicesJar.class) {
            return (T) createServicesJar();
        } else if (type == TransactionManager.class) {
            return (T) createTransactionManager();
        } else if (type == Tomee.class) {
            return (T) createTomee();
        }
        throw new IllegalArgumentException("Unknown type " + type.getName());
    }

    public static <T> T create(String type) {
        if (type == null) throw new NullPointerException("type is null");

        if (type.equals("ConnectionManager")) {
            return (T) createConnectionManager();
        } else if (type.equals("Connector")) {
            return (T) createConnector();
        } else if (type.equals("Container")) {
            return (T) createContainer();
        } else if (type.equals("Deployments")) {
            return (T) createDeployments();
        } else if (type.equals("JndiProvider")) {
            return (T) createJndiProvider();
        } else if (type.equals("Openejb")) {
            return (T) createOpenejb();
        } else if (type.equals("Tomee")) {
            return (T) createTomee();
        } else if (type.equals("ProxyFactory")) {
            return (T) createProxyFactory();
        } else if (type.equals("Resource")) {
            return (T) createResource();
        } else if (type.equals("SecurityService")) {
            return (T) createSecurityService();
        } else if (type.equals("ServiceProvider")) {
            return (T) createServiceProvider();
        } else if (type.equals("ServicesJar")) {
            return (T) createServicesJar();
        } else if (type.equals("TransactionManager")) {
            return (T) createTransactionManager();
        } else if (type.equals("Service")) {
            return (T) createService();
        }
        throw new IllegalArgumentException("Unknown type " + type);
    }

    public static ServicesJar readServicesJar(final String providerPath) throws OpenEJBException {

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        final List<URL> sources = new ArrayList<URL>();

        { // Find URLs in the classpath
            final String path1 = "META-INF/" + providerPath + "/service-jar.xml";
            final String path2 = providerPath.replace('.', '/') + "/service-jar.xml";
            final List<String> paths = Arrays.asList(path1, path2);

            for (String p : paths) {
                try {
                    final Enumeration<URL> resources = loader.getResources(p);
                    if (resources != null){
                        sources.addAll(Collections.list(resources));
                    }
                } catch (IOException e) {
                    throw new OpenEJBException(String.format("Unable to scan for service-jar.xml file: getResource('%s')", p), e);
                }
            }

            if (sources.size() == 0) {
                throw new OpenEJBException("No service-jar.xml files found: searched " + Join.join(" and ", paths));
            }
        }

        final ServicesJar servicesJar = new ServicesJar();

        {
            for (URL url : sources) {
                try {
                    final InputStream read = IO.read(url);
                    try {
                        final ServicesJar jar = parseServicesJar(read);
                        servicesJar.getServiceProvider().addAll(jar.getServiceProvider());
                    } catch (Exception e) {
                        throw new OpenEJBException("Unable to parse service-jar.xml file for provider " + providerPath + " at " + url, e);
                    } finally {
                        IO.close(read);
                    }
                } catch (IOException e) {
                    throw new OpenEJBException("Unable to read service-jar.xml file for provider " + providerPath + " at " + url, e);
                }
            }
        }

        return servicesJar;
    }

    public static ServicesJar parseServicesJar(InputStream in) throws ParserConfigurationException, SAXException, IOException {
        InputSource inputSource = new InputSource(in);

        SAXParser parser = Saxs.namespaceAwareFactory().newSAXParser();

        final ServicesJar servicesJar1 = new ServicesJar();
        final PropertiesAdapter propertiesAdapter = new PropertiesAdapter();

        parser.parse(inputSource, new DefaultHandler() {
            private ServiceProvider provider;
            private StringBuilder content;

            public void startDocument() throws SAXException {
            }

            public void startElement(String uri, String localName, String qName, Attributes att) throws SAXException {
                if (!localName.equals("ServiceProvider")) return;

                provider = new ServiceProvider();
                provider.setId(att.getValue("","id"));
                provider.setService(att.getValue("", "service"));
                provider.setFactoryName(att.getValue("", "factory-name"));
                provider.setConstructor(att.getValue("", "constructor"));
                provider.setClassName(att.getValue("", "class-name"));
                provider.setParent(att.getValue("", "parent"));
                String typesString = att.getValue("", "types");
                if (typesString != null){
                    final ListAdapter listAdapter = new ListAdapter();
                    final List<String> types = listAdapter.unmarshal(typesString);
                    provider.getTypes().addAll(types);
                }
                servicesJar1.getServiceProvider().add(provider);
            }

            public void characters(char ch[], int start, int length) throws SAXException {
                if (content == null) content = new StringBuilder();
                content.append(ch, start, length);
            }

            public void endElement(String uri, String localName, String qName) throws SAXException {
                if (provider == null || content == null) return;

                try {
                    provider.getProperties().putAll(propertiesAdapter.unmarshal(content.toString()));
                } catch (Exception e) {
                    throw new SAXException(e);
                }
                provider = null;
                content = null;
            }
        });
        ServicesJar servicesJar = servicesJar1;
        return servicesJar;
    }

    public static Openejb readConfig(String configFile) throws OpenEJBException {
        InputStream in = null;
        try {
            if (configFile.startsWith("jar:")) {
                URL url = new URL(configFile);
                in = IO.read(url);
            } else if (configFile.startsWith("file:")) {
                URL url = new URL(configFile);
                in = IO.read(url);
            } else {
                in = IO.read(new File(configFile));
            }
            return readConfig(new InputSource(in));
        } catch (MalformedURLException e) {
            throw new OpenEJBException("Unable to resolve location " + configFile, e);
        } catch (Exception e) {
            throw new OpenEJBException("Unable to read OpenEJB configuration file at " + configFile, e);
        } finally {
            IO.close(in);
        }
    }

    public static Openejb readConfig(final InputSource in) throws IOException, SAXException, ParserConfigurationException {
        return SaxOpenejb.parse(in);
    }

    public static void writeConfig(String configFile, Openejb openejb) throws OpenEJBException {
        OutputStream out = null;
        try {
            File file = new File(configFile);
            out = IO.write(file);
            marshal(Openejb.class, openejb, out);
        } catch (IOException e) {
            throw new OpenEJBException(ConfigUtils.messages.format("conf.1040", configFile, e.getLocalizedMessage()), e);
        } catch (MarshalException e) {
            if (e.getCause() instanceof IOException) {
                throw new OpenEJBException(ConfigUtils.messages.format("conf.1040", configFile, e.getLocalizedMessage()), e);
            } else {
                throw new OpenEJBException(ConfigUtils.messages.format("conf.1050", configFile, e.getLocalizedMessage()), e);
            }
        } catch (ValidationException e) {
            /* TODO: Implement informative error handling here.
               The exception will say "X doesn't match the regular
               expression Y"
               This should be checked and more relevant information
               should be given -- not everyone understands regular
               expressions.
             */
            /* NOTE: This doesn't seem to ever happen. When the object graph
             * is invalid, the MarshalException is thrown, not this one as you
             * would think.
             */
            throw new OpenEJBException(ConfigUtils.messages.format("conf.1060", configFile, e.getLocalizedMessage()), e);
        } catch (JAXBException e) {
            throw new OpenEJBException(e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (Exception e) {
                }
            }
        }
    }

    public static final ThreadLocal<Set<String>> currentPublicId = new ThreadLocal<Set<String>>();

    private static Map<Class, JAXBContext> jaxbContexts = new HashMap<Class, JAXBContext>();

    public static <T> String marshal(Class<T> type, Object object) throws JAXBException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        marshal(type, object, baos);

        return new String(baos.toByteArray());
    }

    public static <T> void marshal(Class<T> type, Object object, OutputStream out) throws JAXBException {
        JAXBContext jaxbContext = getContext(type);
        Marshaller marshaller = jaxbContext.createMarshaller();

        marshaller.setProperty("jaxb.formatted.output", true);

        marshaller.marshal(object, out);
    }

    public static <T> JAXBContext getContext(Class<T> type) throws JAXBException {
        JAXBContext jaxbContext = jaxbContexts.get(type);
        if (jaxbContext == null) {
            jaxbContext = JAXBContextFactory.newInstance(type);
            jaxbContexts.put(type, jaxbContext);
        }
        return jaxbContext;
    }

    public static <T> T unmarshal(Class<T> type, InputStream in, boolean filter) throws ParserConfigurationException, SAXException, JAXBException {
        InputSource inputSource = new InputSource(in);

        SAXParser parser = Saxs.namespaceAwareFactory().newSAXParser();

        JAXBContext ctx = getContext(type);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler() {
            public boolean handleEvent(ValidationEvent validationEvent) {
                System.out.println(validationEvent);
                return false;
            }
        });

        SAXSource source;
        if (filter) {
            NamespaceFilter xmlFilter = new NamespaceFilter(parser.getXMLReader());
            xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());
            source = new SAXSource(xmlFilter, inputSource);
        } else {
            source = new SAXSource(inputSource);
        }

        currentPublicId.set(new TreeSet<String>());
        try {
            return unmarshaller.unmarshal(source, type).getValue();
        } finally {
            currentPublicId.set(null);
        }
    }

    @SuppressWarnings({"unchecked"})
    public static <T> T unmarshal(Class<T> type, InputStream in) throws ParserConfigurationException, SAXException, JAXBException {
        return unmarshal(type, in, true);
    }

    public static class NamespaceFilter extends XMLFilterImpl {

        public NamespaceFilter(XMLReader xmlReader) {
            super(xmlReader);
        }

        public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            Set<String> publicIds = currentPublicId.get();
            if (publicIds != null) {
                publicIds.add(publicId);
            }
            return super.resolveEntity(publicId, systemId);
        }

        public void startElement(String uri, String localName, String qname, Attributes atts) throws SAXException {
            super.startElement("http://www.openejb.org/System/Configuration", localName, qname, atts);
        }
    }


    public static ConnectionManager createConnectionManager() {
        return new ConnectionManager();
    }

    public static Connector createConnector() {
        return new Connector();
    }

    public static Container createContainer() {
        return new Container();
    }

    public static Deployments createDeployments() {
        return new Deployments();
    }

    public static Service createService() {
        return new Service();
    }

    public static JndiProvider createJndiProvider() {
        return new JndiProvider();
    }

    public static Openejb createOpenejb() {
        return new Openejb();
    }

    private static Tomee createTomee() {
        return new Tomee();
    }

    public static ProxyFactory createProxyFactory() {
        return new ProxyFactory();
    }

    public static Resource createResource() {
        return new Resource();
    }

    public static SecurityService createSecurityService() {
        return new SecurityService();
    }

    public static ServiceProvider createServiceProvider() {
        return new ServiceProvider();
    }

    public static ServicesJar createServicesJar() {
        return new ServicesJar();
    }

    public static TransactionManager createTransactionManager() {
        return new TransactionManager();
    }
}
