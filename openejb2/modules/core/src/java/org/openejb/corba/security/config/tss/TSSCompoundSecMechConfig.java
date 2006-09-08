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
import javax.security.auth.Subject;

import org.omg.CORBA.ORB;
import org.omg.CSI.EstablishContext;
import org.omg.CSIIOP.CompoundSecMech;
import org.omg.IOP.Codec;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.openejb.corba.security.SASException;
import org.openejb.corba.security.config.ConfigUtil;


/**
 * @version $Rev: $ $Date$
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
