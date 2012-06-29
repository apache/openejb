package org.superbiz;

import com.sun.faces.spi.DiscoverableInjectionProvider;
import com.sun.faces.spi.InjectionProviderException;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.inject.OWBInjector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TomEEInjectionProvider extends DiscoverableInjectionProvider {
    private final Map<Object, OWBInjector> injectors = new ConcurrentHashMap<Object, OWBInjector>();

    @Override
    public void inject(final Object managedBean) throws InjectionProviderException {
        final OWBInjector injector = new OWBInjector(WebBeansContext.currentInstance());
        injectors.put(managedBean, injector);
        try {
            injectors.get(managedBean).inject(managedBean);
        } catch (Exception e) {
            throw new InjectionProviderException(e);
        }
    }

    @Override
    public void invokePreDestroy(Object managedBean) throws InjectionProviderException {
        if (injectors.containsKey(managedBean)) {
            try {
                injectors.remove(managedBean).destroy();
            } catch (Exception e) {
                throw new InjectionProviderException(e);
            }
        }
    }

    @Override
    public void invokePostConstruct(Object managedBean) throws InjectionProviderException {
        // TODO
    }
}
