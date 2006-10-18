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
package org.apache.openejb.corba.security.config.tss;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.security.auth.Subject;

import org.omg.CORBA.ORB;
import org.omg.CSI.EstablishContext;
import org.omg.CSI.ITTPrincipalName;
import org.omg.CSI.IdentityToken;
import org.omg.CSIIOP.DelegationByClient;
import org.omg.CSIIOP.IdentityAssertion;
import org.omg.CSIIOP.SAS_ContextSec;
import org.omg.CSIIOP.ServiceConfiguration;
import org.omg.IOP.Codec;
import org.apache.openejb.corba.security.SASException;
import org.apache.openejb.corba.util.Util;


/**
 * @version $Rev$ $Date$
 */
public class TSSSASMechConfig implements Serializable {

    private short supports;
    private short requires;
    private boolean required;
    private final ArrayList privilegeAuthorities = new ArrayList();
    private final Map idTokens = new HashMap();

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
            String oid = Util.decodeOID(n[i]);

            //TODO is this needed?
            if (TSSITTPrincipalNameGSSUP.OID.equals(oid)) {
                //TODO this doesn't make sense if we plan to use this for identity check.
                addIdentityToken(new TSSITTPrincipalNameGSSUP(null, null, null));
            }
        }

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

    public void addIdentityToken(TSSSASIdentityToken token) {
        idTokens.put(new Integer(token.getType()), token);

        if (token.getType() > 0) supports |= IdentityAssertion.value;
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
        if (required) requires |= (short) (supports & DelegationByClient.value);
    }

    public SAS_ContextSec encodeIOR(ORB orb, Codec codec) throws Exception {

        SAS_ContextSec result = new SAS_ContextSec();

        int i = 0;
        result.privilege_authorities = new ServiceConfiguration[privilegeAuthorities.size()];
        for (Iterator iter = privilegeAuthorities.iterator(); iter.hasNext();) {
            result.privilege_authorities[i++] = ((TSSServiceConfigurationConfig) iter.next()).generateServiceConfiguration();
        }

        ArrayList list = new ArrayList();
        for (Iterator iter = idTokens.values().iterator(); iter.hasNext();) {
            TSSSASIdentityToken token = (TSSSASIdentityToken) iter.next();

            if (token.getType() == ITTPrincipalName.value) {
                list.add(token);
            }
            result.supported_identity_types |= token.getType();
        }

        i = 0;
        result.supported_naming_mechanisms = new byte[list.size()][];
        for (Iterator iter = list.iterator(); iter.hasNext();) {
            TSSSASIdentityToken token = (TSSSASIdentityToken) iter.next();

            result.supported_naming_mechanisms[i++] = Util.encodeOID(token.getOID());
        }

        result.target_supports = supports;
        result.target_requires = requires;

        return result;
    }

    public Subject check(EstablishContext msg) throws SASException {
//        Subject result = null;

//        try {
        if (msg.identity_token != null) {
            IdentityToken identityToken = msg.identity_token;
            int discriminator = identityToken.discriminator();
            TSSSASIdentityToken tssIdentityToken = (TSSSASIdentityToken) idTokens.get(new Integer(discriminator));
            if (tssIdentityToken == null) {
                throw new SASException(1, new Exception("Unsupported IdentityTokenType: " + discriminator));
            } else {
                return tssIdentityToken.check(identityToken);
            }
        } else {
            return null;
        }
//                switch (discriminator) {
//                    case org.omg.CSI.ITTAbsent.value:
//                        break;
//                    case org.omg.CSI.ITTAnonymous.value:
//                        //TODO implement this one or figure out if this is correct???
//                        break;
//                    case ITTPrincipalName.value:
//                        byte[] principalNameToken = identityToken.principal_name();
//                        Any any = Util.getCodec().decode_value(principalNameToken, GSS_NT_ExportedNameHelper.type());
//                        byte[] principalNameBytes = GSS_NT_ExportedNameHelper.extract(any);
//                        String principalName = Util.decodeGSSExportName(principalNameBytes);
//                        Principal basePrincipal = new GeronimoUserPrincipal(principalName);
//                        //TODO parameterize or otherwise select realm name
//                        Principal wrappedPrincipal = new RealmPrincipal("cts-properties-realm", basePrincipal);
//                        result = new Subject();
//                        result.getPrincipals().add(basePrincipal);
//                        result.getPrincipals().add(wrappedPrincipal);
//                        break;
//                    case org.omg.CSI.ITTX509CertChain.value:
//                        byte[] ccChainBytes = identityToken.certificate_chain();
//                        //TODO implement this one
//                        throw new SASException(1, new Exception("NYI -- cert chain identity token"));
//                    case org.omg.CSI.ITTDistinguishedName.value:
//                        //TODO implement this one
//                        throw new SASException(1, new Exception("NYI -- distinguished name identity token"));
//                    default:
//                        throw new SASException(1);
//                }
//
//            }
//        } catch (TypeMismatch typeMismatch) {
//            throw new SASException(1, typeMismatch);
//        } catch (FormatMismatch formatMismatch) {
//            throw new SASException(1, formatMismatch);
////        } catch (UnsupportedEncodingException e) {
////            throw new SASException(1, e);
//        }
//
//        return result;
    }
}
