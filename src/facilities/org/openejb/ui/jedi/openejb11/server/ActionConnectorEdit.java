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
package org.openejb.ui.jedi.openejb11.server;

import java.awt.Component;
import java.util.LinkedList;

import org.openejb.ui.jedi.openejb11.server.gui.ConnectorEditor;
import org.opentools.deployer.plugins.EditAction;
import org.opentools.deployer.plugins.Editor;
import org.opentools.deployer.plugins.Entry;
import org.opentools.deployer.plugins.Plugin;

/**
 * Handles transferring data from metadata to GUI and back again for editing
 * J2EE Connector deployments on screen.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class ActionConnectorEdit extends OpenEjbAction implements EditAction {

    public ActionConnectorEdit(ServerPlugin plugin) {
        super(plugin);
    }

    public Editor execute(final Entry source) {
        final EntryConnector config = (EntryConnector)source;

        return new OpenEjbEditor() {
            ConnectorEditor editor = new ConnectorEditor();

            public Component getComponent() {
                ConnectorMetaData data = (ConnectorMetaData)config.getMetaData();
                OpenEjbMetaData ejb = ((CategoryConnector)source.getParentCategory()).getMetaData();

                CMMetaData mgrs[] = ejb.getConnectionManagers();
                LinkedList list = new LinkedList();
                for(int i=0; i<mgrs.length; i++) {
                    if(mgrs[i].getName() != null && !mgrs[i].getName().equals("")) {
                        list.add(mgrs[i].getName());
                    }
                }
                editor.setConnectionManagerOptions((String[])list.toArray(new String[list.size()]));

                editor.setConnectionManager(data.getConnectionManagerName());
                editor.setName(data.getName());
                editor.setMap(data.getMap());

                editor.clearModified();
                return editor;
            }

            public boolean isModified() {
                return editor.isModified();
            };

            public void save() {
                editor.stopEditing();
                ConnectorMetaData data = (ConnectorMetaData)config.getMetaData();
                data.setConnectionManagerName(editor.getConnectionManager());
                data.setName(editor.getName());
                data.setMap(editor.getMap());

                editor.clearModified();
            }

            public void close() {
                editor = null;
            }

            public void updateMetaData(Object metaData, Plugin plugin) {
            }

            public EditAction getAction() {
                return ActionConnectorEdit.this;
            }

            public Object getMetaData() {
                return config.getMetaData();
            }
        };
    }

    public String getName() {
        return "Edit Connector";
    }

    public String getDescription() {
        return "Edit the properties for this J2EE Connector."+
               "Each resource adapter can be deployed and "+
               "configured more than once.  You set the "+
               "properties for a particular deployment here.";
    }
}
