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
package org.apache.openejb.jee.oejb2;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.sax.SAXSource;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.apache.openejb.jee.JAXBContextFactory;

/**
 * @version $Rev$ $Date$
 */
public class JaxbOpenejbJar2 {

    private static final Map<Class<?>,JAXBContext> contexts = new HashMap<Class<?>,JAXBContext>();

    private static JAXBContext getContext(Class<?> type) throws JAXBException {
        JAXBContext jaxbContext = contexts.get(type);
        if (jaxbContext == null) {
            jaxbContext = JAXBContextFactory.newInstance(type);
            contexts.put(type, jaxbContext);
        }
        return jaxbContext;
    }

    public static <T>String marshal(Class<T> type, Object object) throws JAXBException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        marshal(type, object, out);

        return new String(out.toByteArray());
    }

    public static <T>void marshal(Class<T> type, Object object, OutputStream out) throws JAXBException {
        JAXBContext ctx2 = getContext(type);
        Marshaller marshaller = ctx2.createMarshaller();

        marshaller.setProperty("jaxb.formatted.output", true);

        marshaller.marshal(object, out);
    }

    public static <T>Object unmarshal(Class<T> type, InputStream in) throws ParserConfigurationException, SAXException, JAXBException {
        return unmarshal(type, in, true);
    }

    public static <T>Object unmarshal(Class<T> type, InputStream in, final boolean logErrors) throws ParserConfigurationException, SAXException, JAXBException {
        InputSource inputSource = new InputSource(in);

        SAXParserFactory factory = SAXParserFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setValidating(false);
        SAXParser parser = factory.newSAXParser();

        JAXBContext ctx = getContext(type);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();
        unmarshaller.setEventHandler(new ValidationEventHandler(){
            public boolean handleEvent(ValidationEvent validationEvent) {
                if (logErrors) {
                    System.out.println(validationEvent);
                }
                return false;
            }
        });

        unmarshaller.setListener(new Unmarshaller.Listener(){
            public void afterUnmarshal(Object object, Object object1) {
                super.afterUnmarshal(object, object1);
            }

            public void beforeUnmarshal(Object target, Object parent) {
                super.beforeUnmarshal(target, parent);
            }
        });


        NamespaceFilter xmlFilter = new NamespaceFilter(parser.getXMLReader());
        xmlFilter.setContentHandler(unmarshaller.getUnmarshallerHandler());

        SAXSource source = new SAXSource(xmlFilter, inputSource);

        return unmarshaller.unmarshal(source, type);
    }
}
