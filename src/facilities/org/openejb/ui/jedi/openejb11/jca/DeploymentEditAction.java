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

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;

import org.openejb.ui.jedi.openejb11.jca.gui.DeploymentEditor;
import org.opentools.deployer.plugins.EditAction;
import org.opentools.deployer.plugins.Editor;
import org.opentools.deployer.plugins.Entry;
import org.opentools.deployer.plugins.Plugin;
import org.opentools.deployer.plugins.j2ee13.jca10.ConfigPropertyMetaData;
import org.opentools.deployer.plugins.j2ee13.jca10.ConnectorMetaData;
import org.opentools.deployer.plugins.j2ee13.jca10.ConnectorPlugin;

/**
 * Handles transferring data from metadata to GUI and back again for editing
 * Resource Adapter deployments on screen.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class DeploymentEditAction extends BaseAction implements EditAction {

    public DeploymentEditAction(JcaPlugin plugin) {
        super(plugin);
    }

    public Editor execute(Entry source) {
        final DeploymentEntry config = (DeploymentEntry)source;

        return new BaseEditor() {
            DeploymentEditor editor = new DeploymentEditor();

            public Component getComponent() {
                DeploymentMetaData data = (DeploymentMetaData)config.getMetaData();
                editor.setDeploymentID(data.getName());
                editor.setProperties(data.getProperties());

                editor.clearModified();
                return editor;
            }

            public boolean isModified() {
                return editor.isModified();
            };

            public void save() {
                DeploymentMetaData data = (DeploymentMetaData)config.getMetaData();
                data.setName(editor.getDeploymentID());
                data.setProperties(editor.getProperties());

                editor.clearModified();
            }

            public void close() {
                editor = null;
            }

            public void updateMetaData(Object metaData, Plugin plugin) {
            }

            public void updateRootMetaData(Object metaData, Plugin plugin) {
                if(plugin instanceof ConnectorPlugin) {
                    ConnectorMetaData data = (ConnectorMetaData)metaData;
                    ConfigPropertyMetaData[] props = data.getConfigProperties();
                    Map edits = editor.getProperties();
                    Map results = new HashMap();
                    for(int i=0; i<props.length; i++) {
                        if(!edits.containsKey(props[i].getName())) {
                            results.put(props[i].getName(), "");
                        } else {
                            results.put(props[i].getName(), edits.get(props[i].getName()));
                        }
                    }
                    editor.setProperties(results);
                }
            }

            public EditAction getAction() {
                return DeploymentEditAction.this;
            }

            public Object getMetaData() {
                return config.getMetaData();
            }
        };
    }

    public String getName() {
        return "Edit Resource Adapter Deployment";
    }

    public String getDescription() {
        return "Edit this resource adapter deployment.";
    }
}
