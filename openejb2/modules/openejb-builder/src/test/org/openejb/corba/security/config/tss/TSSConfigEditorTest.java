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
package org.openejb.corba.security.config.tss;

import java.beans.PropertyEditorManager;

import junit.framework.TestCase;

import org.apache.geronimo.common.propertyeditor.PropertyEditorException;


/**
 * @version $Revision$ $Date$
 */
public class TSSConfigEditorTest extends TestCase {

    public void testSimple() throws Exception {
        TSSConfigEditor editor = (TSSConfigEditor) PropertyEditorManager.findEditor(TSSConfig.class);
        editor.setAsText("<foo:tss xmlns:foo=\"http://www.openejb.org/xml/ns/corba-tss-config_1_0\">\n" +
                         "                <foo:SSL port=\"443\" hostname=\"corba.apache.org\">\n" +
                         "                    <foo:supports>Integrity Confidentiality EstablishTrustInTarget EstablishTrustInClient</foo:supports>\n" +
                         "                    <foo:requires>Integrity</foo:requires>\n" +
                         "                </foo:SSL>\n" +
                         "                <foo:compoundSecMechTypeList>\n" +
                         "                    <foo:compoundSecMech>\n" +
                         "                    </foo:compoundSecMech>\n" +
                         "                </foo:compoundSecMechTypeList>\n" +
                         "            </foo:tss>");
        TSSConfig tss = (TSSConfig) editor.getValue();
        assertFalse(tss.isInherit());
        assertNotNull(tss.getTransport_mech());

        editor.setAsText("<foo:tss inherit=\"true\" xmlns:foo=\"http://www.openejb.org/xml/ns/corba-tss-config_1_0\"/>");
        tss = (TSSConfig) editor.getValue();
        assertTrue(tss.isInherit());
        assertNull(tss.getTransport_mech());

        try {
            editor.setAsText("<tss xmlns=\"http://www.openejb.org/xml/ns/corba-tss-config_1_0\">\n" +
                             "                <SSL port=\"443\">\n" +
                             "                    <supports>BAD_ENUM Integrity Confidentiality EstablishTrustInTarget EstablishTrustInClient</supports>\n" +
                             "                    <requires>Integrity</requires>\n" +
                             "                </SSL>\n" +
                             "                <compoundSecMechTypeList>\n" +
                             "                    <compoundSecMech>\n" +
                             "                    </compoundSecMech>\n" +
                             "                </compoundSecMechTypeList>\n" +
                             "            </tss>");
            editor.getValue();
            fail("Should fail");
        } catch (PropertyEditorException e) {
        }

    }
}
