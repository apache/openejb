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
package org.openejb.ui.jedi.openejb11.ejb;

import java.awt.Component;

import org.openejb.ui.jedi.openejb11.ejb.gui.SecurityRoleEditor;
import org.opentools.deployer.plugins.EditAction;
import org.opentools.deployer.plugins.Editor;
import org.opentools.deployer.plugins.Entry;
import org.opentools.deployer.plugins.Plugin;
import org.opentools.deployer.plugins.j2ee12.ejb11.EJB11Plugin;

/**
 * Handles transferring data from metadata to GUI and back again for editing
 * Security Rolds on screen.  Does not list available physical roles to
 * choose from.  That would be a nice feature to add, but we have to
 * implement the server API first so this can communicate with a particular
 * server.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class ActionSecurityRoleEdit extends OpenEjbAction implements EditAction {

    public ActionSecurityRoleEdit(OpenEjbPlugin plugin) {
        super(plugin);
    }

    public Editor execute(final Entry source) {
        final EntrySecurityRole role = (EntrySecurityRole)source;

        return new OpenEJBEditor() {
            SecurityRoleEditor editor = new SecurityRoleEditor();

            public Component getComponent() {
                MetaDataSecurityRole data = (MetaDataSecurityRole)role.getMetaData();
                editor.setEjbRoleName(data.getEjbRoleName());
                editor.setPhysicalRoles(data.getPhysicalRoleNames());

                editor.clearModified();
                return editor;
            }

            public boolean isModified() {
                return editor.isModified();
            };

            public void save() {
                MetaDataSecurityRole data = (MetaDataSecurityRole)role.getMetaData();
                editor.stopEditing();
                data.setEjbRoleName(editor.getEjbRoleName());
                data.setPhysicalRoleNames(editor.getPhysicalRoles());
                editor.clearModified();
            }

            public void close() {
                editor = null;
            }

            public void updateMetaData(Object metaData, Plugin plugin) {
                if(plugin instanceof EJB11Plugin) {
                    org.opentools.deployer.plugins.j2ee12.ejb11.MetaDataSecurityRole other = (org.opentools.deployer.plugins.j2ee12.ejb11.MetaDataSecurityRole)metaData;
                    String newName = other.getRoleName();
                    MetaDataSecurityRole sec = (MetaDataSecurityRole)role.getMetaData();
                    sec.setEjbRoleName(newName);
                    editor.setEjbRoleName(newName);
                }
            }

            public EditAction getAction() {
                return ActionSecurityRoleEdit.this;
            }

            public Object getMetaData() {
                return role.getMetaData();
            }
        };
    }

    public String getName() {
        return "Edit Security Role";
    }

    public String getDescription() {
        return "Associate physical security roles with a given EJB logical role.";
    }
}
