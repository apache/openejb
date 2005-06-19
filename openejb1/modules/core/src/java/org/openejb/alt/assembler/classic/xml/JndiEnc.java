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
import org.openejb.alt.assembler.classic.JndiEncInfo;
import org.w3c.dom.Node;

/**
 * A subclass of JndiEncInfo filled with data from an XML file.
 * 
 * Populates the member variables of JndiEncInfo in this classes initializeFromDOM method.
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @see org.openejb.alt.assembler.classic.JndiEncInfo
 */
public class JndiEnc extends JndiEncInfo implements DomObject{


    /**
     * Represents the <tt>env-entry</tt> element in the XML config file.
     */
    public static final String ENV_ENTRY = "env-entry";

    /**
     * Represents the <tt>ejb-ref</tt> element in the XML config file.
     */
    public static final String EJB_REF = "ejb-ref";
    
    /**
     * Represents the <tt>resource-ref</tt> element, which describes resources such as JDBC connection and JMS factories.
     */
     public static final String RESOURCE_REF = "resource-ref";
     

    public void initializeFromDOM(Node node) throws OpenEJBException{
        /* EnvEntry */
        DomObject[] dos = DomTools.collectChildElementsByType(node, EnvEntry.class, ENV_ENTRY);
        envEntries = new EnvEntry[dos.length];
        for (int i=0; i < dos.length; i++) envEntries[i] = (EnvEntry)dos[i];

        /* EjbReference */
        dos = DomTools.collectChildElementsByType(node, EjbReference.class, EJB_REF);
        ejbReferences = new EjbReference[dos.length];
        for (int i=0; i < dos.length; i++) ejbReferences[i] = (EjbReference)dos[i];

        /* ResourceRefInfo */
        dos = DomTools.collectChildElementsByType(node, ResourceReference.class, RESOURCE_REF);
        resourceRefs = new ResourceReference[dos.length];
        for (int i=0; i < dos.length; i++) resourceRefs[i] = (ResourceReference)dos[i];

    }
    public void serializeToDOM(Node node) throws OpenEJBException{}
}
