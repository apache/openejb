package org.apache.openejb.arquillian.embedded;

import org.apache.openejb.cdi.ThreadSingletonServiceImpl;
import org.jboss.arquillian.test.spi.TestEnricher;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionTarget;
import java.lang.reflect.Method;

/**
 * @author rmannibucau
 */
public class EmbeddedTomEEEnricher implements TestEnricher {
    @Override public void enrich(Object testCase) {
        BeanManager mgr = getBeanManager();
        if (mgr != null) {
            AnnotatedType<?> type =  mgr.createAnnotatedType(getClass());
            InjectionTarget<Object> injectionTarget = (InjectionTarget<Object>) mgr.createInjectionTarget(type);
            CreationalContext<Object> context = mgr.createCreationalContext(null);

            injectionTarget.inject(testCase, context);
            injectionTarget.postConstruct(this);
        } else {
            throw new NullPointerException("bean manager is null");
        }

        /* TODO: see if this code could be called after tests
                *
                * if (injectionTarget != null) {
                *        injectionTarget.preDestroy(this);
                *    }
                *   if (context != null) {
                *        context.release();
                *    }
                *
                *   injectionTarget = null;
                *   context = null;
                */
    }

    @Override public Object[] resolve(Method method) {
        return new Object[method.getParameterTypes().length];
    }

    public BeanManager getBeanManager() {
        return ThreadSingletonServiceImpl.get().getBeanManagerImpl();
    }
}
