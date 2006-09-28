/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


package org.apache.openejb.deployment;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.schema.SchemaConversionUtils;
import org.apache.geronimo.testsupport.XmlBeansTestSupport;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;

/**
 * ejb 1.1 dtd appears to be a subset of ejb 2.0 dtd so the same xsl should
 * work for both.
 *
 * @version $Rev$ $Date$
 */
public class SchemaConversionTest extends XmlBeansTestSupport {
    private static final Log log = LogFactory.getLog(SchemaConversionTest.class);

    private ClassLoader classLoader = this.getClass().getClassLoader();

    public void testEJB11ToEJB21Transform() throws Exception {
        URL srcXml = classLoader.getResource("j2ee_1_2dtd/ejb-1-11.xml");
        URL expectedOutputXml = classLoader.getResource("j2ee_1_2dtd/ejb-1-21.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
        XmlBeansUtil.validateDD(expected);
        xmlObject = OpenEjbModuleBuilder.convertToEJBSchema(xmlObject);
//        log.debug(xmlObject.toString());
//        log.debug(expected.toString());
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(xmlObject, expected, problems);
        Assert.assertTrue("Differences: " + problems, ok);
        //make sure trying to convert twice has no bad effects
        XmlCursor cursor2 = xmlObject.newCursor();
        try {
            String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd";
            String version = "2.1";
            Assert.assertFalse(SchemaConversionUtils.convertToSchema(cursor2, SchemaConversionUtils.J2EE_NAMESPACE, schemaLocationURL, version));
        } finally {
            cursor2.dispose();
        }
        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
        Assert.assertTrue("Differences after reconverting to schema: " + problems, ok2);
        //do the whole transform twice...
        xmlObject = OpenEjbModuleBuilder.convertToEJBSchema(xmlObject);
        boolean ok3 = compareXmlObjects(xmlObject, expected, problems);
        Assert.assertTrue("Differences after reconverting to ejb schema: " + problems, ok3);
    }

    public void testEJB20ToEJB21Transform() throws Exception {
        URL srcXml = classLoader.getResource("j2ee_1_3dtd/ejb-jar.xml");
        URL expectedOutputXml = classLoader.getResource("j2ee_1_3dtd/ejb-jar-21.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
        XmlBeansUtil.validateDD(expected);
        xmlObject = OpenEjbModuleBuilder.convertToEJBSchema(xmlObject);
//        log.debug(xmlObject.toString());
//        log.debug(expected.toString());
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(xmlObject, expected, problems);
        Assert.assertTrue("Differences: " + problems, ok);
        //make sure trying to convert twice has no bad effects
        XmlCursor cursor2 = xmlObject.newCursor();
        try {
            String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd";
            String version = "2.1";
            Assert.assertFalse(SchemaConversionUtils.convertToSchema(cursor2, SchemaConversionUtils.J2EE_NAMESPACE, schemaLocationURL, version));
        } finally {
            cursor2.dispose();
        }
        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
        Assert.assertTrue("Differences after reconverting to schema: " + problems, ok2);
        //do the whole transform twice...
        xmlObject = OpenEjbModuleBuilder.convertToEJBSchema(xmlObject);
        boolean ok3 = compareXmlObjects(xmlObject, expected, problems);
        Assert.assertTrue("Differences after reconverting to ejb schema: " + problems, ok3);
    }

    public void testMDB20ToEJB21TransformBugGERONIMO_1649() throws Exception {
        URL srcXml = classLoader.getResource("j2ee_1_3dtd/mdb-ejb-jar-20-GERONIMO-1649.xml");
        URL expectedOutputXml = classLoader.getResource("j2ee_1_3dtd/mdb-ejb-jar-21-GERONIMO-1649.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
        XmlBeansUtil.validateDD(expected);
        xmlObject = OpenEjbModuleBuilder.convertToEJBSchema(xmlObject);
//        log.debug(xmlObject.toString());
//        log.debug(expected.toString());
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(xmlObject, expected, problems);
        Assert.assertTrue("Differences: " + problems, ok);
        //make sure trying to convert twice has no bad effects
        XmlCursor cursor2 = xmlObject.newCursor();
        try {
            String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd";
            String version = "2.1";
            Assert.assertFalse(SchemaConversionUtils.convertToSchema(cursor2, SchemaConversionUtils.J2EE_NAMESPACE, schemaLocationURL, version));
        } finally {
            cursor2.dispose();
        }
        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
        Assert.assertTrue("Differences after reconverting to schema: " + problems, ok2);
        //do the whole transform twice...
        xmlObject = OpenEjbModuleBuilder.convertToEJBSchema(xmlObject);
        boolean ok3 = compareXmlObjects(xmlObject, expected, problems);
        Assert.assertTrue("Differences after reconverting to ejb schema: " + problems, ok3);
    }

    public void testMDB20To21Transform() throws Exception {
        URL srcXml = classLoader.getResource("j2ee_1_3dtd/mdb-ejb-jar-20.xml");
        URL expectedOutputXml = classLoader.getResource("j2ee_1_3dtd/mdb-ejb-jar-21.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
        XmlBeansUtil.validateDD(expected);
        xmlObject = OpenEjbModuleBuilder.convertToEJBSchema(xmlObject);
//        log.debug(xmlObject.toString());
//        log.debug(expected.toString());
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(xmlObject, expected, problems);
        Assert.assertTrue("Differences: " + problems, ok);
        //make sure trying to convert twice has no bad effects
        XmlCursor cursor2 = xmlObject.newCursor();
        try {
            String schemaLocationURL = "http://java.sun.com/xml/ns/j2ee/ejb-jar_2_1.xsd";
            String version = "2.1";
            Assert.assertFalse(SchemaConversionUtils.convertToSchema(cursor2, SchemaConversionUtils.J2EE_NAMESPACE, schemaLocationURL, version));
        } finally {
            cursor2.dispose();
        }
        boolean ok2 = compareXmlObjects(xmlObject, expected, problems);
        Assert.assertTrue("Differences after reconverting to schema: " + problems, ok2);
        //do the whole transform twice...
        xmlObject = OpenEjbModuleBuilder.convertToEJBSchema(xmlObject);
        boolean ok3 = compareXmlObjects(xmlObject, expected, problems);
        Assert.assertTrue("Differences after reconverting to ejb schema: " + problems, ok3);
    }


    public void testEJB21To21DoesNothing() throws Exception {
        URL srcXml = classLoader.getResource("j2ee_1_4schema/ejb-jar.xml");
        URL expectedOutputXml = classLoader.getResource("j2ee_1_4schema/ejb-jar.xml");
        XmlObject xmlObject = XmlObject.Factory.parse(srcXml);
        xmlObject = OpenEjbModuleBuilder.convertToEJBSchema(xmlObject);
        XmlObject expected = XmlObject.Factory.parse(expectedOutputXml);
        List problems = new ArrayList();
        boolean ok = compareXmlObjects(xmlObject, expected, problems);
        Assert.assertTrue("Differences: " + problems, ok);
    }

}
