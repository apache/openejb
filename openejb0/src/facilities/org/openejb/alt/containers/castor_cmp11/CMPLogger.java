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
package org.openejb.alt.containers.castor_cmp11;

import org.openejb.util.Logger;

/**
 * 
 * @author Stefan Reich -- sreich@apple.com
 */
public class CMPLogger implements org.exolab.castor.persist.spi.LogInterceptor {
    protected final Logger logger = Logger.getInstance( "OpenEJB.CastorCMP", "org.openejb.alt.util.resources" );
    protected final String db;

    public CMPLogger(String db) {
        this.db=db+": ";
    }

    public void loading(java.lang.Object objClass, java.lang.Object identity) {
        logger.debug(db+"Loading an instance of "+objClass+" with identity \""+identity+"\"");
    }
    public void creating(java.lang.Object objClass, java.lang.Object identity) {
        logger.debug(db+"Creating an instance of "+objClass+" with identity \""+identity+"\"");
    }

    public void removing(java.lang.Object objClass, java.lang.Object identity) {
        logger.debug(db+"Removing an instance of "+objClass+" with identity \""+identity+"\"");
    }

    public void storing(java.lang.Object objClass, java.lang.Object identity) {
        logger.debug(db+"Storing an instance of "+objClass+" with identity \""+identity+"\"");
    }

    public void storeStatement(java.lang.String statement) {
        logger.debug(db+statement);
    }

    public void queryStatement(java.lang.String statement) {
        logger.debug(db+statement);
    }

    public void message(java.lang.String message) {
        logger.info(db+"JDO message:"+message);
    }

    public void exception(java.lang.Exception ex) {
        logger.info(db+"JDO exception:", ex);
    }

    public java.io.PrintWriter getPrintWriter() {
        return null;
    }
}

