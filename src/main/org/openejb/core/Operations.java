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
package org.openejb.core;

public class Operations {
    /**
    * Any business method invocation defined by the bean provider on the remote interface
    */
    final public static byte OP_BUSINESS = (byte)1;
    /**
    * ejbAfterBegin method on session bean that implement 
    * the SessionSynchronization interface
    */
    final public static byte OP_AFTER_BEGIN = (byte)2;
    /**
    * ejbAfterCompletition method on session bean that implement 
    * the SessionSynchronization interface
    */
    final public static byte OP_AFTER_COMPLETION = (byte)3;
    /**
    * ejbBeforeCompletition method on session bean that implement 
    * the SessionSynchronization interface
    */
    final public static byte OP_BEFORE_COMPLETION = (byte)4;
    /**
    * Any remove method defined by the bean provider on the home interface.
    */
    final public static byte OP_REMOVE = (byte)5;
    /**
    * setSessionContext, setEntityContext, and setMessageContext methods defined 
    * by SessionBean, EntityBean, and MessageDrivenBean interfaces.
    */
    final public static byte OP_SET_CONTEXT = (byte)6;
    /**
    * unsetEntityContext method defined EntityBean interfaces.
    */
    final public static byte OP_UNSET_CONTEXT = (byte)7;
    /**
    * Any create method defined by the bean provider on the home interface.
    */
    final public static byte OP_CREATE = (byte)8;
    /**
    * all ejbPostCreate methods defined in the bean class.
    */
    final public static byte OP_POST_CREATE = (byte)9;
    /**
    * the ejbActivate method defined in EntityBean and SessionBean interfaces.
    */
    final public static byte OP_ACTIVATE = (byte)10;
    /**
    * the ejbPassivate method defined in EntityBean and SessionBean interfaces.
    */
    final public static byte OP_PASSIVATE = (byte)11;
    /**
    * Any find method defined by the bean provider on the home interface.
    */
    final public static byte OP_FIND = (byte)12;
    /**
    * Any home method (ejbHome) defined by the bean provider on the home interface.
    */
    final public static byte OP_HOME = (byte)13;
    /** 
    * for ejbLoad methods on entity beans
    */
    final public static byte OP_LOAD = (byte)14;
    /** 
    * for ejbStore methods on entity beans
    */
    final public static byte OP_STORE = (byte)15;
    
}