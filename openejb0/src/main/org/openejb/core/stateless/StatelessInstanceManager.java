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


package org.openejb.core.stateless;


import java.io.*;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.util.*;
import javax.ejb.EnterpriseBean;
import javax.ejb.SessionBean;
import org.openejb.ApplicationException;
import org.openejb.OpenEJBException;
import org.openejb.SystemException;
import org.openejb.core.DeploymentInfo;
import org.openejb.core.EnvProps;
import org.openejb.core.Operations;
import org.openejb.core.ThreadContext;
import org.openejb.util.LinkedListStack;
import org.openejb.util.SafeProperties;
import org.openejb.util.SafeToolkit;

/**
 * This instance manager has a pool limit for each bean class 
 * the pooling policy applies to how many beans maybe pooled in the method ready; it 
 * will always return a bean instance for every thread (no waiting for available instances).
 * Instances returning to the pool are removed if pool is already full.
 *
 * Automatic pool reduction will require the the ejbRemove method be invoked.
 * setSessionContext must be done within the instance manager
 */

public class StatelessInstanceManager {

    protected java.util.HashMap poolMap = new HashMap();
    protected int poolLimit = 0;
    protected int beanCount = 0;
    protected boolean strictPooling = false;
    
    protected PoolQueue poolQueue= null;

    protected SafeToolkit toolkit = SafeToolkit.getToolkit("StatefulInstanceManager");
    /******************************************************************
                        CONSTRUCTOR METHODS
    *******************************************************************/
    public StatelessInstanceManager( ){
    }

    public void init(Properties props)
    throws OpenEJBException{

        
        SafeProperties safeProps = toolkit.getSafeProperties(props);

        poolLimit = safeProps.getPropertyAsInt(EnvProps.IM_POOL_SIZE, 10);
        strictPooling = safeProps.getPropertyAsBoolean(EnvProps.IM_STRICT_POOLING,new Boolean(false)).booleanValue();
        if(strictPooling){
            int waitTime = safeProps.getPropertyAsInt(EnvProps.IM_TIME_OUT, 0);
            poolQueue = new PoolQueue(waitTime);
        }
    }
    public EnterpriseBean getInstance(ThreadContext callContext)
    throws OpenEJBException{
            SessionBean bean = null;
            Object deploymentId = callContext.getDeploymentInfo().getDeploymentID();
            StackHolder pool = (StackHolder)poolMap.get(deploymentId);
            if(pool==null){
                pool = new StackHolder(poolLimit);
                poolMap.put(deploymentId,pool);
            }else
                bean = (SessionBean)pool.pop();
            
            // apply strict pooling policy if used. Wait for available instance
             while(strictPooling &&  bean == null && pool.totalBeanCount() >= poolLimit){
               poolQueue.waitForAvailableInstance();
               bean = (SessionBean)pool.pop();
            }
            
            if(bean==null){
                try{
                Class beanClass = callContext.getDeploymentInfo().getBeanClass();
                bean = (SessionBean)toolkit.newInstance(beanClass);
                }catch(OpenEJBException oee){
                    throw (SystemException)oee;
                }
                    
                byte originalOperation = callContext.getCurrentOperation();
                try{
                    // invoke the setSessionContext method
                    callContext.setCurrentOperation(Operations.OP_SET_CONTEXT);
                    DeploymentInfo deploymentInfo = callContext.getDeploymentInfo();
                    ((SessionBean)bean).setSessionContext((javax.ejb.SessionContext)deploymentInfo.getEJBContext());
                    
                    // invoke the ejbCreate method
                    callContext.setCurrentOperation(Operations.OP_CREATE);
                    Method createMethod = deploymentInfo.getCreateMethod();
                    createMethod.invoke(bean, null);
                    
                }catch(Exception e){
                    //TODO:1: Should be logged instead of printed to System.out
		    System.out.println("StatelessInstanceManager----->");
                    throw new org.openejb.ApplicationException(new RemoteException("Can not obtain a free instance."));
                } finally {
                    callContext.setCurrentOperation( originalOperation );
                }
                pool.addToBeanCount(1);
            }
            return bean;
    }
    public void poolInstance(ThreadContext callContext, EnterpriseBean bean)
    throws OpenEJBException{
        if(bean == null)
            throw new SystemException("Invalid arguments");
        Object deploymentId = callContext.getDeploymentInfo().getDeploymentID();
        StackHolder pool = (StackHolder)poolMap.get(deploymentId);
        if(strictPooling){
            pool.push(bean);
            poolQueue.notifyWaitingThreads();
        }else{
            if(pool.totalBeanCount() > poolLimit)
                freeInstance(callContext,bean);
            else
                pool.push(bean);
        }
        
    }
    public void freeInstance(ThreadContext callContext, EnterpriseBean bean){
        StackHolder pool = (StackHolder)poolMap.get(callContext.getDeploymentInfo().getDeploymentID());
        pool.addToBeanCount(-1);
        
        try{
            callContext.setCurrentOperation(Operations.OP_REMOVE);
            ((SessionBean)bean).ejbRemove();
        }catch(Throwable re){
            // not in client scope, do nothing
            // log exception
        }
        
        // allow the bean instance to be GCed.
    }
    
   class StackHolder extends LinkedListStack {
	// access to this variable MUST be synchronized, since we add values to it,
	// which is NOT an atomic operations. We avoid the infamous lost update problem.
        private int totalBeanCount;

	synchronized int totalBeanCount() {
	    return totalBeanCount;
	}

	synchronized void addToBeanCount(int value) {
	    totalBeanCount+=value;
	}

        public StackHolder(int initialSize){super(initialSize);}
    }
    class PoolQueue{
        private final long waitPeriod;
        public PoolQueue(long time){waitPeriod = time;}
        public synchronized void waitForAvailableInstance( )
        throws org.openejb.InvalidateReferenceException{
            try{
                wait(waitPeriod);
            }catch(InterruptedException ie){
                throw new org.openejb.InvalidateReferenceException(new RemoteException("No instance avaiable to service request"));
            }
        }
        public synchronized void notifyWaitingThreads(){
            notify();
        }
    }
    
}
