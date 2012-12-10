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

import static org.apache.openejb.jee.FacesAttribute$JAXB.readFacesAttribute;
import static org.apache.openejb.jee.FacesAttribute$JAXB.writeFacesAttribute;
import static org.apache.openejb.jee.FacesProperty$JAXB.readFacesProperty;
import static org.apache.openejb.jee.FacesProperty$JAXB.writeFacesProperty;
import static org.apache.openejb.jee.FacesValidatorExtension$JAXB.readFacesValidatorExtension;
import static org.apache.openejb.jee.FacesValidatorExtension$JAXB.writeFacesValidatorExtension;
import static org.apache.openejb.jee.Icon$JAXB.readIcon;
import static org.apache.openejb.jee.Icon$JAXB.writeIcon;
import static org.apache.openejb.jee.Text$JAXB.readText;
import static org.apache.openejb.jee.Text$JAXB.writeText;

@SuppressWarnings({
        "StringEquality"
})
public class FacesValidator$JAXB
        extends JAXBObject<FacesValidator> {


    public FacesValidator$JAXB() {
        super(FacesValidator.class, null, new QName("http://java.sun.com/xml/ns/javaee".intern(), "faces-config-validatorType".intern()), Text$JAXB.class, Icon$JAXB.class, FacesAttribute$JAXB.class, FacesProperty$JAXB.class, FacesValidatorExtension$JAXB.class);
    }

    public static FacesValidator readFacesValidator(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public static void writeFacesValidator(XoXMLStreamWriter writer, FacesValidator facesValidator, RuntimeContext context)
            throws Exception {
        _write(writer, facesValidator, context);
    }

    public void write(XoXMLStreamWriter writer, FacesValidator facesValidator, RuntimeContext context)
            throws Exception {
        _write(writer, facesValidator, context);
    }

    public final static FacesValidator _read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {

        // Check for xsi:nil
        if (reader.isXsiNil()) {
            return null;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        FacesValidator facesValidator = new FacesValidator();
        context.beforeUnmarshal(facesValidator, LifecycleCallback.NONE);

        ArrayList<Text> descriptions = null;
        ArrayList<Text> displayNames = null;
        LocalCollection<Icon> icon = null;
        List<FacesAttribute> attribute1 = null;
        List<FacesProperty> property = null;
        List<FacesValidatorExtension> validatorExtension = null;

        // Check xsi:type
        QName xsiType = reader.getXsiType();
        if (xsiType != null) {
            if (("faces-config-validatorType" != xsiType.getLocalPart()) || ("http://java.sun.com/xml/ns/javaee" != xsiType.getNamespaceURI())) {
                return context.unexpectedXsiType(reader, FacesValidator.class);
            }
        }

        // Read attributes
        for (Attribute attribute : reader.getAttributes()) {
            if (("id" == attribute.getLocalName()) && (("" == attribute.getNamespace()) || (attribute.getNamespace() == null))) {
                // ATTRIBUTE: id
                String id = Adapters.collapsedStringAdapterAdapter.unmarshal(attribute.getValue());
                context.addXmlId(reader, id, facesValidator);
                facesValidator.id = id;
            } else if (XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI != attribute.getNamespace()) {
                context.unexpectedAttribute(attribute, new QName("", "id"));
            }
        }

        // Read elements
        for (XoXMLStreamReader elementReader : reader.getChildElements()) {
            if (("description" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: descriptions
                Text descriptionsItem = readText(elementReader, context);
                if (descriptions == null) {
                    descriptions = new ArrayList<Text>();
                }
                descriptions.add(descriptionsItem);
            } else if (("display-name" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: displayNames
                Text displayNamesItem = readText(elementReader, context);
                if (displayNames == null) {
                    displayNames = new ArrayList<Text>();
                }
                displayNames.add(displayNamesItem);
            } else if (("icon" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: icon
                Icon iconItem = readIcon(elementReader, context);
                if (icon == null) {
                    icon = facesValidator.icon;
                    if (icon != null) {
                        icon.clear();
                    } else {
                        icon = new LocalCollection<Icon>();
                    }
                }
                icon.add(iconItem);
            } else if (("validator-id" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: validatorId
                String validatorIdRaw = elementReader.getElementAsString();

                String validatorId;
                try {
                    validatorId = Adapters.collapsedStringAdapterAdapter.unmarshal(validatorIdRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesValidator.validatorId = validatorId;
            } else if (("validator-class" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: validatorClass
                String validatorClassRaw = elementReader.getElementAsString();

                String validatorClass;
                try {
                    validatorClass = Adapters.collapsedStringAdapterAdapter.unmarshal(validatorClassRaw);
                } catch (Exception e) {
                    context.xmlAdapterError(elementReader, CollapsedStringAdapter.class, String.class, String.class, e);
                    continue;
                }

                facesValidator.validatorClass = validatorClass;
            } else if (("attribute" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: attribute
                FacesAttribute attributeItem = readFacesAttribute(elementReader, context);
                if (attribute1 == null) {
                    attribute1 = facesValidator.attribute;
                    if (attribute1 != null) {
                        attribute1.clear();
                    } else {
                        attribute1 = new ArrayList<FacesAttribute>();
                    }
                }
                attribute1.add(attributeItem);
            } else if (("property" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: property
                FacesProperty propertyItem = readFacesProperty(elementReader, context);
                if (property == null) {
                    property = facesValidator.property;
                    if (property != null) {
                        property.clear();
                    } else {
                        property = new ArrayList<FacesProperty>();
                    }
                }
                property.add(propertyItem);
            } else if (("validator-extension" == elementReader.getLocalName()) && ("http://java.sun.com/xml/ns/javaee" == elementReader.getNamespaceURI())) {
                // ELEMENT: validatorExtension
                FacesValidatorExtension validatorExtensionItem = readFacesValidatorExtension(elementReader, context);
                if (validatorExtension == null) {
                    validatorExtension = facesValidator.validatorExtension;
                    if (validatorExtension != null) {
                        validatorExtension.clear();
                    } else {
                        validatorExtension = new ArrayList<FacesValidatorExtension>();
                    }
                }
                validatorExtension.add(validatorExtensionItem);
            } else {
                context.unexpectedElement(elementReader, new QName("http://java.sun.com/xml/ns/javaee", "description"), new QName("http://java.sun.com/xml/ns/javaee", "display-name"), new QName("http://java.sun.com/xml/ns/javaee", "icon"), new QName("http://java.sun.com/xml/ns/javaee", "validator-id"), new QName("http://java.sun.com/xml/ns/javaee", "validator-class"), new QName("http://java.sun.com/xml/ns/javaee", "attribute"), new QName("http://java.sun.com/xml/ns/javaee", "property"), new QName("http://java.sun.com/xml/ns/javaee", "validator-extension"));
            }
        }
        if (descriptions != null) {
            try {
                facesValidator.setDescriptions(descriptions.toArray(new Text[descriptions.size()]));
            } catch (Exception e) {
                context.setterError(reader, FacesValidator.class, "setDescriptions", Text[].class, e);
            }
        }
        if (displayNames != null) {
            try {
                facesValidator.setDisplayNames(displayNames.toArray(new Text[displayNames.size()]));
            } catch (Exception e) {
                context.setterError(reader, FacesValidator.class, "setDisplayNames", Text[].class, e);
            }
        }
        if (icon != null) {
            facesValidator.icon = icon;
        }
        if (attribute1 != null) {
            facesValidator.attribute = attribute1;
        }
        if (property != null) {
            facesValidator.property = property;
        }
        if (validatorExtension != null) {
            facesValidator.validatorExtension = validatorExtension;
        }

        context.afterUnmarshal(facesValidator, LifecycleCallback.NONE);

        return facesValidator;
    }

    public final FacesValidator read(XoXMLStreamReader reader, RuntimeContext context)
            throws Exception {
        return _read(reader, context);
    }

    public final static void _write(XoXMLStreamWriter writer, FacesValidator facesValidator, RuntimeContext context)
            throws Exception {
        if (facesValidator == null) {
            writer.writeXsiNil();
            return;
        }

        if (context == null) {
            context = new RuntimeContext();
        }

        String prefix = writer.getUniquePrefix("http://java.sun.com/xml/ns/javaee");
        if (FacesValidator.class != facesValidator.getClass()) {
            context.unexpectedSubclass(writer, facesValidator, FacesValidator.class);
            return;
        }

        context.beforeMarshal(facesValidator, LifecycleCallback.NONE);


        // ATTRIBUTE: id
        String idRaw = facesValidator.id;
        if (idRaw != null) {
            String id = null;
            try {
                id = Adapters.collapsedStringAdapterAdapter.marshal(idRaw);
            } catch (Exception e) {
                context.xmlAdapterError(facesValidator, "id", CollapsedStringAdapter.class, String.class, String.class, e);
            }
            writer.writeAttribute("", "", "id", id);
        }

        // ELEMENT: descriptions
        Text[] descriptions = null;
        try {
            descriptions = facesValidator.getDescriptions();
        } catch (Exception e) {
            context.getterError(facesValidator, "descriptions", FacesValidator.class, "getDescriptions", e);
        }
        if (descriptions != null) {
            for (Text descriptionsItem : descriptions) {
                if (descriptionsItem != null) {
                    writer.writeStartElement(prefix, "description", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, descriptionsItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesValidator, "descriptions");
                }
            }
        }

        // ELEMENT: displayNames
        Text[] displayNames = null;
        try {
            displayNames = facesValidator.getDisplayNames();
        } catch (Exception e) {
            context.getterError(facesValidator, "displayNames", FacesValidator.class, "getDisplayNames", e);
        }
        if (displayNames != null) {
            for (Text displayNamesItem : displayNames) {
                if (displayNamesItem != null) {
                    writer.writeStartElement(prefix, "display-name", "http://java.sun.com/xml/ns/javaee");
                    writeText(writer, displayNamesItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesValidator, "displayNames");
                }
            }
        }

        // ELEMENT: icon
        LocalCollection<Icon> icon = facesValidator.icon;
        if (icon != null) {
            for (Icon iconItem : icon) {
                if (iconItem != null) {
                    writer.writeStartElement(prefix, "icon", "http://java.sun.com/xml/ns/javaee");
                    writeIcon(writer, iconItem, context);
                    writer.writeEndElement();
                } else {
                    context.unexpectedNullValue(facesValidator, "icon");
                }
            }
        }

        // ELEMENT: validatorId
        String validatorIdRaw = facesValidator.validatorId;
        String validatorId = null;
        try {
            validatorId = Adapters.collapsedStringAdapterAdapter.marshal(validatorIdRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesValidator, "validatorId", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (validatorId != null) {
            writer.writeStartElement(prefix, "validator-id", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(validatorId);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesValidator, "validatorId");
        }

        // ELEMENT: validatorClass
        String validatorClassRaw = facesValidator.validatorClass;
        String validatorClass = null;
        try {
            validatorClass = Adapters.collapsedStringAdapterAdapter.marshal(validatorClassRaw);
        } catch (Exception e) {
            context.xmlAdapterError(facesValidator, "validatorClass", CollapsedStringAdapter.class, String.class, String.class, e);
        }
        if (validatorClass != null) {
            writer.writeStartElement(prefix, "validator-class", "http://java.sun.com/xml/ns/javaee");
            writer.writeCharacters(validatorClass);
            writer.writeEndElement();
        } else {
            context.unexpectedNullValue(facesValidator, "validatorClass");
        }

        // ELEMENT: attribute
        List<FacesAttribute> attribute = facesValidator.attribute;
        if (attribute != null) {
            for (FacesAttribute attributeItem : attribute) {
                writer.writeStartElement(prefix, "attribute", "http://java.sun.com/xml/ns/javaee");
                if (attributeItem != null) {
                    writeFacesAttribute(writer, attributeItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: property
        List<FacesProperty> property = facesValidator.property;
        if (property != null) {
            for (FacesProperty propertyItem : property) {
                writer.writeStartElement(prefix, "property", "http://java.sun.com/xml/ns/javaee");
                if (propertyItem != null) {
                    writeFacesProperty(writer, propertyItem, context);
                } else {
                    writer.writeXsiNil();
                }
                writer.writeEndElement();
            }
        }

        // ELEMENT: validatorExtension
        List<FacesValidatorExtension> validatorExtension = facesValidator.validatorExtension;
        if (validatorExtension != null) {
            for (FacesValidatorExtension validatorExtensionItem : validatorExtension) {
                if (validatorExtensionItem != null) {
                    writer.writeStartElement(prefix, "validator-extension", "http://java.sun.com/xml/ns/javaee");
                    writeFacesValidatorExtension(writer, validatorExtensionItem, context);
                    writer.writeEndElement();
                }
            }
        }

        context.afterMarshal(facesValidator, LifecycleCallback.NONE);
    }

}
