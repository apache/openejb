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

import javax.swing.ImageIcon;

import org.opentools.deployer.plugins.Category;
import org.opentools.deployer.plugins.ClassDescriptor;
import org.opentools.deployer.plugins.Entry;
import org.opentools.deployer.plugins.FileDescriptor;
import org.opentools.deployer.plugins.MetaData;
import org.opentools.deployer.plugins.Plugin;
import org.opentools.deployer.plugins.PluginUtils;
import org.opentools.deployer.plugins.ToolBarCommand;
import org.opentools.deployer.plugins.j2ee12.ejb11.EJB11Plugin;

/**
 * The main Plugin for OpenEJB EJB JARs.  This class provides the name to
 * display on tabs holding OpenEJB information, the XML file name to save
 * and load data from, toolbar icons, access to metadata, and the entries
 * and categories to load into the tree.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version 1.0
 */
public class OpenEjbPlugin extends Plugin {
    public MetaData getMetaDataInstance() {
        return new OpenEjbMetaData();
    }

    public String getName() {
        return "OpenEJB 1.1";
    }

    public String getXMLFileName() {
        return "openejb-jar.xml";
    }

    public ClassDescriptor[] getInterestingClasses() {
        // Taken care of by dependency on EJB 1.1
        return new ClassDescriptor[0];
    }
    public FileDescriptor[] getInterestingFiles() {
        // Taken care of by dependency on EJB 1.1
        return new FileDescriptor[0];
    }
    public Category[] getCategories(MetaData data) {
        return new Category[] {
            new CategoryBean(this, (OpenEjbMetaData)data),
            new CategoryContainer(this, (OpenEjbMetaData)data),
            new CategorySecurityRole(this, (OpenEjbMetaData)data)
        };
    }

    public Plugin[] getDependencies() {
        return new Plugin[] {
            new EJB11Plugin()
        };
    }

    public ToolBarCommand[] getToolBarCommands(Category[] cats) {
        return new ToolBarCommand[] {
            new ToolBarCommand(new ImageIcon(getClass().getClassLoader().getResource("images/container-yellow-text.jpg")),
                               "Create Container", PluginUtils.getCategory("Containers", cats)),
        };
    }

    /**
     * There's currently no data stored at the top level.  This returns
     * null, therefore, but would need to be changed to return an Entry
     * if there's ever data there.
     */
    public Entry getMainConfigEntry(MetaData data) {
        return null;//new EntryPlugin((OpenEjbMetaData)data, this);
    }
}
