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
package org.openejb.test.object;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 */
public class OperationsPolicy implements java.io.Externalizable {

    public static final int Context_getEJBHome           = 0;        
    public static final int Context_getCallerPrincipal   = 1;
    public static final int Context_isCallerInRole       = 2;    
    public static final int Context_getRollbackOnly      = 3;   
    public static final int Context_setRollbackOnly      = 4;   
    public static final int Context_getUserTransaction   = 5;
    public static final int Context_getEJBObject         = 6;   
    public static final int Context_getPrimaryKey        = 7;  
    public static final int JNDI_access_to_java_comp_env = 8; 
    public static final int Resource_manager_access      = 9;      
    public static final int Enterprise_bean_access       = 10;

    private boolean[] allowedOperations = new boolean[9];

    public OperationsPolicy() {
    }

    public OperationsPolicy(int[] operations) {
        for (int i=0; i < operations.length; i++) {
            allow( operations[i] );
        }
    }

    public void allow(int i) {
        if (i < 0 || i > allowedOperations.length - 1 ) return;
        allowedOperations[i] = true;
    }

    public boolean equals(Object object) {
        if ( !(object instanceof OperationsPolicy ) ) return false;

        OperationsPolicy that = (OperationsPolicy)object;
        for (int i=0; i < allowedOperations.length; i++) {
            if (this.allowedOperations[i] != that.allowedOperations[i]) return false;
        }

        return true;
    }

    public void writeExternal(ObjectOutput out) throws IOException {
        for (int i=0; i < allowedOperations.length; i++) {
            out.writeBoolean( allowedOperations[i] );
        }
    }
    
    public void readExternal(ObjectInput in) throws IOException,ClassNotFoundException {
        for (int i=0; i < allowedOperations.length; i++) {
            allowedOperations[i] = in.readBoolean();
        }
    }

    public String toString() {
        String str = "[";
        for (int i=0; i < allowedOperations.length; i++) {
            str += (allowedOperations[i])? "1": "0";
        }
        str += "]";
        return str;

    }
}
