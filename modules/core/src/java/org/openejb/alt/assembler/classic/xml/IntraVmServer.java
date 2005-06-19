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
import org.openejb.alt.assembler.classic.IntraVmServerInfo;
import org.w3c.dom.Node;

/**
 * A subclass of IntraVmServerInfo filled with data from an XML file.
 * 
 * Populates the member variables of IntraVmServerInfo in this classes initializeFromDOM method.
 * 
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 * @author <a href="mailto:Richard@Monson-Haefel.com">Richard Monson-Haefel</a>
 * @see org.openejb.alt.assembler.classic.IntraVmServerInfo
 */
public class IntraVmServer extends IntraVmServerInfo implements DomObject {


    /**
     * Represents the <tt>factory-class</tt> element in the XML config file.
     */
    public static final String PROXY_FACTORY = "proxy-factory";

    /**
     * Represents the <tt>codebase</tt> element in the XML config file.
     */
    public static final String CODEBASE = "codebase";


    /** 
     * Parses out the values needed by this DomObject from the DOM Node passed in.
     * @see org.w3c.dom.Node
     */
    public void initializeFromDOM(Node node) throws OpenEJBException{

        proxyFactoryClassName = DomTools.getChildElementPCData(node, PROXY_FACTORY);
        codebase = DomTools.getChildElementPCData(node, CODEBASE);

        proxyFactoryClass = DomTools.toolkit.forName(proxyFactoryClassName);

        properties = DomTools.readProperties(node);
    }

    public void serializeToDOM(Node node) throws OpenEJBException{}
}
