package org.openejb.alt.assembler.dynamic;

import java.io.*;
import java.net.URL;
import java.util.*;
import org.openejb.alt.config.DTDResolver;
import org.openejb.alt.config.ejb11.EjbJar;
import org.openejb.alt.config.ejb11.OpenejbJar;
import org.openejb.util.Logger;
import org.exolab.castor.xml.*;

/**
 * Knows how to read XML files for deployment descriptors using Castor.
 *
 * @author Aaron Mulder (ammulder@alumni.princeton.edu)
 */
public class DDLoader {
    private final static Logger log = Logger.getInstance("OpenEJB.dynamic.DDLoader", "OpenEJB.dynamic.DDLoader"); //todo: resource bundle
    private static DTDResolver resolver = new DTDResolver();

    static EjbJar[] readEjbJars(ClassLoader loader) throws IOException {
        try {
            List list = new ArrayList();
            Enumeration enum = loader.getResources("META-INF/ejb-jar.xml");
            while(enum.hasMoreElements()) {
                URL url = (URL)enum.nextElement();
                log.debug("Found Standard DD: "+url);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                list.add(readEjbJar(reader));
                reader.close();
            }
            return (EjbJar[])list.toArray(new EjbJar[list.size()]);
        } catch(MarshalException e) {
            throw new IOException("Unable to read EJB Deployment Descriptor: "+e.getMessage());
        } catch(ValidationException e) {
            throw new IOException("Unable to read EJB Deployment Descriptor: " + e.getMessage());
        }
    }

    static EjbJar readEjbJar(Reader reader) throws MarshalException, ValidationException {
        Unmarshaller unmarshaller = new Unmarshaller(EjbJar.class);
        unmarshaller.setEntityResolver(resolver);

        return (EjbJar)unmarshaller.unmarshal(reader);
    }

    static OpenejbJar[] readOpenEjbJars(ClassLoader loader) throws IOException {
        try {
            List list = new ArrayList();
            Enumeration enum = loader.getResources("META-INF/openejb-jar.xml");
            while(enum.hasMoreElements()) {
                URL url = (URL)enum.nextElement();
                log.debug("Found OpenEJB DD: " + url);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                list.add(readOpenEjbJar(reader));
                reader.close();
            }
            return (OpenejbJar[])list.toArray(new OpenejbJar[list.size()]);
        } catch(MarshalException e) {
            throw new IOException("Unable to read OpenEJB Deployment Descriptor: " + e.getMessage());
        } catch(ValidationException e) {
            throw new IOException("Unable to read OpenEJB Deployment Descriptor: " + e.getMessage());
        }
    }

    static OpenejbJar readOpenEjbJar(Reader reader) throws MarshalException, ValidationException {
        Unmarshaller unmarshaller = new Unmarshaller(OpenejbJar.class);
        unmarshaller.setEntityResolver(resolver);
        return (OpenejbJar)unmarshaller.unmarshal(reader);
    }
}
