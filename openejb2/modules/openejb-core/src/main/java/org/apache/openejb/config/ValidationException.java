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
 * $Id: ValidationException.java 444993 2004-10-25 09:55:08Z dblevins $
 */
package org.apache.openejb.config;

import org.apache.openejb.util.Messages;

/**
 * @author <a href="mailto:david.blevins@visi.com">David Blevins</a>
 */
public class ValidationException extends java.lang.Exception {

    protected static Messages messages = new Messages("org.openejb.config.rules");

    protected Bean bean;
    protected Object[] details;
    protected String message;

    protected String prefix;


    public ValidationException(String message) {
        this.message = message;
    }

    public void setDetails(Object arg1) {
        this.details = new Object[]{arg1};
    }

    public void setDetails(Object arg1, Object arg2) {
        this.details = new Object[]{arg1, arg2};
    }

    public void setDetails(Object arg1, Object arg2, Object arg3) {
        this.details = new Object[]{arg1, arg2, arg3};
    }

    public void setDetails(Object arg1, Object arg2, Object arg3, Object arg4) {
        this.details = new Object[]{arg1, arg2, arg3, arg4};
    }

    public void setDetails(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5) {
        this.details = new Object[]{arg1, arg2, arg3, arg4, arg5};
    }

    public void setDetails(Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object arg6) {
        this.details = new Object[]{arg1, arg2, arg3, arg4, arg5, arg6};
    }

    public Object[] getDetails() {
        return details;
    }

    public String getSummary() {
        return getMessage(1);
    }

    public String getMessage() {
        return getMessage(2);
    }

    public String getMessage(int level) {
        return messages.format(level + "." + message, details);
    }

    public Bean getBean() {
        return bean;
    }

    public void setBean(Bean bean) {
        this.bean = bean;
    }

    public String getPrefix() {
        return "";
    }

    public String getCategory() {
        return "";
    }

}
