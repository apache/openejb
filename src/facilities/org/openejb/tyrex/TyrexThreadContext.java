package org.openejb.tyrex;
import org.openejb.core.DeploymentInfo;

public class TyrexThreadContext extends org.openejb.core.ThreadContext {
    
    public void setDeploymentInfo(DeploymentInfo info){
        if(info instanceof TyrexDeploymentInfo){
            tyrex.tm.RuntimeContext rc = ((TyrexDeploymentInfo)info).getRuntimeContext();
            tyrex.tm.RuntimeContext.setRuntimeContext(rc);
        }
        super.setDeploymentInfo(info);  
        
    }
    protected void makeInvalid(){
        tyrex.tm.RuntimeContext.unsetRuntimeContext();
        super.makeInvalid();
        
    }
    
}