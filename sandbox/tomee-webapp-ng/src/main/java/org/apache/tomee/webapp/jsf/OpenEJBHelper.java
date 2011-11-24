package org.apache.tomee.webapp.jsf;

import org.apache.openejb.AppContext;
import org.apache.openejb.BeanContext;
import org.apache.openejb.loader.SystemInstance;
import org.apache.openejb.spi.ContainerSystem;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author rmannibucau
 */
public final class OpenEJBHelper {
    private static final ContainerSystem CONTAINER_SYSTEM = SystemInstance.get().getComponent(ContainerSystem.class);

    public static AppContext app(final String name) {
        for (AppContext appContext : CONTAINER_SYSTEM.getAppContexts()) {
            final String appName = appContext.getId();
            if (appName.equals(name)) {
                return appContext;
            }
        }
        return null;
    }

    public static BeanContext bean(final String app, final String name) {
        AppContext appCtx = app(app);
        if (appCtx == null) {
            return null;
        }
        for (BeanContext ctx : appCtx.getBeanContexts()) {
            if (ctx.getDeploymentID().equals(name)) {
                return ctx;
            }
        }
        return null;
    }

    public static Method method(final String app, final String name, final long id) {
        final BeanContext bean = bean(app, name);
        if (bean != null) {
            final Collection<MethodInfo> methods = methods(baseClass(bean));
            for (OpenEJBHelper.MethodInfo method : methods) {
                if (method.getId() == id) {
                    return method.getMethod();
                }
            }
        }
        return null;
    }

    public static Class<?> baseClass(BeanContext beanContext) {
        if (beanContext.isLocalbean()) {
            return beanContext.getBeanClass();
        } else if (beanContext.getBusinessLocalInterfaces().size() > 0) {
            return beanContext.getBusinessLocalInterface();
        } else if (beanContext.getBusinessRemoteInterface() != null) {
            return beanContext.getBusinessRemoteInterface();
        }
        return beanContext.getBeanClass();
    }

    public static List<MethodInfo> methods(Class<?> beanClass) {
        final List<MethodInfo> methods = new ArrayList<MethodInfo>();
        Class<?> current = beanClass;
        do {
            for (Method method : current.getDeclaredMethods()) {
                methods.add(new MethodInfo(method.toGenericString()
                    .replace(beanClass.getName().concat("."), "")
                    .replace("java.lang.", ""),
                    method.hashCode(), method));
            }
            current = current.getSuperclass();
        } while (current != null && !current.equals(Object.class));
        return methods;
    }

    public static class MethodInfo {
        private String signature;
        private long id;
        private Method method;

        public MethodInfo(String signature, long id, Method mtd) {
            this.signature = signature;
            this.id = id;
            this.method = mtd;
        }

        public String getSignature() {
            return signature;
        }

        public long getId() {
            return id;
        }

        public Method getMethod() {
            return method;
        }
    }
}
