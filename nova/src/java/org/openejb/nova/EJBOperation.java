/* ====================================================================
 * Redistribution and use of this software and associated documentation
 * ("Software"), with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * 1. Redistributions of source code must retain copyright
 *    statements and notices.  Redistributions must also contain a
 *    copy of this document.
 *
 * 2. Redistributions in binary form must reproduce this list of
 *    conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
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
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the OpenEJB Project.  For more information
 * please see <http://openejb.org/>.
 *
 * ====================================================================
 */
package org.openejb.nova;


/**
 *
 *
 * @version $Revision$ $Date$
 */
public final class EJBOperation {
    public static final EJBOperation INACTIVE = new EJBOperation(0);
    public static final EJBOperation SETCONTEXT = new EJBOperation(1);
    public static final EJBOperation EJBCREATE = new EJBOperation(2);
    public static final EJBOperation EJBPOSTCREATE = new EJBOperation(3);
    public static final EJBOperation EJBREMOVE = new EJBOperation(4);
    public static final EJBOperation EJBACTIVATE = new EJBOperation(5);
    public static final EJBOperation EJBLOAD = new EJBOperation(6);
    public static final EJBOperation BIZMETHOD = new EJBOperation(7);
    public static final EJBOperation ENDPOINT = new EJBOperation(8);
    public static final EJBOperation TIMEOUT = new EJBOperation(9);
    public static final EJBOperation EJBFIND = new EJBOperation(10);
    public static final EJBOperation EJBHOME = new EJBOperation(11);

    private static final EJBOperation[] values = {
        INACTIVE, SETCONTEXT, EJBCREATE, EJBPOSTCREATE, EJBREMOVE,
        EJBACTIVATE, EJBLOAD, BIZMETHOD, ENDPOINT, TIMEOUT,
        EJBFIND, EJBHOME
    };

    public static final int MAX_ORDINAL = values.length;

    private final int ordinal;

    private EJBOperation(int ordinal) {
        this.ordinal = ordinal;
    }

    public int getOrdinal() {
        return ordinal;
    }
}
