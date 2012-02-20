package org.apache.openejb.maven.plugin.dd.merger;

import org.apache.maven.plugin.logging.Log;
import org.apache.openejb.jee.AssemblyDescriptor;
import org.apache.openejb.jee.EjbJar;
import org.apache.openejb.jee.EnterpriseBean;
import org.apache.openejb.jee.Interceptor;
import org.apache.openejb.jee.JaxbJavaee;
import org.apache.openejb.maven.plugin.dd.Merger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

public class EjbJarMerger extends Merger<EjbJar> {
    public EjbJarMerger(final Log logger) {
        super(logger);
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
            return (EjbJar) JaxbJavaee.unmarshal(EjbJar.class, new BufferedInputStream(url.openStream()), false);
        } catch (Exception e) {
            return createEmpty();
        }
    }

    @Override
    public String descriptorName() {
        return "ejb-jar.xml";
    }

    @Override
    public void dump(final File dump, final EjbJar ejbJar) throws Exception {
        final BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(dump));
        try {
            JaxbJavaee.marshal(EjbJar.class, ejbJar, stream);
        } finally {
            stream.close();
        }
    }
}
