package org.openejb.proxy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.rmi.RemoteException;
import java.rmi.NoSuchObjectException;
import javax.ejb.EJBException;
import javax.ejb.Handle;
import javax.ejb.EJBObject;
import javax.ejb.NoSuchObjectLocalException;

import org.apache.geronimo.core.service.InvocationResult;

import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.openejb.EJBContainer;
import org.openejb.EJBInterfaceType;
import org.openejb.EJBInvocation;
import org.openejb.EJBInvocationImpl;
import org.openejb.EJBComponentType;
import org.openejb.ContainerNotFoundException;

public class EJBMethodInterceptor implements MethodInterceptor, EJBInterceptor, Serializable {
    /**
     * Either the container or a serialization handler (and then the container)
     */
    private final EJBInterceptor next;

    /**
     * Proxy factory for this proxy
     */
    private final EJBProxyFactory proxyFactory;

    /**
     * The type of the ejb interface.  This is used during construction of the EJBInvocation object.
     */
    private final EJBInterfaceType interfaceType;

    /**
     * Primary key of this proxy, or null if this is a home proxy.
     */
    private final Object primaryKey;

    /**
     * The container we are invokeing
     */
    private transient EJBContainer container;

    /**
     * Map from interface method ids to vop ids.
     */
    private transient int[] operationMap;

    /**
     * Metadata for the proxy
     */
    private transient ProxyInfo proxyInfo;

    public EJBMethodInterceptor(EJBProxyFactory proxyFactory, EJBInterfaceType type, EJBContainer container, int[] operationMap) {
        this(proxyFactory, type, container, operationMap, null);
    }

    public EJBMethodInterceptor(EJBProxyFactory proxyFactory, EJBInterfaceType type, EJBContainer container, int[] operationMap, Object primaryKey) {
        this.proxyFactory = proxyFactory;
        this.interfaceType = type;
        this.container = container;
        this.operationMap = operationMap;
        this.primaryKey = primaryKey;

        if (container != null) {
            // @todo REMOVE: this is a dirty dirty dirty hack to make the old openejb code work
            // this lets really stupid clients get access to the primary key of the proxy, which is readily
            // available from several other sources
            this.proxyInfo = new ProxyInfo(container.getProxyInfo(), primaryKey);
        }

        if (!interfaceType.isLocal() && !skipCopy()) {
            next = new SerializationHanlder(this);
        } else {
            next = this;
        }
    }

    public EJBProxyFactory getProxyFactory() {
        return proxyFactory;
    }

    public ProxyInfo getProxyInfo() throws ContainerNotFoundException {
        if (proxyInfo == null) {
            loadContainerInfo();
        }
        return proxyInfo;
    }

    public Object getPrimaryKey() {
        return primaryKey;
    }

    /** Returns true of the EJB 1.1 comliant copying of
     * remote interfaces should be skipped.
     * @return
     */
    private boolean skipCopy() {
//        String value = org.openejb.OpenEJB.getInitProps().getProperty("openejb.localcopy");
//        if (value == null) {
//            value = System.getProperty("openejb.localcopy");
//        }
//
//        return value != null && !value.equalsIgnoreCase("FALSE");
        return false;
    }

    public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        // fault in the operation map if we don't have it yet
        if (operationMap == null) {
            try {
                loadContainerInfo();
            } catch (ContainerNotFoundException e) {
                if (!interfaceType.isLocal()) {
                    throw new NoSuchObjectException(e.getMessage());
                } else {
                    throw new NoSuchObjectLocalException(e.getMessage());
                }
            }
        }

        // extract the primary key from home ejb remove invocations
        Object id = primaryKey;

        // todo lookup id of remove to make this faster
        if ((interfaceType == EJBInterfaceType.REMOTE || interfaceType == EJBInterfaceType.LOCAL) && proxyInfo.getComponentType() == EJBComponentType.STATELESS && method.getName().equals("remove")) {
            // remove on a stateless bean does nothing
            return null;
        }

        int methodIndex = operationMap[methodProxy.getSuperIndex()];
        if (methodIndex < 0) throw new AssertionError("Unknown method: method=" + method);
        if ((interfaceType == EJBInterfaceType.HOME || interfaceType == EJBInterfaceType.LOCALHOME) && method.getName().equals("remove")) {

            if (args.length != 1) {
                throw new RemoteException().initCause(new EJBException("Expected one argument"));
            }
            id = args[0];
            if (id instanceof Handle && interfaceType == EJBInterfaceType.HOME) {
                HandleImpl handle = (HandleImpl) id;
                EJBObject ejbObject = handle.getEJBObject();
                EJBMethodInterceptor ejbHandler = ((BaseEJB) ejbObject).ejbHandler;
                id = ejbHandler.getPrimaryKey();
            }
        }

        EJBInvocation invocation = new EJBInvocationImpl(interfaceType, id, methodIndex, args);

        InvocationResult result;
        try {
            result = next.invoke(invocation);
        } catch (Throwable t) {
            // system exceptions must be throw as either EJBException or a RemoteException
            if (interfaceType.isLocal()) {
                if (!(t instanceof EJBException)) {
                    t = new EJBException().initCause(t);
                }
            } else {
                if (!(t instanceof RemoteException)) {
                    t = new RemoteException(t.getMessage(), t);
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

    private void loadContainerInfo() throws ContainerNotFoundException {
        container = proxyFactory.getContainer();
        operationMap = proxyFactory.getOperationMap(interfaceType);

        // @todo REMOVE: this is a dirty dirty dirty hack to make the old openejb code work
        // this lets really stupid clients get access to the primary key of the proxy, which is readily
        // available from several other sources
        this.proxyInfo = new ProxyInfo(container.getProxyInfo(), primaryKey);
    }

    public InvocationResult invoke(EJBInvocation ejbInvocation) throws Throwable {
        return container.invoke(ejbInvocation);
    }
}
