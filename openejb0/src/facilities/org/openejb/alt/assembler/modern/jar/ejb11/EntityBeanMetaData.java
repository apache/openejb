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
package org.openejb.alt.assembler.modern.jar.ejb11;

import java.util.ArrayList;

/**
 * Metadata for an EJB 1.1 entity bean.  This includes primary key
 * information and potentially CMP fields, among other things.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class EntityBeanMetaData extends BeanMetaData {
    private ArrayList cmps = new ArrayList();
    private boolean cmp;
    private String pkName;
    private boolean reentrant;
    private String pkFieldName;

    public EntityBeanMetaData() {
    }

    public EntityBeanMetaData(BeanMetaData bean) {
        super(bean);
        if(bean instanceof EntityBeanMetaData) {
            EntityBeanMetaData source = (EntityBeanMetaData)bean;
            cmp = source.cmp;
            pkName = source.pkName;
            reentrant = source.reentrant;
            pkFieldName = source.pkFieldName;
        }
    }

    public void setCMP(boolean isCMP) {cmp = isCMP;}
    public boolean isCMP() {return cmp;}
    public void setPrimaryKeyClassName(String name) {pkName = name;}
    public String getPrimaryKeyClassName() {return pkName;}
    public void setReentrant(boolean reentrant) {this.reentrant = reentrant;}
    public boolean isReentrant() {return reentrant;}
    public void setPrimaryKeyFieldName(String name) {pkFieldName = name;}
    public String getPrimaryKeyFieldName() {return pkFieldName;}

    public void addCMPField(CMPFieldMetaData var) {
        cmps.add(var);
    }
    public void removeCMPField(CMPFieldMetaData var) {
        cmps.remove(var);
    }
    public CMPFieldMetaData[] getCMPFields() {
        return (CMPFieldMetaData[])cmps.toArray(new CMPFieldMetaData[cmps.size()]);
    }
}
