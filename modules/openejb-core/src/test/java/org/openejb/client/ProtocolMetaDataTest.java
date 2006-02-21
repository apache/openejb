package org.openejb.client;

/**
 * @version $Revision$ $Date$
 */

import junit.framework.*;
import org.openejb.client.ProtocolMetaData;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

public class ProtocolMetaDataTest extends TestCase {
    private ProtocolMetaData protocol;

    protected void setUp() throws Exception {
        protocol = new ProtocolMetaData("2.4");
    }

    public void testGetSpec() throws Exception {
        assertEquals("OEJP/2.4", protocol.getSpec());
    }

    public void testGetId() throws Exception {
        assertEquals("OEJP", protocol.getId());
    }

    public void testGetMajor() throws Exception {
        assertEquals(2, protocol.getMajor());
    }

    public void testGetMinor() throws Exception {
        assertEquals(4, protocol.getMinor());
    }

    public void testGetVersion() throws Exception {
        assertEquals("2.4", protocol.getVersion());
    }

    public void testSerialization() throws Exception {
        ProtocolMetaData exptected = new ProtocolMetaData("1.2");
        ProtocolMetaData actual = new ProtocolMetaData();
        externalize(exptected, actual);
        assertEquals(exptected.getId(), actual.getId());
        assertEquals(exptected.getMajor(), actual.getMajor());
        assertEquals(exptected.getMinor(), actual.getMinor());
        assertEquals(exptected.getVersion(), actual.getVersion());
        assertEquals(exptected.getSpec(), actual.getSpec());
    }
    
    private void externalize(ProtocolMetaData original, ProtocolMetaData copy) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(baos);

        original.writeExternal(out);
        out.close();

        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream in = new ObjectInputStream(bais);

        copy.readExternal(in);
    }
}