/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Geronimo" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    "Apache Geronimo", nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 * ====================================================================
 */

package org.openejb.nova.deployment;

import javax.transaction.TransactionManager;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import org.apache.geronimo.kernel.service.GeronimoMBeanInfo;
import org.apache.geronimo.kernel.service.GeronimoAttributeInfo;
import org.apache.geronimo.kernel.service.GeronimoOperationInfo;
import org.apache.geronimo.kernel.service.GeronimoMBeanEndpoint;
import org.openejb.nova.entity.bmp.BMPEntityContainer;
import org.openejb.nova.entity.cmp.CMPEntityContainer;

/**
 * EJBInfo has static methods to construct GeronimoMBeanInfo objects for each type of
 * Container, to avoid contaminating Nova classes with Geronimo kernel structue.
 *
 * @version $Revision$ $Date$
 *
 * */
public class EJBInfo {

    private EJBInfo() {}

    public static GeronimoMBeanInfo getSessionGeronimoMBeanInfo(String className) {
        GeronimoMBeanInfo mbeanInfo= getGeronimoMBeanInfo(className);
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Demarcation", true, false, "Transaction demarcation"));
        return mbeanInfo;
    }

    public static GeronimoMBeanInfo getBMPEntityGeronimoMBeanInfo() {
        GeronimoMBeanInfo mbeanInfo= getGeronimoMBeanInfo(BMPEntityContainer.class.getName());
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("PrimaryKeyClassName", true, false, "Primary Key class name"));
        return mbeanInfo;
    }

    public static GeronimoMBeanInfo getCMPEntityGeronimoMBeanInfo() {
        GeronimoMBeanInfo mbeanInfo= getGeronimoMBeanInfo(CMPEntityContainer.class.getName());
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("PrimaryKeyClassName", true, false, "Primary Key class name"));
        return mbeanInfo;
    }

    public static GeronimoMBeanInfo getMessageDrivenGeronimoMBeanInfo() {
        GeronimoMBeanInfo mbeanInfo = new GeronimoMBeanInfo();
        return mbeanInfo; //TODO
    }



    public static GeronimoMBeanInfo getGeronimoMBeanInfo(String className) {
        GeronimoMBeanInfo mbeanInfo= new GeronimoMBeanInfo();
        mbeanInfo.setTargetClass(className);
        //mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("Uri", true, false, "Original deployment package URI?"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("BeanClassName", true, false, "Bean implementation class name"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("HomeClassName", true, false, "Home interface class name"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("RemoteClassName", true, false, "Remote interface class name"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("LocalHomeClassName", true, false, "Local home interface class name"));
        mbeanInfo.addAttributeInfo(new GeronimoAttributeInfo("LocalClassName", true, false, "Local interface class name"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("getEJBHome"));
        mbeanInfo.addOperationInfo(new GeronimoOperationInfo("getEJBLocalHome"));
        try {
            mbeanInfo.addEndpoint(new GeronimoMBeanEndpoint("TransactionManager",
                    TransactionManager.class.getName(),
                    ObjectName.getInstance("geronimo.transaction:role=TransactionManager"),
                    true));
        } catch (MalformedObjectNameException e) {
            throw new AssertionError();//our o.n. is not malformed.
        }
        return mbeanInfo;
    }

}
