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

import org.opentools.deployer.plugins.CannotCreateException;
import org.opentools.deployer.plugins.CannotRemoveException;
import org.opentools.deployer.plugins.Category;
import org.opentools.deployer.plugins.EditAction;
import org.opentools.deployer.plugins.Entry;

/**
 * The tree category for EJBs.  A Category represents a group of items,
 * while a specific item is represented by an Entry and appears under
 * the Category in the main tree view.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class CategoryBean extends OpenEjbCategory {
    private OpenEjbMetaData data;
    private EntryBean children[];
    private EditAction edit;

    public CategoryBean(OpenEjbPlugin plugin, OpenEjbMetaData data) {
        super(plugin);
        this.data = data;
    }

    public String getName() {
        return "EJBs";
    }

    public Entry getParentEntry() {
        return null;
    }

    public Entry[] getEntries() {
        if(children == null) {
            MetaDataBean[] list = data.getEJBs();
            children = new EntryBean[list.length];
            for(int i=0; i<list.length; i++)
                children[i] = new EntryBean(this, list[i]);
        }
        return children;
    }

    public Entry createEntry() throws CannotCreateException {
        MetaDataBean bean = new MetaDataBean();
        data.addEJB(bean);
        if(children != null) {
            EntryBean newList[] = new EntryBean[children.length+1];
            System.arraycopy(children, 0, newList, 0, children.length);
            newList[children.length] = new EntryBean(this, bean);
            children = newList;
        } else {
            getEntries();
        }
        return children[children.length-1];
    }

    public String getCreateDescription() {
        return "Create a new EJB";
    }

    public EditAction editEntry() {
        if(edit == null)
            edit = new ActionBeanEdit(plugin);
        return edit;
    }

    public void removeEntry(Entry entryToRemove) throws CannotRemoveException {
        MetaDataBean bean = (MetaDataBean)entryToRemove.getMetaData();
        data.removeEJB(bean);
        setContentsChanged();
        ((EntryBean)entryToRemove).close();
    }

    public boolean isContentsChanged() {
        return children == null;
    }

    public void setContentsChanged() {
        if(children != null) {
            for(int i=0; i<children.length; i++)
                children[i].close();
            children = null;
        }
    }

// End Category Impl
    public String toString() {
        return getName();
    }

    public OpenEjbMetaData getMetaData() {
        return data;
    }

    Category[] getSubCategories(EntryBean bean) {
        return new OpenEjbCategory[] {
                   new CategoryEjbRefs(plugin, bean),
                   new CategoryResourceRefs(plugin, bean)
               };
    }
}
