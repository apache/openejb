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

import org.opentools.deployer.plugins.AbstractEntry;
import org.opentools.deployer.plugins.CannotCreateException;
import org.opentools.deployer.plugins.CannotRemoveException;
import org.opentools.deployer.plugins.Category;
import org.opentools.deployer.plugins.EditAction;
import org.opentools.deployer.plugins.Entry;

/**
 * The tree entry for top-level configuration of the EJB JAR.  An Entry
 * represents a specific object in the main tree view.  It is usually under
 * a Category, which represents a group of objects, but this Entry has no
 * parent because it is top-level and there's always only one.  Currently,
 * this Entry is not used becaues there's no data stored at the top level
 * of an OpenEJB EJB JAR configuration.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class EntryPlugin extends AbstractEntry {
    private OpenEjbMetaData data;
    private OpenEjbPlugin plugin;
    private Category cat;

    public EntryPlugin(OpenEjbMetaData data, OpenEjbPlugin plugin) {
        this.data = data;
        this.plugin = plugin;
    }

    public Category getParentCategory() {
        if(cat == null) {
            cat = new Category() {
                EditAction act = new ActionPluginEdit(plugin);
                public String getName() {return null;}
                public Entry getParentEntry() {return null;}
                public Entry[] getEntries() {return new Entry[]{EntryPlugin.this};}
                public Entry createEntry() throws CannotCreateException {throw new CannotCreateException("There can only be one top-level configuration.");}
                public String getCreateDescription() {return null;}
                public EditAction editEntry() {return act;}
                public void removeEntry(Entry entryToRemove) throws CannotRemoveException {throw new CannotRemoveException("There is always one top-level configuration.");}
                public void setContentsChanged() {}
                public boolean isContentsChanged() {return false;}
            };
        }
        return cat;
    }

    public Category[] getSubCategories() {
        return new Category[0];
    }

    public Object getMetaData() {
        return data;
    }

    void close() {
        data = null;
    }

    public String toString() {
        return "Top-Level Configuration";
    }
}
