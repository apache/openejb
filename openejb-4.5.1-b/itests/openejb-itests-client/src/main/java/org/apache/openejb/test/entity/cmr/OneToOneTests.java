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
package org.apache.openejb.test.entity.cmr;


import org.apache.openejb.test.entity.cmr.onetoone.PersonLocal;
import org.apache.openejb.test.entity.cmr.onetoone.PersonLocalHome;
import org.apache.openejb.test.entity.cmr.onetoone.LicenseLocal;
import org.apache.openejb.test.entity.cmr.onetoone.LicenseLocalHome;

import javax.ejb.CreateException;
import javax.ejb.FinderException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

/**
 *
 * @version $Revision$ $Date$
 */
public class OneToOneTests extends AbstractCMRTest {
    private PersonLocalHome personLocalHome;
    private LicenseLocalHome licenseLocalHome;

    public OneToOneTests() {
        super("OneToOne.");
    }

    protected void setUp() throws Exception {
        super.setUp();

        personLocalHome = (PersonLocalHome) initialContext.lookup("client/tests/entity/cmr/oneToOne/PersonLocal");
        licenseLocalHome = (LicenseLocalHome) initialContext.lookup("client/tests/entity/cmr/oneToOne/LicenseLocal");
    }

    public void test00_AGetBExistingAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PersonLocal person = findPerson(1);
            assertNotNull("person is null", person);
            LicenseLocal license = person.getLicense();
            assertNotNull("license is null", license);
            assertEquals(new Integer(11), license.getId());
            assertEquals("value11", license.getNumber());
        } finally {
            completeTransaction();
        }
    }

    public void test01_BGetAExistingAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            LicenseLocal license = findLicense(11);
            PersonLocal person = license.getPerson();
            assertNotNull(person);
            assertEquals(new Integer(1), person.getId());
            assertEquals("value1", person.getName());
        } finally {
            completeTransaction();
        }
    }

    public void test02_ASetBDropExisting() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PersonLocal person = findPerson(1);
            person.setLicense(null);
        } finally {
            completeTransaction();
        }

        assertUnlinked(1);
    }

    public void test03_BSetADropExisting() throws Exception {
        resetDB();
        beginTransaction();
        try {
            LicenseLocal license = findLicense(11);
            license.setPerson(null);
        } finally {
            completeTransaction();
        }

        assertUnlinked(1);
    }

    public void test04_ASetBNewAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PersonLocal person = findPerson(2);
            LicenseLocal license = createLicense(22);
            person.setLicense(license);
        } finally {
            completeTransaction();
        }

        assertLinked(2, 22);
    }

    public void test05_BSetANewAB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PersonLocal person = findPerson(2);
            LicenseLocal license = createLicense(22);
            license.setPerson(person);
        } finally {
            completeTransaction();
        }

        assertLinked(2, 22);
    }

    public void test06_ASetBExistingBNewA() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PersonLocal person = findPerson(2);
            LicenseLocal license = findLicense(11);
            person.setLicense(license);
        } finally {
            completeTransaction();
        }

        assertLinked(2, 11);
    }

    public void test07_BSetAExistingBNewA() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PersonLocal person = createPerson(3);
            LicenseLocal license = findLicense(11);
            license.setPerson(person);
        } finally {
            completeTransaction();
        }
        assertLinked(3, 11);
    }

    public void test09_BSetAExistingANewB() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PersonLocal person = findPerson(1);
            LicenseLocal license = createLicense(22);
            license.setPerson(person);
        } finally {
            completeTransaction();
        }
        assertLinked(1, 22);
    }

    public void test10_RemoveRelationships() throws Exception {
        resetDB();
        beginTransaction();
        try {
            PersonLocal person = findPerson(1);
            person.remove();
        } finally {
            completeTransaction();
        }

        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM License");
        assertTrue(rs.next());
        assertEquals(1, rs.getInt(1));
        close(rs);
        rs = s.executeQuery("SELECT COUNT(*) FROM License WHERE person_id = 1");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        close(rs);
        close(s);
        close(c);
    }

    public void test11_CascadeDelete() throws Exception {
        resetDB();
        beginTransaction();
        try {
            LicenseLocal license = findLicense(11);
            license.remove();
        } finally {
            completeTransaction();
        }

        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM Person WHERE id = 1");
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        close(rs);
        close(s);
        close(c);
    }

    // todo enable these when field to fk is implemented
    public void Xtest12_CMPMappedToForeignKeyColumn() throws Exception {
        resetDB();
        beginTransaction();
        try {
            LicenseLocal license = findLicense(11);

            Integer field3 = license.getPoints();
            assertEquals(license.getPerson().getPrimaryKey(), field3);
        } finally {
            completeTransaction();
        }
    }

    // todo enable these when field to fk is implemented
    public void Xtest13_SetCMPMappedToForeignKeyColumn() throws Exception {
        resetDB();
        beginTransaction();
        try {
            LicenseLocal license = findLicense(11);

            license.setPoints(new Integer(2));

            PersonLocal person = license.getPerson();
            assertEquals(new Integer(2), person.getId());
            assertEquals("value2", person.getName());
        } finally {
            completeTransaction();
        }
    }

    private PersonLocal createPerson(int personId) throws CreateException {
        PersonLocal person = personLocalHome.create(personId);
        person.setName("value" + personId);
        return person;
    }

    private PersonLocal findPerson(int personId) throws FinderException {
        return personLocalHome.findByPrimaryKey(personId);
    }

    private LicenseLocal createLicense(int licenseId) throws CreateException {
        LicenseLocal license = licenseLocalHome.create(licenseId);
        license.setNumber("value" + licenseId);
        return license;
    }
    private LicenseLocal findLicense(int licenseId) throws FinderException {
        return licenseLocalHome.findByPrimaryKey(licenseId);
    }


    private void assertLinked(int personId, int licenseId) throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT name FROM Person WHERE id = " + personId);
        assertTrue(rs.next());
        assertEquals("value" + personId, rs.getString("name"));
        close(rs);

        rs = s.executeQuery("SELECT id, number FROM License WHERE person_id = " + personId);
        assertTrue(rs.next());
        assertEquals(licenseId, rs.getInt("id"));
        assertEquals("value" + licenseId, rs.getString("number"));
        close(rs);
        close(s);
        close(c);
    }

    private void assertUnlinked(int personId) throws Exception {
        Connection c = ds.getConnection();
        Statement s = c.createStatement();
        ResultSet rs = s.executeQuery("SELECT COUNT(*) FROM License WHERE person_id = " + personId);
        assertTrue(rs.next());
        assertEquals(0, rs.getInt(1));
        close(rs);
        close(s);
        close(c);
    }


    private void resetDB() throws Exception {
        Connection connection = ds.getConnection();
        Statement statement = null;
        try {
            statement = connection.createStatement();

            try {
                statement.execute("DELETE FROM Person");
            } catch (SQLException ignored) {
            }
            try {
                statement.execute("DELETE FROM License");
            } catch (SQLException ignored) {
            }
        } finally {
            close(statement);
            close(connection);
        }

        PersonLocal person1 = createPerson(1);
        createPerson(2);

        LicenseLocal license = createLicense(11);
        license.setPerson(person1);
    }

    protected void dump() throws Exception {
        dumpTable(ds, "Person");
        dumpTable(ds, "License");
    }
}
