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
import java.util.Iterator;

import org.omg.CORBA.ORB;
import org.omg.IOP.Codec;

import org.apache.geronimo.interop.CSI.ITTAbsent;
import org.apache.geronimo.interop.CSI.ITTPrincipalName;
import org.apache.geronimo.interop.CSIIOP.DelegationByClient;
import org.apache.geronimo.interop.CSIIOP.IdentityAssertion;
import org.apache.geronimo.interop.CSIIOP.SAS_ContextSec;
import org.apache.geronimo.interop.CSIIOP.ServiceConfiguration;

import org.openejb.corba.util.Util;


/**
 * @version $Rev: $ $Date$
 */
public class TSSSASMechConfig implements Serializable {

    private short supports;
    private short requires;
    private boolean required;
    private final ArrayList privilegeAuthorities = new ArrayList();
    private final ArrayList namingMechanisms = new ArrayList();
    private int identityTypes = ITTAbsent.value;

    public TSSSASMechConfig() {
    }

    public TSSSASMechConfig(SAS_ContextSec context) throws Exception {
        supports = context.target_supports;
        requires = context.target_requires;

        ServiceConfiguration[] c = context.privilege_authorities;
        for (int i = 0; i < c.length; i++) {
            privilegeAuthorities.add(TSSServiceConfigurationConfig.decodeIOR(c[i]));
        }

        byte[][] n = context.supported_naming_mechanisms;
        for (int i = 0; i < n.length; i++) {
            namingMechanisms.add(Util.decodeOID(n[i]));
        }

        identityTypes = context.supported_identity_types;
        supports = context.target_supports;
        requires = context.target_requires;
    }

    public void addServiceConfigurationConfig(TSSServiceConfigurationConfig config) {
        privilegeAuthorities.add(config);

        supports |= DelegationByClient.value;
        if (required) requires = DelegationByClient.value;
    }

    public TSSServiceConfigurationConfig serviceConfigurationAt(int i) {
        return (TSSServiceConfigurationConfig) privilegeAuthorities.get(i);
    }

    public int paSize() {
        return privilegeAuthorities.size();
    }

    public void addnamingMechanism(String mech) {
        namingMechanisms.add(mech);

        identityTypes |= ITTPrincipalName.value;
        supports |= IdentityAssertion.value;
    }

    public String namingMechanismAt(int i) {
        return (String) namingMechanisms.get(i);
    }

    public int nmSize() {
        return namingMechanisms.size();
    }

    public int getIdentityTypes() {
        return identityTypes;
    }

    public void setIdentityTypes(int identityTypes) {
        this.identityTypes = identityTypes;
        if (identityTypes != 0) supports |= IdentityAssertion.value;
    }

    public short getSupports() {
        return supports;
    }

    public short getRequires() {
        return requires;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;

        if (required) requires = (short) (supports & DelegationByClient.value);
    }

    public SAS_ContextSec encodeIOR(ORB orb, Codec codec) throws Exception {
        SAS_ContextSec result = new SAS_ContextSec();

        int i = 0;
        result.privilege_authorities = new ServiceConfiguration[privilegeAuthorities.size()];
        for (Iterator iter = privilegeAuthorities.iterator(); iter.hasNext();) {
            result.privilege_authorities[i++] = ((TSSServiceConfigurationConfig) iter.next()).generateServiceConfiguration();
        }

        i = 0;
        result.supported_naming_mechanisms = new byte[namingMechanisms.size()][];
        for (Iterator iter = namingMechanisms.iterator(); iter.hasNext();) {
            result.supported_naming_mechanisms[i++] = Util.encodeOID((String) iter.next());
        }

        result.supported_identity_types = identityTypes;

        result.target_supports = supports;
        result.target_requires = requires;


        return result;
    }
}
