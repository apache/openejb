/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. The name "OpenEJB" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of The OpenEJB Group.  For written permission,
 *    please contact openejb-group@openejb.sf.net.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.apache.openejb.corba.security.config.tss;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.net.ssl.SSLSession;
import javax.security.auth.Subject;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CSIIOP.SECIOP_SEC_TRANS;
import org.omg.CSIIOP.SECIOP_SEC_TRANSHelper;
import org.omg.CSIIOP.TAG_SECIOP_SEC_TRANS;
import org.omg.CSIIOP.TLS_SEC_TRANSHelper;
import org.omg.CSIIOP.TransportAddress;
import org.omg.IOP.Codec;
import org.omg.IOP.TaggedComponent;

import org.apache.openejb.corba.security.SASException;
import org.apache.openejb.corba.util.Util;


/**
 * TODO: this class needs to be revisited.
 *
 * @version $Rev$ $Date$
 */
public class TSSSECIOPTransportConfig extends TSSTransportMechConfig {

    private short supports;
    private short requires;
    private String mechOID;
    private String targetName;
    private final List addresses = new ArrayList(1);

    public TSSSECIOPTransportConfig() {
    }

    public TSSSECIOPTransportConfig(TaggedComponent component, Codec codec) throws Exception {
        Any any = codec.decode_value(component.component_data, TLS_SEC_TRANSHelper.type());
        SECIOP_SEC_TRANS tst = SECIOP_SEC_TRANSHelper.extract(any);

        supports = tst.target_supports;
        requires = tst.target_requires;
        mechOID = Util.decodeOID(tst.mech_oid);
        targetName = new String(tst.target_name);

        for (int i = 0; i < tst.addresses.length; i++) {
            addresses.add(new TSSTransportAddressConfig(tst.addresses[i].port, tst.addresses[i].host_name));
        }
    }

    public short getSupports() {
        return supports;
    }

    public void setSupports(short supports) {
        this.supports = supports;
    }

    public short getRequires() {
        return requires;
    }

    public void setRequires(short requires) {
        this.requires = requires;
    }

    public String getMechOID() {
        return mechOID;
    }

    public void setMechOID(String mechOID) {
        this.mechOID = mechOID;
    }

    public String getTargetName() {
        return targetName;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public List getAddresses() {
        return addresses;
    }

    public TaggedComponent encodeIOR(ORB orb, Codec codec) throws Exception {
        TaggedComponent result = new TaggedComponent();

        SECIOP_SEC_TRANS sst = new SECIOP_SEC_TRANS();

        sst.target_supports = supports;
        sst.target_requires = requires;
        sst.mech_oid = Util.encodeOID(mechOID);
        sst.target_name = targetName.getBytes();

        sst.addresses = new TransportAddress[addresses.size()];

        int i = 0;
        TSSTransportAddressConfig transportConfig;
        for (Iterator iter = addresses.iterator(); iter.hasNext();) {
            transportConfig = (TSSTransportAddressConfig) iter.next();
            sst.addresses[i++] = new TransportAddress(transportConfig.getHostname(), transportConfig.getPort());
        }

        Any any = orb.create_any();
        SECIOP_SEC_TRANSHelper.insert(any, sst);

        result.tag = TAG_SECIOP_SEC_TRANS.value;
        result.component_data = codec.encode_value(any);

        return result;
    }

    public Subject check(SSLSession session) throws SASException {
        return new Subject();
    }

}
