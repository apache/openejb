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

import org.openejb.ui.jedi.openejb11.ejb.gui.EjbRefEditor;
import org.opentools.deployer.plugins.EditAction;
import org.opentools.deployer.plugins.Editor;
import org.opentools.deployer.plugins.Entry;
import org.opentools.deployer.plugins.Plugin;
import org.opentools.deployer.plugins.j2ee12.ejb11.EJB11Plugin;

/**
 * Handles transferring data from metadata to GUI and back again for editing
 * EJB References on screen.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class ActionEjbRefEdit extends OpenEjbAction implements EditAction {

    public ActionEjbRefEdit(OpenEjbPlugin plugin) {
        super(plugin);
    }

    public String getName() {
        return "Edit EJB Reference";
    }

    public String getDescription() {
        return "Edit EJB Reference";
    }

    public Editor execute(final Entry source) {
        final EntryEjbRef ref = (EntryEjbRef)source;

        return new OpenEJBEditor() {
            EjbRefEditor editor = new EjbRefEditor();

            public Component getComponent() {
                MetaDataEjbRef data = (MetaDataEjbRef)ref.getMetaData();
                editor.setRefName(data.getEJBRefName());
                editor.setOpenEjbName(data.getOpenEjbName());
                editor.setMap(data.getMap());
                editor.clearModified();
                return editor;
            }

            public boolean isModified() {
                return editor.isModified();
            };

            public void save() {
                MetaDataEjbRef data = (MetaDataEjbRef)ref.getMetaData();
                data.setOpenEjbName(editor.getOpenEjbName());
                data.setEJBRefName(editor.getRefName());
                data.setMap(editor.getMap());
                editor.clearModified();
            }

            public void close() {
                editor = null;
            }

            public EditAction getAction() {
                return ActionEjbRefEdit.this;
            }

            public Object getMetaData() {
                return ref.getMetaData();
            }

            public void updateMetaData(Object metaData, Plugin plugin) {
                if(plugin instanceof EJB11Plugin) {
                    org.opentools.deployer.plugins.j2ee12.ejb11.MetaDataEjbRef other = (org.opentools.deployer.plugins.j2ee12.ejb11.MetaDataEjbRef)metaData;
                    editor.setRefName(other.getRefName());
                    ((MetaDataEjbRef)ref.getMetaData()).setEJBRefName(other.getRefName());
                    String link = other.getEjbLink();
                    if(link != null && link.length() > 0) {
                        editor.setOpenEjbName(link);
                        editor.setOpenEjbEnabled(true);
                    } else {
                        editor.setOpenEjbEnabled(false);
                    }
                }
            }
        };
    }
}
