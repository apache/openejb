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
