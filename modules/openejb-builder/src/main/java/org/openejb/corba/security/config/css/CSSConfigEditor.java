/**
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce the
 *    above copyright notice, this list of conditions and the
 *    following disclaimer in the documentation and/or other
 *    materials provided with the distribution.
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
 *    (http://openejb.sf.net/).
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
 * Copyright 2005 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.openejb.corba.security.config.css;

import java.util.Iterator;
import java.util.List;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.omg.CSIIOP.CompositeDelegation;
import org.omg.CSIIOP.Confidentiality;
import org.omg.CSIIOP.DetectMisordering;
import org.omg.CSIIOP.DetectReplay;
import org.omg.CSIIOP.EstablishTrustInClient;
import org.omg.CSIIOP.EstablishTrustInTarget;
import org.omg.CSIIOP.Integrity;
import org.omg.CSIIOP.NoDelegation;
import org.omg.CSIIOP.NoProtection;
import org.omg.CSIIOP.SimpleDelegation;

import org.apache.geronimo.common.DeploymentException;
import org.apache.geronimo.common.propertyeditor.PropertyEditorException;
import org.apache.geronimo.deployment.service.XmlAttributeBuilder;
import org.apache.geronimo.deployment.xmlbeans.XmlBeansUtil;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.GBeanInfoBuilder;
import org.apache.geronimo.kernel.ClassLoading;

import org.openejb.xbeans.csiv2.css.CSSCompoundSecMechType;
import org.openejb.xbeans.csiv2.css.CSSCssType;
import org.openejb.xbeans.csiv2.css.CSSGSSUPDynamicType;
import org.openejb.xbeans.csiv2.css.CSSGSSUPStaticType;
import org.openejb.xbeans.csiv2.css.CSSITTPrincipalNameDynamicType;
import org.openejb.xbeans.csiv2.css.CSSITTPrincipalNameStaticType;
import org.openejb.xbeans.csiv2.css.CSSSSLType;
import org.openejb.xbeans.csiv2.css.CSSSasMechType;
import org.openejb.xbeans.csiv2.css.CSSCssDocument;
import org.openejb.xbeans.csiv2.tss.TSSAssociationOption;


/**
 * @version $Revision$ $Date$
 */
public class CSSConfigEditor implements XmlAttributeBuilder {
    private static final String NAMESPACE = CSSCssDocument.type.getDocumentElementName().getNamespaceURI();

    public String getNamespace() {
        return NAMESPACE;
    }

    public Object getValue(XmlObject xmlObject, String type, ClassLoader cl) throws DeploymentException {

        CSSCssType css;
        if (xmlObject instanceof CSSCssType) {
            css = (CSSCssType) xmlObject;
        }
        css = (CSSCssType) xmlObject.copy().changeType(CSSCssType.type);
        try {
            XmlBeansUtil.validateDD(css);
        } catch (XmlException e) {
            throw new DeploymentException(e);
        }

        CSSConfig cssConfig = new CSSConfig();

        if (css.isSetCompoundSecMechTypeList()) {
            CSSCompoundSecMechListConfig mechListConfig = cssConfig.getMechList();
            mechListConfig.setStateful(css.getCompoundSecMechTypeList().getStateful());

            CSSCompoundSecMechType[] mechList = css.getCompoundSecMechTypeList().getCompoundSecMechArray();
            for (int i = 0; i < mechList.length; i++) {
                mechListConfig.add(extractCompoundSecMech(mechList[i], cl));
            }
        }

        return cssConfig;
    }

    protected static CSSCompoundSecMechConfig extractCompoundSecMech(CSSCompoundSecMechType mechType, ClassLoader cl) throws DeploymentException {

        CSSCompoundSecMechConfig result = new CSSCompoundSecMechConfig();

        if (mechType.isSetSSL()) {
            result.setTransport_mech(extractSSLTransport(mechType.getSSL()));
        } else if (mechType.isSetSECIOP()) {
            throw new PropertyEditorException("SECIOP processing not implemented");
        } else {
            result.setTransport_mech(new CSSNULLTransportConfig());
        }

        if (mechType.isSetGSSUPStatic()) {
            result.setAs_mech(extractGSSUPStatic(mechType.getGSSUPStatic()));
        } else if (mechType.isSetGSSUPDynamic()) {
            result.setAs_mech(extractGSSUPDynamic(mechType.getGSSUPDynamic()));
        } else {
            result.setAs_mech(new CSSNULLASMechConfig());
        }

        result.setSas_mech(extractSASMech(mechType.getSasMech(), cl));

        return result;
    }

    protected static CSSTransportMechConfig extractSSLTransport(CSSSSLType sslType) {
        CSSSSLTransportConfig result = new CSSSSLTransportConfig();

        result.setSupports(extractAssociationOptions(sslType.getSupports()));
        result.setRequires(extractAssociationOptions(sslType.getRequires()));

        return result;
    }

    protected static CSSASMechConfig extractGSSUPStatic(CSSGSSUPStaticType gssupType) {
        return new CSSGSSUPMechConfigStatic(gssupType.getUsername(), gssupType.getPassword(), gssupType.getDomain());
    }

    protected static CSSASMechConfig extractGSSUPDynamic(CSSGSSUPDynamicType gssupType) {
        return new CSSGSSUPMechConfigDynamic(gssupType.getDomain());
    }

    protected static CSSSASMechConfig extractSASMech(CSSSasMechType sasMechType, ClassLoader cl) throws DeploymentException {
        CSSSASMechConfig result = new CSSSASMechConfig();

        if (sasMechType == null) {
            result.setIdentityToken(new CSSSASITTAbsent());
        } else if (sasMechType.isSetITTAbsent()) {
            result.setIdentityToken(new CSSSASITTAbsent());
        } else if (sasMechType.isSetITTAnonymous()) {
            result.setIdentityToken(new CSSSASITTAnonymous());
        } else if (sasMechType.isSetITTPrincipalNameStatic()) {
            CSSITTPrincipalNameStaticType principal = sasMechType.getITTPrincipalNameStatic();
            result.setIdentityToken(new CSSSASITTPrincipalNameStatic(principal.getOid(), principal.getName()));
        } else if (sasMechType.isSetITTPrincipalNameDynamic()) {
            CSSITTPrincipalNameDynamicType principal = sasMechType.getITTPrincipalNameDynamic();
            String principalClassName = principal.getPrincipalClass();
            Class principalClass = null;
            try {
                principalClass = ClassLoading.loadClass(principalClassName, cl);
            } catch (ClassNotFoundException e) {
                throw new DeploymentException("Could not load principal class");
            }
            String domainName = principal.getDomain();
            String realmName = null;
            if (domainName != null) {
                realmName = principal.getRealm();
            }
            result.setIdentityToken(new CSSSASITTPrincipalNameDynamic(principal.getOid(), principalClass, domainName, realmName));
        }

        return result;
    }

    protected static short extractAssociationOptions(List list) {
        short result = 0;

        for (Iterator iter = list.iterator(); iter.hasNext();) {
            TSSAssociationOption.Enum obj = TSSAssociationOption.Enum.forString((String) iter.next());

            if (TSSAssociationOption.NO_PROTECTION.equals(obj)) {
                result |= NoProtection.value;
            } else if (TSSAssociationOption.INTEGRITY.equals(obj)) {
                result |= Integrity.value;
            } else if (TSSAssociationOption.CONFIDENTIALITY.equals(obj)) {
                result |= Confidentiality.value;
            } else if (TSSAssociationOption.DETECT_REPLAY.equals(obj)) {
                result |= DetectReplay.value;
            } else if (TSSAssociationOption.DETECT_MISORDERING.equals(obj)) {
                result |= DetectMisordering.value;
            } else if (TSSAssociationOption.ESTABLISH_TRUST_IN_TARGET.equals(obj)) {
                result |= EstablishTrustInTarget.value;
            } else if (TSSAssociationOption.ESTABLISH_TRUST_IN_CLIENT.equals(obj)) {
                result |= EstablishTrustInClient.value;
            } else if (TSSAssociationOption.NO_DELEGATION.equals(obj)) {
                result |= NoDelegation.value;
            } else if (TSSAssociationOption.SIMPLE_DELEGATION.equals(obj)) {
                result |= SimpleDelegation.value;
            } else if (TSSAssociationOption.COMPOSITE_DELEGATION.equals(obj)) {
                result |= CompositeDelegation.value;
            }
        }
        return result;
    }

    public static final GBeanInfo GBEAN_INFO;

    static {
        GBeanInfoBuilder infoBuilder = GBeanInfoBuilder.createStatic(CSSConfigEditor.class, "XmlAttributeBuilder");
        infoBuilder.addInterface(XmlAttributeBuilder.class);
        GBEAN_INFO = infoBuilder.getBeanInfo();
    }

    public static GBeanInfo getGBeanInfo() {
        return GBEAN_INFO;
    }

}
