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
 * 3. The name "Exolab" must not be used to endorse or promote
 *    products derived from this Software without prior written
 *    permission of Exoffice Technologies.  For written permission,
 *    please contact info@exolab.org.
 *
 * 4. Products derived from this Software may not be called "Exolab"
 *    nor may "Exolab" appear in their names without prior written
 *    permission of Exoffice Technologies. Exolab is a registered
 *    trademark of Exoffice Technologies.
 *
 * 5. Due credit should be given to the Exolab Project
 *    (http://www.exolab.org/).
 *
 * THIS SOFTWARE IS PROVIDED BY EXOFFICE TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 * EXOFFICE TECHNOLOGIES OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * Copyright 1999 (C) Exoffice Technologies Inc. All Rights Reserved.
 *
 * $Id$
 */

package org.openejb.alt.assembler.classic.xml;


import org.openejb.OpenEJBException;
import org.openejb.alt.assembler.classic.EnterpriseBeanInfo;
import org.w3c.dom.Node;

/**
 * A subclass of EnterpriseBeanInfo filled with data from an XML file.
 * 
 * Populates the member variables of EnterpriseBeanInfo in this classes initializeFromDOM method.
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @see org.openejb.alt.assembler.classic.EnterpriseBeanInfo
 * @see #EnterpriseBean.initializeFromDOM
 */
public class EnterpriseBean extends EnterpriseBeanInfo{


    /**
     * Represents the <tt>description</tt> element in the XML config file.
     */
    public static final String DESCRIPTION = "description";

    /**
     * Represents the <tt>display-name</tt> element in the XML config file.
     */
    public static final String DISPLAY_NAME = "display-name";

    /**
     * Represents the <tt>small-icon</tt> element in the XML config file.
     */
    public static final String SMALL_ICON = "small-icon";

    /**
     * Represents the <tt>large-icon</tt> element in the XML config file.
     */
    public static final String LARGE_ICON = "large-icon";

    /**
     * Represents the <tt>ejb-deployment-id</tt> element in the XML config file.
     */
    public static final String EJB_DEPLOYMENT_ID = "ejb-deployment-id";

    /**
     * Represents the <tt>home</tt> element in the XML config file.
     */
    public static final String HOME = "home";

    /**
     * Represents the <tt>remote</tt> element in the XML config file.
     */
    public static final String REMOTE = "remote";

    /**
     * Represents the <tt>ejb-class</tt> element in the XML config file.
     */
    public static final String EJB_CLASS = "ejb-class";

    /**
     * Represents the <tt>jndi-enc</tt> element in the XML config file.
     */
    public static final String JNDI_ENC = "jndi-enc";

    /**
     * Represents the <tt>security-role-ref</tt> element in the XML config file.
     */
    public static final String SECURITY_ROLE_REF = "security-role-ref";


    /** 
     * Parses out the values needed by this DomObject from the DOM Node passed in.
     */
    public static void initializeFromDOM(Node node, EnterpriseBeanInfo beanInfo) throws OpenEJBException{
        beanInfo.description = DomTools.getChildElementPCData(node, DESCRIPTION);
        beanInfo.displayName = DomTools.getChildElementPCData(node, DISPLAY_NAME);
        beanInfo.smallIcon = DomTools.getChildElementPCData(node, SMALL_ICON);
        beanInfo.largeIcon = DomTools.getChildElementPCData(node, LARGE_ICON);
        beanInfo.ejbDeploymentId = DomTools.getChildElementPCData(node, EJB_DEPLOYMENT_ID);
        beanInfo.home = DomTools.getChildElementPCData(node, HOME);
        beanInfo.remote = DomTools.getChildElementPCData(node, REMOTE);
        beanInfo.ejbClass = DomTools.getChildElementPCData(node, EJB_CLASS);
        
        /* SecurityRoleReference */
        DomObject[] dos = DomTools.collectChildElementsByType(node, SecurityRoleReference.class, SECURITY_ROLE_REF);
        beanInfo.securityRoleReferences = new SecurityRoleReference[dos.length];
        for (int i=0; i < dos.length; i++) beanInfo.securityRoleReferences[i] = (SecurityRoleReference)dos[i];

        beanInfo.jndiEnc = (JndiEnc)DomTools.collectChildElementByType(node, JndiEnc.class, JNDI_ENC);
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
