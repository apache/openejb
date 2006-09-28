/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY THE OPENEJB GROUP AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * THE OPENEJB GROUP OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
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
