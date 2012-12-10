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

import static org.apache.openejb.jee.FacesLifecycleExtension$JAXB.readFacesLifecycleExtension;
import static org.apache.openejb.jee.FacesLifecycleExtension$JAXB.writeFacesLifecycleExtension;

@SuppressWarnings({
        "StringEquality"
})
public class FacesLifecycle$JAXB
        extends JAXBObject<FacesLifecycle> {


    public FacesLifecycle$JAXB() {
        super(FacesLifecycle.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-lifecycleType".intern()), FacesLifecycleExtension$JAXB.class);
    }

    public static FacesLifecycle readFacesLifecycle(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesLifecycle(XoXMLStreamWriter writer, FacesLifecycle facesLifecycle, RuntimeContext context)
            throws Exception {
        _write(writer, facesLifecycle, context);
    }

    public void write(XoXMLStreamWriter writer, FacesLifecycle facesLifecycle, RuntimeContext context)
            throws Exception {
        _write(writer, facesLifecycle, context);
    }

    public final static FacesLifecycle _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesLifecycle facesLifecycle = new FacesLifecycle();
        context.beforeUnmarshal(facesLifecycle, LifecycleCallback.NONE);

        List<String> phaseListener = null;
        List<FacesLifecycleExtension> lifecycleExtension = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-lifecycleType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesLifecycle.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesLifecycle);
                facesLifecycle.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("phase-listener" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: phaseListener
                String phaseListenerItemRaw = elementReader.getElementAsString();

                String phaseListenerItem;
                try {
                    phaseListenerItem = Adapters.collapsedStringAdapterAdapter.unmarshal(phaseListenerItemRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                if (phaseListener == null) {
                    phaseListener = facesLifecycle.phaseListener;
                    if (phaseListener != null) {
                        phaseListener.clear();
                    } else {
                        phaseListener = new ArrayList<String>();
                    }
                }
                phaseListener.add(phaseListenerItem);
            } else if (("lifecycle-extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: lifecycleExtension
                FacesLifecycleExtension lifecycleExtensionItem = readFacesLifecycleExtension(elementReader, context);
                if (lifecycleExtension == null) {
                    lifecycleExtension = facesLifecycle.lifecycleExtension;
                    if (lifecycleExtension != null) {
                        lifecycleExtension.clear();
                    } else {
                        lifecycleExtension = new ArrayList<FacesLifecycleExtension>();
                    }
                }
                lifecycleExtension.add(lifecycleExtensionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "phase-listener"), new QName("http://java.sun.com/xml/ns/javaee", "lifecycle-extension"));
            }
        }
        if (phaseListener != null) {
            facesLifecycle.phaseListener = phaseListener;
        }
        if (lifecycleExtension != null) {
            facesLifecycle.lifecycleExtension = lifecycleExtension;
        }

        context.afterUnmarshal(facesLifecycle, LifecycleCallback.NONE);

        return facesLifecycle;
    }

    public final FacesLifecycle read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, FacesLifecycle facesLifecycle, RuntimeContext context)
            throws Exception {
        if (facesLifecycle == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesLifecycle.class != facesLifecycle.getClass()) {
            context.unexpectedSubclass(writer, facesLifecycle, FacesLifecycle.class);
            return;
        }

        context.beforeMarshal(facesLifecycle, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesLifecycle.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesLifecycle, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: phaseListener
        List<String> phaseListenerRaw = facesLifecycle.phaseListener;
        if (phaseListenerRaw != null) {
            for (String phaseListenerItem : phaseListenerRaw) {
                String phaseListener = null;
                try {
                    phaseListener = Adapters.collapsedStringAdapterAdapter.marshal(phaseListenerItem);
                } catch (Exception e) {
                    context.xmlAdapterError(facesLifecycle, "phaseListener", CollapsedStringAdapter.class, List.class, List.class, e);
                }
                if (phaseListener != null) {
                    writer.writeStartElement(prefix, "phase-listener", "http://java.sun.com/xml/ns/javaee");
                    writer.writeCharacters(phaseListener);
                    writer.writeEndElement();
                }
            }
        }

        // ELEMENT: lifecycleExtension
        List<FacesLifecycleExtension> lifecycleExtension = facesLifecycle.lifecycleExtension;
        if (lifecycleExtension != null) {
            for (FacesLifecycleExtension lifecycleExtensionItem : lifecycleExtension) {
                if (lifecycleExtensionItem != null) {
                    writer.writeStartElement(prefix, "lifecycle-extension", "http://java.sun.com/xml/ns/javaee");
                    writeFacesLifecycleExtension(writer, lifecycleExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesLifecycle, LifecycleCallback.NONE);
    }

}
