package org.openejb.util.proxy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;

import org.openejb.OpenEJBException;
import org.opentools.proxies.Proxy;

/**
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 * @version $Revision$
 */
public class Jdk12ProxyFactory implements ProxyFactory {
    public void init(Properties props) throws OpenEJBException {
    }

    /**
     * Returns the invocation handler for the specified proxy instance.
     */
    public org.openejb.util.proxy.InvocationHandler getInvocationHandler(Object proxy) throws IllegalArgumentException {
        Jdk12InvocationHandler handler = (Jdk12InvocationHandler)Proxy.getInvocationHandler(proxy);
        if(handler == null)
            return null;
        return handler.getInvocationHandler();
    }

    /**
     * Sets the invocation handler for the specified proxy instance.
     */
    public Object setInvocationHandler(Object proxy, org.openejb.util.proxy.InvocationHandler handler) throws IllegalArgumentException {
        Jdk12InvocationHandler jdk12 = (Jdk12InvocationHandler)Proxy.getInvocationHandler(proxy);
        if(jdk12 == null)
            throw new IllegalArgumentException("Proxy "+proxy+" unknown!");
        return jdk12.setInvocationHandler(handler);
    }

    /**
     * Returns the java.lang.Class object for a proxy class given a class loader and an array of interfaces.
     */
    public Class getProxyClass(Class interfce) throws IllegalArgumentException {
        return Proxy.getProxyClass(interfce.getClassLoader(), new Class[]{interfce});
    }
    /**
     * Returns the java.lang.Class object for a proxy class given a class loader and an array of interfaces.
     */
    public Class getProxyClass(Class[] interfaces) throws IllegalArgumentException {
        if(interfaces.length < 1) {
            throw new IllegalArgumentException("It's boring to implement 0 interfaces!");
        }
        return Proxy.getProxyClass(interfaces[0].getClassLoader(), interfaces);
    }


    /*
     * Returns true if and only if the specified class was dynamically generated to be a proxy class using the getProxyClass method or the newProxyInstance method.
     */
    public boolean isProxyClass(Class cl) {
        return Proxy.isProxyClass(cl);
    }

    private final static Class[] constructorParams = { org.opentools.proxies.InvocationHandler.class };

    public Object newProxyInstance(Class proxyClass) throws IllegalArgumentException {
        if(!Proxy.isProxyClass(proxyClass))
            throw new IllegalArgumentException();
        try {
            Constructor cons = proxyClass.getConstructor(constructorParams);
            return (Object) cons.newInstance(new Object[] { new Jdk12InvocationHandler() });
        } catch (NoSuchMethodException e) {
            throw new InternalError(e.toString());
        } catch (IllegalAccessException e) {
            throw new InternalError(e.toString());
        } catch (InstantiationException e) {
            throw new InternalError(e.toString());
        } catch (InvocationTargetException e) {
            throw new InternalError(e.toString());
        }
    }

    /*
     * Returns an instance of a proxy class for the specified interface that dispatches method invocations to
     * the specified invocation handler.
     */
    public Object newProxyInstance(Class interfce, org.openejb.util.proxy.InvocationHandler h) throws IllegalArgumentException {
        Jdk12InvocationHandler handler = new Jdk12InvocationHandler(h);
        return Proxy.newProxyInstance(interfce.getClassLoader(), new Class[]{interfce}, handler);
    }

    /*
     * Returns an instance of a proxy class for the specified interface that dispatches method invocations to
     * the specified invocation handler.
     */
    public Object newProxyInstance(Class[] interfaces, org.openejb.util.proxy.InvocationHandler h) throws IllegalArgumentException {
        if(interfaces.length < 1) {
            throw new IllegalArgumentException("It's boring to implement 0 interfaces!");
        }
        Jdk12InvocationHandler handler = new Jdk12InvocationHandler(h);
        return Proxy.newProxyInstance(interfaces[0].getClassLoader(), interfaces, handler);
    }
}