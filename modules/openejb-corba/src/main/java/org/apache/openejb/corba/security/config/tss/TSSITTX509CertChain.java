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

import javax.security.auth.Subject;

import org.omg.CSI.ITTX509CertChain;
import org.omg.CSI.IdentityToken;
import org.apache.openejb.corba.security.SASException;


/**
 * @version $Rev$ $Date$
 */
public class TSSITTX509CertChain extends TSSSASIdentityToken {

    public static final String OID = "";
    private final String realmName;
    private final String domainName;

    public TSSITTX509CertChain(String realmName, String domainName) {
        this.realmName = realmName;
        this.domainName = domainName;
    }

    public short getType() {
        return ITTX509CertChain.value;
    }

    public String getOID() {
        return OID;
    }

    public Subject check(IdentityToken identityToken) throws SASException {
        throw new SASException(1, new Exception("NYI -- cert chain identity token"));
    }
}
