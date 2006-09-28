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
package org.openejb.corba.security.config.tss;

import java.io.Serializable;
import java.util.ArrayList;
import javax.security.auth.Subject;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CSI.EstablishContext;
import org.omg.CSIIOP.CompoundSecMech;
import org.omg.CSIIOP.CompoundSecMechList;
import org.omg.CSIIOP.CompoundSecMechListHelper;
import org.omg.CSIIOP.TAG_CSI_SEC_MECH_LIST;
import org.omg.IOP.Codec;
import org.omg.IOP.TaggedComponent;

import org.openejb.corba.security.SASException;


/**
 * @version $Rev$ $Date$
 */
public class TSSCompoundSecMechListConfig implements Serializable {

    private boolean stateful;
    private final ArrayList mechs = new ArrayList();

    public boolean isStateful() {
        return stateful;
    }

    public void setStateful(boolean stateful) {
        this.stateful = stateful;
    }

    public void add(TSSCompoundSecMechConfig mech) {
        mechs.add(mech);
    }

    public TSSCompoundSecMechConfig mechAt(int i) {
        return (TSSCompoundSecMechConfig) mechs.get(i);
    }

    public int size() {
        return mechs.size();
    }

    public TaggedComponent encodeIOR(ORB orb, Codec codec) throws Exception {
        CompoundSecMechList csml = new CompoundSecMechList();

        csml.stateful = stateful;
        csml.mechanism_list = new CompoundSecMech[mechs.size()];

        for (int i = 0; i < mechs.size(); i++) {
            csml.mechanism_list[i] = ((TSSCompoundSecMechConfig) mechs.get(i)).encodeIOR(orb, codec);
        }

        Any any = orb.create_any();
        CompoundSecMechListHelper.insert(any, csml);

        return new TaggedComponent(TAG_CSI_SEC_MECH_LIST.value, codec.encode_value(any));
    }

    public static TSSCompoundSecMechListConfig decodeIOR(Codec codec, TaggedComponent taggedComponent) throws Exception {
        TSSCompoundSecMechListConfig result = new TSSCompoundSecMechListConfig();

        Any any = codec.decode_value(taggedComponent.component_data, CompoundSecMechListHelper.type());
        CompoundSecMechList csml = CompoundSecMechListHelper.extract(any);

        result.setStateful(csml.stateful);

        for (int i = 0; i < csml.mechanism_list.length; i++) {
            result.add(TSSCompoundSecMechConfig.decodeIOR(codec, csml.mechanism_list[i]));
        }

        return result;
    }

    public Subject check(EstablishContext msg) throws SASException {
        Subject result = null;

        for (int i = 0; i < mechs.size(); i++) {
            result = ((TSSCompoundSecMechConfig) mechs.get(i)).check(msg);
            if (result != null) break;
        }

        return result;
    }
}
