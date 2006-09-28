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
 *    please contact info@openejb.org.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.org/).
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
 * $Id: CmpBuilder.java 446246 2006-06-22 22:08:34Z dain $
 */
package org.apache.openejb.deployment;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.gbean.GBeanInfo;
import org.apache.geronimo.gbean.AbstractName;
import org.apache.openejb.CmpEjbDeploymentGBean;
import org.apache.openejb.CmpEjbContainer;

/**
 * @version $Revision$ $Date$
 */
public class CmpBuilder extends EntityBuilder {
    private AbstractName moduleCmpEngineName;
    private boolean cmp2;

    public void setModuleCmpEngineName(AbstractName moduleCmpEngineName) {
        this.moduleCmpEngineName = moduleCmpEngineName;
    }

    public void setCmp2(boolean cmp2) {
        this.cmp2 = cmp2;
    }

    protected GBeanInfo getTargetGBeanInfo() {
        return CmpEjbDeploymentGBean.GBEAN_INFO;
    }

    protected Class getEjbContainerType() {
        return CmpEjbContainer.class;
    }

    public GBeanData createConfiguration() throws Exception {
        GBeanData gbean = super.createConfiguration();
        gbean.setReferencePattern("moduleCmpEngine", moduleCmpEngineName);
        gbean.setAttribute("cmp2", Boolean.valueOf(cmp2));
        return gbean;
    }
}
