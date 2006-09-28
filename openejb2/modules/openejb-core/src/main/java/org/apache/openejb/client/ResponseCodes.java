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
package org.apache.openejb.client;

/**
 * 
 * @since 11/25/2001
 */
public interface ResponseCodes {

    // TODO: Implement more specific response codes for EJB

    public static final int AUTH_GRANTED             =  1;
    public static final int AUTH_REDIRECT            =  2;
    public static final int AUTH_DENIED              =  3;
    public static final int EJB_OK                   =  4;
    public static final int EJB_OK_CREATE            =  5;
    public static final int EJB_OK_FOUND             =  6;
    public static final int EJB_OK_FOUND_COLLECTION  =  7;
    public static final int EJB_OK_NOT_FOUND         =  8;
    public static final int EJB_APP_EXCEPTION        =  9;
    public static final int EJB_SYS_EXCEPTION        = 10;
    public static final int EJB_ERROR                = 11;
    public static final int JNDI_OK                  = 12;
    public static final int JNDI_EJBHOME             = 13;
    public static final int JNDI_CONTEXT             = 14;
    public static final int JNDI_ENUMERATION         = 15;
    public static final int JNDI_NOT_FOUND           = 16;
    public static final int JNDI_NAMING_EXCEPTION    = 17;
    public static final int JNDI_RUNTIME_EXCEPTION   = 18;
    public static final int JNDI_ERROR               = 19;
    public static final int EJB_OK_FOUND_ENUMERATION = 20;
    public static final int JNDI_CONTEXT_TREE        = 21;
}

