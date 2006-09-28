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
package org.apache.openejb.corba.security.config.css;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.omg.CORBA.Any;
import org.omg.CORBA.UserException;
import org.omg.CSI.EstablishContext;
import org.omg.CSI.SASContextBody;
import org.omg.CSI.SASContextBodyHelper;
import org.omg.IOP.SecurityAttributeService;
import org.omg.IOP.ServiceContext;

import org.apache.openejb.corba.security.config.ConfigUtil;
import org.apache.openejb.corba.security.config.tss.TSSCompoundSecMechConfig;
import org.apache.openejb.corba.util.Util;


/**
 * @version $Rev$ $Date$
 */
public class CSSCompoundSecMechConfig implements Serializable {

    private final static Log log = LogFactory.getLog(CSSCompoundSecMechConfig.class);

    private short supports;
    private short requires;
    private CSSTransportMechConfig transport_mech;
    private CSSASMechConfig as_mech;
    private CSSSASMechConfig sas_mech;

    public CSSTransportMechConfig getTransport_mech() {
        return transport_mech;
    }

    public void setTransport_mech(CSSTransportMechConfig transport_mech) {
        this.transport_mech = transport_mech;
        this.supports |= transport_mech.getSupports();
        this.requires |= transport_mech.getRequires();
    }

    public CSSASMechConfig getAs_mech() {
        return as_mech;
    }

    public void setAs_mech(CSSASMechConfig as_mech) {
        this.as_mech = as_mech;
        this.supports |= as_mech.getSupports();
        this.requires |= as_mech.getRequires();
    }

    public CSSSASMechConfig getSas_mech() {
        return sas_mech;
    }

    public void setSas_mech(CSSSASMechConfig sas_mech) {
        this.sas_mech = sas_mech;
        this.supports |= sas_mech.getSupports();
        this.requires |= sas_mech.getRequires();
    }

    public boolean canHandle(TSSCompoundSecMechConfig requirement) {

        if (log.isDebugEnabled()) {
            log.debug("canHandle()");
            log.debug("    CSS SUPPORTS: " + ConfigUtil.flags(supports));
            log.debug("    CSS REQUIRES: " + ConfigUtil.flags(requires));
            log.debug("    TSS SUPPORTS: " + ConfigUtil.flags(requirement.getSupports()));
            log.debug("    TSS REQUIRES: " + ConfigUtil.flags(requirement.getRequires()));
        }

        if ((supports & requirement.getRequires()) != requirement.getRequires()) return false;
        if ((requires & requirement.getSupports()) != requires) return false;

        if (!transport_mech.canHandle(requirement.getTransport_mech())) return false;
        if (!as_mech.canHandle(requirement.getAs_mech())) return false;
        if (!sas_mech.canHandle(requirement.getSas_mech())) return false;

        return true;
    }

    public ServiceContext generateServiceContext() throws UserException {

        if (as_mech instanceof CSSNULLASMechConfig && sas_mech.getIdentityToken() instanceof CSSSASITTAbsent) return null;

        EstablishContext msg = new EstablishContext();

        msg.client_context_id = 0;
        msg.client_authentication_token = as_mech.encode();
        msg.authorization_token = sas_mech.encodeAuthorizationElement();
        msg.identity_token = sas_mech.encodeIdentityToken();

        ServiceContext context = new ServiceContext();

        SASContextBody sas = new SASContextBody();
        sas.establish_msg(msg);
        Any sas_any = Util.getORB().create_any();
        SASContextBodyHelper.insert(sas_any, sas);
        context.context_data = Util.getCodec().encode_value(sas_any);

        context.context_id = SecurityAttributeService.value;

        return context;
    }
}
