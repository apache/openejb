package org.openejb.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.RemoteException;

import javax.ejb.EJBException;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import org.apache.geronimo.core.service.InvocationResult;
import org.openejb.EJBContainer;
import org.openejb.EJBInterfaceType;
import org.openejb.EJBInvocation;
import org.openejb.EJBInvocationImpl;

public class EJBMethodInterceptor implements MethodInterceptor, Serializable  {
    

    private final ProxyInfo proxyInfo;
    private final Object primaryKey;

    private EJBInterceptor next;
    
    /**
     * The type of the ejb interface.  This is used during construction of the EJBInvocation object.
     */
    private final EJBInterfaceType interfaceType;

    /**
     * Map from interface method ids to vop ids.
     */
    private final int[] operationMap;
    
    
    private transient EJBContainer ejbContainer;

    public EJBContainer getEjbContainer() {
        return ejbContainer;
    }
    
    public EJBMethodInterceptor(EJBContainer ejbContainer, ProxyInfo proxyInfo, EJBInterfaceType type, int[] map){
        this.primaryKey = proxyInfo.getPrimaryKey();
        this.proxyInfo = proxyInfo;
        this.ejbContainer = ejbContainer;
        this.interfaceType = type;
        this.operationMap = map;
        
        next = new ContainerHandler(ejbContainer);
        if (!interfaceType.isLocal() || !skipCopy()) next = new SerializationHanlder(next);
    }

    /** Returns true of the EJB 1.1 comliant copying of
     * remote interfaces should be skipped.
     * @return
     */
    private boolean skipCopy() {
        String value = org.openejb.OpenEJB.getInitProps().getProperty("openejb.localcopy");
        if(value == null){
            value = System.getProperty("openejb.localcopy");
        }
        
        return  value != null && !value.equalsIgnoreCase("FALSE");
    }

    private void readObject(java.io.ObjectInputStream in)
    throws java.io.IOException,ClassNotFoundException, NoSuchMethodException{
        in.defaultReadObject();
    }
        
    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        int methodIndex = operationMap[methodProxy.getSuperIndex()];
        InvocationResult result;
        try {
            EJBInvocation invocation = null;
            invocation = new EJBInvocationImpl(interfaceType, primaryKey, methodIndex, args);
            result = next.invoke(invocation);
        } catch (Throwable t) {
            // System exception from interceptor chain - throw as is or wrapped in an EJBException
            if (t instanceof Exception && t instanceof RuntimeException == false) {
                if (interfaceType.isLocal()){
                    t = new EJBException((Exception) t);
                } else {
                    t = new RemoteException(t.getMessage(),(Exception) t);
                }
            }
            throw t;
        }
        if (result.isNormal()) {
            return result.getResult();
        } else {
            throw result.getException();
        }
    }
    
//    public Object invoke(Object proxy, Method method, Object[] args)
//    throws Throwable {
//        return next.invoke(primaryKey, method, args);
//    }
    
    public ProxyInfo getProxyInfo(){
        return proxyInfo;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }
}
