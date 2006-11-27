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

package org.openejb.deployment;

import java.io.File;
import java.net.URI;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.management.ObjectName;

import junit.framework.TestCase;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.deployment.util.DeploymentUtil;
import org.apache.geronimo.j2ee.deployment.EARContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContext;
import org.apache.geronimo.j2ee.j2eeobjectnames.J2eeContextImpl;
import org.apache.geronimo.j2ee.j2eeobjectnames.NameFactory;
import org.apache.geronimo.kernel.Kernel;
import org.apache.geronimo.kernel.config.ConfigurationModuleType;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.xbeans.j2ee.EjbJarDocument;
import org.apache.geronimo.xbeans.j2ee.EjbJarType;
import org.apache.xmlbeans.XmlObject;
import org.openejb.deployment.pkgen.TranQLPKGenBuilder;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarDocument;
import org.openejb.xbeans.ejbjar.OpenejbOpenejbJarType;
import org.tranql.ejb.EJBSchema;
import org.tranql.schema.Association;
import org.tranql.schema.AssociationEnd;
import org.tranql.schema.Attribute;
import org.tranql.schema.Entity;
import org.tranql.schema.FKAttribute;
import org.tranql.schema.Association.JoinDefinition;
import org.tranql.sql.SQLSchema;
import org.tranql.sql.Table;

/**
 *
 *
 * @version $Revision$ $Date$
 *
 * */
public class CMPEntityBuilderTest extends TestCase {
    private ObjectName listener = null;
    private Repository repository = null;
    private Kernel kernel = null;

    private static final File basedir = new File(System.getProperty("basedir", System.getProperty("user.dir")));

    protected void setUp() throws Exception {
        kernel = KernelHelper.getPreparedKernel();
    }

    protected void tearDown() throws Exception {
        kernel.shutdown();
    }

    public void testOneToOne() throws Exception {
        executeOneToOne("src/test/test-cmp/onetoone/simplepk/ejb-jar.xml", "src/test/test-cmp/onetoone/simplepk/openejb-jar.xml");
    }

    public void testOneToOneUnidirectional() throws Exception {
        executeOneToOne("src/test/test-cmp/onetoone/simplepk/ejb-jar.xml", "src/test/test-cmp/onetoone/simplepk/unidirectional-openejb-jar.xml");
    }

    public void testOneToOneRoleNameMappingOK() throws Exception {
        executeOneToOne("src/test/test-cmp/cmr-mapping/ejb-jar.xml", "src/test/test-cmp/cmr-mapping/names-ok-openejb-jar.xml");
    }

    public void testOneToOneRoleNameMappingWrongCMRName() throws Exception {
        try {
            executeOneToOne("src/test/test-cmp/cmr-mapping/ejb-jar.xml", "src/test/test-cmp/cmr-mapping/cmr-name-nok-openejb-jar.xml");
            fail();
        } catch (DeploymentException e) {
        }
    }

    public void testOneToOneRoleNameMappingWrongSourceName() throws Exception {
        try {
            executeOneToOne("src/test/test-cmp/cmr-mapping/ejb-jar.xml", "src/test/test-cmp/cmr-mapping/source-name-nok-openejb-jar.xml");
            fail();
        } catch (DeploymentException e) {
        }
    }

    private void executeOneToOne(String ejbJarFileName, String openejbJarFileName) throws Exception {
        File ejbJarFile = new File(basedir, ejbJarFileName);
        File openejbJarFile = new File(basedir, openejbJarFileName);
        EjbJarType ejbJarType = ((EjbJarDocument) XmlObject.Factory.parse(ejbJarFile)).getEjbJar();
        OpenejbOpenejbJarType openejbJarType = ((OpenejbOpenejbJarDocument) XmlObject.Factory.parse(openejbJarFile)).getOpenejbJar();

        OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder(KernelHelper.DEFAULT_PARENTID_ARRAY, listener, null, null, repository, kernel);
        CMPEntityBuilder builder = new CMPEntityBuilder(moduleBuilder);
        TranQLPKGenBuilder pkGen = new TranQLPKGenBuilder();

        File tempDir = DeploymentUtil.createTempDir();
        try {
            EARContext earContext = new EARContext(tempDir,
                    new URI("test"),
                    ConfigurationModuleType.EJB,
                    KernelHelper.DEFAULT_PARENTID_LIST,
                    kernel,
                    "null",
                    null,
                    null,
                    null,
                    null,
                    null, null);
            J2eeContext moduleJ2eeContext = new J2eeContextImpl("geronimo.server", "TestGeronimoServer", "null", NameFactory.EJB_MODULE, "MockModule", null, null);
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Schemata schemata = builder.buildSchemata(earContext, moduleJ2eeContext, "Test", ejbJarType, openejbJarType, cl, pkGen, null, null);

            EJBSchema ejbSchema = schemata.getEjbSchema();
            SQLSchema sqlSchema = schemata.getSqlSchema();

            assertOneToOneEntity(ejbSchema.getEntity("A"), ejbSchema.getEntity("B"));
            assertOneToOneEntity(sqlSchema.getEntity("A"), sqlSchema.getEntity("B"));
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    public void testOneToMany() throws Exception {
        executeOneToMany("src/test/test-cmp/onetomany/simplepk/ejb-jar.xml", "src/test/test-cmp/onetomany/simplepk/openejb-jar.xml");
    }

    public void testOneToManyUnidirectional() throws Exception {
        executeOneToMany("src/test/test-cmp/onetomany/simplepk/ejb-jar.xml", "src/test/test-cmp/onetomany/simplepk/unidirectional-openejb-jar.xml");
    }

    public void executeOneToMany(String ejbJarFileName, String openejbJarFileName) throws Exception {
        File ejbJarFile = new File(basedir, ejbJarFileName);
        File openejbJarFile = new File(basedir, openejbJarFileName);
        EjbJarType ejbJarType = ((EjbJarDocument) XmlObject.Factory.parse(ejbJarFile)).getEjbJar();
        OpenejbOpenejbJarType openejbJarType = ((OpenejbOpenejbJarDocument) XmlObject.Factory.parse(openejbJarFile)).getOpenejbJar();

        OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder(KernelHelper.DEFAULT_PARENTID_ARRAY, listener, null, null, repository, kernel);
        CMPEntityBuilder builder = new CMPEntityBuilder(moduleBuilder);
        TranQLPKGenBuilder pkGen = new TranQLPKGenBuilder();

        File tempDir = DeploymentUtil.createTempDir();
        try {
            EARContext earContext = new EARContext(tempDir,
                    new URI("test"),
                    ConfigurationModuleType.EJB,
                    KernelHelper.DEFAULT_PARENTID_LIST,
                    kernel,
                    "null",
                    null,
                    null,
                    null,
                    null,
                    null, null);

            J2eeContext moduleJ2eeContext = new J2eeContextImpl("geronimo.server", "TestGeronimoServer", "null", NameFactory.EJB_MODULE, "MockModule", null, null);
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Schemata schemata = builder.buildSchemata(earContext, moduleJ2eeContext, "Test", ejbJarType, openejbJarType, cl, pkGen, null, null);

            EJBSchema ejbSchema = schemata.getEjbSchema();
            SQLSchema sqlSchema = schemata.getSqlSchema();

            assertOneToManyEntity(ejbSchema.getEntity("A"), ejbSchema.getEntity("B"));
            assertOneToManyEntity(sqlSchema.getEntity("A"), sqlSchema.getEntity("B"));
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    public void testManyToMany() throws Exception {
        executeManyToMany("src/test/test-cmp/manytomany/simplepk/ejb-jar.xml", "src/test/test-cmp/manytomany/simplepk/openejb-jar.xml");
    }

    public void testManyToManyUnidirectional() throws Exception {
        executeManyToMany("src/test/test-cmp/manytomany/simplepk/ejb-jar.xml", "src/test/test-cmp/manytomany/simplepk/unidirectional-openejb-jar.xml");
    }

    public void executeManyToMany(String ejbJarFileName, String openejbJarFileName) throws Exception {
        File ejbJarFile = new File(basedir, ejbJarFileName);
        File openejbJarFile = new File(basedir, openejbJarFileName);
        EjbJarType ejbJarType = ((EjbJarDocument) XmlObject.Factory.parse(ejbJarFile)).getEjbJar();
        OpenejbOpenejbJarType openejbJarType = ((OpenejbOpenejbJarDocument) XmlObject.Factory.parse(openejbJarFile)).getOpenejbJar();

        OpenEJBModuleBuilder moduleBuilder = new OpenEJBModuleBuilder(KernelHelper.DEFAULT_PARENTID_ARRAY, listener, null, null, repository, kernel);
        CMPEntityBuilder builder = new CMPEntityBuilder(moduleBuilder);
        TranQLPKGenBuilder pkGen = new TranQLPKGenBuilder();

        File tempDir = DeploymentUtil.createTempDir();
        try {
            EARContext earContext = new EARContext(tempDir,
                    new URI("test"),
                    ConfigurationModuleType.EJB,
                    KernelHelper.DEFAULT_PARENTID_LIST,
                    kernel,
                    "null",
                    null,
                    null,
                    null,
                    null,
                    null, null);

            J2eeContext moduleJ2eeContext = new J2eeContextImpl("geronimo.server", "TestGeronimoServer", "null", NameFactory.EJB_MODULE, "MockModule", null, null);
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            Schemata schemata = builder.buildSchemata(earContext, moduleJ2eeContext, "Test", ejbJarType, openejbJarType, cl, pkGen, null, null);

            EJBSchema ejbSchema = schemata.getEjbSchema();
            SQLSchema sqlSchema = schemata.getSqlSchema();

            assertManyToManyEntity(ejbSchema.getEntity("A"), ejbSchema.getEntity("B"));
            assertManyToManyEntity(sqlSchema.getEntity("A"), sqlSchema.getEntity("B"));
        } finally {
            DeploymentUtil.recursiveDelete(tempDir);
        }
    }

    private void assertEntity(Entity leftEntity, Entity rightEntity) {
        List attributes = leftEntity.getAttributes();
        assertEquals(2, attributes.size());
        for (Iterator iter = attributes.iterator(); iter.hasNext();) {
            Attribute att = (Attribute) iter.next();
            if ( leftEntity instanceof Table ) {
                if ( att.getName().equals("field1") ) {
                    assertTrue(att.getPhysicalName().equals("a1"));
                    assertTrue(att.isIdentity());
                } else if ( att.getName().equals("field2") ) {
                    assertTrue(att.getPhysicalName().equals("a2"));
                } else {
                    fail("Unknow field.");
                }
            } else {
                if ( att.getName().equals("field1") ) {
                    assertTrue(att.getPhysicalName().equals("field1"));
                    assertTrue(att.isIdentity());
                } else if ( att.getName().equals("field2") ) {
                    assertTrue(att.getPhysicalName().equals("field2"));
                } else {
                    fail("Unknow field.");
                }
            }
        }
        attributes = rightEntity.getAttributes();
        assertEquals(3, attributes.size());
        for (Iterator iter = attributes.iterator(); iter.hasNext();) {
            Attribute att = (Attribute) iter.next();
            if ( rightEntity instanceof Table ) {
                if ( att.getName().equals("field1") ) {
                    assertTrue(att.getPhysicalName().equals("b1"));
                    assertTrue(att.isIdentity());
                } else if ( att.getName().equals("field2") ) {
                    assertTrue(att.getPhysicalName().equals("b2"));
                } else if ( att.getName().equals("field3") ) {
                    assertTrue(att.getPhysicalName().equals("fka1"));
                } else {
                    fail("Unknow field.");
                }
            } else {
                if ( att.getName().equals("field1") ) {
                    assertTrue(att.getPhysicalName().equals("field1"));
                    assertTrue(att.isIdentity());
                } else if ( att.getName().equals("field2") ) {
                    assertTrue(att.getPhysicalName().equals("field2"));
                } else if ( att.getName().equals("field3") ) {
                    assertTrue(att.getPhysicalName().equals("field3"));
                } else if ( att.getName().equals("field4") ) {
                    assertTrue(att.getPhysicalName().equals("field4"));
                } else {
                    fail("Unknow field.");
                }
            }
        }

        List pkEnds = leftEntity.getAssociationEnds();
        List fkEnds = rightEntity.getAssociationEnds();
        assertEquals(1, pkEnds.size());
        assertEquals(1, fkEnds.size());
        AssociationEnd pkEnd = (AssociationEnd) pkEnds.get(0);
        AssociationEnd fkEnd = (AssociationEnd) fkEnds.get(0);
        assertEquals("b", pkEnd.getName());
        assertEquals("a", fkEnd.getName());
        assertEquals(rightEntity, pkEnd.getEntity());
        assertEquals(leftEntity, fkEnd.getEntity());

        Association association = pkEnd.getAssociation();
        assertTrue(association == fkEnd.getAssociation());
    }

    private void assertOneToOneEntity(Entity pkEntity, Entity fkEntity) {
        assertEntity(pkEntity, fkEntity);

        List pkEnds = pkEntity.getAssociationEnds();
        List fkEnds = fkEntity.getAssociationEnds();
        AssociationEnd pkEnd = (AssociationEnd) pkEnds.get(0);
        assertTrue(pkEnd.isSingle());
        AssociationEnd fkEnd = (AssociationEnd) fkEnds.get(0);
        assertTrue(fkEnd.isSingle());

        Association association = pkEnd.getAssociation();
        assertTrue(pkEnd.isOneToOne());
        JoinDefinition joinDefinition = association.getJoinDefinition();
        assertEquals(pkEntity, joinDefinition.getPKEntity());
        assertEquals(fkEntity, joinDefinition.getFKEntity());
        LinkedHashMap linkedHashMap = joinDefinition.getPKToFKMapping();
        for (Iterator iter = linkedHashMap.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            if ( pkEntity instanceof Table ) {
                assertEquals("a1", ((Attribute) entry.getKey()).getPhysicalName());
            } else {
                assertEquals("field1", ((Attribute) entry.getKey()).getPhysicalName());
            }
            assertEquals("fka1", ((FKAttribute) entry.getValue()).getPhysicalName());
        }
    }

    private void assertOneToManyEntity(Entity pkEntity, Entity fkEntity) {
        assertEntity(pkEntity, fkEntity);

        List pkEnds = pkEntity.getAssociationEnds();
        List fkEnds = fkEntity.getAssociationEnds();
        AssociationEnd pkEnd = (AssociationEnd) pkEnds.get(0);
        assertTrue(pkEnd.isMulti());
        AssociationEnd fkEnd = (AssociationEnd) fkEnds.get(0);
        assertTrue(fkEnd.isSingle());

        Association association = pkEnd.getAssociation();
        assertTrue(pkEnd.isOneToMany());
        assertTrue(fkEnd.isManyToOne());
        JoinDefinition joinDefinition = association.getJoinDefinition();
        assertEquals(pkEntity, joinDefinition.getPKEntity());
        assertEquals(fkEntity, joinDefinition.getFKEntity());
        LinkedHashMap linkedHashMap = joinDefinition.getPKToFKMapping();
        for (Iterator iter = linkedHashMap.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            if ( pkEntity instanceof Table ) {
                assertEquals("a1", ((Attribute) entry.getKey()).getPhysicalName());
            } else {
                assertEquals("field1", ((Attribute) entry.getKey()).getPhysicalName());
            }
            assertEquals("fka1", ((FKAttribute) entry.getValue()).getPhysicalName());
        }
    }

    private void assertManyToManyEntity(Entity leftEntity, Entity rightEntity) {
        assertEntity(leftEntity, rightEntity);

        List leftEnds = leftEntity.getAssociationEnds();
        List rightEnds = rightEntity.getAssociationEnds();
        AssociationEnd leftEnd = (AssociationEnd) leftEnds.get(0);
        assertTrue(leftEnd.isMulti());
        AssociationEnd rightEnd = (AssociationEnd) rightEnds.get(0);
        assertTrue(rightEnd.isMulti());

        Association association = leftEnd.getAssociation();
        Entity mtmEntity = association.getManyToManyEntity();
        assertTrue(leftEnd.isManyToMany());
        assertNotNull(mtmEntity);
        assertEquals("MTM", mtmEntity.getName());
        JoinDefinition joinDefinition = association.getLeftJoinDefinition();
        boolean isMappedToFKA1 = false, isMappedToFKB1 = false; // we must be able to map both entities to the MTM table
        if ( leftEntity == joinDefinition.getPKEntity() ) {
            LinkedHashMap linkedHashMap = joinDefinition.getPKToFKMapping();
            for (Iterator iter = linkedHashMap.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                if ( leftEntity instanceof Table ) {
                    assertEquals("a1", ((Attribute) entry.getKey()).getPhysicalName());
                } else {
                    assertEquals("field1", ((Attribute) entry.getKey()).getPhysicalName());
                }
                assertEquals("fka1", ((FKAttribute) entry.getValue()).getPhysicalName());
                isMappedToFKA1 = true;
            }
        } else if ( rightEntity == joinDefinition.getPKEntity() ) {
            LinkedHashMap linkedHashMap = joinDefinition.getPKToFKMapping();
            for (Iterator iter = linkedHashMap.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                if ( rightEntity instanceof Table ) {
                    assertEquals("b1", ((Attribute) entry.getKey()).getPhysicalName());
                } else {
                    assertEquals("field1", ((Attribute) entry.getKey()).getPhysicalName());
                }
                assertEquals("fkb1", ((FKAttribute) entry.getValue()).getPhysicalName());
                isMappedToFKB1 = true;
            }
        } else {
            fail("JoinDefinitions are misconfigured.");
        }
        joinDefinition = association.getRightJoinDefinition();
        if ( leftEntity == joinDefinition.getPKEntity() ) {
            LinkedHashMap linkedHashMap = joinDefinition.getPKToFKMapping();
            for (Iterator iter = linkedHashMap.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                if ( leftEntity instanceof Table ) {
                    assertEquals("a1", ((Attribute) entry.getKey()).getPhysicalName());
                } else {
                    assertEquals("field1", ((Attribute) entry.getKey()).getPhysicalName());
                }
                assertEquals("fka1", ((FKAttribute) entry.getValue()).getPhysicalName());
                isMappedToFKA1 = true;
            }
        } else if ( rightEntity == joinDefinition.getPKEntity() ) {
            LinkedHashMap linkedHashMap = joinDefinition.getPKToFKMapping();
            for (Iterator iter = linkedHashMap.entrySet().iterator(); iter.hasNext();) {
                Map.Entry entry = (Map.Entry) iter.next();
                if ( rightEntity instanceof Table ) {
                    assertEquals("b1", ((Attribute) entry.getKey()).getPhysicalName());
                } else {
                    assertEquals("field1", ((Attribute) entry.getKey()).getPhysicalName());
                }
                assertEquals("fkb1", ((FKAttribute) entry.getValue()).getPhysicalName());
                isMappedToFKB1 = true;
            }
        } else {
            fail("JoinDefinitions are misconfigured.");
        }
        if(!isMappedToFKA1) {
            fail("No mapping present from A.a1 to MTM.fka1");
        }
        if(!isMappedToFKB1) {
            fail("No mapping present from B.b1 to MTM.fkb1");
        }
    }
}