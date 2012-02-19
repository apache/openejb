package org.apache.openejb.maven.plugin.dd.merger;

import org.apache.maven.plugin.logging.Log;
import org.apache.openejb.config.sys.JaxbOpenejb;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.maven.plugin.dd.Merger;

import java.io.BufferedInputStream;
import java.net.URL;

public class EjbjarxmlMerger implements Merger<EjbJar> {
    private final Log log;

    public EjbjarxmlMerger(final Log logger) {
        log = logger;
    }

    @Override
    public EjbJar merge(final EjbJar reference, final EjbJar toMerge) {
        for (EnterpriseBean bean : toMerge.getEnterpriseBeans()) {
            if (reference.getEnterpriseBeansByEjbName().containsKey(bean.getEjbName())) {
                log.warn("bean " + bean.getEjbName() + " already defined");
            } else {
                reference.addEnterpriseBean(bean);
            }
        }

        for (Interceptor interceptor : toMerge.getInterceptors()) {
            if (reference.getInterceptor(interceptor.getInterceptorClass()) != null) {
                log.warn("interceptor " + interceptor.getInterceptorClass() + " already defined");
            } else {
                reference.addInterceptor(interceptor);
            }
        }

        final AssemblyDescriptor descriptor = toMerge.getAssemblyDescriptor();
        mergeAssemblyDescriptor(reference.getAssemblyDescriptor(), descriptor);

        return reference;
    }

    private static void mergeAssemblyDescriptor(final AssemblyDescriptor assemblyDescriptor, final AssemblyDescriptor descriptor) {
        // TODO
    }

    @Override
    public EjbJar createEmpty() {
        return new EjbJar();
    }

    @Override
    public EjbJar read(URL url) {
        try {
            return JaxbOpenejb.unmarshal(EjbJar.class, new BufferedInputStream(url.openStream()));
        } catch (Exception e) {
            return createEmpty();
        }
    }
}
