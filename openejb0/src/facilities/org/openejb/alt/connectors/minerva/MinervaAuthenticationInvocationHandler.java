package org.openejb.alt.connectors.minerva;

import java.lang.reflect.Method;
import java.util.HashMap;

import javax.resource.spi.ConnectionRequestInfo;

import org.openejb.spi.OpenEJBConnectionManager;
import org.openejb.util.proxy.InvocationHandler;
import org.opentools.minerva.connector.AuthenticationRequestInfoWrapper;

/**
 * Proxy handler to translate normal JCA calls to modified ones that include
 * a flag indicating whether the call should use container managed sign in
 * or bean managed sign in.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class MinervaAuthenticationInvocationHandler implements InvocationHandler {
    private OpenEJBConnectionManager mgr;
    private boolean containerManagedSignIn;
    private HashMap methods;

    public MinervaAuthenticationInvocationHandler(OpenEJBConnectionManager mgr,
                                                  boolean containerManagedSignIn) {
        this.mgr = mgr;
        this.containerManagedSignIn = containerManagedSignIn;
        methods = new HashMap();
    }

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(method.getDeclaringClass().getName().equals("org.openejb.util.proxy.OpenEJBProxy")) {
            return this;
        }
        if(method.getName().equals("allocateConnection")) {
            args[1] = new AuthenticationRequestInfoWrapper(containerManagedSignIn,
                                                           (ConnectionRequestInfo)args[1]);
        }
        Method target = (Method)methods.get(method.getName());
        if(target == null) {
            target = mgr.getClass().getMethod(method.getName(), method.getParameterTypes());
            methods.put(method.getName(), target);
        }
        return target.invoke(mgr, args);
    }
}