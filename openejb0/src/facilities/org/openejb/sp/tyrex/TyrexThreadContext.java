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
 * $Id: 
 */
package org.openejb.sp.tyrex;
import org.openejb.core.DeploymentInfo;

/**
This class is really just a hook so that Tyrex can be informed when 
a thread is associated to a new deployment by the container.

Basically, Tyrex provides thread based functionality and must be made aware
of the changes to a threads state.  Other service providers may need to do 
the same thing and would also need hooks, we may consider providing a listener
interface and just notify them when things are changed.
*/


public class TyrexThreadContext extends org.openejb.core.ThreadContext {
    
    // staticHash = new Hash()

    public void setDeploymentInfo(DeploymentInfo info){

        /*[1]
         *  Get the RuntimeContext from a static hash.
         *  tyrex.tm.RuntimeContext rc = "theHash.get(info.deploymentId())"
         */
        /*[1.1]
         *  If the RuntimeContext is null, create a new one for this
         *  deployment
         */
            

        /*[2]
         *  Set the RuntimeContext on the Tyrex thread context
         *  tyrex.tm.RuntimeContext.setRuntimeContext(rc);
         */
////////if(info instanceof TyrexDeploymentInfo){
////////    tyrex.tm.RuntimeContext rc = ((TyrexDeploymentInfo)info).getRuntimeContext();
////////    tyrex.tm.RuntimeContext.setRuntimeContext(rc);
////////}
        super.setDeploymentInfo(info);  
        
    }
    
    protected void makeInvalid(){
        tyrex.tm.RuntimeContext.unsetRuntimeContext();
        super.makeInvalid();
    }

}
