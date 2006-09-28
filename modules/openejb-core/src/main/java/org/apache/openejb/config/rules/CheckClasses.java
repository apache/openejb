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
 *    please contact openejb-group@openejb.sf.net.
 *
 * 4. Products derived from this Software may not be called "OpenEJB"
 *    nor may "OpenEJB" appear in their names without prior written
 *    permission of The OpenEJB Group. OpenEJB is a registered
 *    trademark of The OpenEJB Group.
 *
 * 5. Due credit should be given to the OpenEJB Project
 *    (http://openejb.sf.net/).
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
 * Copyright 2001 (C) The OpenEJB Group. All Rights Reserved.
 *
 * $Id$
 */
package org.apache.openejb.config.rules;

import javax.ejb.EJBLocalHome;
import javax.ejb.EJBLocalObject;

import org.apache.openejb.OpenEJBException;
import org.apache.openejb.config.Bean;
import org.apache.openejb.config.EjbSet;
import org.apache.openejb.config.ValidationFailure;
import org.apache.openejb.config.ValidationRule;
import org.apache.openejb.util.SafeToolkit;


/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */

public class CheckClasses implements ValidationRule {

    EjbSet set;

    public void validate(EjbSet set) {
        this.set = set;

        Bean[] beans = set.getBeans();
        Bean b = null;
        try {
            for (int i = 0; i < beans.length; i++) {
                b = beans[i];
                check_hasEjbClass(b);
                check_isEjbClass(b);
                if (b.getHome() != null) {
                    check_hasHomeClass(b);
                    check_hasRemoteClass(b);
                    check_isHomeInterface(b);
                    check_isRemoteInterface(b);
                }
                if (b.getLocalHome() != null) {
                    check_hasLocalHomeClass(b);
                    check_hasLocalClass(b);
                    check_isLocalHomeInterface(b);
                    check_isLocalInterface(b);
                }
            }
        } catch (RuntimeException e) {
            throw new RuntimeException(b.getEjbName(), e);
        }

        SafeToolkit.unloadTempCodebase(set.getJarPath());
    }

    private void check_hasLocalClass(Bean b) {
        lookForClass(b, b.getLocal(), "<local>");
    }

    private void check_hasLocalHomeClass(Bean b) {
        lookForClass(b, b.getLocalHome(), "<local-home>");
    }


    public void check_hasEjbClass(Bean b) {

        lookForClass(b, b.getEjbClass(), "<ejb-class>");

    }


    public void check_hasHomeClass(Bean b) {

        lookForClass(b, b.getHome(), "<home>");

    }


    public void check_hasRemoteClass(Bean b) {

        lookForClass(b, b.getRemote(), "<remote>");

    }


    public void check_isEjbClass(Bean b) {

        if (b instanceof org.apache.openejb.config.SessionBean) {

            compareTypes(b, b.getEjbClass(), javax.ejb.SessionBean.class);

        } else if (b instanceof org.apache.openejb.config.EntityBean) {

            compareTypes(b, b.getEjbClass(), javax.ejb.EntityBean.class);

        }

    }


    private void check_isLocalInterface(Bean b) {
        compareTypes(b, b.getLocal(), EJBLocalObject.class);
    }

    private void check_isLocalHomeInterface(Bean b) {
        compareTypes(b, b.getLocalHome(), EJBLocalHome.class);
    }


    public void check_isHomeInterface(Bean b) {

        compareTypes(b, b.getHome(), javax.ejb.EJBHome.class);

    }


    public void check_isRemoteInterface(Bean b) {

        compareTypes(b, b.getRemote(), javax.ejb.EJBObject.class);

    }


    private void lookForClass(Bean b, String clazz, String type) {
        try {
            SafeToolkit.loadTempClass(clazz, set.getJarPath());
        } catch (OpenEJBException e) {
/*
            # 0 - Class name
            # 1 - Element (home, ejb-class, remote)
            # 2 - Bean name
            */

            ValidationFailure failure = new ValidationFailure("missing.class");
            failure.setDetails(clazz, type, b.getEjbName());
            failure.setBean(b);

            set.addFailure(failure);

//set.addFailure( new ValidationFailure("missing.class", clazz, type, b.getEjbName()) );
        } catch (NoClassDefFoundError e) {
/*
             # 0 - Class name
             # 1 - Element (home, ejb-class, remote)
             # 2 - Bean name
             # 3 - Misslocated Class name
             */
            ValidationFailure failure = new ValidationFailure("misslocated.class");
            failure.setDetails(clazz, type, b.getEjbName(), e.getMessage());
            failure.setBean(b);

            set.addFailure(failure);
            throw e;
        }

    }

    private void compareTypes(Bean b, String clazz1, Class class2) {
        Class class1 = null;
        try {
            class1 = SafeToolkit.loadTempClass(clazz1, set.getJarPath());
        } catch (OpenEJBException e) {
        }

        if (class1 != null && !class2.isAssignableFrom(class1)) {
            ValidationFailure failure = new ValidationFailure("wrong.class.type");
            failure.setDetails(clazz1, class2.getName());
            failure.setBean(b);

            set.addFailure(failure);
            //set.addFailure( new ValidationFailure("wrong.class.type", clazz1, class2.getName()) );
        }
    }
}





