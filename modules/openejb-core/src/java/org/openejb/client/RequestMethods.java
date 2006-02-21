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
package org.openejb.client;

/**
 * 
 * @since 11/25/2001
 */
public interface RequestMethods {

    public static final byte EJB_REQUEST  = (byte)0;
    public static final byte JNDI_REQUEST = (byte)1;
    public static final byte AUTH_REQUEST = (byte)2;
    public static final byte STOP_REQUEST_Quit = (byte)'Q';
    public static final byte STOP_REQUEST_quit = (byte)'q';
    public static final byte STOP_REQUEST_Stop = (byte)'S';
    public static final byte STOP_REQUEST_stop = (byte)'s';

    //-------------------------------------------------------//
    // Methods from the javax.ejb.EJBHome interface          //
    //-------------------------------------------------------//
    public static final int EJB_HOME_GET_EJB_META_DATA  =  1;
    public static final int EJB_HOME_GET_HOME_HANDLE    =  2;
    public static final int EJB_HOME_REMOVE_BY_HANDLE   =  3;
    public static final int EJB_HOME_REMOVE_BY_PKEY     =  4;
    //-------------------------------------------------------//
    // Methods from the user defined EJBHome subinterface    //
    // a.k.a.  The Home Interface                            //
    //-------------------------------------------------------//
    public static final int EJB_HOME_METHOD             =  8;
    public static final int EJB_HOME_FIND               =  9;
    public static final int EJB_HOME_CREATE             = 10;
    //-------------------------------------------------------//
    // Methods from the javax.ejb.EJBObject interface        //
    //-------------------------------------------------------//
    public static final int EJB_OBJECT_GET_EJB_HOME     = 14;
    public static final int EJB_OBJECT_GET_HANDLE       = 15;
    public static final int EJB_OBJECT_GET_PRIMARY_KEY  = 16;
    public static final int EJB_OBJECT_IS_IDENTICAL     = 17;
    public static final int EJB_OBJECT_REMOVE           = 18;
    //-------------------------------------------------------//
    // Methods from the user defined EJBObject subinterfac   //
    // a.k.a.  The Remote Interface                          //
    //-------------------------------------------------------//
    public static final int EJB_OBJECT_BUSINESS_METHOD  = 23;
    //-------------------------------------------------------//
    // Methods from the javax.naming.Context                 //
    //-------------------------------------------------------//
    public static final int JNDI_LOOKUP                 = 27;
    public static final int JNDI_LIST                   = 28;
    public static final int JNDI_LIST_BINDINGS          = 29;
    public static final int JNDI_APP_CTX_PULL           = 30;


}

