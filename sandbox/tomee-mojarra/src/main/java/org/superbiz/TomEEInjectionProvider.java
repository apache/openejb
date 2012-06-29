package org.superbiz;

import com.sun.faces.spi.DiscoverableInjectionProvider;
import com.sun.faces.spi.InjectionProviderException;
import org.apache.openejb.OpenEJBException;
import org.apache.openejb.core.WebContext;
import org.apache.webbeans.config.WebBeansContext;
import org.apache.webbeans.inject.OWBInjector;

import javax.servlet.ServletContext;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TomEEInjectionProvider extends DiscoverableInjectionProvider {

    private final ServletContext servletContext;
    private final WebContext webContext;

    public TomEEInjectionProvider(ServletContext servletContext) {
        this.servletContext = servletContext;
        final Object attribute = servletContext.getAttribute(WebContext.class.getName());

        if (attribute == null) throw new IllegalStateException("No WebContext found in ServletContext attributes");

        if (!(attribute instanceof WebContext)) throw new IllegalStateException("WebContext entry in ServletContext attributes is not an instance of WebContext");

        webContext = (WebContext) attribute;
    }

    @Override
    public void inject(final Object managedBean) throws InjectionProviderException {
        try {
            webContext.inject(managedBean);
        } catch (OpenEJBException e) {
            throw new InjectionProviderException(e);
        }
    }

    @Override
    public void invokePreDestroy(Object managedBean) throws InjectionProviderException {
        webContext.destroy(managedBean);
    }

    @Override
    public void invokePostConstruct(Object managedBean) throws InjectionProviderException {
        // TODO
    }
}
