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
package org.openejb.ui.jedi.openejb11.jca;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.opentools.deployer.plugins.XMLUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Saves data to an XML file using DOM.  The actual mechanism used to save
 * is parser-dependent.  The JEDI framework currently autodetects Xerces
 * and Sun's parser that ships with JAXP.  That bit is handled in the
 * JcaMetaData class - this class just populates the DOM.
 *
 * @see org.openejb.ui.jedi.openejb11.jca.JcaMetaData
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class XMLWriter extends XMLUtils {
    private Document doc;
    private JcaMetaData data;
    private LinkedList warnings;

    public XMLWriter(Document doc, JcaMetaData data) {
        this.doc = doc;
        this.data = data;
        warnings = new LinkedList();
    }

    public String[] getWarnings() {
        return (String[])warnings.toArray(new String[warnings.size()]);
    }

    public void save() {
        Element root = doc.createElement("openejb-connector");
        doc.appendChild(root);
        DeploymentMetaData[] deploys = data.getDeployments();
        for(int i=0; i<deploys.length; i++) {
            createDeployment(createChild(doc, root, "resourceadapter"), deploys[i]);
        }
    }

    private void createDeployment(Element root, DeploymentMetaData deploy) {
        createChildText(doc, root, "connector-id", deploy.getName());
        Map map = deploy.getProperties();
        for(Iterator it = map.keySet().iterator(); it.hasNext(); ) {
            Element property = createChild(doc, root, "config-property");
            String name = (String)it.next();
            String value = (String)map.get(name);
            createChildText(doc, property, "config-property-name", name);
            createChildText(doc, property, "config-property-value", value);
        }
    }
}
