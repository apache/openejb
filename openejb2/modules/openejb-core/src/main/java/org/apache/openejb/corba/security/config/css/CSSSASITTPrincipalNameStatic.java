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
package org.apache.openejb.corba.security.config.css;

import org.omg.CORBA.Any;
import org.omg.CSI.GSS_NT_ExportedNameHelper;
import org.omg.CSI.IdentityToken;
import org.omg.GSSUP.GSSUPMechOID;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;

import org.apache.openejb.corba.util.Util;


/**
 * @version $Revision$ $Date$
 */
public class CSSSASITTPrincipalNameStatic implements CSSSASIdentityToken {

    private final String oid;
    private final String name;
    private transient IdentityToken token;

    public CSSSASITTPrincipalNameStatic(String name) {

        this(GSSUPMechOID.value.substring(4), name);
    }

    public CSSSASITTPrincipalNameStatic(String oid, String name) {
        this.oid = (oid == null ? GSSUPMechOID.value.substring(4) : oid);
        this.name = name;
    }

    public IdentityToken encodeIdentityToken() {

        if (token == null) {
            Any any = Util.getORB().create_any();

            GSS_NT_ExportedNameHelper.insert(any, Util.encodeGSSExportName(oid, name));

            byte[] encoding = null;
            try {
                encoding = Util.getCodec().encode_value(any);
            } catch (InvalidTypeForEncoding itfe) {
                throw new IllegalStateException("Unable to encode principal name '" + name + "' " + itfe);
            }

            token = new IdentityToken();
            token.principal_name(encoding);
        }
        return token;
    }
}
