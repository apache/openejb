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
package org.apache.openejb.jee;

import com.envoisolutions.sxc.jaxb.JAXBObject;
import com.envoisolutions.sxc.jaxb.LifecycleCallback;
import com.envoisolutions.sxc.jaxb.RuntimeContext;
import com.envoisolutions.sxc.util.Attribute;
import com.envoisolutions.sxc.util.XoXMLStreamReader;
import com.envoisolutions.sxc.util.XoXMLStreamWriter;

import javax.xml.XMLConstants;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

import static org.apache.openejb.jee.VariableMapping$JAXB.readVariableMapping;
import static org.apache.openejb.jee.VariableMapping$JAXB.writeVariableMapping;

@SuppressWarnings({
        "StringEquality"
})
public class JavaXmlTypeMapping$JAXB
        extends JAXBObject<JavaXmlTypeMapping> {


    public JavaXmlTypeMapping$JAXB() {
        super(JavaXmlTypeMapping.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "java-xml-type-mappingType".intern()), VariableMapping$JAXB.class);
    }

    public static JavaXmlTypeMapping readJavaXmlTypeMapping(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeJavaXmlTypeMapping(XoXMLStreamWriter writer, JavaXmlTypeMapping javaXmlTypeMapping, RuntimeContext context)
            throws Exception {
        _write(writer, javaXmlTypeMapping, context);
    }

    public void write(XoXMLStreamWriter writer, JavaXmlTypeMapping javaXmlTypeMapping, RuntimeContext context)
            throws Exception {
        _write(writer, javaXmlTypeMapping, context);
    }

    public final static JavaXmlTypeMapping _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        JavaXmlTypeMapping javaXmlTypeMapping = new JavaXmlTypeMapping();
        context.beforeUnmarshal(javaXmlTypeMapping, LifecycleCallback.NONE);

        List<VariableMapping> variableMapping = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("java-xml-type-mappingType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, JavaXmlTypeMapping.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, javaXmlTypeMapping);
                javaXmlTypeMapping.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("java-type" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: javaType
                String javaTypeRaw = elementReader.getElementAsString();

                String javaType;
                try {
                    javaType = Adapters.collapsedStringAdapterAdapter.unmarshal(javaTypeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                javaXmlTypeMapping.javaType = javaType;
            } else if (("root-type-qname" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: rootTypeQname
                QName rootTypeQname = elementReader.getElementAsQName();
                javaXmlTypeMapping.rootTypeQname = rootTypeQname;
            } else if (("anonymous-type-qname" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: anonymousTypeQname
                String anonymousTypeQnameRaw = elementReader.getElementAsString();

                String anonymousTypeQname;
                try {
                    anonymousTypeQname = Adapters.collapsedStringAdapterAdapter.unmarshal(anonymousTypeQnameRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                javaXmlTypeMapping.anonymousTypeQname = anonymousTypeQname;
            } else if (("qname-scope" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: qnameScope
                String qnameScopeRaw = elementReader.getElementAsString();

                String qnameScope;
                try {
                    qnameScope = Adapters.collapsedStringAdapterAdapter.unmarshal(qnameScopeRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                javaXmlTypeMapping.qnameScope = qnameScope;
            } else if (("variable-mapping" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: variableMapping
                VariableMapping variableMappingItem = readVariableMapping(elementReader, context);
                if (variableMapping == null) {
                    variableMapping = javaXmlTypeMapping.variableMapping;
                    if (variableMapping != null) {
                        variableMapping.clear();
                    } else {
                        variableMapping = new ArrayList<VariableMapping>();
                    }
                }
                variableMapping.add(variableMappingItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "java-type"), new QName("http://java.sun.com/xml/ns/javaee", "root-type-qname"), new QName("http://java.sun.com/xml/ns/javaee", "anonymous-type-qname"), new QName("http://java.sun.com/xml/ns/javaee", "qname-scope"), new QName("http://java.sun.com/xml/ns/javaee", "variable-mapping"));
            }
        }
        if (variableMapping != null) {
            javaXmlTypeMapping.variableMapping = variableMapping;
        }

        context.afterUnmarshal(javaXmlTypeMapping, LifecycleCallback.NONE);

        return javaXmlTypeMapping;
    }

    public final JavaXmlTypeMapping read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, JavaXmlTypeMapping javaXmlTypeMapping, RuntimeContext context)
            throws Exception {
        if (javaXmlTypeMapping == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (JavaXmlTypeMapping.class != javaXmlTypeMapping.getClass()) {
            context.unexpectedSubclass(writer, javaXmlTypeMapping, JavaXmlTypeMapping.class);
            return;
        }

        context.beforeMarshal(javaXmlTypeMapping, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = javaXmlTypeMapping.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(javaXmlTypeMapping, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: javaType
        String javaTypeRaw = javaXmlTypeMapping.javaType;
        String javaType = null;
        try {
            javaType = Adapters.collapsedStringAdapterAdapter.marshal(javaTypeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(javaXmlTypeMapping, "javaType", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (javaType != null) {
            writer.writeStartElement(prefix, "java-type", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(javaType);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(javaXmlTypeMapping, "javaType");
        }

        // ELEMENT: rootTypeQname
        QName rootTypeQname = javaXmlTypeMapping.rootTypeQname;
        if (rootTypeQname != null) {
            writer.writeStartElement(prefix, "root-type-qname", "http://java.sun.com/xml/ns/javaee");
            writer.writeQName(rootTypeQname);
            writer.writeEndElement();
        }

        // ELEMENT: anonymousTypeQname
        String anonymousTypeQnameRaw = javaXmlTypeMapping.anonymousTypeQname;
        String anonymousTypeQname = null;
        try {
            anonymousTypeQname = Adapters.collapsedStringAdapterAdapter.marshal(anonymousTypeQnameRaw);
        } catch (Exception e) {
            context.xmlAdapterError(javaXmlTypeMapping, "anonymousTypeQname", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (anonymousTypeQname != null) {
            writer.writeStartElement(prefix, "anonymous-type-qname", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(anonymousTypeQname);
            writer.writeEndElement();
        }

        // ELEMENT: qnameScope
        String qnameScopeRaw = javaXmlTypeMapping.qnameScope;
        String qnameScope = null;
        try {
            qnameScope = Adapters.collapsedStringAdapterAdapter.marshal(qnameScopeRaw);
        } catch (Exception e) {
            context.xmlAdapterError(javaXmlTypeMapping, "qnameScope", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (qnameScope != null) {
            writer.writeStartElement(prefix, "qname-scope", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(qnameScope);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(javaXmlTypeMapping, "qnameScope");
        }

        // ELEMENT: variableMapping
        List<VariableMapping> variableMapping = javaXmlTypeMapping.variableMapping;
        if (variableMapping != null) {
            for (VariableMapping variableMappingItem : variableMapping) {
                if (variableMappingItem != null) {
                    writer.writeStartElement(prefix, "variable-mapping", "http://java.sun.com/xml/ns/javaee");
                    writeVariableMapping(writer, variableMappingItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(javaXmlTypeMapping, LifecycleCallback.NONE);
    }

}
