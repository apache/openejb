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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.openejb.deployment;

import java.util.Map;
import java.util.HashMap;
import java.io.File;
import java.io.IOException;
import javax.xml.namespace.QName;

import org.apache.openejb.xbeans.ejbjar.OpenejbOpenejbJarDocument;
import org.apache.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.apache.openejb.xbeans.pkgen.EjbKeyGeneratorDocument;
import org.apache.geronimo.schema.NamespaceElementConverter;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlException;

/**
 * @version $Revision$ $Date$
 */
public final class XmlBeansHelper {
    private XmlBeansHelper() {
    }

    private static QName OPENEJBJAR_QNAME = OpenejbOpenejbJarDocument.type.getDocumentElementName();

    static {
        Map conversions = new HashMap();
        QName name = EjbKeyGeneratorDocument.type.getDocumentElementName();
        conversions.put(name.getLocalPart(), new NamespaceElementConverter(name.getNamespaceURI()));
        SchemaConversionUtils.registerNamespaceConversions(conversions);
    }

    public static EjbJarType loadEjbJar(File file) throws IOException, XmlException {
        EjbJarDocument ejbJarDoc = ((EjbJarDocument) XmlObject.Factory.parse(file));
        ejbJarDoc = OpenEjbModuleBuilder.convertToEJBSchema(ejbJarDoc);
        return ejbJarDoc.getEjbJar();
    }

    public static OpenejbOpenejbJarType loadOpenEjbJar(File file) throws IOException, XmlException {
        OpenejbOpenejbJarDocument openejbOpenejbJarDocument = ((OpenejbOpenejbJarDocument) XmlBeansUtil.parse(file));
        OpenejbOpenejbJarType openejbJarType = openejbOpenejbJarDocument.getOpenejbJar();
        openejbJarType = (OpenejbOpenejbJarType) SchemaConversionUtils.fixGeronimoSchema(openejbJarType, OPENEJBJAR_QNAME, OpenejbOpenejbJarType.type);
        return openejbJarType;
    }
}
