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
import javax.security.auth.Subject;

import org.omg.CORBA.ORB;
import org.omg.CSI.EstablishContext;
import org.omg.CSIIOP.CompoundSecMech;
import org.omg.IOP.Codec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.openejb.corba.security.SASException;
import org.apache.openejb.corba.security.config.ConfigUtil;


/**
 * @version $Rev$ $Date$
 */
public class TSSCompoundSecMechConfig implements Serializable {

    private final static Log log = LogFactory.getLog(TSSCompoundSecMechConfig.class);
    private TSSTransportMechConfig transport_mech;
    private TSSASMechConfig as_mech;
    private TSSSASMechConfig sas_mech;

    public TSSTransportMechConfig getTransport_mech() {
        return transport_mech;
    }

    public void setTransport_mech(TSSTransportMechConfig transport_mech) {
        this.transport_mech = transport_mech;
    }

    public TSSASMechConfig getAs_mech() {
        return as_mech;
    }

    public void setAs_mech(TSSASMechConfig as_mech) {
        this.as_mech = as_mech;
    }

    public TSSSASMechConfig getSas_mech() {
        return sas_mech;
    }

    public void setSas_mech(TSSSASMechConfig sas_mech) {
        this.sas_mech = sas_mech;
    }

    public short getSupports() {
        short result = 0;

        result |= transport_mech.getSupports();
        result |= as_mech.getSupports();
        result |= sas_mech.getSupports();

        return result;
    }

    public short getRequires() {
        short result = 0;

        result |= transport_mech.getRequires();
        result |= as_mech.getRequires();
        result |= sas_mech.getRequires();

        return result;
    }

    public CompoundSecMech encodeIOR(ORB orb, Codec codec) throws Exception {
        CompoundSecMech result = new CompoundSecMech();

        result.target_requires = 0;

        // transport mechanism
        result.transport_mech = transport_mech.encodeIOR(orb, codec);
        result.target_requires |= transport_mech.getRequires();
        if (log.isDebugEnabled()) {
            log.debug("transport adds supported: " + ConfigUtil.flags(transport_mech.getSupports()));
            log.debug("transport adds required: " + ConfigUtil.flags(transport_mech.getRequires()));
        }

        // AS_ContextSec
        result.as_context_mech = as_mech.encodeIOR(orb, codec);
        result.target_requires |= as_mech.getRequires();
        if (log.isDebugEnabled()) {
            log.debug("AS adds supported: " + ConfigUtil.flags(as_mech.getSupports()));
            log.debug("AS adds required: " + ConfigUtil.flags(as_mech.getRequires()));
        }

        // SAS_ContextSec
        result.sas_context_mech = sas_mech.encodeIOR(orb, codec);
        result.target_requires |= sas_mech.getRequires();
        if (log.isDebugEnabled()) {
            log.debug("SAS adds supported: " + ConfigUtil.flags(sas_mech.getSupports()));
            log.debug("SAS adds required: " + ConfigUtil.flags(sas_mech.getRequires()));

            log.debug("REQUIRES: " + ConfigUtil.flags(result.target_requires));
        }


        return result;
    }

    public static TSSCompoundSecMechConfig decodeIOR(Codec codec, CompoundSecMech compoundSecMech) throws Exception {
        TSSCompoundSecMechConfig result = new TSSCompoundSecMechConfig();

        result.setTransport_mech(TSSTransportMechConfig.decodeIOR(codec, compoundSecMech.transport_mech));
        result.setAs_mech(TSSASMechConfig.decodeIOR(compoundSecMech.as_context_mech));
        result.setSas_mech(new TSSSASMechConfig(compoundSecMech.sas_context_mech));

        return result;
    }

    public Subject check(EstablishContext msg) throws SASException {
        Subject asSubject = as_mech.check(msg);
        Subject sasSubject = sas_mech.check(msg);

        if (sasSubject != null) return sasSubject;

        return asSubject;
    }
}
