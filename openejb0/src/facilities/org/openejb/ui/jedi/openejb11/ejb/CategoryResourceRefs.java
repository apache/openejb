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

import org.opentools.deployer.plugins.EditAction;
import org.opentools.deployer.plugins.Entry;

/**
 * The tree category for Resource References.  A Category represents a group of
 * items, while a specific item is represented by an Entry and appears under
 * the Category in the main tree view.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class CategoryResourceRefs extends OpenEjbCategory {
    private EntryBean bean;
    private EntryResourceRef[] children;
    private ActionResourceEdit edit;

    public CategoryResourceRefs(OpenEjbPlugin plugin, EntryBean bean) {
        super(plugin);
        this.bean = bean;
    }

// Category Impl
    public String getName() {
        return "Resource References";
    }

    public Entry getParentEntry() {
        return bean;
    }

    public Entry[] getEntries() {
        if(children == null) {
            MetaDataResourceRef[] list = ((MetaDataBean)bean.getMetaData()).getResourceRefs();
            children = new EntryResourceRef[list.length];
            for(int i=0; i<list.length; i++)
                children[i] = new EntryResourceRef(list[i], this);
        }
        return children;
    }

    public Entry createEntry() {
        MetaDataBean data = (MetaDataBean)bean.getMetaData();
        MetaDataResourceRef ref = new MetaDataResourceRef();
        data.addResourceRef(ref);
        if(children != null) {
            EntryResourceRef newList[] = new EntryResourceRef[children.length+1];
            System.arraycopy(children, 0, newList, 0, children.length);
            newList[children.length] = new EntryResourceRef(ref, this);
            children = newList;
        } else {
            getEntries();
        }
        return children[children.length-1];
    }
    public EditAction editEntry() {
        if(edit == null)
            edit = new ActionResourceEdit(plugin);
        return edit;
    }
    public void removeEntry(Entry entryToRemove) {
        MetaDataResourceRef ref = (MetaDataResourceRef)entryToRemove.getMetaData();
        ((MetaDataBean)bean.getMetaData()).removeResourceRef(ref);
        setContentsChanged();
        ((EntryResourceRef)entryToRemove).close();
    }

    public boolean isContentsChanged() {
        return children == null;
    }

    public String getCreateDescription() {
        return "If this bean uses resources such as database "+
               "connections or e-mail services, the only "+
               "server-independent way to get a reference to "+
               "those resources is to create a Resource Reference "+
               "here. You will need to specify the OpenEJB JCX "+
               "deployment ID to use "+
               "for the resource, as well as whether "+
               "you want the container to control access to it.";
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
}
